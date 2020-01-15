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

import cn.luern0313.wristbilibili.models.ReplyModel;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 被 luern0313 创建于 2019/2/20.
 * 好麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦
 * 麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦啊
 */

public class ReplyApi
{
    private String cookie;
    private String csrf;
    private String oid;
    private String type;
    private JSONObject replyJson;

    public ReplyApi(String cookie, String csrf, String oid, String type)
    {
        this.cookie = cookie;
        this.csrf = csrf;
        this.oid = oid;
        this.type = type;
    }

    public ArrayList<ReplyModel> getReply(int page, String sort, int limit, String root) throws IOException
    {
        try
        {
            replyJson = new JSONObject((String) get("https://api.bilibili.com/x/v2" + (root.equals("") ? "" : "/reply") + "/reply?pn=" + page + "&type=" + type + "&oid=" + oid + "&sort=" + sort + (root.equals("") ? "" : ("&root=" + root)), 1)).getJSONObject("data");
            JSONArray replyJsonArray = replyJson.getJSONArray("replies");
            ArrayList<ReplyModel> replyArrayList = new ArrayList<>();
            for (int i = 0; i < (limit != 0 ? Math.min(limit, replyJsonArray.length()) : replyJsonArray.length()); i++)
                replyArrayList.add(new ReplyModel(cookie, csrf, replyJsonArray.getJSONObject(i), oid));
            return replyArrayList;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public String sendReply(String rpid, String text)
    {
        try
        {
            JSONObject j = new JSONObject(post("https://api.bilibili.com/x/v2/reply/add", "oid=" + oid + "&type=" + type + (rpid.equals("") ? "" : ("&root=" + rpid + "&parent=" + rpid)) + "&message=" + text + "&jsonp=jsonp&csrf=" + csrf).body().string());
            if(j.getInt("code") == 0)
                return "";
            else
                return j.getString("message");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return "未知问题，请重试？";
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return "网络错误！";
        }
    }

    public boolean isShowFloor()
    {
        return replyJson.optJSONObject("config").optInt("showfloor") == 1;
    }

    private Object get(String url, int mode) throws IOException
    {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).build();
        Request.Builder requestb = new Request.Builder().url(url).header("Referer", "https://www.bilibili.com/anime/timeline").addHeader("Accept", "*/*").addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
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
        request = new Request.Builder().url(url).post(body).header("Referer", "https://www.bilibili.com/").addHeader("Accept", "*/*").addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)").addHeader("Cookie", cookie).build();
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
