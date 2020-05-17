package cn.luern0313.wristbilibili.util;

import android.app.Application;
import android.content.Context;

/**
 * 被 luern0313 创建于 2020/5/4.
 */
public class MyApplication extends Application
{
    private static Context mContext;

    @Override
    public void onCreate()
    {
        super.onCreate();
        mContext = getApplicationContext();
    }

    public static Context getContext()
    {
        return mContext;
    }
}
