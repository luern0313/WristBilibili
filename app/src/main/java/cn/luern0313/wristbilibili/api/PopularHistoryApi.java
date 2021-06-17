package cn.luern0313.wristbilibili.api;


import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.lson.LsonUtil;
import cn.luern0313.lson.TypeReference;
import cn.luern0313.wristbilibili.models.BaseModel;
import cn.luern0313.wristbilibili.models.popular.PopularHistoryModel;
import cn.luern0313.wristbilibili.util.NetWorkUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;

/**
 * 被 luern0313 创建于 2020/1/12.
 */

public class PopularHistoryApi
{
    private final String mid;
    private final String csrf;

    public ArrayList<PopularHistoryModel.PopularHistoryVideoModel> popularHistoryVideoModelArrayList = new ArrayList<>();

    private final ArrayList<String> webHeaders;
    private final ArrayList<String> appHeaders;

    public PopularHistoryApi()
    {
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

    public void getPopularHistoryVideo() throws IOException
    {
        String url = "https://api.bilibili.com/x/web-interface/popular/precious";
        String per = "page_size=100&page=1";
        BaseModel<PopularHistoryModel> baseModel = LsonUtil.fromJson(LsonUtil.parse(NetWorkUtil.get(url + "?" + per, webHeaders).body().string()), new TypeReference<BaseModel<PopularHistoryModel>>(){});
        if(baseModel.isSuccess())
            popularHistoryVideoModelArrayList = baseModel.getData().getList();
    }
}
