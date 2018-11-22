package cn.luern0313.wristbilibili.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.widget.QRCodeUtil;

public class QRActivity extends Activity
{
    Context ctx;
    ImageView qrImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        ctx = this;

        Intent intent = getIntent();
        String url = intent.getStringExtra("url");

        qrImageView = findViewById(R.id.qr_qr);
        qrImageView.setImageBitmap(QRCodeUtil.createQRCodeBitmap(url, 96, 96));
    }
}
