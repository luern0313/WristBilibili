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
    private final String DYNAMICTYPE = "268435455";
    private String mid;
    private String cookie;
    private JSONObject dynamicJson;

    public UserDynamic(String cookie, String mid)
    {
        this.cookie = cookie;
        this.mid = mid;
    }

    public void getDynamic() throws IOException
    {
        try
        {
            dynamicJson = new JSONObject((String) get("https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/dynamic_new?uid=" + mid + "&type=" + DYNAMICTYPE, 1));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public Object getDynamicClass(JSONObject json)
    {
        return null;
    }

    public class cardOriginalVideo
    {
        private JSONObject oriVideoJson;

        public cardOriginalVideo(JSONObject oriVideoJson)
        {
            this.oriVideoJson = oriVideoJson;
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
            return (String) getInfoFromJson(getJsonFromJson(oriVideoJson, "owner"), "name");
        }

        public Bitmap getOwnerHead() throws IOException
        {
            return (Bitmap) get((String) getInfoFromJson(getJsonFromJson(oriVideoJson, "owner"), "face"), 2);
        }

        public String getVideoTime()
        {
            return getTime((int) getInfoFromJson(oriVideoJson, "pubdate"));
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
        private JSONObject oriTextUserJson;

        public cardOriginalText(JSONObject oriTextJson)
        {
            this.oriTextItemJson = getJsonFromJson(oriTextJson, "item");
            this.oriTextUserJson = getJsonFromJson(oriTextJson, "user");
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
                for(int i = 0; i < pics.length(); i++)
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
            return (String) getInfoFromJson(oriTextUserJson, "name");
        }

        public Bitmap getUserHead() throws IOException
        {
            return (Bitmap) get((String) getInfoFromJson(oriTextUserJson, "head_url"), 2);
        }

        public String getDynamicTime()
        {
            return getTime((int) getInfoFromJson(oriTextItemJson, "upload_time"));
        }
    }

    public class cardShareVideo
    {
        private JSONObject shareVideoJson;
        private JSONObject shareVideoItemJson;
        private JSONObject shareVideoUserJson;

        public cardShareVideo(JSONObject shareVideoJson)
        {
            this.shareVideoJson = shareVideoJson;
            this.shareVideoItemJson = getJsonFromJson(shareVideoJson, "item");
            this.shareVideoUserJson = getJsonFromJson(shareVideoJson, "user");
        }

        public String getDynamicText()
        {
            return (String) getInfoFromJson(shareVideoItemJson, "content");
        }

        public String getUserName()
        {
            return (String) getInfoFromJson(shareVideoUserJson, "uname");
        }

        public Bitmap getUserHead() throws IOException
        {
            return (Bitmap) get((String) getInfoFromJson(shareVideoUserJson, "face"), 2);
        }

        public String getDynamicTime()
        {
            return getTime((int) getInfoFromJson(shareVideoItemJson, "timestamp"));
        }

        public String getOriginalVideo()
        {
            return (String) getInfoFromJson(shareVideoJson, "origin");
        }
    }

    public class cardShareText
    {
        private JSONObject shareTextJson;
        private JSONObject shareTextItemJson;
        private JSONObject shareTextUserJson;

        public cardShareText(JSONObject shareTextJson)
        {
            this.shareTextJson = shareTextJson;
            this.shareTextItemJson = getJsonFromJson(shareTextJson, "item");
            this.shareTextUserJson = getJsonFromJson(shareTextJson, "user");
        }

        public String getDynamicText()
        {
            return (String) getInfoFromJson(shareTextItemJson, "content");
        }

        public String getUserName()
        {
            return (String) getInfoFromJson(shareTextUserJson, "uname");
        }

        public Bitmap getUserHead() throws IOException
        {
            return (Bitmap) get((String) getInfoFromJson(shareTextUserJson, "face"), 2);
        }

        public String getDynamicTime()
        {
            return getTime((int) getInfoFromJson(shareTextItemJson, "timestamp"));
        }

        public String getOriginalText()
        {
            return (String) getInfoFromJson(shareTextJson, "origin");
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
