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
 * Created by liupe on 2018/11/13.
 * 好像用不到了。。
 * 谁说的！！
 */

public class OthersUserApi
{
    private String cookie;
    private String mid;
    private String csrf;

    private int followPage = 0;
    private int fansPage = 0;
    public OthersUserApi(String cookie, String csrf, String mid)
    {
        this.cookie = cookie;
        this.mid = mid;
        this.csrf = csrf;
    }

    public String getOtheruserInfo() throws IOException
    {
        return (String) get("https://api.bilibili.com/x/web-interface/card?mid=" + mid + "&photo=1", 1);
    }

    public String getOtheruserVideo() throws IOException
    {
        return (String) get("https://space.bilibili.com/ajax/member/getSubmitVideos?mid=" + mid + "&pagesize=30&tid=0&page=1&keyword=&order=pubdate", 1);
    }

    public ArrayList<People> getUserFollow() throws IOException
    {
        try
        {
            followPage++;
            JSONArray peopleJsonArray = new JSONObject((String) get("https://api.bilibili.com/x/relation/followings?vmid=" + mid + "&pn=" + followPage + "&ps=20&order=desc", 1)).getJSONObject("data").getJSONArray("list");
            return getPeople(peopleJsonArray);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<People> getUserFans() throws IOException
    {
        try
        {
            fansPage++;
            JSONArray peopleJsonArray = new JSONObject((String) get("https://api.bilibili.com/x/relation/followers?vmid=" + mid + "&pn=" + fansPage + "&ps=20&order=desc", 1)).getJSONObject("data").getJSONArray("list");
            return getPeople(peopleJsonArray);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private ArrayList<People> getPeople(JSONArray peopleJsonArray)
    {
        try
        {
            ArrayList<People> peopleArrayList = new ArrayList<People>();
            for(int i = 0; i < peopleJsonArray.length(); i++)
            {
                JSONObject p = peopleJsonArray.optJSONObject(i);
                peopleArrayList.add(new People(String.valueOf(p.optInt("mid", 0)),
                        p.optString("uname", ""),
                        p.optString("face", ""),
                        p.optString("sign", ""),
                        getTime(p.optInt("mtime", 0)),
                        String.valueOf(p.optJSONObject("official_verify").optInt("type", 0)),
                        p.optJSONObject("official_verify").optString("desc", ""),
                        p.optJSONObject("vip").optInt("vipType", 0)));
            }
            return peopleArrayList;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public void follow() throws IOException
    {
        String followAPI = "https://api.bilibili.com/x/relation/modify";
        post(followAPI, "fid=" + mid + "&act=1&re_src=11&jsonp=jsonp&csrf=" + csrf);
    }

    public void unfollow() throws IOException
    {
        String followAPI = "https://api.bilibili.com/x/relation/modify";
        post(followAPI, "fid=" + mid + "&act=2&re_src=11&jsonp=jsonp&csrf=" + csrf);
    }

    public class People
    {
        public String uid;
        public String nmae;
        public String face;
        public String sign;
        public String mtime;
        public String verifyType;
        public String verifyName;
        public int vip;
        People(String uid, String n, String f, String s, String m, String vt, String vn, int vip)
        {
            this.uid = uid;
            this.nmae = n;
            this.face = f;
            this.sign = s;
            this.mtime = m;
            this.verifyType = vt;
            this.verifyName = vn;
            this.vip = vip;
        }
    }

    private String getTime(int timeStamp)
    {
        try
        {
            Date date = new Date(timeStamp * 1000L);
            SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm");
            return format.format(date);
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }
        return "";
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
