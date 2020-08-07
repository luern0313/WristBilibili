package cn.luern0313.wristbilibili.util;

import android.content.Context;

import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.ArrayList;

import cn.luern0313.wristbilibili.R;

public class ThemeUtil {
    public static class Theme {
        private final @StringRes int name;
        private final @ColorRes int previewColor;
        private final @StyleRes int style;
        private final int id;
        private final boolean isDarkTheme;

        private Theme(@StringRes int name, int previewColor, int style, boolean isDarkTheme, int id) {
            this.name = name;
            this.previewColor = previewColor;
            this.style = style;
            this.isDarkTheme = isDarkTheme;
            this.id = id;
        }

        public @StringRes int getName() {
            return name;
        }

        public int getPreviewColor() {
            return previewColor;
        }

        public int getStyle() {
            return style;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Theme theme = (Theme) o;

            return id == theme.id;
        }

        @Override
        public int hashCode() {
            return id;
        }

        public boolean isDarkTheme() {
            return isDarkTheme;
        }
    }

    public interface ThemeChangeListener {
        void onThemeChange();
    }

    private static int currentTheme = -1;

    public static final Theme[] themes = new Theme[]
    {
        new Theme(R.string.theme_name_pink, R.color.mainColor, R.style.AppTheme, false, 0),
        new Theme(R.string.theme_name_dark, R.color.colorPrimaryDarkNormal, R.style.DarkTheme, true, 1),
        new Theme(R.string.theme_name_black, R.color.black, R.style.BlackTheme, true, 5),
        new Theme(R.string.theme_name_blue, R.color.mainColorBlue, R.style.BlueTheme, false, 2),
        new Theme(R.string.theme_name_green, R.color.mainColorGreen, R.style.GreenTheme, false, 3),
        new Theme(R.string.theme_name_red, R.color.mainColorRed, R.style.RedTheme, false, 4)
    };

    private static final ArrayList<ThemeChangeListener> listenerList =
            new ArrayList<>();

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
