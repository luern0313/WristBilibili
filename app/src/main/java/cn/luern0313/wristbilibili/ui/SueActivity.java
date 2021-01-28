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

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.SendDynamicApi;
import cn.luern0313.wristbilibili.util.ListViewTouchListener;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;
import cn.luern0313.wristbilibili.widget.TitleView;

public class SueActivity extends BaseActivity implements TitleView.TitleViewListener
{
    Context ctx;
    SendDynamicApi sendDynamicApi;

    TitleView titleView;
    TextView uiText;
    LinearLayout uiLoading;
    LinearLayout uiNoLogin;

    Handler handler = new Handler();
    Runnable runnableUi;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sue);
        ctx = this;

        titleView = findViewById(R.id.sue_title);
        uiText = findViewById(R.id.sue_text);
        uiLoading = findViewById(R.id.sue_loading);
        uiNoLogin = findViewById(R.id.dt_nologin);
        sendDynamicApi = new SendDynamicApi();
        uiText.setText(Html.fromHtml("<font color=\"#188ad0\">#腕上哔哩# #用手表上b站# </font>" + sendDynamicApi.getNextShareText()));

        if(!SharedPreferencesUtil.contains(SharedPreferencesUtil.cookies))
            uiNoLogin.setVisibility(View.VISIBLE);

        findViewById(R.id.sue_lay).setOnTouchListener(new ListViewTouchListener(findViewById(R.id.sue_lay), (TitleView.TitleViewListener) ctx));

        runnableUi = () -> uiLoading.setVisibility(View.GONE);
    }

    public void clickRe(View view)
    {
        uiText.setText(Html.fromHtml("<font color=\"#188ad0\">#腕上哔哩# #用手表上b站# </font>" + sendDynamicApi.getNextShareText()));
    }

    public void clickSend(View view)
    {
        uiLoading.setVisibility(View.VISIBLE);
        new Thread(() -> {
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
        }).start();
    }

    @Override
    public boolean hideTitle()
    {
        return titleView.hide();
    }

    @Override
    public boolean showTitle()
    {
        return titleView.show();
    }
}
