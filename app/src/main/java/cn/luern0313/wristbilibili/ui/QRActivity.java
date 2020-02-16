package cn.luern0313.wristbilibili.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;

import cn.luern0313.wristbilibili.R;

public class QRActivity extends AppCompatActivity
{
    Context ctx;
    ImageView qrImageView;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
    }
}
