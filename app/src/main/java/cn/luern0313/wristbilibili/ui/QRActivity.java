package cn.luern0313.wristbilibili.ui;

import android.content.Context;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import cn.luern0313.wristbilibili.R;

public class QRActivity extends BaseActivity
{
    Context ctx;
    ImageView qrImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
    }
}
