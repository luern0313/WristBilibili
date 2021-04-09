package cn.luern0313.wristbilibili.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.ArticleApi;
import cn.luern0313.wristbilibili.fragment.ArticleDetailFragment;
import cn.luern0313.wristbilibili.fragment.ReplyFragment;
import cn.luern0313.wristbilibili.models.article.ArticleModel;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;
import cn.luern0313.wristbilibili.widget.ExceptionHandlerView;
import cn.luern0313.wristbilibili.widget.TitleView;


public class ArticleActivity extends BaseActivity implements ArticleDetailFragment.ArticleDetailFragmentListener, TitleView.TitleViewListener
{
    Context ctx;
    Intent intent;
    LayoutInflater inflater;
    String article_id;

    ArticleApi articleApi;
    ArticleModel articleModel;
    PagerAdapter pagerAdapter;
    ArticleDetailActivityListener articleDetailActivityListener;

    TitleView uiTitleView;
    ExceptionHandlerView exceptionHandlerView;
    ViewPager uiViewPager;
    View layoutReplyLoading;

    boolean isLogin = false;

    Handler handler = new Handler();
    Runnable runnableUi;

    final private int RESULT_DETAIL_SHARE = 101;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        ctx = this;
        intent = getIntent();
        article_id = intent.getStringExtra("article_id");

