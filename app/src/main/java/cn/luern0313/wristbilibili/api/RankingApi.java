package cn.luern0313.wristbilibili.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.models.RankingModel;
import cn.luern0313.wristbilibili.util.NetWorkUtil;
import okhttp3.Response;

/**
 * 被 luern0313 创建于 2020/1/12.
 */

public class RankingApi
{
    private String cookie;
    private String mid;
    private String csrf;
    private ArrayList<String> defaultHeaders = new ArrayList<String>();

    public RankingApi(String mid, final String cookie, String csrf)
    {
        this.mid = mid;
        this.cookie = cookie;
        this.csrf = csrf;
        defaultHeaders = new ArrayList<String>(){{
            add("Cookie"); add(cookie);
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_OWN);
        }};
    }

    public ArrayList<RankingModel> getRankingVideo(int pn) throws IOException
    {
        ArrayList<RankingModel> rankingVideoArrayList = new ArrayList<>();
        try
        {
            String url = "http://app.bilibili.com/x/v2/rank/region";
            String temp_per = "appkey=" + ConfInfoApi.getConf("appkey") + "&build=" + ConfInfoApi.getConf("build") + "&mobi_app=android&platform=android&pn=" + pn + "&ps=20&rid=0&ts=" + (int) (System.currentTimeMillis() / 1000);
            String sign = ConfInfoApi.calc_sign(temp_per);
            Response response = NetWorkUtil.get(url + "?" + temp_per + "&sign=" + sign, defaultHeaders);
            JSONArray resultJSONArray = new JSONObject(response.body().string()).getJSONArray("data");
            for(int i = 0; i < resultJSONArray.length(); i++)
                rankingVideoArrayList.add(new RankingModel(resultJSONArray.getJSONObject(i)));
            return rankingVideoArrayList;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return rankingVideoArrayList;
    }

}
