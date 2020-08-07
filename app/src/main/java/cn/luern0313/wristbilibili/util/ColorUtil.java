package cn.luern0313.wristbilibili.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

public class ColorUtil
{
    public static @ColorInt int getColor(@AttrRes int attr, @ColorRes int defaultColorRes, Context context)
    {
        return getColor(attr, ContextCompat.getColor(context, defaultColorRes), context.getTheme());
    }

    public static @ColorInt int getColor(@AttrRes int attr, @ColorInt int defaultColor, Resources.Theme theme)
    {
        @ColorInt int result;
        TypedArray array = theme.obtainStyledAttributes(new int[]{attr});
        result = array.getColor(0, defaultColor);
        array.recycle();
        return result;
    }

    public static @ColorInt int[] getColors(@AttrRes int[] attrs, @ColorInt int[] defaultColors, Context context)
    {
        @ColorInt int[] results = new int[attrs.length];
        @SuppressLint("ResourceType") TypedArray array = context.getTheme().obtainStyledAttributes(attrs);
        for (int i = 0; i < results.length; i++)
        {
            results[i] = array.getColor(i, defaultColors[i]);
        }
        array.recycle();
        return results;
    }
}
