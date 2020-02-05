package cn.luern0313.wristbilibili.api;

import java.io.IOException;

import cn.luern0313.wristbilibili.util.NetWorkUtil;

/**
 * 被 luern0313 创建于 2019/10/9.
 */

public class StatisticsApi
{
    public static void Statistics(String uid) throws IOException
    {
        NetWorkUtil.get("http://luern0313.cn:8080/b/statistic?uid=" + uid + "&tip=仅用于统计用户量");
    }
}
