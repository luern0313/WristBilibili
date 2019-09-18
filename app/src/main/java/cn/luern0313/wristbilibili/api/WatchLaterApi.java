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
 * 被 luern0313 创建于 2019/8/31.
 * 仅用于<稍后再看>界面
 * 添加稍后再看在视频详情界面和视频详情api
 */

public class WatchLaterApi
{
    private String cookie;
    private String csrf;
    private String mid;

    public WatchLaterApi(String cookie, String csrf, String mid)
    {
        this.cookie = cookie;
        this.csrf = csrf;
        this.mid = mid;
    }

    public ArrayList<WatchLaterVideo> getWatchLater() throws IOException
    {
        try
        {
            ArrayList<WatchLaterVideo> videoArrayList = new ArrayList<>();
            JSONArray wlJsonArray = new JSONObject((String) get("https://api.bilibili.com/x/v2/history/toview/web", 1)).getJSONObject("data").getJSONArray("list");
            for(int i = 0; i < wlJsonArray.length(); i++)
            {
                JSONObject j = wlJsonArray.optJSONObject(i);
                videoArrayList.add(new WatchLaterVideo(String.valueOf(j.optInt("aid", 0)),
                        j.optString("title", ""),
                        j.optJSONObject("owner").optString("name", ""),
                        j.optString("pic", ""),
                        j.optInt("duration", 0),
                        j.optInt("progress", 0),
                        getView(j.optJSONObject("stat").optInt("view", 0)),
                        getView(j.optJSONObject("stat").optInt("danmaku", 0))));
            }
            return videoArrayList;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public class WatchLaterVideo
    {
        public String aid;
        public String title;
        public String up;
        public String cover;
        public int duration;
        public int progress;
        public String play;
        public String danmaku;

        WatchLaterVideo(String a, String t, String u, String c, int d, int p, String play, String dan)
        {
            this.aid = a;
            this.title = t;
            this.up = u;
            this.cover = c;
            this.duration = d;
            this.progress = p;
            this.play = play;
            this.danmaku = dan;
        }
    }

    private String getView(int view)
    {
        if(view > 10000) return view / 1000 / 10.0 + "万";
        else return String.valueOf(view);
    }

    private Object get(String url, int mode) throws IOException
    {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).build();
        Request.Builder requestb = new Request.Builder().url(url).header("Referer", "https://www.bilibili.com/watchlater/").addHeader("Accept", "*/*").addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        if(!cookie.equals("")) requestb.addHeader("Cookie", cookie);
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
        client = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).build();
        body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded; charset=utf-8"), data);
        request = new Request.Builder().url(url).post(body).header("Referer", "https://www.bilibili.com/watchlater/").addHeader("Accept", "*/*").addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)").addHeader("Cookie", cookie).build();
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
