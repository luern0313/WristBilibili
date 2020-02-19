package cn.luern0313.wristbilibili.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.ReplyAdapter;
import cn.luern0313.wristbilibili.adapter.VideoPartAdapter;
import cn.luern0313.wristbilibili.adapter.VideoRecommendAdapter;
import cn.luern0313.wristbilibili.api.FavorBoxApi;
import cn.luern0313.wristbilibili.api.OnlineVideoApi;
import cn.luern0313.wristbilibili.api.ReplyApi;
import cn.luern0313.wristbilibili.api.VideoDetailsApi;
import cn.luern0313.wristbilibili.models.FavorBoxModel;
import cn.luern0313.wristbilibili.models.ReplyModel;
import cn.luern0313.wristbilibili.models.VideoModel;
import cn.luern0313.wristbilibili.service.DownloadService;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.ImageDownloaderUtil;

public class VideodetailsActivity extends AppCompatActivity
{
    Context ctx;
    Intent intent;
    LayoutInflater inflater;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    VideoDetailsApi videoDetail;
    VideoModel videoModel;
    OnlineVideoApi onlineVideoApi;
    ReplyApi replyApi;
    ArrayList<ReplyModel> replyArrayList;
    ArrayList<VideoModel.VideoRecommendModel> recommendList;
    VideoPartAdapter videoPartAdapter;

    View layoutSendReply;
    View layoutChangeMode;
    View layoutLoading;

    Handler handler = new Handler();
    Runnable runnableNoWeb;
    Runnable runnableUi;
    Runnable runnableImg;
    Runnable runnableSetface;
    Runnable runnableNodata;
    Runnable runnableVideoLoadingFin;

    Runnable runnableReply;
    Runnable runnableRecommend;
    Runnable runnableReplyMoreNomore;
    Runnable runnableReplyMoreErr;
    Runnable runnableReplyUpdate;

    AnimationDrawable loadingImgAnim;
    Bitmap videoUpFace;

    TextView titleTextView;
    ViewPager viewPager;

    ImageView uiLoadingImg;
    LinearLayout uiLoading;
    LinearLayout uiNoWeb;

    ImageView uiVideoDoLike;
    ImageView uiVideoDoCoin;
    ImageView uiVideoDoFav;

    ListView uiReplyListView;
    ListView uiRecommendListView;
    ReplyAdapter replyAdapter;
    ReplyAdapter.ReplyAdapterListener replyAdapterListener;

    boolean isLogin = false;
    int replyPage = 1;
    boolean isReplyLoading = true;

    final private int RESULT_VD_FAVOR = 101;
    final private int RESULT_VD_DOWNLOAD = 102;
    final private int RESULT_VD_PART = 103;
    final private int RESULT_VD_SHARE = 104;
    final private int RESULT_REPLY_SEND = 201;

