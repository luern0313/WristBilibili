package cn.luern0313.wristbilibili.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ViewFlipper;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.ArticleApi;
import cn.luern0313.wristbilibili.fragment.ArticleDetailFragment;
import cn.luern0313.wristbilibili.fragment.ReplyFragment;
import cn.luern0313.wristbilibili.models.article.ArticleModel;


public class ArticleActivity extends AppCompatActivity implements ArticleDetailFragment.ArticleDetailFragmentListener
{
    Context ctx;
    Intent intent;
    LayoutInflater inflater;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String article_id;

    ArticleApi articleApi;
    ArticleModel articleModel;
    PagerAdapter pagerAdapter;
    ArticleDetailActivityListener articleDetailActivityListener;

    ViewFlipper uiTitle;
    ViewPager uiViewPager;
    ImageView uiLoadingImg;
    LinearLayout uiLoading;
    LinearLayout uiNoWeb;
    View layoutReplyLoading;

    boolean isLogin = false;

    AnimationDrawable loadingImgAnim;

    Handler handler = new Handler();
    Runnable runnableUi;
    Runnable runnableNoWeb;
    Runnable runnableNodata;
    Runnable runnableLoadingFin;

    final private int RESULT_DETAIL_SHARE = 101;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        ctx = this;
        intent = getIntent();
        sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        article_id = intent.getStringExtra("article_id");

        inflater = getLayoutInflater();
        layoutReplyLoading = inflater.inflate(R.layout.widget_loading, null);

        uiTitle = findViewById(R.id.art_title_title);
        uiViewPager = findViewById(R.id.art_viewpager);
        uiViewPager.setOffscreenPageLimit(1);
        uiLoadingImg = findViewById(R.id.art_loading_img);
        uiLoading = findViewById(R.id.art_loading);
        uiNoWeb = findViewById(R.id.art_noweb);

        isLogin = !sharedPreferences.getString("cookies", "").equals("");
        articleApi = new ArticleApi(sharedPreferences.getString("cookies", ""),
                                    sharedPreferences.getString("mid", ""),
                                    sharedPreferences.getString("csrf", ""),
                                    sharedPreferences.getString("access_key", ""),
                                    article_id);

        uiLoadingImg.setImageResource(R.drawable.anim_loading);
        loadingImgAnim = (AnimationDrawable) uiLoadingImg.getDrawable();
        loadingImgAnim.start();
        uiLoading.setVisibility(View.VISIBLE);

        runnableUi = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    uiLoading.setVisibility(View.GONE);
                    uiNoWeb.setVisibility(View.GONE);
                    uiViewPager.setAdapter(pagerAdapter);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    handler.post(runnableNodata);
                }
            }
        };

        runnableNoWeb = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    uiLoading.setVisibility(View.GONE);
                    uiNoWeb.setVisibility(View.VISIBLE);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableNodata = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    findViewById(R.id.art_novideo).setVisibility(View.VISIBLE);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableLoadingFin = new Runnable()
        {
            @Override
            public void run()
            {
                findViewById(R.id.article_article_loading).setVisibility(View.GONE);
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
                    return ReplyFragment.newInstance(articleModel.article_id, "12");
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
                if(uiTitle.getDisplayedChild() != position)
                {
                    if(uiTitle.getDisplayedChild() < position)
                    {
                        uiTitle.setInAnimation(ctx, R.anim.slide_in_right);
                        uiTitle.setOutAnimation(ctx, R.anim.slide_out_left);
                        uiTitle.showNext();
                    }
                    else
                    {
                        uiTitle.setInAnimation(ctx, android.R.anim.slide_in_left);
                        uiTitle.setOutAnimation(ctx, android.R.anim.slide_out_right);
                        uiTitle.showPrevious();
                    }
                }
            }
        });

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    articleModel = articleApi.getArticleModel();
                    if(articleModel != null)
                    {
                        handler.post(runnableUi);
                    }
                    else
                    {
                        handler.post(runnableNodata);
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    handler.post(runnableNoWeb);
                }
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
            intent.putExtra("imgUrl", articleModel.article_cover);
            startActivity(intent);
        }
        else if(viewId == R.id.article_article_bt_like)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(articleModel.article_user_like)
                        {
                            String result = articleApi.likeArticle(2);
                            if(result.equals(""))
                            {
                                articleModel.article_like--;
                                articleModel.article_user_like = false;
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
                                articleModel.article_like++;
                                articleModel.article_user_like = true;
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
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        Looper.prepare();
                        Toast.makeText(ctx, "操作失败，请检查你的网络..", Toast.LENGTH_SHORT).show();
                    }
                    finally
                    {
                        EventBus.getDefault().post(articleModel);
                        Looper.loop();
                    }
                }
            }).start();
        }
        else if(viewId == R.id.article_article_bt_coin)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(articleModel.article_user_coin == 0)
                        {
                            String result = articleApi.coinArticle();
                            if(result.equals(""))
                            {
                                articleModel.article_coin++;
                                articleModel.article_user_coin = 1;
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
                        Toast.makeText(ctx, "操作失败，请检查你的网络..", Toast.LENGTH_SHORT).show();
                    }
                    finally
                    {
                        EventBus.getDefault().post(articleModel);
                        Looper.loop();
                    }
                }
            }).start();
        }
        else if(viewId == R.id.article_article_bt_fav)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(articleModel.article_user_fav)
                        {
                            String result = articleApi.favArticle(2);
                            if(result.equals(""))
                            {
                                articleModel.article_fav--;
                                articleModel.article_user_fav = false;
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
                                articleModel.article_fav++;
                                articleModel.article_user_fav = true;
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
                        Toast.makeText(ctx, "操作失败，请检查你的网络..", Toast.LENGTH_SHORT).show();
                    }
                    finally
                    {
                        EventBus.getDefault().post(articleModel);
                        Looper.loop();
                    }
                }
            }).start();
        }
        else if(viewId == R.id.article_article_bt_share)
        {
            Intent intent = new Intent(ctx, SendDynamicActivity.class);
            intent.putExtra("is_share", true);
            intent.putExtra("share_up", articleModel.article_up_name);
            intent.putExtra("share_title", articleModel.article_title);
            intent.putExtra("share_img", articleModel.article_cover[0]);
            startActivityForResult(intent, RESULT_DETAIL_SHARE);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, final Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != 0) return;
        if(requestCode == RESULT_DETAIL_SHARE)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
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
                        Toast.makeText(ctx, "分享视频失败。。请检查网络？", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }
            }).start();
        }
    }

    public interface ArticleDetailActivityListener
    {
        void onArticleDetailActivityLoadingStart();
        void onArticleDetailActivityLoadingFin();
    }
}
