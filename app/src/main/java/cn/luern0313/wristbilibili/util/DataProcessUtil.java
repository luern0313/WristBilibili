package cn.luern0313.wristbilibili.util;

import android.content.Context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

/**
 * 被 luern0313 创建于 2020/2/3.
 */

public class DataProcessUtil
{
    public static String getView(int view)
    {
        if(view > 100000000) return view / 10000000 / 10.0 + "亿";
        else if(view > 10000) return view / 1000 / 10.0 + "万";
        else return String.valueOf(view);
    }

    public static float getFloatRandom(Random r, float lrange, float urange)
    {
        return r.nextFloat() * (urange - lrange) + lrange;
    }

    public static String getMinFromSec(int sec)
    {
        String m = String.valueOf(sec / 60);
        String s = String.valueOf(sec - sec / 60 * 60);
        if(m.length() == 1) m = "0" + m;
        if(s.length() == 1) s = "0" + s;
        return m + ":" + s;
    }

    public static String joinList(String[] list, String split)
    {
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < list.length; i++)
            stringBuilder.append(i == 0 ? "" : split).append(list[i]);
        return stringBuilder.toString();
    }

    public static String joinArrayList(ArrayList<String> list, String split)
    {
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < list.size(); i++)
            stringBuilder.append(i == 0 ? "" : split).append(list.get(i));
        return stringBuilder.toString();
    }

    public static int dip2px(Context context, float dpValue)
    {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static int getPositionInArrayList(ArrayList arrayList, String string)
    {
        for(int i = 0; i < arrayList.size(); i++)
            if(arrayList.get(i).equals(string))
                return i;
        return -1;
    }

    public static <T extends Serializable> int getPositionInList(T[] list, T element)
    {
        for(int i = 0; i < list.length; i++)
            if(list[i].equals(element))
                return i;
        return -1;
    }

    public static String handleUrl(String url)
    {
        if(url.indexOf("//") == 0)
            url = "http:" + url;
        if(url.endsWith(".webp"))
            url = url.substring(0, url.lastIndexOf("@"));
        return url;
    }

    public static String getSize(long size)
    {
        String[] unit = new String[]{"B", "KB", "MB", "GB"};
        long s = size * 10;
        int u = 0;
        while (s > 10240 && u < unit.length - 1)
        {
            s /= 1024;
            u++;
        }
        return String.valueOf(s / 10.0) + unit[u];
    }

    public static String getSurplusTime(long surplusByte, int speed)
    {
        if(speed <= 0) return "未知";
        long time = surplusByte / speed;

        String sec = String.valueOf(time % 60);
        if(sec.length() == 1) sec = "0" + sec;
        String min = String.valueOf(time / 60 % 60);
        if(min.length() == 1) min = "0" + min;
        String hour = String.valueOf(time / 3600 % 60);
        if(hour.length() == 1) hour = "0" + hour;

        if(hour.equals("00")) return min + ":" + sec;
        else return hour + ":" + min + ":" + sec;
    }
}
