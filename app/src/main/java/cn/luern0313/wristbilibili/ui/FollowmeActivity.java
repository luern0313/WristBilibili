package cn.luern0313.wristbilibili.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.OthersUser;
import cn.luern0313.wristbilibili.api.VideoDetails;

/**
 * Created by liupe on 2018/11/11.
 * 关注我！投硬币！给好评！
 */

public class FollowmeActivity extends Activity
{
    Context ctx;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    String cookies;
    String csrf;
    String mid;

    CardView cardView;
    RelativeLayout cardViewLay;
    TextView cardViewText;
    LinearLayout likeView;
    LinearLayout coinView;
    ImageView likeViewImg;
    ImageView coinViewImg;

    OthersUser othersUser;
    JSONArray jsonArray = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followme);
        ctx = this;

        sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        cookies = sharedPreferences.getString("cookies", "");
        csrf = sharedPreferences.getString("csrf", "");
        mid = sharedPreferences.getString("mid", "");
        othersUser = new OthersUser(cookies, csrf, "8014831");

        cardView = findViewById(R.id.fme_card);
        cardViewLay = findViewById(R.id.fme_card_lay);
        cardViewText = findViewById(R.id.fme_card_button);
        likeView = findViewById(R.id.fme_like);
        coinView = findViewById(R.id.fme_coin);
        likeViewImg = findViewById(R.id.fme_like_img);
        coinViewImg = findViewById(R.id.fme_coin_img);

        if(!sharedPreferences.contains("cookies"))
            findViewById(R.id.fme_nologin).setVisibility(View.VISIBLE);
        else
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        jsonArray = new JSONObject(othersUser.getOtheruserVideo()).getJSONObject("data").getJSONArray("vlist");
                        VideoDetails videoDetail = new VideoDetails(cookies, csrf, mid, String.valueOf(jsonArray.getJSONObject(0).getInt("aid")));
                        videoDetail.playHistory();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        cardView.setOnTouchListener(new View.OnTouchListener()
        {

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    cardViewText.setText("已关注");
                    cardViewText.setBackgroundResource(R.drawable.shape_anre_followbgyes);
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                othersUser.follow();
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                                Looper.prepare();
                                Toast.makeText(ctx, "关注失败...", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        }
                    }).start();
                }
                return true;
            }
        });

        likeView.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    likeViewImg.setImageResource(R.drawable.icon_like_yes);
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                if(jsonArray == null)
                                    jsonArray = new JSONObject(othersUser.getOtheruserVideo()).getJSONObject("data").getJSONArray("vlist");
                                VideoDetails videoDetails = null;
                                for(int i = 0; i < jsonArray.length(); i++)
                                {
                                   videoDetails = new VideoDetails(cookies, csrf, mid, String.valueOf(jsonArray.getJSONObject(i).getInt("aid")));
                                   videoDetails.likeVideo(1);
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
                return true;
            }
        });

        coinView.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    coinViewImg.setImageResource(R.drawable.icon_coin_yes);
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                if(jsonArray == null)
                                    jsonArray = new JSONObject(othersUser.getOtheruserVideo()).getJSONObject("data").getJSONArray("vlist");
                                VideoDetails videoDetails = null;
                                for(int i = 0; i <= 7; i++)
                                {
                                    videoDetails = new VideoDetails(cookies, csrf, mid, String.valueOf(jsonArray.getJSONObject(i).getInt("aid")));
                                    videoDetails.coinVideo(2);
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
                return true;
            }
        });
    }
}
