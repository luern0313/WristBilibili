package cn.luern0313.wristbilibili.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

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

    LinearLayout uiLoading;
    LinearLayout uiNoWeb;
    TextView uiLikeText;
    TextView uiCoinText;
    TextView uiFavText;
    LinearLayout uiLikeLay;
    LinearLayout uiCoinLay;
    LinearLayout uiFavLay;
    LinearLayout uiDislikeLay;

    boolean isLogin = false;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videodetails);

        ctx = this;
        intent = getIntent();
        sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        uiLoading = findViewById(R.id.vd_loading);
        uiNoWeb = findViewById(R.id.vd_noweb);
        uiLikeText = findViewById(R.id.vd_like_text);
        uiCoinText = findViewById(R.id.vd_coin_text);
        uiFavText = findViewById(R.id.vd_fav_text);
        uiLikeLay = findViewById(R.id.vd_like);
        uiCoinLay = findViewById(R.id.vd_coin);
        uiFavLay = findViewById(R.id.vd_fav);
        uiDislikeLay = findViewById(R.id.vd_dislike);

        isLogin = !MainActivity.sharedPreferences.getString("cookies", "").equals("");
        videoDetail = new VideoDetails(sharedPreferences.getString("cookies", ""), intent.getStringExtra("aid"));

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
                uiLikeText.setText(videoDetail.getVideoLike());
                uiCoinText.setText(videoDetail.getVideoCoin());
                uiFavText.setText(videoDetail.getVideoFav());
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
                    videoDetail.getVideoDetails();
                    handler.post(runnableUi);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    handler.post(runnableNoWeb);
                }
            }
        }).start();
    }
}
