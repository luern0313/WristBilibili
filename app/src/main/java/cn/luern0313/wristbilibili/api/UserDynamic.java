package cn.luern0313.wristbilibili.api;

import android.graphics.Bitmap;
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
 * Created by luern0313 on 2018/10/30.
 * 动态的api
 * 写完这个文件我才知道为什么程序员被叫成代码民工。。
 * 这绝对就只是个力气活啊。。
 * 不过好在不用动脑子（滑稽）
 * 辛苦b站程序们，动态有至少十几种
 * 我只做了五种23333
 */

public class UserDynamic
{
    private final String DYNAMICAPIURL = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/dynamic_new";
    private final String HISTORYAPIURL = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/dynamic_history";
    private final String DYNAMICTYPE = "268435455";
    private String mid;
    private String csrf;
    private String cookie;
    private JSONArray dynamicJsonArray;

    private String lastDynamicId;

    public UserDynamic(String cookie, String csrf, String mid)
    {
        this.cookie = cookie;
        this.csrf = csrf;
        this.mid = mid;
    }

    public void getDynamic() throws IOException
    {
        try
        {
            dynamicJsonArray = new JSONObject((String) get(DYNAMICAPIURL + "?uid=" + mid + "&type=" + DYNAMICTYPE, 1)).getJSONObject("data").getJSONArray("cards");
            if(dynamicJsonArray.length() == 0) dynamicJsonArray = null;
        }
        catch (JSONException e)
        {
            dynamicJsonArray = null;
            e.printStackTrace();
        }
    }

