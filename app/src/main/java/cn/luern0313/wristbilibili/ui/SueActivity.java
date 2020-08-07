package cn.luern0313.wristbilibili.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.SendDynamicApi;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;

public class SueActivity extends BaseActivity
{
    Context ctx;
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

        uiText = findViewById(R.id.dt_text);
        uiLoading = findViewById(R.id.dt_loading);
        uiNologin = findViewById(R.id.dt_nologin);
        sendDynamicApi = new SendDynamicApi();
        uiText.setText(Html.fromHtml("<font color=\"#3F51B5\">#腕上哔哩# #用手表上b站# </font>" + sendDynamicApi.getNextShareText()));

        if(!SharedPreferencesUtil.contains(SharedPreferencesUtil.cookies))
            uiNologin.setVisibility(View.VISIBLE);

        runnableUi = new Runnable()
        {
            @Override
            public void run()
            {
                uiLoading.setVisibility(View.GONE);
            }
        };

        findViewById(R.id.dt_title_layout).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
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
