package cn.luern0313.wristbilibili.api;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.lson.LsonUtil;
import cn.luern0313.lson.element.LsonArray;
import cn.luern0313.lson.element.LsonObject;
import cn.luern0313.wristbilibili.models.ListVideoModel;
import cn.luern0313.wristbilibili.util.NetWorkUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;

/**
 * 被 luern0313 创建于 2019/8/31.
 * 仅用于<稍后再看>界面
 * 添加稍后再看在视频详情界面和视频详情api
 */

public class WatchLaterApi
{
    private final String csrf;
    private final String mid;

    private final ArrayList<String> webHeaders;

    public WatchLaterApi()
    {
        this.csrf = SharedPreferencesUtil.getString(SharedPreferencesUtil.csrf, "");
        this.mid = SharedPreferencesUtil.getString(SharedPreferencesUtil.mid, "");
        webHeaders = new ArrayList<String>(){{
            add("Cookie"); add(SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, ""));
            add("Referer"); add("https://www.bilibili.com/");
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_WEB);
        }};
    }

    public ArrayList<ListVideoModel> getWatchLater() throws IOException
    {
        String url = "https://api.bilibili.com/x/v2/history/toview/web";
        ArrayList<ListVideoModel> videoArrayList = new ArrayList<>();
        LsonObject result = LsonUtil.parseAsObject(NetWorkUtil.get(url, webHeaders).body().string());
        if(result.getInt("code") == 0)
        {
            LsonArray list = result.getJsonObject("data").getJsonArray("list");
            for(int i = 0; i < list.size(); i++)
                videoArrayList.add(LsonUtil.fromJson(list.getJsonObject(i), ListVideoModel.class));
        }
        return videoArrayList;
    }
}