    public void getHistoryDynamic() throws IOException
    {
        try
        {
            dynamicJsonArray = new JSONObject((String) get(HISTORYAPIURL + "?uid=" + mid + "&offset_dynamic_id=" + lastDynamicId + "&type=" + DYNAMICTYPE, 1)).getJSONObject("data").getJSONArray("cards");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public ArrayList<Object> getDynamicList()
    {
        try
        {
            ArrayList<Object> dynamicList = new ArrayList<Object>();
            for (int i = 0; i < dynamicJsonArray.length(); i++)
            {
                JSONObject dy = (JSONObject) dynamicJsonArray.get(i);
                Object d = getDynamicClass(new JSONObject((String) dy.get("card")), dy.getJSONObject("desc"));
                if(d != null) dynamicList.add(d);
                if(i == dynamicJsonArray.length() - 1)
                    lastDynamicId = String.valueOf(dy.getJSONObject("desc").get("dynamic_id"));
            }
            return dynamicList;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public Object getDynamicClass(String cardJson, int which)
    {
        try
        {
            if(which == 1)  //video
                return new cardOriginalVideo(new JSONObject(cardJson));
            else  //text
                return new cardOriginalText(new JSONObject(cardJson));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private Object getDynamicClass(JSONObject cardJson, JSONObject descJson)
    {
        if(((int) getInfoFromJson(descJson, "type")) == 1)  //转载
        {
            if(((int) getInfoFromJson(descJson, "orig_type")) == 2 || ((int) getInfoFromJson(descJson, "orig_type")) == 4)  //文字
                return new cardShareText(cardJson, descJson);
            else if(((int) getInfoFromJson(descJson, "orig_type")) == 8)  //视频
                return new cardShareVideo(cardJson, descJson);
            else return new cardUnknow(cardJson, descJson);
        }
        else if(((int) getInfoFromJson(descJson, "type")) == 2 || ((int) getInfoFromJson(descJson, "type")) == 4)  //文字
            return new cardOriginalText(cardJson, descJson);
        else if(((int) getInfoFromJson(descJson, "type")) == 8)  //视频
            return new cardOriginalVideo(cardJson, descJson);
        else if(((int) getInfoFromJson(descJson, "type")) == 512 && ((int) getInfoFromJson(descJson, "orig_type")) == 0)//番剧，暂时不处理
            return null;
        else return new cardUnknow(cardJson, descJson);
    }

    public boolean sendReply(String oid, String type, String text) throws IOException
    {
        Log.i("bilibili", String.valueOf(oid));
        Log.i("bilibili", String.valueOf(type));
        Log.i("bilibili", String.valueOf(text));
        try
        {
            return new JSONObject(post("https://api.bilibili.com/x/v2/reply/add", "oid=" + oid + "&type=" + type + "&message=" + text + "&plat=1&jsonp=jsonp&csrf=" + csrf).body().string()).getInt("code") == 0;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public class cardOriginalVideo //原创视频
    {
        private int mode;
        private JSONObject oriVideoJson;
        private JSONObject oriVideoDesc;
        private JSONObject oriVideoDescUser;

        cardOriginalVideo(JSONObject oriVideoJson, JSONObject oriVideoDesc)
        {
            mode = 1;
            this.oriVideoJson = oriVideoJson;
            this.oriVideoDesc = oriVideoDesc;
            this.oriVideoDescUser = getJsonFromJson(oriVideoDesc, "user_profile");
        }

        public cardOriginalVideo(JSONObject oriVideoJson)
        {
            mode = 2;
            this.oriVideoJson = oriVideoJson;
        }

        public String getVideoAid()
        {
            return String.valueOf(getInfoFromJson(oriVideoJson, "aid"));
        }

        public String getVideoTitle()
        {
            return (String) getInfoFromJson(oriVideoJson, "title");
        }

        public String getVideoImg()
        {
            return (String) getInfoFromJson(oriVideoJson, "pic");
        }

        public String getDynamic()
        {
            return (String) getInfoFromJson(oriVideoJson, "dynamic");
        }

        public String getVideoView()
        {
            int view = (int) getInfoFromJson(getJsonFromJson(oriVideoJson, "stat"), "view");
            if(view > 10000) return view / 1000 / 10.0 + "万";
            else return String.valueOf(view);
        }

        public String getVideoDuration()
        {
            return getMinFromSec((int) getInfoFromJson(oriVideoJson, "duration"));
        }

        public String getOwnerUid()
        {
            if(mode == 1)
                return String.valueOf(getInfoFromJson(getJsonFromJson(oriVideoDescUser, "info"), "uid"));
            else
                return String.valueOf(getInfoFromJson(getJsonFromJson(oriVideoJson, "owner"), "mid"));
        }

        public String getOwnerName()
        {
            if(mode == 1)
                return (String) getInfoFromJson(getJsonFromJson(oriVideoDescUser, "info"), "uname");
            else return (String) getInfoFromJson(getJsonFromJson(oriVideoJson, "owner"), "name");
        }

        public String getOwnerHead()
        {
            if(mode == 1)
                return (String) getInfoFromJson(getJsonFromJson(oriVideoDescUser, "info"), "face");
            else return (String) getInfoFromJson(getJsonFromJson(oriVideoJson, "owner"), "face");
        }

        public String getDynamicTime()
        {
            return getTime((int) getInfoFromJson(oriVideoDesc, "timestamp"));
        }

        public int getBeLiked()
        {
            return (int) getInfoFromJson(oriVideoDesc, "like");
        }

        private String getMinFromSec(int sec)
        {
            String m = String.valueOf(sec / 60);
            String s = String.valueOf(sec - sec / 60 * 60);
            if(m.length() == 1) m = "0" + m;
            if(s.length() == 1) s = "0" + s;
            return m + ":" + s;
        }
    }

    public class cardOriginalText //原创文字/图片
    {
        private int mode;
        private JSONObject oriTextUserJson;
        private JSONObject oriTextItemJson;
        private JSONObject oriTextDesc;
        private JSONObject oriTextDescUser;

        cardOriginalText(JSONObject oriTextJson, JSONObject oriTextDesc)
        {
            mode = 1;
            this.oriTextItemJson = getJsonFromJson(oriTextJson, "item");
            this.oriTextDesc = oriTextDesc;
            this.oriTextDescUser = getJsonFromJson(oriTextDesc, "user_profile");
        }

        cardOriginalText(JSONObject oriTextJson)
        {
            mode = 2;
            this.oriTextUserJson = getJsonFromJson(oriTextJson, "user");
            this.oriTextItemJson = getJsonFromJson(oriTextJson, "item");
        }

        public String getDynamicId()
        {
            if(getTextImgCount().equals("0"))
                return oriTextDesc.optString("dynamic_id_str");
            else
                return String.valueOf(oriTextItemJson.optInt("id"));
        }

        public String getDynamicText()
        {
            if(getTextImgCount().equals("0"))
                return (String) getInfoFromJson(oriTextItemJson, "content");
            else return (String) getInfoFromJson(oriTextItemJson, "description");
        }

        public String getTextImgCount()
        {
            if(oriTextItemJson.has("pictures_count"))
                return String.valueOf(getInfoFromJson(oriTextItemJson, "pictures_count"));
            else return "0";
        }

        public String[] getImgsSrc()
        {
            try
            {
                ArrayList<String> picssrc = new ArrayList<>();
                JSONArray pics = oriTextItemJson.getJSONArray("pictures");
                for (int i = 0; i < pics.length(); i++)
                    picssrc.add((String) ((JSONObject) pics.get(i)).get("img_src"));
                return picssrc.toArray(new String[]{});
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            return null;
        }

        public String getUserUid()
        {
            if(mode == 1)
                return String.valueOf(getInfoFromJson(getJsonFromJson(oriTextDescUser, "info"), "uid"));
            else
            {
                return String.valueOf(getInfoFromJson(oriTextUserJson, "uid"));
            }
        }

        public String getUserName()
        {
            if(mode == 1)
                return (String) getInfoFromJson(getJsonFromJson(oriTextDescUser, "info"), "uname");
            else
            {
                if(getTextImgCount().equals("0"))
                    return (String) getInfoFromJson(oriTextUserJson, "uname");
                else return (String) getInfoFromJson(oriTextUserJson, "name");
            }
        }

        public String getUserHead()
        {
            if(mode == 1)
                return (String) getInfoFromJson(getJsonFromJson(oriTextDescUser, "info"), "face");
            else
            {
                if(getTextImgCount().equals("0"))
                    return (String) getInfoFromJson(oriTextUserJson, "face");
                else return (String) getInfoFromJson(oriTextUserJson, "head_url");
            }
        }

        public String getDynamicTime()
        {
            return getTime((int) getInfoFromJson(oriTextDesc, "timestamp"));
        }

        public int getBeLiked()
        {
            return (int) getInfoFromJson(oriTextDesc, "like");
        }

        public int getBeReply()
        {
            return oriTextItemJson.optInt("reply");
        }

        public String getReplyType()
        {
            if(getTextImgCount().equals("0")) return "17";
            else return "11";
        }
    }

    public class cardShareVideo //转发视频
    {
        private JSONObject shareVideoJson;
        private JSONObject shareVideoItemJson;
        private JSONObject shareVideoDesc;
        private JSONObject shareVideoDescUser;

        cardShareVideo(JSONObject shareVideoJson, JSONObject shareVideoDesc)
        {
            this.shareVideoJson = shareVideoJson;
            this.shareVideoItemJson = getJsonFromJson(shareVideoJson, "item");
            this.shareVideoDesc = shareVideoDesc;
            this.shareVideoDescUser = getJsonFromJson(shareVideoDesc, "user_profile");
        }

        public String getDynamicId()
        {
            return shareVideoDesc.optString("dynamic_id_str");
        }

        public String getDynamicText()
        {
            return (String) getInfoFromJson(shareVideoItemJson, "content");
        }

        public String getUserUid()
        {
            return String.valueOf(getInfoFromJson(getJsonFromJson(shareVideoDescUser, "info"), "uid"));
        }

        public String getUserName()
        {
            return (String) getInfoFromJson(getJsonFromJson(shareVideoDescUser, "info"), "uname");
        }

        public String getUserHead()
        {
            return (String) getInfoFromJson(getJsonFromJson(shareVideoDescUser, "info"), "face");
        }

        public String getDynamicTime()
        {
            return getTime((int) getInfoFromJson(shareVideoDesc, "timestamp"));
        }

        public String getOriginalVideo()
        {
            return (String) getInfoFromJson(shareVideoJson, "origin");
        }

        public int getBeLiked()
        {
            return (int) getInfoFromJson(shareVideoDesc, "like");
        }

        public int getBeReply()
        {
            return shareVideoItemJson.optInt("reply");
        }

        public String getReplyType()
        {
            return "17";
        }
    }

    public class cardShareText //转发文字/图片
    {
        private JSONObject shareTextJson;
        private JSONObject shareTextItemJson;
        private JSONObject shareTextDesc;
        private JSONObject shareTextDescUser;

        cardShareText(JSONObject shareTextJson, JSONObject shareTextDesc)
        {
            this.shareTextJson = shareTextJson;
            this.shareTextItemJson = getJsonFromJson(shareTextJson, "item");
            this.shareTextDesc = shareTextDesc;
            this.shareTextDescUser = getJsonFromJson(shareTextDesc, "user_profile");
        }

        public String getDynamicId()
        {
            return shareTextDesc.optString("dynamic_id_str");
        }

        public String getDynamicText()
        {
            return (String) getInfoFromJson(shareTextItemJson, "content");
        }

        public String getUserUid()
        {
            return String.valueOf(getInfoFromJson(getJsonFromJson(shareTextDescUser, "info"), "uid"));
        }

        public String getUserName()
        {
            return (String) getInfoFromJson(getJsonFromJson(shareTextDescUser, "info"), "uname");
        }

        public String getUserHead()
        {
            return (String) getInfoFromJson(getJsonFromJson(shareTextDescUser, "info"), "face");
        }

        public String getDynamicTime()
        {
            return getTime((int) getInfoFromJson(shareTextDesc, "timestamp"));
        }

        public String getOriginalText()
        {
            return (String) getInfoFromJson(shareTextJson, "origin");
        }

        public int getBeLiked()
        {
            return (int) getInfoFromJson(shareTextDesc, "like");
        }

        public int getBeReply()
        {
            return shareTextItemJson.optInt("reply");
        }

        public String getReplyType()
        {
            return "17";
        }
    }

    public class cardUnknow //不支持的动态类型
    {
        private JSONObject unknowJson;
        private JSONObject unknowDesc;
        private JSONObject unknowDescUser;

        cardUnknow(JSONObject unknowJson, JSONObject unknowDesc)
        {
            this.unknowJson = unknowJson;
            this.unknowDesc = unknowDesc;
            this.unknowDescUser = getJsonFromJson(unknowDesc, "user_profile");
        }

        public String getOwnerUid()
        {
            try
            {
                return String.valueOf(unknowDescUser.getJSONObject("info").get("uid"));
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            return "0";
        }

        public String getOwnerName()
        {
            try
            {
                return (String) unknowDescUser.getJSONObject("info").get("uname");
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            return "未知UP";
        }

        public String getOwnerHead()
        {
            return (String) getInfoFromJson(getJsonFromJson(unknowDescUser, "info"), "face");
        }

        public String getDynamicTime()
        {
            try
            {
                return getTime((int) unknowDesc.get("timestamp"));
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            return "未知时间";
        }
    }

    private String getTime(int timeStamp)
    {
        try
        {
            Date date = new Date(timeStamp * 1000L);
            SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
            return format.format(date);
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }
        return "";
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
