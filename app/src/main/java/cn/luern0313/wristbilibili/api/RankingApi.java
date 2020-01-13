package cn.luern0313.wristbilibili.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

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
    private static ArrayList<String> defaultHeaders = new ArrayList<String>();

    public RankingApi(String mid, final String cookie, String csrf)
    {
        this.mid = mid;
        this.cookie = cookie;
        this.csrf = csrf;
        defaultHeaders = new ArrayList<String>(){{
            add("Cookie"); add(cookie);
            add("User-Agent"); add("Wrist Bilibili Client/2.6 (liupeiran0313@163.com)");
        }};
    }

    public ArrayList<RankingVideo> getRankingVideo(int pn) throws IOException
    {
        ArrayList<RankingVideo> rankingVideoArrayList = new ArrayList<>();
        try
        {
            String url = "http://app.bilibili.com/x/v2/rank/region";
            String temp_per = "appkey=" + ConfInfoApi.getConf("appkey") + "&build=" + ConfInfoApi.getConf("build") +
                    "&mobi_app=android&platform=android&pn=" + pn + "&ps=20&rid=0&ts=" + (int) (System.currentTimeMillis() / 1000);
            String sign = ConfInfoApi.calc_sign(temp_per);
            Response response = NetWorkUtil.get(url + "?" + temp_per + "&sign=" + sign, defaultHeaders);
            JSONArray resultJSONArray = new JSONObject(response.body().string()).getJSONArray("data");
            for(int i = 0; i < resultJSONArray.length(); i++)
                rankingVideoArrayList.add(new RankingVideo(resultJSONArray.getJSONObject(i)));
            return rankingVideoArrayList;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return rankingVideoArrayList;
    }

    public class RankingVideo
    {
        public String video_title;
        public String video_pic;
        public String video_score;
        public int video_play;
        public int video_danmaku;
        public String up_name;
        public String up_face;
        RankingVideo(JSONObject jsonObject)
        {
            video_title = jsonObject.optString("title");
            video_pic = jsonObject.optString("cover");
            video_score = String.valueOf(jsonObject.optInt("pts"));
            video_play = jsonObject.optInt("play");
            video_danmaku = jsonObject.optInt("danmaku");
            up_name = jsonObject.optString("name");
            up_face = jsonObject.optString("face");
        }
    }
}
