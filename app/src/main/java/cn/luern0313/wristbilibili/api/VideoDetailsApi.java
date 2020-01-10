package cn.luern0313.wristbilibili.api;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
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
 * 不给title的api！！！
 * ...
 * ...
 * ...我错了，有这个api，我自罚重写
 */

public class VideoDetailsApi
{
    private String cookie;
    private String csrf;
    private String mid;
    private String aid;

    private JSONObject videoJSON;
    private JSONObject videoUserJson;
    private JSONObject videoViewJson;
    private JSONArray videoPartJson;
    private int isLiked;//0,1喜欢,2不喜欢
    private int isCoined;//已投个数
    private boolean isFaved;

    private int beLiked;//喜欢总数
    private int beCoined;//硬币总数
    private int beFaved;//收藏总数

    public VideoDetailsApi(String cookie, String csrf, String mid, String aid)
    {
        this.cookie = cookie;
        this.csrf = csrf;
        this.mid = mid;
        this.aid = aid;
    }

    public boolean getVideoDetails() throws IOException
    {
        try
        {
            videoJSON = new JSONObject((String) get("https://api.bilibili.com/x/web-interface/view/detail?aid=" + aid, 1));
            if(videoJSON.getInt("code") == -404) return false;
            videoUserJson = videoJSON.getJSONObject("data").getJSONObject("Card");
            videoViewJson = videoJSON.getJSONObject("data").getJSONObject("View");
            isLiked = new JSONObject((String) get("https://api.bilibili.com/x/web-interface/archive/has/like?aid=" + aid, 1)).getInt("data");
            isCoined = new JSONObject((String) get("https://api.bilibili.com/x/web-interface/archive/coins?aid=" + aid, 1)).getJSONObject("data").getInt("multiply");
            isFaved = new JSONObject((String) get("https://api.bilibili.com/x/v2/fav/video/favoured?aid=" + aid, 1)).getJSONObject("data").getBoolean("favoured");

            beLiked = (int) getInfoFromJson(getJsonFromJson(videoViewJson, "stat"), "like");
            beCoined = (int) getInfoFromJson(getJsonFromJson(videoViewJson, "stat"), "coin");
            beFaved = (int) getInfoFromJson(getJsonFromJson(videoViewJson, "stat"), "favorite");

            videoPartJson = new JSONObject((String) get("https://api.bilibili.com/x/player/pagelist?aid=" + aid, 1)).getJSONArray("data");
            return true;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public class VideoPart
    {
        private JSONObject videoPartJson;
        public VideoPart(JSONObject json)
        {
            videoPartJson = json;
        }

        public int getPartCid()
        {
            return videoPartJson.optInt("cid", 0);
        }

        public int getPartNum()
        {
            return videoPartJson.optInt("page", 0);
        }

        public String getPartName()
        {
            return videoPartJson.optString("part", "P" + getPartNum());
        }
    }

    public ArrayList<VideoPart> getVideoPartList()
    {
        ArrayList<VideoPart> vp = new ArrayList<>();
        for(int i = 0; i < videoPartJson.length(); i++)
            vp.add(new VideoPart(videoPartJson.optJSONObject(i)));
        return vp;
    }

    public int getVideoPartSize()
    {
        return videoPartJson.length();
    }

    public String getVideoTitle()
    {
        return (String) getInfoFromJson(videoViewJson, "title");
    }

    public int getVideoCopyright()
    {
        return (int) getInfoFromJson(videoViewJson, "copyright");
    }

    public String getVideoCid()
    {
        return String.valueOf(videoViewJson.optJSONArray("pages").optJSONObject(0).optInt("cid"));
    }

    private int getVideoDuration()
    {
        return videoViewJson.optInt("duration");
    }

    public String getVideoUpAid()
    {
        return (String) getInfoFromJson(getJsonFromJson(videoUserJson, "card"), "mid");
    }

    public String getVideoUpName()
    {
        return (String) getInfoFromJson(getJsonFromJson(videoUserJson, "card"), "name");
    }

    public String getVideoUpSign()
    {
        return (String) getInfoFromJson(getJsonFromJson(videoUserJson, "card"), "sign");
    }

    public Bitmap getVideoUpFace() throws IOException
    {
        return (Bitmap) get((String) getInfoFromJson(getJsonFromJson(videoUserJson, "card"), "face"), 2);
    }

    public String getVideoFace()
    {
        return (String) getInfoFromJson(videoViewJson, "pic");
    }

    public boolean isFollowing()
    {
        return (boolean) getInfoFromJson(videoUserJson, "following");
    }

    public String getVideoPlay()
    {
        int view = (int) getInfoFromJson(getJsonFromJson(videoViewJson, "stat"), "view");
        if(view > 10000) return view / 1000 / 10.0 + "万";
        else return String.valueOf(view);
    }

    public String getVideoDanmaku()
    {
        return String.valueOf(getInfoFromJson(getJsonFromJson(videoViewJson, "stat"), "danmaku"));
    }

    public String getVideoupTime()
    {
        Date date = new Date((int) getInfoFromJson(videoViewJson, "pubdate") * 1000L);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return format.format(date);
    }

    public String getVideoAid()
    {
        return aid;
    }

    public String getVideoDetail()
    {
        return (String) getInfoFromJson(videoViewJson, "desc");
    }

    public String getVideoLike()
    {
        if(beLiked > 10000) return beLiked / 1000 / 10.0 + "万";
        else return String.valueOf(beLiked);
    }

    public String getVideoCoin()
    {
        if(beCoined > 10000) return beCoined / 1000 / 10.0 + "万";
        else return String.valueOf(beCoined);
    }

    public String getVideoFav()
    {
        if(beFaved > 10000) return beFaved / 1000 / 10.0 + "万";
        else return String.valueOf(beFaved);
    }

    public int getSelfLiked()
    {
        return isLiked;
    }

    public int getSelfCoined()
    {
        return isCoined;
    }

    public boolean getSelfFaved()
    {
        return isFaved;
    }

    public ArrayList<ListofVideoApi> getRecommendVideos() throws IOException
    {
        try
        {
            ArrayList<ListofVideoApi> videoArrayList = new ArrayList<>();
            JSONArray videoJson = new JSONObject((String) get("https://comment.bilibili.com/recommendnew," +  aid, 1)).getJSONArray("data");
            for(int i = 0; i < videoJson.length(); i++)
                videoArrayList.add(new ListofVideoApi(videoJson.getJSONObject(i)));
            return videoArrayList;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public void setSelfLiked(int w)
    {
        this.beLiked += w;
    }

    public void setSelfCoined(int w)
    {
        this.beCoined += w;
    }

    public void setSelfFaved(int w)
    {
        this.beFaved += w;
    }

    public boolean likeVideo(int mode) throws IOException  //1好评，2取消差评，3差评，4取消差评，后一个会覆盖前一个
    {
        try
        {
            if(post("https://api.bilibili.com/x/web-interface/archive/like", "aid=" + aid + "&like=" + mode + "&csrf=" + csrf).body().string().equals("{\"code\":0,\"message\":\"0\",\"ttl\":1}"))
                return true;
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public boolean coinVideo(int how) throws IOException
    {
        try
        {
            if(post("https://api.bilibili.com/x/web-interface/coin/add", "aid=" + aid + "&multiply=" + how + "&cross_domain=true&csrf=" + csrf).body().string().equals("{\"code\":0,\"message\":\"0\",\"ttl\":1}"))
                return true;
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public String favVideo(String favId) throws IOException
    {
        try
        {
            return new JSONObject(post("https://api.bilibili.com/medialist/gateway/coll/resource/deal", "rid=" + aid + "&type=2&add_media_ids=" + favId + "&del_media_ids=&csrf=" + csrf).body().string()).optString("message");
        }
        catch (Exception e)
        {
            return e.getMessage();
        }
    }

    public boolean favCancalVideo() throws IOException
    {
        try
        {
            if(post("https://api.bilibili.com/x/v2/fav/video/del", "aid=" + aid + "&csrf=" + csrf).body().string().equals("{\"code\":0,\"message\":\"0\",\"ttl\":1}"))
                return true;
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public boolean playLater() throws IOException
    {
        try
        {
            if(post("https://api.bilibili.com/x/v2/history/toview/add", "aid=" + aid + "&csrf=" + csrf).body().string().equals("{\"code\":0,\"message\":\"0\",\"ttl\":1}"))
                return true;
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public boolean playHistory() throws IOException
    {
        try
        {
            if(post("https://api.bilibili.com/x/report/web/heartbeat", "aid=" + aid + "&cid=" + getVideoCid() + "&mid=" + mid + "&csrf=" + csrf + "&played_time=0&realtime=0&start_ts=" + (System.currentTimeMillis() / 1000) + "&type=3&dt=2&play_type=1").body().string().equals("{\"code\":0,\"message\":\"0\",\"ttl\":1}"))
                return true;
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public boolean shareVideo(String text) throws IOException
    {
        try
        {
            String result = post("https://api.vc.bilibili.com/dynamic_repost/v1/dynamic_repost/share", "csrf_token=" + csrf + "&platform=pc&uid=" + getVideoUpAid() + "&type=8&share_uid=" + mid + "&content=" + URLEncoder.encode(text, "UTF-8") + "&repost_code=20000&rid=" + getVideoAid()).body().string();
            if(new JSONObject(result).getInt("code") == 0)
                return true;
        }
        catch (NullPointerException | JSONException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public boolean scoreVideo(int score) throws IOException
    {
        try
        {
            String result = post("https://api.bilibili.com/x/stein/mark", "aid=" + aid + "&mark=" + score + "&csrf=" + csrf).body().string();
            if(new JSONObject(result).getInt("code") == 0)
                return true;
        }
        catch (NullPointerException | JSONException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public boolean sendReply(String text) throws IOException
    {
        try
        {
            return new JSONObject(post("https://api.bilibili.com/x/v2/reply/add", "oid=" + aid + "&type=1&message=" + text + "&plat=1&jsonp=jsonp&csrf=" + csrf).body().string()).optInt("code") == 0;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return false;
        }
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
