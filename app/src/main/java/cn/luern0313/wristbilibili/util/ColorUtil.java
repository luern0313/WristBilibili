package cn.luern0313.wristbilibili.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;

public class ColorUtil
{
    public static @ColorInt int getColor(@AttrRes int attr, Context context)
    {
        return getColor(attr, context.getTheme());
    }

    public static @ColorInt int getColor(@AttrRes int attr, Resources.Theme theme)
    {
        TypedValue value = new TypedValue();
        theme.resolveAttribute(attr, value, true);
        return value.data;
    }
}
