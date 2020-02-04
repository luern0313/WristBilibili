package cn.luern0313.wristbilibili.util;

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
}
