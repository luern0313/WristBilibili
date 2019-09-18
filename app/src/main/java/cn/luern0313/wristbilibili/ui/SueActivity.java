package cn.luern0313.wristbilibili.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.SendDynamicApi;

public class SueActivity extends Activity
{
    Context ctx;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    SendDynamicApi sendDynamicApi;

    TextView uiText;
    LinearLayout uiLoading;
    LinearLayout uiNologin;

    Handler handler = new Handler();
    Runnable runnableUi;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sue);
        ctx = this;
        sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        uiText = findViewById(R.id.dt_text);
        uiLoading = findViewById(R.id.dt_loading);
        uiNologin = findViewById(R.id.dt_nologin);
        sendDynamicApi = new SendDynamicApi(sharedPreferences.getString("cookies", ""), sharedPreferences.getString("mid", ""), sharedPreferences.getString("csrf", ""));
        uiText.setText(Html.fromHtml("<font color=\"#3F51B5\">#腕上哔哩# #用手表上b站# </font>" + sendDynamicApi.getNextShareText()));

        if(!sharedPreferences.contains("cookies"))
            uiNologin.setVisibility(View.VISIBLE);

        runnableUi = new Runnable()
        {
            @Override
            public void run()
            {
                uiLoading.setVisibility(View.GONE);
            }
        };
    }

    public void clickRe(View view)
    {
        uiText.setText(Html.fromHtml("<font color=\"#3F51B5\">#腕上哔哩# #用手表上b站# </font>" + sendDynamicApi.getNextShareText()));
    }

    public void clickSend(View view)
    {
        uiLoading.setVisibility(View.VISIBLE);
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    sendDynamicApi.shareVideo("#腕上哔哩# #用手表上b站# " + sendDynamicApi.getNowShareText());
                    Looper.prepare();
                    Toast.makeText(ctx, "发送成功！", Toast.LENGTH_SHORT).show();
                    handler.post(runnableUi);
                    Looper.loop();
                }
                catch (IOException e)
                {
                    Looper.prepare();
                    Toast.makeText(ctx, "好像没有网络连接呢...", Toast.LENGTH_SHORT).show();
                    handler.post(runnableUi);
                    Looper.loop();
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