    private DownloadService.MyBinder myBinder;
    private VideoDownloadServiceConnection connection = new VideoDownloadServiceConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videodetails);

        ctx = this;
        intent = getIntent();
        sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        Intent serviceIntent = new Intent(ctx, DownloadService.class);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);

        inflater = getLayoutInflater();
        layoutSendReply = inflater.inflate(R.layout.widget_reply_sendreply, null);
        layoutLoading = inflater.inflate(R.layout.widget_loading, null);
        layoutChangeMode = inflater.inflate(R.layout.widget_reply_changemode, null);

        titleTextView = findViewById(R.id.vd_title_title);
        viewPager = findViewById(R.id.vd_viewpager);
        viewPager.setOffscreenPageLimit(2);
        uiLoadingImg = findViewById(R.id.vd_loading_img);
        uiLoading = findViewById(R.id.vd_loading);
        uiNoWeb = findViewById(R.id.vd_noweb);

        isLogin = !sharedPreferences.getString("cookies", "").equals("");
        videoDetail = new VideoDetailsApi(sharedPreferences.getString("cookies", ""),
                                          sharedPreferences.getString("csrf", ""),
                                          sharedPreferences.getString("mid", ""),
                                          sharedPreferences.getString("access_key", ""),
                                          intent.getStringExtra("aid"));
        replyApi = new ReplyApi(sharedPreferences.getString("cookies", ""),
                                sharedPreferences.getString("csrf", ""),
                                intent.getStringExtra("aid"), "1");

        uiLoadingImg.setImageResource(R.drawable.anim_loading);
        loadingImgAnim = (AnimationDrawable) uiLoadingImg.getDrawable();
        loadingImgAnim.start();
        uiLoading.setVisibility(View.VISIBLE);

        if(sharedPreferences.getBoolean("tip_vd", true)) findViewById(R.id.vd_tip).setVisibility(View.VISIBLE);

        replyAdapterListener = new ReplyAdapter.ReplyAdapterListener()
        {
            @Override
            public void onClick(int viewId, int position)
            {
                onReplyViewClick(viewId, position);
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
                    isReplyLoading = false;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableUi = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    uiVideoDoLike = findViewById(R.id.vd_like_img);
                    uiVideoDoCoin = findViewById(R.id.vd_coin_img);
                    uiVideoDoFav = findViewById(R.id.vd_fav_img);

                    ((TextView) findViewById(R.id.vd_video_title)).setText(videoModel.video_title);
                    ((TextView) findViewById(R.id.vd_video_play)).setText("播放:" + videoModel.video_play + "  弹幕:" + videoModel.video_danmaku);
                    ((TextView) findViewById(R.id.vd_video_time)).setText(videoModel.video_time + "  AV" + videoModel.video_aid);
                    ((TextView) findViewById(R.id.vd_video_details)).setText(videoModel.video_desc);
                    ((TextView) findViewById(R.id.vd_card_name)).setText(videoModel.video_up_name);
                    ((TextView) findViewById(R.id.vd_card_sen)).setText(videoModel.video_up_official);

                    if(videoModel.video_part_array_list.size() > 1)
                    {
                        findViewById(R.id.vd_video_part_layout).setVisibility(View.VISIBLE);
                        findViewById(R.id.vd_bt_play).setVisibility(View.GONE);
                        ((TextView) findViewById(R.id.vd_video_part_text)).setText("共" + videoModel.video_part_array_list.size() + "P");
                        LinearLayoutManager layoutManager = new LinearLayoutManager(VideodetailsActivity.super.getParent());
                        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                        ((RecyclerView) findViewById(R.id.vd_video_part)).setLayoutManager(layoutManager);
                        videoPartAdapter = new VideoPartAdapter(videoModel.video_part_array_list);
                        ((RecyclerView) findViewById(R.id.vd_video_part)).setAdapter(videoPartAdapter);
                    }
                    else findViewById(R.id.vd_video_part_layout).setVisibility(View.GONE);

                    findViewById(R.id.vd_like).setOnTouchListener(new View.OnTouchListener()
                    {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent)
                        {
                            switch (motionEvent.getAction())
                            {
                                case MotionEvent.ACTION_DOWN:
                                    tripleAnim();
                                    break;
                                case MotionEvent.ACTION_UP:
                                    //view.performClick(); //TODO ？？？？
                                case MotionEvent.ACTION_CANCEL:
                                    break;
                            }
                            return false;
                        }
                    });

                    setIcon();
                    isReplyLoading = false;

                    uiLoading.setVisibility(View.GONE);
                    uiNoWeb.setVisibility(View.GONE);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableImg = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    setIcon();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableSetface = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    ((ImageView) findViewById(R.id.vd_card_head)).setImageBitmap(videoUpFace);
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
                    findViewById(R.id.vd_novideo).setVisibility(View.VISIBLE);
                    isReplyLoading = false;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableVideoLoadingFin = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    findViewById(R.id.vd_vd_loading).setVisibility(View.GONE);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableReply = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    replyAdapter = new ReplyAdapter(inflater, uiReplyListView, replyArrayList, replyApi.isShowFloor(), replyAdapterListener);
                    uiReplyListView.setAdapter(replyAdapter);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableRecommend = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    VideoRecommendAdapter recommendAdapter = new VideoRecommendAdapter(inflater, recommendList, uiRecommendListView);
                    uiRecommendListView.setAdapter(recommendAdapter);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableReplyMoreNomore = new Runnable()
        {
            @Override
            public void run()
            {
                ((TextView) layoutLoading.findViewById(R.id.wid_load_text)).setText("  没有更多了...");
            }
        };

        runnableReplyMoreErr = new Runnable()
        {
            @Override
            public void run()
            {
                ((TextView) layoutLoading.findViewById(R.id.wid_load_text)).setText("好像没有网络...\n检查下网络？");
                layoutLoading.findViewById(R.id.wid_load_button).setVisibility(View.VISIBLE);
                isReplyLoading = false;
            }
        };

        runnableReplyUpdate = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    replyAdapter.notifyDataSetChanged();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        layoutLoading.findViewById(R.id.wid_load_button).setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        ((TextView) layoutLoading.findViewById(R.id.wid_load_text)).setText(" 加载中. . .");
                        layoutLoading.findViewById(R.id.wid_load_button).setVisibility(View.GONE);
                        getMoreReply();
                    }
                });

        final PagerAdapter pagerAdapter = new PagerAdapter()
        {
            @Override
            public int getCount()
            {
                return 3;
            }

            @Override
            public boolean isViewFromObject(View view, @NonNull Object object)
            {
                return view.getTag().equals(object);
            }

            @Override
            public void destroyItem(ViewGroup container, int position, @NonNull Object object)
            {
                container.removeView(container.findViewWithTag(object));
            }

            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position)
            {
                if(position == 0)
                {
                    View v = inflater.inflate(R.layout.viewpager_vd_vd, null);
                    v.setTag(0);

                    v.findViewById(R.id.vd_card).setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Intent intent = new Intent(ctx, OtherUserActivity.class);
                            intent.putExtra("mid", videoModel.video_up_mid);
                            startActivity(intent);
                        }
                    });

                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                videoModel = videoDetail.getVideoDetails();
                                if(videoModel != null)
                                {
                                    handler.post(runnableUi);
                                    videoUpFace = ImageDownloaderUtil.downloadImage(videoModel.video_up_face);
                                    handler.post(runnableSetface);
                                    recommendList = videoModel.video_recommend_array_list;
                                    handler.post(runnableRecommend);
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
                    container.addView(v);
                    return 0;
                }
                else if(position == 1)
                {
                    View v = inflater.inflate(R.layout.viewpager_vd_reply, null);
                    v.setTag(1);

                    uiReplyListView = v.findViewById(R.id.vd_reply_listview);
                    uiReplyListView.setEmptyView(v.findViewById(R.id.vd_reply_nothing));
                    uiReplyListView.addHeaderView(layoutSendReply, null, true);
                    uiReplyListView.addFooterView(layoutLoading, null, true);
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
                                handler.post(runnableReply);
                            }
                            catch (IOException | NullPointerException e)
                            {
                                e.printStackTrace();
                                replyArrayList = new ArrayList<>();
                                handler.post(runnableReply);
                            }
                        }
                    }).start();
                    container.addView(v);
                    return 1;
                }
                else
                {
                    View v = inflater.inflate(R.layout.viewpager_vd_recommend, null);
                    v.setTag(2);

                    uiRecommendListView = v.findViewById(R.id.vd_recommend_listview);
                    uiRecommendListView.setEmptyView(v.findViewById(R.id.vd_recommend_nothing));
                    uiRecommendListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                    {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                        {
                            Intent intent = new Intent(ctx, VideodetailsActivity.class);
                            intent.putExtra("aid", String.valueOf(recommendList.get(position).video_recommend_video_aid));
                            startActivity(intent);
                        }
                    });

                    container.addView(v);
                    return 2;
                }
            }
        };

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
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
                if(position == 0) titleAnim("视频详情");
                else if(position == 1) titleAnim("视频评论");
                else if(position == 2) titleAnim("相关推荐");
            }
        });

        viewPager.setAdapter(pagerAdapter);
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
            rintent.putExtra("oid", intent.getStringExtra("aid"));
            rintent.putExtra("type", "1");
            rintent.putExtra("root", replyModel.getReplyId());
            startActivity(rintent);
        }
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
                        isReplyLoading = false;
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

    void titleAnim(final String title)
    {
        ObjectAnimator alpha = ObjectAnimator.ofFloat(titleTextView, "alpha", 1f, 0f);
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
                titleTextView.setText(title);
                titleTextView.setAlpha(1);
            }
        });
    }

    void tripleAnim()
    {
        AnimatorSet animatorSet = new AnimatorSet();
        ArrayList<ObjectAnimator> objectAnimatorArrayList = new ArrayList<>();
        Random r = new Random();
        float translationX = 0f, translationY = 0f, rotation = 0f, scaleX = 1f, scaleY = 1f;
        for(int i = 0; i < 60; i++)
        {
            float tX = DataProcessUtil.getFloatRandom(r, -3, 3);
            float tY = DataProcessUtil.getFloatRandom(r, -3, 3);
            float ro = DataProcessUtil.getFloatRandom(r, -3, 3);
            float sX = DataProcessUtil.getFloatRandom(r, 0.97f, 1.03f);
            float sY = DataProcessUtil.getFloatRandom(r, 0.97f, 1.03f);
            objectAnimatorArrayList.add(0, ObjectAnimator.ofFloat(uiVideoDoCoin, "translationX", translationX, tX));
            objectAnimatorArrayList.add(0, ObjectAnimator.ofFloat(uiVideoDoCoin, "translationY", translationY, tY));
            objectAnimatorArrayList.add(0, ObjectAnimator.ofFloat(uiVideoDoCoin, "rotation", rotation, ro));
            objectAnimatorArrayList.add(0, ObjectAnimator.ofFloat(uiVideoDoCoin, "scaleX", scaleX, sX));
            objectAnimatorArrayList.add(0, ObjectAnimator.ofFloat(uiVideoDoCoin, "scaleY", scaleY, sY));
            if(i == 0)
                animatorSet.play(objectAnimatorArrayList.get(0)).with(objectAnimatorArrayList.get(1)).with(objectAnimatorArrayList.get(2))
                        .with(objectAnimatorArrayList.get(3)).with(objectAnimatorArrayList.get(4));
            else
                animatorSet.play(objectAnimatorArrayList.get(0)).with(objectAnimatorArrayList.get(1)).with(objectAnimatorArrayList.get(2))
                        .with(objectAnimatorArrayList.get(3)).with(objectAnimatorArrayList.get(4)).after(objectAnimatorArrayList.get(5));
            translationX = tX;
            translationY = tY;
            rotation = ro;
            scaleX = sX;
            scaleY = sY;
        }
        animatorSet.setDuration(22);
        animatorSet.start();
    }

    public void clickVdTip(View view)
    {
        findViewById(R.id.vd_tip).setVisibility(View.GONE);
        editor.putBoolean("tip_vd", false);
        editor.commit();
    }

    void setIcon()
    {
        ((TextView) findViewById(R.id.vd_like_text)).setText(videoModel.video_detail_like == 0 ? "点赞" : DataProcessUtil.getView(videoModel.video_detail_like));
        ((TextView) findViewById(R.id.vd_coin_text)).setText(videoModel.video_detail_coin == 0 ? "投币" : DataProcessUtil.getView(videoModel.video_detail_coin));
        ((TextView) findViewById(R.id.vd_fav_text)).setText(videoModel.video_detail_fav == 0 ? "收藏" : DataProcessUtil.getView(videoModel.video_detail_fav));

        if(videoModel.video_user_like)
        {
            ((ImageView) findViewById(R.id.vd_like_img)).setImageResource(R.drawable.icon_vdd_do_like_yes_nobg);
            ((ImageView) findViewById(R.id.vd_like_img_bg)).setImageResource(R.drawable.icon_vdd_do_yes_bg);
            ((ImageView) findViewById(R.id.vd_dislike_img)).setImageResource(R.drawable.icon_vdd_do_dislike_no);
        }
        else if(videoModel.video_user_dislike)
        {
            ((ImageView) findViewById(R.id.vd_like_img)).setImageResource(R.drawable.icon_vdd_do_like_no_nobg);
            ((ImageView) findViewById(R.id.vd_like_img_bg)).setImageResource(R.drawable.icon_vdd_do_no_bg);
            ((ImageView) findViewById(R.id.vd_dislike_img)).setImageResource(R.drawable.icon_vdd_do_dislike_yes);
        }
        else
        {
            ((ImageView) findViewById(R.id.vd_like_img)).setImageResource(R.drawable.icon_vdd_do_like_no_nobg);
            ((ImageView) findViewById(R.id.vd_like_img_bg)).setImageResource(R.drawable.icon_vdd_do_no_bg);
            ((ImageView) findViewById(R.id.vd_dislike_img)).setImageResource(R.drawable.icon_vdd_do_dislike_no);
        }

        if(videoModel.video_user_coin > 0)
        {
            ((ImageView) findViewById(R.id.vd_coin_img)).setImageResource(R.drawable.icon_vdd_do_coin_yes_nobg);
            ((ImageView) findViewById(R.id.vd_coin_img_bg)).setImageResource(R.drawable.icon_vdd_do_yes_bg);
        }
        else
        {
            ((ImageView) findViewById(R.id.vd_coin_img)).setImageResource(R.drawable.icon_vdd_do_coin_no_nobg);
            ((ImageView) findViewById(R.id.vd_coin_img_bg)).setImageResource(R.drawable.icon_vdd_do_no_bg);
        }

        if(videoModel.video_user_fav)
        {
            ((ImageView) findViewById(R.id.vd_fav_img)).setImageResource(R.drawable.icon_vdd_do_fav_yes_nobg);
            ((ImageView) findViewById(R.id.vd_fav_img_bg)).setImageResource(R.drawable.icon_vdd_do_yes_bg);
        }
        else
        {
            ((ImageView) findViewById(R.id.vd_fav_img)).setImageResource(R.drawable.icon_vdd_do_fav_no_nobg);
            ((ImageView) findViewById(R.id.vd_fav_img_bg)).setImageResource(R.drawable.icon_vdd_do_no_bg);
        }
    }


    public void clickMorePart(View view)
    {
        String[] videoPartNames = new String[videoModel.video_part_array_list.size()];
        String[] videoPartCids = new String[videoModel.video_part_array_list.size()];
        for(int i = 0; i < videoModel.video_part_array_list.size(); i++)
            videoPartNames[i] = videoModel.video_part_array_list.get(i).video_part_name;
        for(int i = 0; i < videoModel.video_part_array_list.size(); i++)
            videoPartCids[i] = String.valueOf(videoModel.video_part_array_list.get(i).video_part_cid);
        Intent intent = new Intent(ctx, SelectPartActivity.class);
        intent.putExtra("title", "分P");
        intent.putExtra("options_name", videoPartNames);
        intent.putExtra("options_id", videoPartCids);
        startActivityForResult(intent, RESULT_VD_PART);
    }

    public void clickCover(View view)
    {
        Intent intent = new Intent(ctx, ImgActivity.class);
        intent.putExtra("imgUrl", new String[]{videoModel.video_cover});
        startActivity(intent);
    }

    public void clickPlay(View view)
    {
        Intent intent = new Intent(ctx, PlayerActivity.class);
        intent.putExtra("title", videoModel.video_title);
        intent.putExtra("aid", videoModel.video_aid);
        intent.putExtra("cid", videoModel.video_cid);
        startActivity(intent);
    }

    public void clickDownload(View view)
    {
        String[] videoPartNames = new String[videoModel.video_part_array_list.size()];
        String[] videoPartCids = new String[videoModel.video_part_array_list.size()];
        for(int i = 0; i < videoModel.video_part_array_list.size(); i++)
            videoPartNames[i] = videoModel.video_part_array_list.get(i).video_part_name;
        for(int i = 0; i < videoModel.video_part_array_list.size(); i++)
            videoPartCids[i] = String.valueOf(videoModel.video_part_array_list.get(i).video_part_cid);
        Intent intent = new Intent(ctx, SelectPartActivity.class);
        intent.putExtra("title", "分P下载");
        intent.putExtra("tip", "选择要下载的分P");
        intent.putExtra("options_name", videoPartNames);
        intent.putExtra("options_id", videoPartCids);
        startActivityForResult(intent, RESULT_VD_DOWNLOAD);
    }

    public void clickCoverLater(View view)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    String result = videoDetail.playLater();
                    if(result.equals(""))
                    {
                        Looper.prepare();
                        Toast.makeText(ctx, "已添加至稍后再看", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ctx, "未成功添加至稍后观看！请检查网络再试", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }
        }).start();
    }

    public void clickHistory(View view)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    String result = videoDetail.playHistory();
                    if(result.equals(""))
                    {
                        Looper.prepare();
                        Toast.makeText(ctx, "已添加至历史记录！你可以在历史记录找到", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ctx, "未成功添加至历史记录！请检查网络再试", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }
        }).start();
    }

    public void clickShare(View view)
    {
        Intent intent = new Intent(ctx, SendDynamicActivity.class);
        intent.putExtra("is_share", true);
        intent.putExtra("share_dyid", videoModel.video_aid);
        intent.putExtra("share_up", videoModel.video_up_name);
        intent.putExtra("share_img", videoModel.video_cover);
        intent.putExtra("share_title", videoModel.video_title);
        startActivityForResult(intent, RESULT_VD_SHARE);
    }

    public void clickLike(View view)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if(videoModel.video_user_like)
                    {
                        String result = videoDetail.likeVideo(2);
                        if(result.equals(""))
                        {
                            videoModel.video_detail_like--;
                            videoModel.video_user_like = false;
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
                        String result = videoDetail.likeVideo(1);
                        if(result.equals(""))
                        {
                            videoModel.video_detail_like++;
                            videoModel.video_user_like = true;
                            videoModel.video_user_dislike = false;
                            Looper.prepare();
                            Toast.makeText(ctx, "已喜欢！这个视频会被更多人看到！", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Looper.prepare();
                            Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
                        }
                    }
                    handler.post(runnableImg);
                    Looper.loop();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(ctx, "喜欢失败...请检查你的网络..", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }
        }).start();
    }

    public void clickCoin(View view)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if(videoModel.video_detail_copyright == 1)  //1原创
                    {
                        if(videoModel.video_user_coin < 2)
                        {
                            String result = videoDetail.coinVideo(1);
                            if(result.equals(""))
                            {
                                videoModel.video_detail_coin++;
                                videoModel.video_user_coin++;
                                Looper.prepare();
                                Toast.makeText(ctx, "你投了一个硬币！再次点击可以再次投币！", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(ctx, "最多投两个硬币...", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else  //2转载
                    {
                        if(videoModel.video_user_coin < 1)
                        {
                            String result = videoDetail.coinVideo(1);
                            if(result.equals(""))
                            {
                                videoModel.video_detail_coin++;
                                videoModel.video_user_coin++;
                                Looper.prepare();
                                Toast.makeText(ctx, "你投了一个硬币！本稿件最多投一个硬币", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(ctx, "本稿件最多投一个硬币...", Toast.LENGTH_SHORT).show();
                        }
                    }
                    handler.post(runnableImg);
                    Looper.loop();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(ctx, "投币失败！请检查你的网络..", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }
        }).start();
    }

    public void clickFav(final View view)
    {
        findViewById(R.id.vd_vd_loading).setVisibility(View.VISIBLE);
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    FavorBoxApi favorBoxApi = new FavorBoxApi(
                            sharedPreferences.getString("cookies", ""),
                            sharedPreferences.getString("mid", ""));
                    ArrayList<FavorBoxModel> favorBoxArrayList = favorBoxApi.getFavorbox();
                    String[] favorBoxNames = new String[favorBoxArrayList.size()];
                    for (int i = 0; i < favorBoxArrayList.size(); i++)
                        favorBoxNames[i] = favorBoxArrayList.get(i).title;
                    String[] favorBoxIds = new String[favorBoxArrayList.size()];
                    for (int i = 0; i < favorBoxArrayList.size(); i++)
                        favorBoxIds[i] = favorBoxArrayList.get(i).id;
                    Intent intent = new Intent(ctx, SelectPartActivity.class);
                    intent.putExtra("title", "收藏");
                    intent.putExtra("tip", "选择收藏夹");
                    intent.putExtra("options_name", favorBoxNames);
                    intent.putExtra("options_id", favorBoxIds);
                    startActivityForResult(intent, RESULT_VD_FAVOR);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(ctx, "收藏失败！请检查你的网络..", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
                finally
                {
                    handler.post(runnableVideoLoadingFin);
                }
            }
        }).start();
    }

    public void clickDislike(View view)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if(videoModel.video_user_dislike)
                    {
                        videoDetail.likeVideo(4);
                        videoModel.video_user_dislike = false;
                        Looper.prepare();
                        Toast.makeText(ctx, "取消点踩成功！", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        videoDetail.likeVideo(3);
                        videoModel.video_detail_like -= videoModel.video_user_like ? 1 : 0;
                        videoModel.video_user_dislike = true;
                        videoModel.video_user_like = false;
                        Looper.prepare();
                        Toast.makeText(ctx, "点踩成功！", Toast.LENGTH_SHORT).show();
                    }
                    handler.post(runnableImg);
                    Looper.loop();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(ctx, "点踩失败！请检查你的网络..", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }
        }).start();
    }

    public void clickSendReply(View view)
    {
        Intent replyIntent = new Intent(ctx, ReplyActivity.class);
        replyIntent.putExtra("oid", intent.getStringExtra("aid"));
        replyIntent.putExtra("type", "1");
        startActivityForResult(replyIntent, RESULT_REPLY_SEND);
    }

    @Override
    public void onActivityResult(final int requestCode, int resultCode, final Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != 0) return;
        switch (requestCode)
        {
            case RESULT_VD_FAVOR:
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            String result = videoDetail.favVideo(data.getStringExtra("option_id"));
                            if(result.equals(""))
                            {
                                videoModel.video_detail_fav += videoModel.video_user_fav ? 0 : 1;
                                videoModel.video_user_fav = true;
                                Looper.prepare();
                                Toast.makeText(ctx, "已收藏至 " + data.getStringExtra("option_name") + " 收藏夹！",
                                               Toast.LENGTH_SHORT).show();
                                handler.post(runnableImg);
                            }
                            else
                            {
                                Looper.prepare();
                                Toast.makeText(ctx, "错误：" + result, Toast.LENGTH_SHORT).show();
                            }
                            Looper.loop();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }).start();
                break;

            case RESULT_VD_DOWNLOAD:
                findViewById(R.id.vd_vd_loading).setVisibility(View.VISIBLE);
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            onlineVideoApi = new OnlineVideoApi(
                                    sharedPreferences.getString("cookies", ""),
                                    sharedPreferences.getString("csrf", ""),
                                    sharedPreferences.getString("mid", ""), videoModel.video_aid,
                                    data.getStringExtra("option_id"));
                            onlineVideoApi.connectionVideoUrl();
                            handler.post(runnableVideoLoadingFin);
                            connection.setVideoPartData(data.getStringExtra("option_name") + " - " + videoModel.video_title,
                                                        data.getStringExtra("option_id"));
                            connection.downloadVideo();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                            Looper.prepare();
                            Toast.makeText(ctx, "网络连接失败，请检查网络", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    }
                }).start();
                break;

            case RESULT_VD_PART:
                Intent intent = new Intent(ctx, PlayerActivity.class);
                intent.putExtra("title", data.getStringExtra("option_name") + " - " + videoModel.video_title);
                intent.putExtra("aid", videoModel.video_aid);
                intent.putExtra("cid", data.getStringExtra("option_id"));
                startActivity(intent);
                break;

            case RESULT_VD_SHARE:
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            String result = videoDetail.shareVideo(data.getStringExtra("text"));
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
                break;

            case RESULT_REPLY_SEND:
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            String result = videoDetail.sendReply(data.getStringExtra("text"));
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
                            Toast.makeText(ctx, "评论发送失败。。请检查网络？", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    }
                }).start();
                break;
        }
    }

    class VideoDownloadServiceConnection implements ServiceConnection
    {
        String title;
        String cid;

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            myBinder = (DownloadService.MyBinder) service;
        }

        void setVideoPartData(String title, String cid)
        {
            this.title = title;
            this.cid = cid;
        }

        void downloadVideo()
        {
            String result = myBinder.startDownload(videoModel.video_aid, cid, title,
                                                   videoModel.video_cover, onlineVideoApi.getVideoUrl(),
                                                   onlineVideoApi.getDanmakuUrl());
            Looper.prepare();
            if(result.equals("")) Toast.makeText(ctx, "已添加至下载列表", Toast.LENGTH_SHORT).show();
            else Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
            Looper.loop();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        try
        {
            unbindService(connection);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
