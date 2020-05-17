package cn.luern0313.wristbilibili.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.models.RecommendModel;
import cn.luern0313.wristbilibili.util.NetWorkUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;
import okhttp3.Response;

/**
 * 被 luern0313 创建于 2020/1/16.
 */

public class RecommendApi
{
    private String mid;
    private String csrf;
    private String access_key;
    private ArrayList<String> appHeaders;

    public RecommendApi()
    {
        this.csrf = SharedPreferencesUtil.getString(SharedPreferencesUtil.csrf, "");
        this.mid = SharedPreferencesUtil.getString(SharedPreferencesUtil.mid, "");
        this.access_key = SharedPreferencesUtil.getString(SharedPreferencesUtil.accessKey, "");
        appHeaders = new ArrayList<String>(){{
            add("Cookie"); add(SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, ""));
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_OWN);
        }};
    }

    public ArrayList<RecommendModel> getRecommendVideo(boolean isPull) throws IOException
    {
        ArrayList<RecommendModel> recommendModelArrayList = new ArrayList<>();
        try
        {
            String url = "http://app.bilibili.com/x/v2/feed/index";
            String temp_per = "access_key=" + access_key + "&appkey=" + ConfInfoApi.getConf("appkey") +
                    "&build=" + ConfInfoApi.getConf("build") + "&flush=0&idx=" + (int) (System.currentTimeMillis() / 1000) +
                    "&login_event=2&mobi_app=" + ConfInfoApi.getConf("mobi_app") + "&xml=wifi&open_event=&platform=" +
                    ConfInfoApi.getConf("platform") + "&pull=" + (isPull ? "true" : "false") + "&qn=32&style=1&ts=" +
                    (int) (System.currentTimeMillis() / 1000);
            String sign = ConfInfoApi.calc_sign(temp_per, ConfInfoApi.getConf("app_secret"));
            Response response = NetWorkUtil.get(url + "?" + temp_per + "&sign=" + sign, appHeaders);
            JSONArray resultJSONArray = new JSONObject(response.body().string()).getJSONObject("data").getJSONArray("items");
            for(int i = 0; i < resultJSONArray.length(); i++)
            {
                JSONObject recommend = resultJSONArray.getJSONObject(i);
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