        WindowManager manager = this.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);

        inflater = getLayoutInflater();
        layoutReplyLoading = inflater.inflate(R.layout.widget_loading, null);

        uiTitleView = findViewById(R.id.art_title);
        exceptionHandlerView = findViewById(R.id.art_exception);
        uiViewPager = findViewById(R.id.art_viewpager);
        uiViewPager.setOffscreenPageLimit(1);

        isLogin = SharedPreferencesUtil.contains(SharedPreferencesUtil.cookies);
        articleApi = new ArticleApi(article_id, outMetrics.widthPixels);

        runnableUi = () -> {
            try
            {
                exceptionHandlerView.hideAllView();
                uiViewPager.setAdapter(pagerAdapter);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                exceptionHandlerView.noData();
            }
        };

        pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager())
        {
            @Override
            public int getCount()
            {
                return 2;
            }

            @NonNull
            @Override
            public Fragment getItem(int position)
            {
                if(position == 0)
                    return ArticleDetailFragment.newInstance(articleModel);
                else if(position == 1)
                    return ReplyFragment.newInstance(articleModel.getId(), "12", null, -1);
                return null;
            }
        };

        uiViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageScrollStateChanged(int state) {}

            @Override
            public void onPageSelected(int position)
            {
                while(uiTitleView.getDisplayedChild() != position)
                {
                    uiTitleView.show();
                    if(uiTitleView.getDisplayedChild() < position)
                    {
                        uiTitleView.setInAnimation(ctx, R.anim.slide_in_right);
                        uiTitleView.setOutAnimation(ctx, R.anim.slide_out_left);
                        uiTitleView.showNext();
                    }
                    else
                    {
                        uiTitleView.setInAnimation(ctx, android.R.anim.slide_in_left);
                        uiTitleView.setOutAnimation(ctx, android.R.anim.slide_out_right);
                        uiTitleView.showPrevious();
                    }
                }
            }
        });

        new Thread(() -> {
            try
            {
                articleModel = articleApi.getArticleModel();
                if(articleModel != null)
                    handler.post(runnableUi);
                else
                    exceptionHandlerView.noData();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                exceptionHandlerView.noWeb();
            }
        }).start();
    }

    public void setArticleDetailActivityListener(ArticleDetailActivityListener articleDetailActivityListener)
    {
        this.articleDetailActivityListener = articleDetailActivityListener;
    }

    @Override
    public void onArticleDetailFragmentViewClick(int viewId)
    {
        if(viewId == R.id.article_article_bt_cover)
        {
            Intent intent = new Intent(ctx, ImgActivity.class);
            intent.putExtra("imgUrl", articleModel.getCover());
            startActivity(intent);
        }
        else if(viewId == R.id.article_article_bt_like)
        {
            new Thread(() -> {
                try
                {
                    if(articleModel.isUserLike())
                    {
                        String result = articleApi.likeArticle(2);
                        if(result.equals(""))
                        {
                            articleModel.setLike(articleModel.getLike() - 1);
                            articleModel.setUserLike(false);
                            Looper.prepare();
                            Toast.makeText(ctx, "已取消喜欢...", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Looper.prepare();
                            Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    {
                        String result = articleApi.likeArticle(1);
                        if(result.equals(""))
                        {
                            articleModel.setLike(articleModel.getLike() + 1);
                            articleModel.setUserLike(true);
                            Looper.prepare();
                            Toast.makeText(ctx, "已喜欢专栏！", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Looper.prepare();
                            Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(ctx, ArticleActivity.this.getString(R.string.main_error_web), Toast.LENGTH_SHORT).show();
                }
                finally
                {
                    EventBus.getDefault().post(articleModel);
                    Looper.loop();
                }
            }).start();
        }
        else if(viewId == R.id.article_article_bt_coin)
        {
            new Thread(() -> {
                try
                {
                    if(articleModel.getUserCoin() == 0)
                    {
                        String result = articleApi.coinArticle();
                        if(result.equals(""))
                        {
                            articleModel.setCoin(articleModel.getCoin() + 1);
                            articleModel.setUserCoin(1);
                            Looper.prepare();
                            Toast.makeText(ctx, "你投了一个硬币！", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Looper.prepare();
                            Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    {
                        Looper.prepare();
                        Toast.makeText(ctx, "投币失败，超过投币上限！", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(ctx, ArticleActivity.this.getString(R.string.main_error_web), Toast.LENGTH_SHORT).show();
                }
                finally
                {
                    EventBus.getDefault().post(articleModel);
                    Looper.loop();
                }
            }).start();
        }
        else if(viewId == R.id.article_article_bt_fav)
        {
            new Thread(() -> {
                try
                {
                    if(articleModel.isUserFavor())
                    {
                        String result = articleApi.favArticle(2);
                        if(result.equals(""))
                        {
                            articleModel.setFavor(articleModel.getFavor() - 1);
                            articleModel.setUserFavor(false);
                            Looper.prepare();
                            Toast.makeText(ctx, "已取消收藏...", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Looper.prepare();
                            Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    {
                        String result = articleApi.favArticle(1);
                        if(result.equals(""))
                        {
                            articleModel.setFavor(articleModel.getFavor() + 1);
                            articleModel.setUserFavor(true);
                            Looper.prepare();
                            Toast.makeText(ctx, "已收藏专栏！", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Looper.prepare();
                            Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(ctx, ArticleActivity.this.getString(R.string.main_error_web), Toast.LENGTH_SHORT).show();
                }
                finally
                {
                    EventBus.getDefault().post(articleModel);
                    Looper.loop();
                }
            }).start();
        }
        else if(viewId == R.id.article_article_bt_share)
        {
            Intent intent = new Intent(ctx, SendDynamicActivity.class);
            intent.putExtra("is_share", true);
            intent.putExtra("share_up", articleModel.getUpName());
            intent.putExtra("share_title", articleModel.getTitle());
            intent.putExtra("share_img", articleModel.getCover()[0]);
            startActivityForResult(intent, RESULT_DETAIL_SHARE);
        }
        else if(viewId == R.id.article_card_follow)
        {
            new Thread(() -> {
                try
                {
                    String result = articleApi.followUp();
                    if(result.equals(""))
                    {
                        articleModel.setUpFansNum(articleModel.getUpFansNum() + 1);
                        Looper.prepare();
                        Toast.makeText(ctx, "已关注UP主", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Looper.prepare();
                        Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(ctx, ArticleActivity.this.getString(R.string.main_error_web), Toast.LENGTH_SHORT).show();
                }
                finally
                {
                    EventBus.getDefault().post(articleModel);
                    Looper.loop();
                }
            }).start();
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, final Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != 0) return;
        if(requestCode == RESULT_DETAIL_SHARE)
        {
            new Thread(() -> {
                try
                {
                    String result = articleApi.shareArticle(data.getStringExtra("text"));
                    if(result.equals(""))
                    {
                        Looper.prepare();
                        Toast.makeText(ctx, "发送成功！", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                    else
                    {
                        Looper.prepare();
                        Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(ctx, ArticleActivity.this.getString(R.string.main_error_web), Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }).start();
        }
    }

    @Override
    public boolean hideTitle()
    {
        return uiTitleView.hide();
    }

    @Override
    public boolean showTitle()
    {
        return uiTitleView.show();
    }

    public interface ArticleDetailActivityListener
    {
        void onArticleDetailActivityLoadingStart();
        void onArticleDetailActivityLoadingFin();
    }
}
