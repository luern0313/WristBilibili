package cn.luern0313.wristbilibili.api;


import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.lson.LsonUtil;
import cn.luern0313.lson.TypeReference;
import cn.luern0313.wristbilibili.models.BaseModel;
import cn.luern0313.wristbilibili.models.popular.PopularModel;
import cn.luern0313.wristbilibili.util.NetWorkUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;

/**
 * 被 luern0313 创建于 2020/1/12.
 */

public class PopularApi
{
    private final String mid;
    private final String csrf;

    public ArrayList<PopularModel.PopularVideoModel> popularVideoModelArrayList = new ArrayList<>();
    public boolean noMore;

    private final ArrayList<String> webHeaders;
    private final ArrayList<String> appHeaders;

    public PopularApi()
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

    public boolean getPopularVideo(int pn) throws IOException
    {
        String url = "https://api.bilibili.com/x/web-interface/popular";
        String per = "ps=20&pn=" + pn;
        BaseModel<PopularModel> baseModel = LsonUtil.fromJson(LsonUtil.parse(NetWorkUtil.get(url + "?" + per, webHeaders).body().string()), new TypeReference<BaseModel<PopularModel>>(){});
        if(baseModel.isSuccess())
        {
            if(pn == 1)
                popularVideoModelArrayList.clear();
            popularVideoModelArrayList.addAll(baseModel.getData().getList());
            noMore = baseModel.getData().isNoMore();
            return true;
        }
        return false;
    }
}
