package cn.luern0313.wristbilibili.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.OthersUserApi;
import cn.luern0313.wristbilibili.api.VideoDetailsApi;

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

    OthersUserApi othersUserApi;
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
        othersUserApi = new OthersUserApi(cookies, csrf, "8014831");

        cardView = findViewById(R.id.fme_card);
        cardViewLay = findViewById(R.id.fme_card_lay);
        cardViewText = findViewById(R.id.fme_card_button);

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
                        jsonArray = new JSONObject(othersUserApi.getOtheruserVideo()).getJSONObject("data").getJSONArray("vlist");
                        VideoDetailsApi videoDetail = new VideoDetailsApi(cookies, csrf, mid, String.valueOf(jsonArray.getJSONObject(0).getInt("aid")));
                        videoDetail.playHistory();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        cardView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
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
                            othersUserApi.follow();
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
        });
    }
}
