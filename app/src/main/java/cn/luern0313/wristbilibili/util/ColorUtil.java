package cn.luern0313.wristbilibili.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.StyleableRes;

import cn.luern0313.wristbilibili.R;

public class ColorUtil
{
    public static @ColorInt int getColor(@AttrRes int attr, @ColorInt int defaultColor, Context context){
        @ColorInt int result;
        TypedArray array = context.getTheme().obtainStyledAttributes(new int[]
        {
            attr
        });
        result = array.getColor(0, defaultColor);
        array.recycle();
        return result;
    }

    public static @ColorInt int[] getColors(@AttrRes int[] attrs, @ColorInt int[] defaultColors, Context context){
        @ColorInt int[] results = new int[attrs.length];
        @SuppressLint("ResourceType")
        TypedArray array = context.getTheme().obtainStyledAttributes(attrs);
        for(int i = 0; i < results.length; i++) {
            results[i] = array.getColor(0, defaultColors[i]);
        }
        array.recycle();
        return results;
    }
}
