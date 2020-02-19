package cn.luern0313.wristbilibili.util;

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
}
