package cn.luern0313.wristbilibili.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.ArticleAdapter;
import cn.luern0313.wristbilibili.adapter.ReplyAdapter;
import cn.luern0313.wristbilibili.api.ArticleApi;
import cn.luern0313.wristbilibili.api.ReplyApi;
import cn.luern0313.wristbilibili.models.article.ArticleModel;
import cn.luern0313.wristbilibili.models.ReplyModel;
import cn.luern0313.wristbilibili.util.DataProcessUtil;


public class ArticleActivity extends AppCompatActivity
{
    Context ctx;
    Intent intent;
    LayoutInflater inflater;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ArticleApi articleApi;
    ArticleModel articleModel;
    ReplyApi replyApi;
    ArrayList<ReplyModel> replyArrayList;
    String article_id;
    int img_width;

    TextView uiTitle;
    ViewPager uiViewPager;
    ImageView uiLoadingImg;
    LinearLayout uiLoading;
    LinearLayout uiNoWeb;
    View layoutArticleHeader;
    View layoutReplyLoading;

    ListView uiArticleListView;
    ArticleAdapter articleAdapter;
    ListView uiReplyListView;
    ReplyAdapter replyAdapter;
    ReplyAdapter.ReplyAdapterListener replyAdapterListener;

    boolean isLogin = false;
    boolean isReplyLoading = true;
    int replyPage;

    AnimationDrawable loadingImgAnim;

    Handler handler = new Handler();
    Runnable runnableDetailUi;
    Runnable runnableDetailNoWeb;
    Runnable runnableDetailNodata;
    Runnable runnableDetailLoadingFin;
    Runnable runnableDetailSetIcon;
    Runnable runnableReplyUi;
    Runnable runnableReplyUpdate;
    Runnable runnableReplyMoreNomore;
    Runnable runnableReplyMoreErr;

    final private int RESULT_DETAIL_SHARE = 101;
    final private int RESULT_REPLY_SEND = 201;
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

        WindowManager manager = getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        img_width = outMetrics.widthPixels - dip2px(ctx, 28) * 2;

        inflater = getLayoutInflater();
        layoutArticleHeader = inflater.inflate(R.layout.widget_article_header, null);
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

        replyAdapterListener = new ReplyAdapter.ReplyAdapterListener()
        {
            @Override
            public void onClick(int viewId, int position)
            {
                onReplyViewClick(viewId, position);
            }
        };

        runnableDetailUi = new Runnable()
        {
            @Override
            public void run()
            {
                ((TextView) layoutArticleHeader.findViewById(R.id.article_article_title)).setText(articleModel.article_title);
                ((TextView) layoutArticleHeader.findViewById(R.id.article_article_channel)).setText(articleModel.article_channel);
                ((TextView) layoutArticleHeader.findViewById(R.id.article_article_view)).setText(DataProcessUtil.getView(articleModel.article_view));
                ((TextView) layoutArticleHeader.findViewById(R.id.article_article_time)).setText(articleModel.article_time);

                setArticleIcon();

                articleAdapter = new ArticleAdapter(inflater, img_width, articleModel.article_article_card_model_list, uiArticleListView);
                uiArticleListView.addHeaderView(layoutArticleHeader);
                uiArticleListView.setAdapter(articleAdapter);

                uiLoading.setVisibility(View.GONE);
                uiNoWeb.setVisibility(View.GONE);

                getReply();
            }
        };

        runnableDetailNoWeb = new Runnable()
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

        runnableDetailNodata = new Runnable()
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

        runnableDetailLoadingFin = new Runnable()
        {
            @Override
            public void run()
            {
                findViewById(R.id.article_article_loading).setVisibility(View.GONE);
            }
        };

