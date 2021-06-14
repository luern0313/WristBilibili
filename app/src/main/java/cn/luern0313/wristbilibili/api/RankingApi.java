package cn.luern0313.wristbilibili.api;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import cn.luern0313.lson.LsonUtil;
import cn.luern0313.lson.TypeReference;
import cn.luern0313.lson.element.LsonArray;
import cn.luern0313.lson.element.LsonObject;
import cn.luern0313.wristbilibili.models.BaseModel;
import cn.luern0313.wristbilibili.models.RankingModel;
import cn.luern0313.wristbilibili.util.FileUtil;
import cn.luern0313.wristbilibili.util.MyApplication;
import cn.luern0313.wristbilibili.util.NetWorkUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;

/**
 * 被 luern0313 创建于 2020/1/12.
 */

public class RankingApi
{
    private final String mid;
    private final String csrf;

    private RankingModel rankingModel;
    private static LinkedHashMap<String, String> rankingRegionMap;

    private final ArrayList<String> webHeaders;
    private final ArrayList<String> appHeaders;

    public RankingApi()
    {
        initConfig();
        this.csrf = SharedPreferencesUtil.getString(SharedPreferencesUtil.csrf, "");
        this.mid = SharedPreferencesUtil.getString(SharedPreferencesUtil.mid, "");

        webHeaders = new ArrayList<String>(){{
            add("Cookie"); add(SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, ""));
            add("Referer"); add("https://www.bilibili.com/");
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_WEB);
        }};
        appHeaders = new ArrayList<String>(){{
            add("Cookie"); add(SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, ""));
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_OWN);
        }};
    }

    public RankingModel getRankingVideo(String rid) throws IOException
    {
        String url = "https://api.bilibili.com/x/web-interface/ranking/v2";
        String per = "rid=" + rid + "&type=all";
        BaseModel<RankingModel> baseModel = LsonUtil.fromJson(LsonUtil.parse(NetWorkUtil.get(url + "?" + per, webHeaders).body().string()), new TypeReference<BaseModel<RankingModel>>(){});
        if(baseModel.isSuccess())
            rankingModel = baseModel.getData();
        return rankingModel;
    }

    public LinkedHashMap<Integer, String> getPickUpVideo()
    {
        try
        {
            LinkedHashMap<Integer, String> pickUpMap = new LinkedHashMap<>();
            String url = "http://luern0313.cn:8080/bp/getHistoryPickUpVideo";
            LsonObject result = LsonUtil.parseAsObject(NetWorkUtil.get(url, appHeaders).body().string());
            LsonArray list = result.getJsonArray("Data");
            for(int i = 0; i < list.size(); i++)
            {
                final LsonObject jsonObject = list.getJsonObject(i);
                pickUpMap.put(Integer.valueOf(jsonObject.getString("Date")), jsonObject.getString("Aid"));
            }
            return pickUpMap;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public String getRegionName(String rid)
    {
        return rankingRegionMap.get(rid);
    }

    public HashMap<String, String> getRankingRegionMap()
    {
        return rankingRegionMap;
    }

    private void initConfig()
    {
        try
        {
            if(rankingRegionMap == null)
                rankingRegionMap = LsonUtil.fromJson(LsonUtil.parse(FileUtil.fileReader(
                        MyApplication.getContext().getAssets().open("ranking_region.json"))), new TypeReference<LinkedHashMap<String, String>>(){});
        }
        catch (IOException ignored)
        {
        }
    }
}
