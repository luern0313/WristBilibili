package cn.luern0313.wristbilibili.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.ImageView;

import cn.luern0313.wristbilibili.R;

public class AboutActivity extends Activity
{
    Context ctx;

    ImageView ui33;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ctx = this;
        ui33 = findViewById(R.id.about_33);
        ui33.setImageResource(R.drawable.anim_33);
        AnimationDrawable loadingImgAnim = (AnimationDrawable) ui33.getDrawable();
        loadingImgAnim.start();
    }
}