        runnableDetailSetIcon = new Runnable()
        {

            @Override
            public void run()
            {
                try
                {
                    setArticleIcon();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableReplyUi = new Runnable()
        {
            @Override
            public void run()
            {
                isReplyLoading = false;
                replyAdapter = new ReplyAdapter(inflater, uiReplyListView, replyArrayList, replyApi.isShowFloor(), replyAdapterListener);
                uiReplyListView.setAdapter(replyAdapter);
            }
        };

        runnableReplyUpdate = new Runnable()
        {
            @Override
            public void run()
            {
                isReplyLoading = false;
                replyAdapter.notifyDataSetChanged();
            }
        };

        runnableReplyMoreNomore = new Runnable()
        {
            @Override
            public void run()
            {
                isReplyLoading = false;
                ((TextView) layoutReplyLoading.findViewById(R.id.wid_load_text)).setText("  没有更多了...");
            }
        };

        runnableReplyMoreErr = new Runnable()
        {
            @Override
            public void run()
            {
                isReplyLoading = false;
                ((TextView) layoutReplyLoading.findViewById(R.id.wid_load_text)).setText("好像没有网络...\n检查下网络？");
                layoutReplyLoading.findViewById(R.id.wid_load_button).setVisibility(View.VISIBLE);
            }
        };

        final PagerAdapter pagerAdapter = new PagerAdapter()
        {
            @Override
            public int getCount()
            {
                return 2;
            }

            @Override
            public boolean isViewFromObject(View view, Object object)
            {
                return view.getTag().equals(object);
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object)
            {
                container.removeView(container.findViewWithTag(object));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position)
            {
                if(position == 0)
                {
                    View v = inflater.inflate(R.layout.viewpager_article_article, null);
                    v.setTag(0);

                    uiArticleListView = v.findViewById(R.id.article_article_listview);

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
                                    handler.post(runnableDetailUi);
                                }
                                else
                                {
                                    handler.post(runnableDetailNodata);
                                }
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                                handler.post(runnableDetailNoWeb);
                            }
                        }
                    }).start();
                    container.addView(v);
                    return 0;
                }
                else
                {
                    View v = inflater.inflate(R.layout.viewpager_article_reply, null);
                    v.setTag(1);

                    uiReplyListView = v.findViewById(R.id.article_reply_listview);
                    uiReplyListView.setEmptyView(v.findViewById(R.id.article_reply_nothing));
                    uiReplyListView.addHeaderView(inflater.inflate(R.layout.widget_reply_sendreply, null), null, true);
                    uiReplyListView.addFooterView(layoutReplyLoading, null, true);
                    uiReplyListView.setHeaderDividersEnabled(false);
                    uiReplyListView.setOnScrollListener(new AbsListView.OnScrollListener()
                    {
                        @Override
                        public void onScrollStateChanged(AbsListView view, int scrollState)
                        {
                        }

                        @Override
                        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
                        {
                            if(visibleItemCount + firstVisibleItem == totalItemCount && !isReplyLoading && isLogin)
                            {
                                getMoreReply();
                            }
                        }
                    });

                    container.addView(v);
                    return 1;
                }
            }
        };

        uiViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {
            }

            @Override
            public void onPageSelected(int position)
            {
                if(position == 0) titleAnim("专栏");
                else if(position == 1) titleAnim("评论");
            }
        });

        uiViewPager.setAdapter(pagerAdapter);
    }

    private void onReplyViewClick(int viewId, int position)
    {
        final ReplyModel replyModel = replyArrayList.get(position);
        if(viewId == R.id.item_reply_head)
        {
            Intent intent = new Intent(ctx, OtherUserActivity.class);
            intent.putExtra("mid", replyModel.getUserMid());
            startActivity(intent);
        }
        else if(viewId == R.id.item_reply_like)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    String va = replyModel.likeReply(replyModel.getReplyId(), replyModel.isReplyLike() ? 0 : 1, "1");
                    if(va.equals("")) handler.post(runnableReplyUpdate);
                    else
                    {
                        Looper.prepare();
                        Toast.makeText(ctx, (replyModel.isReplyLike() ? "取消" : "点赞") + "失败：\n" + va, Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }
            }).start();
        }
        else if(viewId == R.id.item_reply_dislike)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    String va = replyModel.hateReply(replyModel.getReplyId(), replyModel.isReplyDislike() ? 0 : 1, "1");
                    if(va.equals("")) handler.post(runnableReplyUpdate);
                    else
                    {
                        Looper.prepare();
                        Toast.makeText(ctx, (replyModel.isReplyDislike() ? "取消" : "点踩") + "失败：\n" + va, Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }
            }).start();
        }
        else if(viewId == R.id.item_reply_reply)
        {
            Intent rintent = new Intent(ctx, CheckreplyActivity.class);
            rintent.putExtra("oid", articleModel.article_id);
            rintent.putExtra("type", "1");
            rintent.putExtra("root", replyModel.getReplyId());
            startActivity(rintent);
        }
    }

    void setArticleIcon()
    {
        findViewById(R.id.article_article_loading).setVisibility(View.GONE);
        if(articleModel.article_user_like)
            ((ImageView) layoutArticleHeader.findViewById(R.id.article_article_bt_like)).setImageResource(R.drawable.icon_vdd_do_like_yes);
        else
            ((ImageView) layoutArticleHeader.findViewById(R.id.article_article_bt_like)).setImageResource(R.drawable.icon_vdd_do_like_no);

        if(articleModel.article_user_coin == 1)
            ((ImageView) layoutArticleHeader.findViewById(R.id.article_article_bt_coin)).setImageResource(R.drawable.icon_vdd_do_coin_yes);
        else
            ((ImageView) layoutArticleHeader.findViewById(R.id.article_article_bt_coin)).setImageResource(R.drawable.icon_vdd_do_coin_no);

        if(articleModel.article_user_fav)
            ((ImageView) layoutArticleHeader.findViewById(R.id.article_article_bt_fav)).setImageResource(R.drawable.icon_vdd_do_fav_yes);
        else
            ((ImageView) layoutArticleHeader.findViewById(R.id.article_article_bt_fav)).setImageResource(R.drawable.icon_vdd_do_fav_no);

        if(articleModel.article_like == 0)
            ((TextView) layoutArticleHeader.findViewById(R.id.article_article_bt_like_text)).setText("点赞");
        else
            ((TextView) layoutArticleHeader.findViewById(R.id.article_article_bt_like_text)).setText(DataProcessUtil.getView(articleModel.article_like));

        if(articleModel.article_coin == 0)
            ((TextView) layoutArticleHeader.findViewById(R.id.article_article_bt_coin_text)).setText("投币");
        else
            ((TextView) layoutArticleHeader.findViewById(R.id.article_article_bt_coin_text)).setText(DataProcessUtil.getView(articleModel.article_coin));

        if(articleModel.article_fav == 0)
            ((TextView) layoutArticleHeader.findViewById(R.id.article_article_bt_fav_text)).setText("收藏");
        else
            ((TextView) layoutArticleHeader.findViewById(R.id.article_article_bt_fav_text)).setText(DataProcessUtil.getView(articleModel.article_fav));
    }

    void getReply()
    {
        replyPage = 1;
        isReplyLoading = true;
        replyApi = new ReplyApi(sharedPreferences.getString("cookies", ""),
                                sharedPreferences.getString("csrf", ""),
                                articleModel.article_id, "12");
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    replyArrayList = new ArrayList<>();
                    replyArrayList.add(new ReplyModel(1));
                    replyArrayList.addAll(replyApi.getReply(1, "2", 5, ""));
                    replyArrayList.add(new ReplyModel(2));
                    replyArrayList.addAll(replyApi.getReply(1, "0", 0, ""));
                    handler.post(runnableReplyUi);
                }
                catch (IOException | NullPointerException e)
                {
                    e.printStackTrace();
                    replyArrayList = new ArrayList<>();
                    handler.post(runnableReplyUi);
                }
            }
        }).start();
    }

    void getMoreReply()
    {
        isReplyLoading = true;
        replyPage++;
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    ArrayList<ReplyModel> r = replyApi.getReply(replyPage, "0", 0, "");
                    if(r != null && r.size() != 0)
                    {
                        replyArrayList.addAll(r);
                        handler.post(runnableReplyUpdate);
                    }
                    else
                    {
                        handler.post(runnableReplyMoreNomore);
                    }
                }
                catch (IOException e)
                {
                    handler.post(runnableReplyMoreErr);
                    e.printStackTrace();
                }
            }
        }).start();
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
        else if(requestCode == RESULT_REPLY_SEND)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        String result = articleApi.sendReply(data.getStringExtra("text"));
                        if(result.equals(""))
                        {
                            Looper.prepare();
                            Toast.makeText(ctx, "发送成功！", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                        else
                        {
                            Looper.prepare();
                            Toast.makeText(ctx, "发送失败，可能是短时间发送过多？", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        Looper.prepare();
                        Toast.makeText(ctx, "评论发送失败。。请检查网络？", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }
            }).start();
        }
    }

    public void clickArticleCover(View view)
    {
        Intent intent = new Intent(ctx, ImgActivity.class);
        intent.putExtra("imgUrl", articleModel.article_cover);
        startActivity(intent);
    }

    public void clickArticleLike(View view)
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
                    handler.post(runnableDetailSetIcon);
                    Looper.loop();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(ctx, "操作失败，请检查你的网络..", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }
        }).start();
    }

    public void clickArticleCoin(View view)
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
                    handler.post(runnableDetailSetIcon);
                    Looper.loop();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(ctx, "操作失败，请检查你的网络..", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }
        }).start();
    }

    public void clickArticleFav(View view)
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
                    handler.post(runnableDetailSetIcon);
                    Looper.loop();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(ctx, "操作失败，请检查你的网络..", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }
        }).start();
    }

    public void clickArticleShare(View view)
    {
        Intent intent = new Intent(ctx, SendDynamicActivity.class);
        intent.putExtra("is_share", true);
        intent.putExtra("share_up", articleModel.article_up_name);
        intent.putExtra("share_title", articleModel.article_title);
        intent.putExtra("share_img", articleModel.article_cover[0]);
        startActivityForResult(intent, RESULT_DETAIL_SHARE);
    }

    public void clickSendReply(View view)
    {
        Intent replyIntent = new Intent(ctx, ReplyActivity.class);
        replyIntent.putExtra("oid", articleModel.article_id);
        replyIntent.putExtra("type", "12");
        startActivityForResult(replyIntent, RESULT_REPLY_SEND);
    }


    void titleAnim(final String title)
    {
        ObjectAnimator alpha = ObjectAnimator.ofFloat(uiTitle, "alpha", 1f, 0f);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(alpha);
        animatorSet.setDuration(500);
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {
            }

            @Override
            public void onAnimationCancel(Animator animation)
            {
            }

            @Override
            public void onAnimationRepeat(Animator animation)
            {
            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                uiTitle.setText(title);
                uiTitle.setAlpha(1);
            }
        });
    }

    private static int dip2px(Context context, float dpValue)
    {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
