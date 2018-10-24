package cn.luern0313.wristbilibili.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import cn.luern0313.wristbilibili.R;

public class DownloadActivity extends Activity
{
    Context ctx;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        ctx = this;
    }

    public void buttonTitle(View view)
    {
        Intent intent = new Intent(ctx, MenuActivity.class);
        startActivity(intent);
    }
}
