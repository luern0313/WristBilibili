package cn.luern0313.wristbilibili.api;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.models.BangumiModel;
import cn.luern0313.wristbilibili.util.NetWorkUtil;
import okhttp3.Response;

/**
 * 被 luern0313 创建于 2020/1/19.
 */

public class BangumiApi
{
    private String cookie;
    private String mid;
    private String csrf;
    private String access_key;
    private ArrayList<String> defaultHeaders = new ArrayList<String>();

    private String season_id;

    public BangumiApi(String mid, String cookies, String csrf, String access_key, String season_id)
    {
        this.mid = mid;
        this.cookie = cookies;
        this.csrf = csrf;
        this.access_key = access_key;

        this.season_id = season_id;
        defaultHeaders = new ArrayList<String>(){{
            add("Cookie"); add(cookie);
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_OWN);
        }};
    }

    public BangumiModel getBangumiInfo() throws IOException
    {
        try
        {
            String temp_per = "access_key=" + access_key + "&appkey=" + ConfInfoApi.getConf("appkey") +
                "&build=" + ConfInfoApi.getConf("build") + "&platform=android&season_id=" + season_id +
                "&ts=" + (int) (System.currentTimeMillis() / 1000);
            String sign = ConfInfoApi.calc_sign(temp_per);
            Response response = NetWorkUtil.get("https://api.bilibili.com/pgc/view/app/season?" + temp_per + "&sign=" + sign, defaultHeaders);
            JSONObject result = new JSONObject(response.body().string());
            String a = result.toString();
            for(int i=0; i < a.length(); i += 3000)
            {
                if(i + 3000 < a.length()) Log.i("bilibili" + i, a.substring(i, i + 3000));
                else Log.i("bilibili" + i, a.substring(i, a.length()));
            }
            if(result.optInt("code") == 0)
                return new BangumiModel(result.getJSONObject("result"));
            else
                return null;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
