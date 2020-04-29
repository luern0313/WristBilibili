package cn.luern0313.wristbilibili.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    Uri url;
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

        Uri uri = intent.getData();
        if (uri != null)
            url = uri;
        else
            url = Uri.parse(intent.getStringExtra("url"));

        unsupportedLinkApi = new UnsupportedLinkApi(ctx, url);

        if(unsupportedLinkApi.getIntent() != null)
        {
            startActivity(unsupportedLinkApi.getIntent());
            finish();
        }

        ((TextView) findViewById(R.id.ul_link)).setText(unsupportedLinkApi.getUrl());
        ((ImageView) findViewById(R.id.ul_qr)).setImageBitmap(QRCodeUtil.createQRCodeBitmap(unsupportedLinkApi.getUrl(), 120, 120));

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
                catch (IOException | IllegalArgumentException e)
                {
                    e.printStackTrace();
                    handler.post(runnableErr);
                }
            }
        }).start();
    }

    public void clickUnsupported(View view)
    {
        final Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(url);
        if (intent.resolveActivity(getPackageManager()) != null)
            startActivity(Intent.createChooser(intent, "请选择浏览器"));
        else
            Toast.makeText(ctx, "没有匹配的程序", Toast.LENGTH_SHORT).show();
    }
}
