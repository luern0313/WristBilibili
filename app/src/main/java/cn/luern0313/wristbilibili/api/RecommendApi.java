package cn.luern0313.wristbilibili.api;


import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.lson.LsonUtil;
import cn.luern0313.lson.element.LsonArray;
import cn.luern0313.lson.element.LsonObject;
import cn.luern0313.wristbilibili.models.RecommendModel;
import cn.luern0313.wristbilibili.util.NetWorkUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;

/**
 * 被 luern0313 创建于 2020/1/16.
 */

public class RecommendApi
{
    private String access_key;

    private ArrayList<String> appHeaders;

    public RecommendApi()
    {
        this.access_key = SharedPreferencesUtil.getString(SharedPreferencesUtil.accessKey, "");
        appHeaders = new ArrayList<String>(){{
            add("Cookie"); add(SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, ""));
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_OWN);
        }};
    }

    public ArrayList<RecommendModel> getRecommendVideo(boolean isPull) throws IOException
    {
        ArrayList<RecommendModel> recommendModelArrayList = new ArrayList<>();
        String url = "http://app.bilibili.com/x/v2/feed/index";
        String temp_per = "access_key=" + access_key + "&appkey=" + ConfInfoApi.getConf("appkey") +
                "&build=" + ConfInfoApi.getConf("build") + "&flush=0&idx=" + (int) (System.currentTimeMillis() / 1000) +
                "&login_event=2&mobi_app=" + ConfInfoApi.getConf("mobi_app") + "&xml=wifi&open_event=&platform=" +
                ConfInfoApi.getConf("platform") + "&pull=" + (isPull ? "true" : "false") + "&qn=32&style=1&ts=" + (int) (System.currentTimeMillis() / 1000);
        String sign = ConfInfoApi.calc_sign(temp_per, ConfInfoApi.getConf("app_secret"));
        LsonObject result = LsonUtil.parseAsObject(NetWorkUtil.get(url + "?" + temp_per + "&sign=" + sign, appHeaders).body().string());
        if(result.getAsInt("code", -1) == 0)
        {
            LsonArray list = result.getAsJsonObject("data").getAsJsonArray("items");
            for (int i = 0; i < list.size(); i++)
            {
                LsonObject recommend = list.getAsJsonObject(i);
                if(recommend.getAsString("card_type").contains("small_cover") && recommend.getAsString("card_goto").equals("av"))
                    recommendModelArrayList.add(LsonUtil.fromJson(recommend, RecommendModel.class));
            }
        }
        return recommendModelArrayList;
    }
}
