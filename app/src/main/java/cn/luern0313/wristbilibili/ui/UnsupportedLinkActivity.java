package cn.luern0313.wristbilibili.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.UnsupportedLinkApi;
import cn.luern0313.wristbilibili.models.UnsupportedLinkModel;
import cn.luern0313.wristbilibili.util.QRCodeUtil;

public class UnsupportedLinkActivity extends AppCompatActivity
{
    Context ctx;
    Intent intent;
    String url;
    UnsupportedLinkApi unsupportedLinkApi;
    UnsupportedLinkModel unsupportedLinkModel;

    Handler handler = new Handler();
    Runnable runnableUi;
    Runnable runnableErr;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unsupported_link);
        ctx = this;
        intent = getIntent();
        url = intent.getStringExtra("url");
        unsupportedLinkApi = new UnsupportedLinkApi(url);

        ((TextView) findViewById(R.id.ul_link)).setText(url);
        ((ImageView) findViewById(R.id.ul_qr)).setImageBitmap(QRCodeUtil.createQRCodeBitmap(url, 120, 120));

        runnableUi = new Runnable()
        {
            @Override
            public void run()
            {
                findViewById(R.id.ul_loading).setVisibility(View.GONE);
                ((HtmlTextView) findViewById(R.id.ul_info)).setHtml(unsupportedLinkModel.getDetail());
            }
        };

        runnableErr = new Runnable()
        {
            @Override
            public void run()
            {
                findViewById(R.id.ul_loading).setVisibility(View.GONE);
                ((HtmlTextView) findViewById(R.id.ul_info)).setHtml("获取页面信息失败");
            }
        };

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    unsupportedLinkModel = unsupportedLinkApi.getUnsupportedLink();
                    if(unsupportedLinkModel != null)
                        handler.post(runnableUi);
                    else
                        handler.post(runnableErr);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
