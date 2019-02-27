package cn.luern0313.wristbilibili.api;

import android.graphics.BitmapFactory;
import android.util.Log;

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

    public ArrayList<reply> getReply(int page, String sort, int limit, String root) throws IOException
    {
        try
        {
            Log.i("bilibili", "https://api.bilibili.com/x/v2/reply?pn=" + page + "&type=" + type + "&oid=" + oid + "&sort=" + sort + (root.equals("") ? "" : ("&root=" + root)));
            replyJson = new JSONObject((String) get("https://api.bilibili.com/x/v2" + (root.equals("") ? "" : "/reply") + "/reply?pn=" + page + "&type=" + type + "&oid=" + oid + "&sort=" + sort + (root.equals("") ? "" : ("&root=" + root)), 1)).getJSONObject("data");
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

    public class reply
    {
        private JSONObject replyJson;
        private JSONObject replyUserJson;

        private boolean isLike;
        private boolean isHate;
        private int likeCount;
        private int replyCount;

        private int mode;

        reply(JSONObject replyJson)
        {
            this.replyJson = replyJson;
            this.replyUserJson = replyJson.optJSONObject("member");
            this.isLike = replyJson.optInt("action") == 1;
            this.isHate = replyJson.optInt("action") == 2;
            this.likeCount = replyJson.optInt("like", 0);
            this.replyCount = replyJson.optInt("rcount", 0);
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

        public String getReplyId()
        {
            return replyJson.optString("rpid_str");
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
            if(likeCount > 10000) return likeCount / 1000 / 10.0 + "万";
            else return String.valueOf(likeCount);
        }

        public String getReplyBeReply()
        {
            if(replyCount > 10000) return replyCount / 1000 / 10.0 + "万";
            else return String.valueOf(replyCount);
        }

        public boolean isReplyLike()
        {
            return isLike;
        }

        public boolean isReplyDislike()
        {
            return isHate;
        }

        public String likeReply(String rpid, int action, String type)
        {
            try
            {
                JSONObject j = new JSONObject(post("https://api.bilibili.com/x/v2/reply/action", "oid=" + oid + "&type=" + type + "&rpid=" + rpid + "&action=" + action + "&jsonp=jsonp&csrf=" + csrf).body().string());
                if(j.getInt("code") == 0)
                {
                    isLike = action == 1;
                    likeCount += action * 2 - 1;
                    isHate = !isLike;
                    return "";
                }
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

        public String hateReply(String rpid, int action, String type)
        {
            try
            {
                JSONObject j = new JSONObject(post("https://api.bilibili.com/x/v2/reply/hate", "oid=" + oid + "&type=" + type + "&rpid=" + rpid + "&action=" + action + "&jsonp=jsonp&csrf=" + csrf).body().string());
                if(j.getInt("code") == 0)
                {
                    isHate = action == 1;
                    isLike = !isHate;
                    return "";
                }
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
