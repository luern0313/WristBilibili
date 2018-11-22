package cn.luern0313.wristbilibili.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.VideoDetails;

public class VideodetailsActivity extends Activity
{
    Context ctx;
    Intent intent;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    VideoDetails videoDetail;

    Handler handler = new Handler();
    Runnable runnableNoWeb;
    Runnable runnableUi;

    AnimationDrawable loadingImgAnim;

    ImageView uiLoadingImg;
    LinearLayout uiLoading;
    LinearLayout uiNoWeb;
    TextView uiLikeText;
    TextView uiCoinText;
    TextView uiFavText;
    ImageView uiLikeImg;
    ImageView uiCoinImg;
    ImageView uiFavImg;
    ImageView uiDislikeImg;
    LinearLayout uiLikeLay;
    LinearLayout uiCoinLay;
    LinearLayout uiFavLay;
    LinearLayout uiDislikeLay;

    boolean isLogin = false;
    int isLiked = 0;//012
    int isCoined = 0;
    boolean isFaved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videodetails);

        ctx = this;
        intent = getIntent();
        sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        uiLoadingImg = findViewById(R.id.vd_loading_img);
        uiLoading = findViewById(R.id.vd_loading);
        uiNoWeb = findViewById(R.id.vd_noweb);
        uiLikeText = findViewById(R.id.vd_like_text);
        uiCoinText = findViewById(R.id.vd_coin_text);
        uiFavText = findViewById(R.id.vd_fav_text);
        uiLikeImg = findViewById(R.id.vd_like_img);
        uiCoinImg = findViewById(R.id.vd_coin_img);
        uiFavImg = findViewById(R.id.vd_fav_img);
        uiDislikeImg = findViewById(R.id.vd_dislike_img);
        uiLikeLay = findViewById(R.id.vd_like);
        uiCoinLay = findViewById(R.id.vd_coin);
        uiFavLay = findViewById(R.id.vd_fav);
        uiDislikeLay = findViewById(R.id.vd_dislike);

        isLogin = !MainActivity.sharedPreferences.getString("cookies", "").equals("");
        videoDetail = new VideoDetails(sharedPreferences.getString("cookies", ""), sharedPreferences.getString("csrf", ""), sharedPreferences.getString("mid", ""), intent.getStringExtra("aid"));

        uiLoadingImg.setImageResource(R.drawable.anim_loading);
        loadingImgAnim = (AnimationDrawable) uiLoadingImg.getDrawable();
        loadingImgAnim.start();
        uiLoading.setVisibility(View.VISIBLE);

        runnableNoWeb = new Runnable()
        {
            @Override
            public void run()
            {
                uiLoading.setVisibility(View.GONE);
                uiNoWeb.setVisibility(View.VISIBLE);
            }
        };

        runnableUi = new Runnable()
        {
            @Override
            public void run()
            {
                ((TextView) findViewById(R.id.vd_video_title)).setText(videoDetail.getVideoTitle());
                ((TextView) findViewById(R.id.vd_video_play)).setText("播放:" + videoDetail.getVideoPlay() + "  弹幕:" + videoDetail.getVideoDanmaku());
                ((TextView) findViewById(R.id.vd_video_time)).setText(videoDetail.getVideoupTime() + "  AV" + videoDetail.getVideoAid());
                ((TextView) findViewById(R.id.vd_video_details)).setText(videoDetail.getVideoDetail());
                ((TextView) findViewById(R.id.vd_card_name)).setText(videoDetail.getVideoUP());
                ((TextView) findViewById(R.id.vd_card_sen)).setText(videoDetail.getVideoUpSign());
                if(videoDetail.isFollowing())
                {
                    ((TextView) findViewById(R.id.vd_card_button)).setText("已关注");
                    ((TextView) findViewById(R.id.vd_card_button)).setBackgroundResource(R.drawable.shape_anre_followbgyes);
                }
                uiLikeText.setText(videoDetail.getVideoLike());
                uiCoinText.setText(videoDetail.getVideoCoin());
                uiFavText.setText(videoDetail.getVideoFav());
                isLiked = videoDetail.getSelfLiked();
                isCoined = videoDetail.getSelfCoined();
                isFaved = videoDetail.getSelfFaved();

                setIcon();

                uiLoading.setVisibility(View.GONE);
                uiNoWeb.setVisibility(View.GONE);
            }
        };

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if(videoDetail.getVideoDetails())
                    {
                        handler.post(runnableUi);
                    }
                    else
                    {
                        findViewById(R.id.vd_novideo).setVisibility(View.VISIBLE);
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

    void setIcon()
    {
        uiLikeImg.setImageResource(R.drawable.icon_vdd_do_like_no);
        uiCoinImg.setImageResource(R.drawable.icon_vdd_do_coin_no);
        uiFavImg.setImageResource(R.drawable.icon_vdd_do_fav_no);
        uiDislikeImg.setImageResource(R.drawable.icon_vdd_do_dislike_no);
        if(isLiked == 1)
            uiLikeImg.setImageResource(R.drawable.icon_vdd_do_like_yes);
        else if(isLiked == 2)
            uiDislikeImg.setImageResource(R.drawable.icon_vdd_do_dislike_yes);
        if(isCoined > 0)
            uiCoinImg.setImageResource(R.drawable.icon_vdd_do_coin_yes);
        if(isFaved)
            uiFavImg.setImageResource(R.drawable.icon_vdd_do_fav_yes);
    }

    public void clickCover(View view)
    {
    }

    public void clickPlay(View view)
    {
        Intent intent = new Intent(ctx, QRActivity.class);
        intent.putExtra("url", "https://www.bilibili.com/video/av" + videoDetail.getVideoAid());
        startActivity(intent);
    }

    public void clickCoverLater(View view)
    {
    }

    public void clickHistory(View view)
    {
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
                    if(isLiked == 1)
                    {
                        videoDetail.likeVideo(2);
                        isLiked = 0;
                    }
                    else
                    {
                        videoDetail.likeVideo(1);
                        isLiked = 1;
                    }
                    setIcon();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(ctx, "点赞失败！请检查你的网络..", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }
        }).start();
    }

    public void clickCoin(View view)
    {

    }

    public void clickFav(View view)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if(isFaved)
                    {
                        videoDetail.likeVideo(4);
                        isLiked = 0;
                    }
                    else
                    {
                        videoDetail.likeVideo(3);
                        isLiked = 2;
                    }
                    setIcon();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(ctx, "收藏失败！请检查你的网络..", Toast.LENGTH_SHORT).show();
                    Looper.loop();
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
                    if(isLiked == 2)
                    {
                        videoDetail.likeVideo(4);
                        isLiked = 0;
                    }
                    else
                    {
                        videoDetail.likeVideo(3);
                        isLiked = 2;
                    }
                    setIcon();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(ctx, "点赞失败！请检查你的网络..", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }
        }).start();
    }

}
