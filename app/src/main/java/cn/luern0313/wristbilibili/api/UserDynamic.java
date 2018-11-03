package cn.luern0313.wristbilibili.api;

import android.graphics.Bitmap;
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
 * Created by liupe on 2018/10/30.
 */

public class UserDynamic
{
    private final String APIURL = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/dynamic_new";
    private final String DYNAMICTYPE = "268435455";
    private String mid;
    private String cookie;
    private JSONArray dynamicJsonArray;

    public UserDynamic(String cookie, String mid)
    {
        this.cookie = cookie;
        this.mid = mid;
    }

    public void getDynamic() throws IOException
    {
        try
        {
            dynamicJsonArray = new JSONObject((String) get(APIURL + "?uid=" + mid + "&type=" + DYNAMICTYPE, 1)).getJSONObject("data").getJSONArray("cards");
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
                dynamicList.add(getDynamicClass(new JSONObject((String) dy.get("card")), dy.getJSONObject("desc")));
            }
            return dynamicList;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private Object getDynamicClass(JSONObject cardJson, JSONObject descJson)
    {
        if(((int) getInfoFromJson(descJson, "type")) == 1)//转载
        {
            if((int) getInfoFromJson(descJson, "orig_type") == 2 || (int) getInfoFromJson(descJson, "orig_type") == 4)//文字
                return new cardShareText(cardJson, descJson);
            else if((int) getInfoFromJson(descJson, "orig_type") == 8)//视频
                return new cardShareVideo(cardJson, descJson);
            else return new cardUnknow(cardJson, descJson);
        }
        else if(((int) getInfoFromJson(descJson, "type")) == 2 || ((int) getInfoFromJson(descJson, "type")) == 4)//文字
            return new cardOriginalText(cardJson, descJson);
        else if(((int) getInfoFromJson(descJson, "type")) == 8)//视频
            return new cardOriginalVideo(cardJson, descJson);
        else return new cardUnknow(cardJson, descJson);
    }

    public class cardOriginalVideo
    {
        private JSONObject oriVideoJson;
        private JSONObject oriVideoDesc;
        private JSONObject oriVideoDescUser;

        cardOriginalVideo(JSONObject oriVideoJson, JSONObject oriVideoDesc)
        {
            this.oriVideoJson = oriVideoJson;
            this.oriVideoDesc = oriVideoDesc;
            this.oriVideoDescUser = getJsonFromJson(getJsonFromJson(oriVideoDesc, "user_profile"), "info");
        }

        public String getVideoAid()
        {
            return (String) getInfoFromJson(oriVideoJson, "aid");
        }

        public Bitmap getVideoImg() throws IOException
        {
            return (Bitmap) get((String) getInfoFromJson(oriVideoJson, "pic"), 2);
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
            return getMinFromSec((int) getInfoFromJson(getJsonFromJson(oriVideoJson, "stat"), "duration"));
        }

        public String getOwnerName()
        {
            return (String) getInfoFromJson(oriVideoDescUser, "uname");
        }

        public Bitmap getOwnerHead() throws IOException
        {
            return (Bitmap) get((String) getInfoFromJson(oriVideoDescUser, "face"), 2);
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
            return m + ":" + s;
        }
    }

    public class cardOriginalText
    {
        private JSONObject oriTextItemJson;
        private JSONObject oriTextDesc;
        private JSONObject oriTextDescUser;

        cardOriginalText(JSONObject oriTextJson, JSONObject oriTextDesc)
        {
            this.oriTextItemJson = getJsonFromJson(oriTextJson, "item");
            this.oriTextDesc = oriTextDesc;
            this.oriTextDescUser = getJsonFromJson(oriTextDesc, "user_profile");
        }

        public String getDynamicText()
        {
            return (String) getInfoFromJson(oriTextItemJson, "description");
        }

        public int getTextImgCount()
        {
            return (int) getInfoFromJson(oriTextItemJson, "pictures_count");
        }

        public ArrayList<String> getImgsSrc()
        {
            try
            {
                ArrayList<String> picssrc = new ArrayList<String>();
                JSONArray pics = oriTextItemJson.getJSONArray("pictures");
                for (int i = 0; i < pics.length(); i++)
                    picssrc.add((String) ((JSONObject) pics.get(i)).get("img_src"));
                return picssrc;
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            return null;
        }

        public String getUserName()
        {
            return (String) getInfoFromJson(oriTextDescUser, "uname");
        }

        public Bitmap getUserHead() throws IOException
        {
            return (Bitmap) get((String) getInfoFromJson(oriTextDescUser, "face"), 2);
        }

        public String getDynamicTime()
        {
            return getTime((int) getInfoFromJson(oriTextDesc, "timestamp"));
        }

        public int getBeLiked()
        {
            return (int) getInfoFromJson(oriTextDesc, "like");
        }
    }

    public class cardShareVideo
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

        public String getDynamicText()
        {
            return (String) getInfoFromJson(shareVideoItemJson, "content");
        }

        public String getUserName()
        {
            return (String) getInfoFromJson(shareVideoDescUser, "uname");
        }

        public Bitmap getUserHead() throws IOException
        {
            return (Bitmap) get((String) getInfoFromJson(shareVideoDescUser, "face"), 2);
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
    }

    public class cardShareText
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

        public String getDynamicText()
        {
            return (String) getInfoFromJson(shareTextItemJson, "content");
        }

        public String getUserName()
        {
            return (String) getInfoFromJson(shareTextDescUser, "uname");
        }

        public Bitmap getUserHead() throws IOException
        {
            return (Bitmap) get((String) getInfoFromJson(shareTextDescUser, "face"), 2);
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
    }

    public class cardUnknow
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

        public String getOwnerName()
        {
            return (String) getInfoFromJson(unknowDesc, "uname");
        }

        public Bitmap getOwnerHead() throws IOException
        {
            return (Bitmap) get((String) getInfoFromJson(unknowDescUser, "face"), 2);
        }

        public String getDynamicTime()
        {
            return getTime((int) getInfoFromJson(unknowDescUser, "timestamp"));
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
