package cn.luern0313.wristbilibili.api;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 被 luern0313 创建于 2019/10/9.
 */

public class StatisticsApi
{
    public static void Statistics(String uid) throws IOException
    {
        get("http://luern0313.cn:8080/b/statistic?uid=" + uid + "&tip=仅用于统计用户量");
    }

    public static String isShowVideoVote()
    {
        try
        {
            return (String) get("http://luern0313.cn:8080/b/videovote");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return "0";
    }

    public static void voteHudongGameVideo(String csrf, String cookie) throws IOException
    {
        post("https://api.bilibili.com/x/activity/likeact", "score=1&lid=15809171&sid=12085&csrf=" + csrf, cookie).body().string();
    }

    private static Object get(String url) throws IOException
    {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS).build();
        Request.Builder requestb = new Request.Builder().url(url);
        Request request = requestb.build();
        Response response = client.newCall(request).execute();
        if(response.isSuccessful()) return response.body().string();
        return null;
    }

    private static Response post(String url, String data, String cookie) throws IOException
    {
        Response response;
        OkHttpClient client;
        RequestBody body;
        Request request;
        client = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).build();
        body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded; charset=utf-8"), data);
        request = new Request.Builder().url(url).post(body).header("Accept", "*/*")
                .addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)")
                .addHeader("Referer", "https://passport.bilibili.com/login").addHeader("Cookie", cookie)
                .build();
        response = client.newCall(request).execute();
        if(response.isSuccessful())
        {
            return response;
        }
        return null;
    }
}
