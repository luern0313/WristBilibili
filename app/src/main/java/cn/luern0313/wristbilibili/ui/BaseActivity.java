package cn.luern0313.wristbilibili.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import cn.luern0313.wristbilibili.util.ThemeUtil;

public class BaseActivity extends AppCompatActivity
{
    ThemeUtil.ThemeChangeListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        ThemeUtil.changeTheme(this, ThemeUtil.getCurrentTheme());
    }

    @Override
    protected void onStart() {
        super.onStart();
        listener = new ThemeUtil.ThemeChangeListener() {
            @Override
            public void onThemeChange() {
                recreate();
            }
        };
        ThemeUtil.addThemeChangeListener(listener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        ThemeUtil.removeThemeChangeListener(listener);
    }
}
