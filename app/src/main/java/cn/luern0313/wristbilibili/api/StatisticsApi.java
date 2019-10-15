package cn.luern0313.wristbilibili.api;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 被 luern0313 创建于 2019/10/9.
 * 仅用于统计用户量
 */

public class StatisticsApi
{
    public static void Statistics(String uid) throws IOException
    {
        get("http://luern0313.cn:8080/b/statistic?uid=" + uid + "&tip=仅用于统计用户量");
    }

    private static Object get(String url) throws IOException
    {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).build();
        Request.Builder requestb = new Request.Builder().url(url);
        Request request = requestb.build();
        Response response = client.newCall(request).execute();
        if(response.isSuccessful())
            return response.body().string();
        return null;
    }
}
