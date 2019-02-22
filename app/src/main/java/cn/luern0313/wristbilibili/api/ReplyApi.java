package cn.luern0313.wristbilibili.api;

import android.graphics.BitmapFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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

    public ArrayList<reply> getReply(int page, String sort, int limit) throws IOException
    {
        try
        {
            replyJson = new JSONObject((String) get("https://api.bilibili.com/x/v2/reply?pn=" + page + "&type=" + type + "&oid=" + oid + "&sort=" + sort, 1)).getJSONObject("data");
            JSONArray replyJsonArray = replyJson.getJSONArray("replies");
            ArrayList<reply> replyArrayList = new ArrayList<>();
            for (int i = 0; i < (limit != 0 ? Math.min(limit, replyJsonArray.length()) : replyJsonArray.length()); i++)
                replyArrayList.add(new reply(replyJsonArray.getJSONObject(i)));
            return replyArrayList;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isShowFloor()
    {
        return replyJson.optJSONObject("config").optInt("showfloor") == 1;
    }

    public class reply
    {
        JSONObject replyJson;
        JSONObject replyUserJson;

        int mode;

        reply(JSONObject replyJson)
        {
            this.replyJson = replyJson;
            this.replyUserJson = replyJson.optJSONObject("member");
            this.mode = 0;
        }

        public reply(int mode)
        {
            this.mode = mode;
        }

        public int getMode()
        {
            return mode;
        }

        public String getUserMid()
        {
            return replyUserJson.optString("mid");
        }

        public String getUserHead()
        {
            return replyUserJson.optString("avatar");
        }

        public String getUserName()
        {
            return replyUserJson.optString("uname");
        }

        public int getUserVip()
        {
            return replyUserJson.optJSONObject("vip").optInt("vipType");
        }

        public String getReplyTime()
        {
            try
            {
                Date date = new Date(replyJson.optInt("ctime") * 1000L);
                SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
                return format.format(date);
            }
            catch (NullPointerException e)
            {
                e.printStackTrace();
                return "";
            }
        }

        public int getUserLv()
        {
            return replyUserJson.optJSONObject("level_info").optInt("current_level");
        }

        public String getReplyText()
        {
            return replyJson.optJSONObject("content").optString("message", "");
        }

        public String getReplyFloor(boolean showfloor)
        {
            return replyJson.has("floor") && showfloor ? ("#" + String.valueOf(replyJson.optInt("floor"))) : "";
        }

        public String getReplyBeLiked()
        {
            int l = replyJson.optInt("like", 0);
            if(l > 10000) return l / 1000 / 10.0 + "万";
            else return String.valueOf(l);
        }

        public String getReplyBeReply()
        {
            int r = replyJson.optInt("rcount", 0);
            if(r > 10000) return r / 1000 / 10.0 + "万";
            else return String.valueOf(r);
        }

        public boolean isReplyLike()
        {
            return replyJson.optInt("action") == 1;
        }

        public boolean isReplyDislike()
        {
            return replyJson.optInt("action") == 2;
        }
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
        request = new Request.Builder().url(url).post(body).header("Referer", "https://www.bilibili.com/").addHeader("Accept", "*/*").addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)").addHeader("Referer", "https://www.bilibili.com").addHeader("Cookie", cookie).build();
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
