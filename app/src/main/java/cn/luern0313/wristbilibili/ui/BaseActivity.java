package cn.luern0313.wristbilibili.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import cn.luern0313.wristbilibili.util.ThemeUtil;

public class BaseActivity extends AppCompatActivity
{
    ThemeUtil.ThemeChangeListener listener;
    ThemeUtil.Theme currentTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        ThemeUtil.changeTheme(this, ThemeUtil.getCurrentTheme());
        currentTheme = ThemeUtil.getCurrentTheme();
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

    @Override
    protected void onRestart() {
        super.onRestart();
        listener = new ThemeUtil.ThemeChangeListener() {
            @Override
            public void onThemeChange() {
                recreate();
            }
        };
        ThemeUtil.addThemeChangeListener(listener);
        if(!ThemeUtil.getCurrentTheme().equals(currentTheme)) {
            recreate();
        }
    }
}
