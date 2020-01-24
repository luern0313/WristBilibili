package cn.luern0313.wristbilibili.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.BangumiApi;
import cn.luern0313.wristbilibili.models.BangumiModel;
import cn.luern0313.wristbilibili.service.DownloadService;

public class BangumiActivity extends Activity
{
    Context ctx;
    Intent intent;
    LayoutInflater inflater;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    BangumiApi bangumiApi;
    BangumiModel bangumiModel;

    TextView uiTitle;
    ViewPager uiViewPager;
    ImageView uiLoadingImg;
    LinearLayout uiLoading;
    LinearLayout uiNoWeb;

    boolean isLogin = false;

    AnimationDrawable loadingImgAnim;

    Handler handler = new Handler();
    Runnable runnableDetailUi;
    Runnable runnableDetailNoWeb;
    Runnable runnableDetailNodata;

    private DownloadService.MyBinder myBinder;
    private BangumiDownloadServiceConnection connection = new BangumiDownloadServiceConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bangumi);
        ctx = this;
        intent = getIntent();
        sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        inflater = getLayoutInflater();

        uiTitle = findViewById(R.id.bgm_title_title);
        uiViewPager = findViewById(R.id.bgm_viewpager);
        uiViewPager.setOffscreenPageLimit(2);
        uiLoadingImg = findViewById(R.id.bgm_loading_img);
        uiLoading = findViewById(R.id.bgm_loading);
        uiNoWeb = findViewById(R.id.bgm_noweb);

        isLogin = !sharedPreferences.getString("cookies", "").equals("");
        bangumiApi = new BangumiApi(sharedPreferences.getString("cookies", ""),
                                    sharedPreferences.getString("csrf", ""),
                                    sharedPreferences.getString("mid", ""),
                                    sharedPreferences.getString("access_key", ""),
                                    intent.getStringExtra("season_id"));

        uiLoadingImg.setImageResource(R.drawable.anim_loading);
        loadingImgAnim = (AnimationDrawable) uiLoadingImg.getDrawable();
        loadingImgAnim.start();
        uiLoading.setVisibility(View.VISIBLE);

        runnableDetailUi = new Runnable()
        {
            @Override
            public void run()
            {
                ((TextView) findViewById(R.id.bgm_detail_title)).setText(bangumiModel.bangumi_title);
                ((TextView) findViewById(R.id.bgm_detail_score)).setText(bangumiModel.bangumi_score);
                ((TextView) findViewById(R.id.bgm_detail_play)).setText(bangumiModel.bangumi_play);
                ((TextView) findViewById(R.id.bgm_detail_like)).setText(bangumiModel.bangumi_like);
                ((TextView) findViewById(R.id.bgm_detail_series)).setText(bangumiModel.bangumi_series);
                ((TextView) findViewById(R.id.bgm_detail_needvip)).setText(bangumiModel.bangumi_needvip);
                ((TextView) findViewById(R.id.bgm_detail_series)).setText(bangumiModel.bangumi_series);

                Drawable playNumDrawable = getResources().getDrawable(R.drawable.icon_video_play_num);
                Drawable danmakuNumDrawable = getResources().getDrawable(R.drawable.icon_video_like_num);
                playNumDrawable.setBounds(0,0,22,22);
                danmakuNumDrawable.setBounds(0,0,22,22);
                ((TextView) findViewById(R.id.bgm_detail_play)).setCompoundDrawables(playNumDrawable,null, null,null);
                ((TextView) findViewById(R.id.bgm_detail_like)).setCompoundDrawables(danmakuNumDrawable,null, null,null);

                ((TextView) findViewById(R.id.bgm_detail_video_part_text)).setText("共" + String.valueOf(bangumiModel.bangumi_seasons.size()) + "P");

                for (int i = 0; i < bangumiModel.bangumi_seasons.size(); i++)
                {
                    ((LinearLayout) findViewById(R.id.bgm_detail_video_part_layout)).addView(getVideoPartButton(bangumiModel.bangumi_episodes.get(i)));
                }

                uiLoading.setVisibility(View.GONE);
                uiNoWeb.setVisibility(View.GONE);
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
                    findViewById(R.id.bgm_novideo).setVisibility(View.VISIBLE);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        final PagerAdapter pagerAdapter = new PagerAdapter()
        {
            @Override
            public int getCount()
            {
                return 3;
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
                    View v = inflater.inflate(R.layout.viewpager_bangumi_detail, null);
                    v.setTag(0);

                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                bangumiModel = bangumiApi.getBangumiInfo();
                                if(bangumiModel != null)
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
                return 0;
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
                if(position == 0) titleAnim("视频详情");
                else if(position == 1) titleAnim("视频评论");
                else if(position == 2) titleAnim("相关推荐");
            }
        });

        uiViewPager.setAdapter(pagerAdapter);
    }

    TextView getVideoPartButton(final BangumiModel.BangumiEpisodeModel part)
    {
        TextView textView = new TextView(ctx);
        textView.setWidth(170);
        textView.setBackgroundResource(R.drawable.selector_bg_vd_videopart);
        textView.setPadding(12, 6, 12, 6);
        textView.setText(part.bangumi_episode_title + " " + part.bangumi_episode_title_long);
        textView.setLines(2);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(ctx, PlayerActivity.class);
                intent.putExtra("title", part.bangumi_episode_title + " " + part.bangumi_episode_title_long);
                intent.putExtra("aid", part.bangumi_episode_aid);
                intent.putExtra("part", String.valueOf(part.position));
                intent.putExtra("cid", String.valueOf(part.bangumi_episode_cid));
                startActivity(intent);
            }
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 4, 0);
        textView.setLayoutParams(lp);
        return textView;
    }

    class BangumiDownloadServiceConnection implements ServiceConnection
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
            /*String result = myBinder.startDownload(videoDetail.getVideoAid(), cid, title,
                                                   videoDetail.getVideoFace(),
                                                   onlineVideoApi.getVideoUrl(),
                                                   onlineVideoApi.getDanmakuUrl());
            Looper.prepare();
            if(result.equals("")) Toast.makeText(ctx, "已添加至下载列表", Toast.LENGTH_SHORT).show();
            else Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
            Looper.loop();*/
        }
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
