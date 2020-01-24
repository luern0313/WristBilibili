package cn.luern0313.wristbilibili.api;

import android.graphics.BitmapFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 被 luern0313 创建于 2019/2/28.
 * 播放视频相关
 */

public class OnlineVideoApi
{
    private String cookie;
    private String csrf;
    private String mid;
    private String aid;
    private String part;
    private String cid;

    private JSONObject playUrlJson;
    private String playUrl;

    public OnlineVideoApi(String cookie, String csrf, String mid, String aid, String part, String cid)
    {
        this.cookie = cookie;
        this.csrf = csrf;
        this.mid = mid;
        this.aid = aid;
        this.part = part;
        this.cid = cid;
    }

    public void connectionVideoUrl() throws IOException
    {
        try
        {
            playUrlJson = new JSONObject((String) get("https://api.bilibili.com/x/player/playurl?avid=" + aid + "&cid=" + cid + "&qn=16&type=mp4", 1));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public String getVideoUrl()
    {
        try
        {
            playUrl = playUrlJson.getJSONObject("data").getJSONArray("durl").getJSONObject(0).getString("url");
            return playUrl;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return "";
        }
    }

    public String[] getVideoBackupUrl()
    {
        try
        {
            JSONArray backUpUrl = playUrlJson.getJSONObject("data").getJSONArray("durl").getJSONObject(0).getJSONArray("backup_url");
            ArrayList<String> backUpUrlList = new ArrayList<>();
            for(int i = 0; i < backUpUrl.length(); i++)
                backUpUrlList.add(backUpUrl.optString(i));
            return backUpUrlList.toArray(new String[]{});
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return new String[]{};
        }
    }

    public String getDanmakuUrl()
    {
        return "https://comment.bilibili.com/" + cid + ".xml";
    }

    public boolean playHistory(int playTime, boolean isFin) throws IOException
    {
        try
        {
            if(!isFin) if(post("https://api.bilibili.com/x/report/web/heartbeat",
                               "aid=" + aid + "&cid=" + cid + "&mid=" + mid + "&csrf=" + csrf + "&played_time=" + playTime + "&realtime=" + playTime + "&start_ts=" + (((int) System
                                       .currentTimeMillis() / 1000) - playTime) + "&type=3&dt=2&play_type=1")
                    .body().string().equals("{\"code\":0,\"message\":\"0\",\"ttl\":1}"))
                return true;
            else if(post("https://api.bilibili.com/x/report/web/heartbeat",
                         "aid=" + aid + "&cid=" + cid + "&mid=" + mid + "&csrf=" + csrf + "&played_time=-1&realtime=" + playTime + "&start_ts=" + (((int) System
                                 .currentTimeMillis() / 1000) - playTime) + "&type=3&dt=2&play_type=4")
                    .body().string().equals("{\"code\":0,\"message\":\"0\",\"ttl\":1}"))
                return true;
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
            return false;
        }
        return false;
    }


    private Object get(String url, int mode) throws IOException
    {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS).build();
        Request.Builder requestb = new Request.Builder().url(url).addHeader("User-Agent",
                                                                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.119 Safari/537.36");
        if(!cookie.equals("")) requestb.addHeader("Cookie",
                                                  "buvid3=FE09F518-E432-414C-AF62-4493C27AD0366147infoc; " + cookie);
        Request request = requestb.build();
        Response response = client.newCall(request).execute();

        if(response.isSuccessful())
        {
            if(mode == 1) return response.body().string();
            else if(mode == 2)
            {
                byte[] buffer = readStream(response.body().byteStream());
                return BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
            }
        }
        return null;
    }

    private Object getSize(String url, int mode) throws IOException
    {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS).build();
        Request.Builder requestb = new Request.Builder().url(url).addHeader("User-Agent",
                                                                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.119 Safari/537.36");
        if(!cookie.equals("")) requestb.addHeader("Cookie",
                                                  "buvid3=FE09F518-E432-414C-AF62-4493C27AD0366147infoc" + cookie);
        Request request = requestb.build();
        Response response = client.newCall(request).execute();

        if(response.isSuccessful())
        {
            if(mode == 1) return response.body().string();
            else if(mode == 2)
            {
                byte[] buffer = readStream(response.body().byteStream());
                return BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
            }
        }
        return null;
    }

    private Response post(String url, String data) throws IOException
    {
        Response response;
        OkHttpClient client;
        RequestBody body;
        Request request;
        client = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).readTimeout(15,
                                                                                             TimeUnit.SECONDS)
                .build();
        body = RequestBody.create(
                MediaType.parse("application/x-www-form-urlencoded; charset=utf-8"), data);
        request = new Request.Builder().url(url).post(body).header("Referer",
                                                                   "https://www.bilibili.com/")
                .addHeader("Accept", "*/*").addHeader("User-Agent",
                                                      "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)")
                .addHeader("Referer", "https://passport.bilibili.com/login").addHeader("Cookie",
                                                                                       cookie)
                .build();
        response = client.newCall(request).execute();
        if(response.isSuccessful())
        {
            return response;
        }
        return null;
    }

    private byte[] readStream(InputStream inStream) throws IOException
    {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1)
        {
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        inStream.close();
        return outStream.toByteArray();
    }
}
