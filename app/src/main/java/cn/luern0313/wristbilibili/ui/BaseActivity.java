package cn.luern0313.wristbilibili.ui;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;
import cn.luern0313.wristbilibili.util.ThemeUtil;

/**
 * 所有需要自动切换主题的Activity都要继承这个类
 */
public class BaseActivity extends AppCompatActivity
{
    ActivityChangeListener listener;
    ThemeUtil.Theme currentTheme;

    private static final ArrayList<ActivityChangeListener> listenerList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        getWindow().requestFeature(Window.FEATURE_SWIPE_TO_DISMISS);
        ThemeUtil.changeTheme(this, ThemeUtil.getCurrentTheme());
        currentTheme = ThemeUtil.getCurrentTheme();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            Resources resources = getResources();
            Configuration configuration = resources.getConfiguration();
            configuration.screenLayout = (configuration.screenLayout & ~Configuration.SCREENLAYOUT_ROUND_MASK) |
                    (SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.screenRound, !configuration.isScreenRound()) ?
                            Configuration.SCREENLAYOUT_ROUND_NO : Configuration.SCREENLAYOUT_ROUND_YES);
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        listener = this::recreate;
        if(getClass() != ThemeActivity.class)
            addThemeChangeListener(listener);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        removeThemeChangeListener(listener);
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        listener = this::recreate;
        if(getClass() != ThemeActivity.class)
            addThemeChangeListener(listener);
        if(!ThemeUtil.getCurrentTheme().equals(currentTheme))
        {
            recreate();
        }
    }

    public boolean isRound()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return getResources().getConfiguration().isScreenRound();
        return true;
    }

    public static void addThemeChangeListener(ActivityChangeListener listener)
    {
        listenerList.add(listener);
    }

    public static void removeThemeChangeListener(ActivityChangeListener listener)
    {
        listenerList.remove(listener);
    }

    public static void restartAllActivity()
    {
        for (ActivityChangeListener listener : listenerList)
        {
            listener.onThemeChange();
        }
    }

    public interface ActivityChangeListener
    {
        void onThemeChange();
    }
}
