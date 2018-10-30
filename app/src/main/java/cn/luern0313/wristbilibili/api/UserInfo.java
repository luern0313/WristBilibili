package cn.luern0313.wristbilibili.api;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

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
 * Created by liupe on 2018/10/5.
 */

public class UserInfo
{
    private String cookie;

    private JSONObject userInfoJson;

    public UserInfo(String cookie)
    {
        this.cookie = cookie;
    }

    public void getUserInfo() throws IOException
    {
        try
        {
            userInfoJson = new JSONObject((String) get("https://api.bilibili.com/x/web-interface/nav", 1));
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public String getUserName()
    {
        try
        {
            return (String) userInfoJson.getJSONObject("data").get("uname");
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return "";
    }

    public String getUserCoin()
    {
        try
        {
            return String.valueOf(userInfoJson.getJSONObject("data").get("money"));
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return "0";
    }

    public int getUserLV()
    {
        try
        {
            return (int) userInfoJson.getJSONObject("data").getJSONObject("level_info").get("current_level");
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean isVip()
    {
        try
        {
            return ((int) userInfoJson.getJSONObject("data").get("vipStatus")) == 1;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public Bitmap getUserHead() throws IOException
    {
        try
        {
            return (Bitmap) get((String) userInfoJson.getJSONObject("data").get("face"), 2);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private Object get(String url, int mode) throws IOException
    {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)//设置连接超时时间
                .readTimeout(15, TimeUnit.SECONDS)//设置读取超时时间
                .build();
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
        client = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)//设置连接超时时间
                .readTimeout(15, TimeUnit.SECONDS)//设置读取超时时间
                .build();
        //参数传递
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
