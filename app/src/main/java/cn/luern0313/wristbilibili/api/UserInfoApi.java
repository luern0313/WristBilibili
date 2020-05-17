package cn.luern0313.wristbilibili.api;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.util.NetWorkUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;

/**
 * Created by liupe on 2018/10/5.
 * emmmm.....
 */

public class UserInfoApi
{
    private ArrayList<String> webHeaders;
    private JSONObject userInfoJson;

    public UserInfoApi()
    {
        webHeaders = new ArrayList<String>(){{
            add("Cookie"); add(SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, ""));
            add("Referer"); add("https://search.bilibili.com/");
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_WEB);
        }};
    }

    public int getUserInfo() throws IOException
    {
        try
        {
            String url = "https://api.bilibili.com/x/web-interface/nav";
            userInfoJson = new JSONObject(NetWorkUtil.get(url, webHeaders).body().string());
            return userInfoJson.optInt("code") == -101 ? -2 : 0;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    public String getUserName()
    {
        try
        {
            return (String) userInfoJson.getJSONObject("data").get("uname");
        }
        catch (JSONException e)
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
        }
        catch (JSONException e)
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
        }
        catch (JSONException e)
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
            byte[] buffer = NetWorkUtil.readStream(NetWorkUtil.get((String) userInfoJson.getJSONObject("data").get("face"), webHeaders).body().byteStream());
            return BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
