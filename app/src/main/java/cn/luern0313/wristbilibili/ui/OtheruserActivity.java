package cn.luern0313.wristbilibili.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import cn.luern0313.wristbilibili.R;

public class OtheruserActivity extends Activity
{
    Context ctx;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otheruser);

        ctx = this;
    }
}
