package cn.luern0313.wristbilibili.api;

import android.graphics.BitmapFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by liupe on 2018/11/13.
 * 不给title的api！！！
 */

public class VideoDetails
{
    private String cookie;
    private String aid;

    private Elements videoWebHead;
    private JSONObject videoJSON;

    public VideoDetails(String cookie, String aid)
    {
        this.cookie = cookie;
        this.aid = aid;
    }

    public void getVideoDetails() throws IOException
    {
        try
        {
            videoWebHead = Jsoup.parse((String) get("https://www.bilibili.com/video/av" + aid + "/?redirectFrom=h5", 1)).head().getElementsByTag("meta");
            videoJSON = new JSONObject((String) get("https://api.bilibili.com/x/web-interface/archive/stat?aid=" + aid, 1)).getJSONObject("data");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public String getVideoTitle()
    {
        try
        {
            return videoWebHead.select("meta[itemprop=keywords]").attr("content").split(",")[0];
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "获取视频标题错误...";
        }
    }

    public String getVideoUP()
    {
        try
        {
            return videoWebHead.select("meta[itemprop=author]").attr("content");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "获取UP错误...";
        }
    }

    public String getVideoPlay()
    {
        int view = (int) getInfoFromJson(videoJSON, "view");
        if(view > 10000) return view / 1000 / 10.0 + "万";
        else return String.valueOf(view);
    }

    public String getVideoDanmaku()
    {
        return String.valueOf(getInfoFromJson(videoJSON, "danmaku"));
    }

    public String getVideoupTime()
    {
        try
        {
            return videoWebHead.select("meta[itemprop=datePublished]").attr("content");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "获取时间错误...";
        }
    }

    public String getVideoAid()
    {
        return aid;
    }

    public String getVideoDetail()
    {
        try
        {
            return videoWebHead.select("meta[itemprop=description]").attr("content");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "获取介绍错误...";
        }
    }

    public String getVideoLike()
    {
        return String.valueOf(getInfoFromJson(videoJSON, "like"));
    }

    public String getVideoCoin()
    {
        return String.valueOf(getInfoFromJson(videoJSON, "coin"));
    }

    public String getVideoFav()
    {
        return String.valueOf(getInfoFromJson(videoJSON, "favorite"));
    }

    private Object getInfoFromJson(JSONObject json, String get)
    {
        try
        {
            return json.get(get);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private JSONObject getJsonFromJson(JSONObject json, String get)
    {
        try
        {
            return json.getJSONObject(get);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private Object get(String url, int mode) throws IOException
    {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).build();
        Request.Builder requestb = new Request.Builder().url(url).header("Referer", "https://www.bilibili.com/").addHeader("Accept", "*/*").addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
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
        request = new Request.Builder().url(url).post(body).header("Referer", "https://www.bilibili.com/").addHeader("Accept", "*/*").addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)").addHeader("Referer", "https://passport.bilibili.com/login").addHeader("Cookie", cookie).build();
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
