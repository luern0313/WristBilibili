package cn.luern0313.wristbilibili.api;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.models.RecommendModel;
import cn.luern0313.wristbilibili.util.NetWorkUtil;
import okhttp3.Response;

/**
 * 被 luern0313 创建于 2020/1/16.
 */

public class RecommendApi
{
    private String cookie;
    private String mid;
    private String csrf;
    private String access_key;
    private ArrayList<String> defaultHeaders = new ArrayList<String>();

    public RecommendApi(String mid, String cookies, String csrf, String access_key)
    {
        this.mid = mid;
        this.cookie = cookies;
        this.csrf = csrf;
        this.access_key = access_key;
        defaultHeaders = new ArrayList<String>(){{
            add("Cookie"); add(cookie);
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_OWN);
        }};
    }

    public ArrayList<RecommendModel> getRecommendVideo() throws IOException
    {
        ArrayList<RecommendModel> recommendModelArrayList = new ArrayList<>();
        try
        {
            String url = "http://app.bilibili.com/x/v2/feed/index";
            String temp_per = "access_key=" + access_key + "&appkey=" + ConfInfoApi.getConf("appkey") +
                    "&build=" + ConfInfoApi.getConf("build") + "&flush=0&idx=" + (int) (System.currentTimeMillis() / 1000) +
                    "&login_event=2&mobi_app=android&network=wifi&open_event=&platform=android&pull=false&qn=32&style=1&ts=" +
                    (int) (System.currentTimeMillis() / 1000);
            String sign = ConfInfoApi.calc_sign(temp_per);
            Response response = NetWorkUtil.get(url + "?" + temp_per + "&sign=" + sign, defaultHeaders);
            JSONArray resultJSONArray = new JSONObject(response.body().string()).getJSONObject("data").getJSONArray("items");
            for(int i = 0; i < resultJSONArray.length(); i++)
            {
                JSONObject recommend = resultJSONArray.getJSONObject(i);
                Log.i("bilibili", recommend.toString());
                if(recommend.optString("card_type").contains("small_cover") && recommend.optString("card_goto").equals("av"))
                    recommendModelArrayList.add(new RecommendModel(recommend));
            }
            return recommendModelArrayList;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return recommendModelArrayList;
    }
}
