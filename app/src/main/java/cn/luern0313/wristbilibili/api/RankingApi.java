package cn.luern0313.wristbilibili.api;


import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import cn.luern0313.lson.LsonArrayUtil;
import cn.luern0313.lson.LsonObjectUtil;
import cn.luern0313.lson.LsonParser;
import cn.luern0313.lson.LsonUtil;
import cn.luern0313.wristbilibili.models.RankingModel;
import cn.luern0313.wristbilibili.util.NetWorkUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;

/**
 * 被 luern0313 创建于 2020/1/12.
 */

public class RankingApi
{
    private String mid;
    private String csrf;
    private ArrayList<String> appHeaders;

    public RankingApi()
    {
        this.csrf = SharedPreferencesUtil.getString(SharedPreferencesUtil.csrf, "");
        this.mid = SharedPreferencesUtil.getString(SharedPreferencesUtil.mid, "");
        appHeaders = new ArrayList<String>(){{
            add("Cookie"); add(SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, ""));
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_OWN);
        }};
    }

    public ArrayList<RankingModel> getRankingVideo(int pn) throws IOException
    {
        ArrayList<RankingModel> rankingVideoArrayList = new ArrayList<>();
        String url = "http://app.bilibili.com/x/v2/rank/region";
        String temp_per = "appkey=" + ConfInfoApi.getConf("appkey") + "&build=" + ConfInfoApi.getConf("build") + "&mobi_app=android&platform=android&pn=" + pn + "&ps=20&rid=0&ts=" + (int) (System.currentTimeMillis() / 1000);
        String sign = ConfInfoApi.calc_sign(temp_per, ConfInfoApi.getConf("app_secret"));
        LsonObjectUtil result = LsonParser.parseString(NetWorkUtil.get(url + "?" + temp_per + "&sign=" + sign, appHeaders).body().string());
        if(result.getAsInt("code", -1) == 0)
        {
            LsonArrayUtil list = result.getAsJsonArray("data");
            for (int i = 0; i < list.size(); i++)
                rankingVideoArrayList.add(LsonUtil.fromJson(list.getAsJsonObject(i), RankingModel.class));
        }
        return rankingVideoArrayList;
    }

    public LinkedHashMap<Integer, String> getPickUpVideo()
    {
        try
        {
            LinkedHashMap<Integer, String> pickUpMap = new LinkedHashMap<>();
            String url = "http://luern0313.cn:8080/bp/getHistoryPickUpVideo";
            LsonObjectUtil result = LsonParser.parseString(NetWorkUtil.get(url).body().string());
            LsonArrayUtil list = result.getAsJsonArray("Data");
            for(int i = 0; i < list.size(); i++)
            {
                final LsonObjectUtil jsonObject = list.getAsJsonObject(i);
                pickUpMap.put(Integer.valueOf(jsonObject.getAsString("Date")), jsonObject.getAsString("Aid"));
            }
            return pickUpMap;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public void clickPickUpVideo()
    {
        try
        {
            String url = "http://luern0313.cn:8080/bp/clickPickUpVideo";
            NetWorkUtil.get(url);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
