package cn.luern0313.wristbilibili.util;

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
}
