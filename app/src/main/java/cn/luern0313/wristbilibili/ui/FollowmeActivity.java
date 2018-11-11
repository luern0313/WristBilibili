package cn.luern0313.wristbilibili.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import cn.luern0313.wristbilibili.R;

/**
 * Created by liupe on 2018/11/11.
 */

public class FollowmeActivity extends Activity
{
    Context ctx;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followme);
        ctx = this;
    }
}
