package cn.luern0313.wristbilibili.util;

import android.content.Context;

import androidx.annotation.ColorRes;
import androidx.annotation.StyleRes;

import java.util.ArrayList;

import cn.luern0313.wristbilibili.R;

public class ThemeUtil {
    public static class Theme {
        public final String name;
        public final @ColorRes int previewColor;
        public final @StyleRes int style;
        public final int id;

        private Theme(String name, int previewColor, int style, int id) {
            this.name = name;
            this.previewColor = previewColor;
            this.style = style;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public int getPreviewColor() {
            return previewColor;
        }

        public int getStyle() {
            return style;
        }
    }

    public static interface ThemeChangeListener {
        void onThemeChange();
    }

    private static int currentTheme = -1;

    public static final Theme[] themes = new Theme[]
    {
        new Theme("少女粉", R.color.mainColor, R.style.AppTheme, 0),
        new Theme("黑暗模式", R.color.colorPrimaryDarkNormal, R.style.DarkTheme, 1),
        new Theme("天依蓝", R.color.mainColorBlue, R.style.BlueTheme, 2),
        new Theme("初音绿", R.color.mainColorGreen, R.style.GreenTheme, 3)
    };

    private static final ArrayList<ThemeChangeListener> listenerList =
            new ArrayList<ThemeChangeListener>();

    public static void addThemeChangeListener(ThemeChangeListener listener) {
        listenerList.add(listener);
    }

    public static void removeThemeChangeListener(ThemeChangeListener listener) {
        listenerList.remove(listener);
    }

    private static int readCurrentTheme()
    {
        int id = SharedPreferencesUtil.getInt(SharedPreferencesUtil.theme, 0);
        int finalPos = 0;
        for (int i = 0; i < themes.length; i++)
        {
            if(themes[i].id == id)
            {
                finalPos = i;
                break;
            }
        }
        return finalPos;
    }

    public static int getCurrentThemePos()
    {
        return currentTheme == -1 ? currentTheme = readCurrentTheme() : currentTheme;
    }

    public static Theme getCurrentTheme()
    {
        return themes[getCurrentThemePos()];
    }

    public static void changeCurrentTheme(Theme theme)
    {
        SharedPreferencesUtil.putInt(SharedPreferencesUtil.theme, theme.id);
        for (int i = 0; i < themes.length; i++)
        {
            if(themes[i].id == theme.id)
            {
                currentTheme = i;
                for(ThemeChangeListener listener : listenerList)
                {
                    listener.onThemeChange();
                }
                break;
            }
        }
    }

    public static void changeTheme(Context context, Theme theme)
    {
        context.setTheme(theme.style);
    }
}
