package cn.luern0313.wristbilibili.api;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.lson.LsonUtil;
import cn.luern0313.lson.element.LsonArray;
import cn.luern0313.lson.element.LsonObject;
import cn.luern0313.lson.json.LsonParser;
import cn.luern0313.wristbilibili.models.ListVideoModel;
import cn.luern0313.wristbilibili.util.NetWorkUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;

/**
 * 被 luern0313 创建于 2020/4/30.
 */
public class HistoryApi
{
    private ArrayList<String> webHeaders;
    public HistoryApi()
    {
        webHeaders = new ArrayList<String>(){{
            add("Cookie"); add(SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, ""));
            add("Referer"); add("https://www.bilibili.com/");
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_WEB);
        }};
    }

    public ArrayList<ListVideoModel> getHistory(int pn) throws IOException
    {
        String url = "https://api.bilibili.com/x/v2/history";
        String arg = "pn=" + pn + "&ps=30";
        LsonObject result = LsonParser.parseAsObject(NetWorkUtil.get(url + "?" + arg, webHeaders).body().string());
        ArrayList<ListVideoModel> videoModelArrayList = new ArrayList<>();
        if(result.getAsInt("code", -1) == 0)
        {
            LsonArray videoJSONArray = result.getAsJsonArray("data");
            for(int i = 0; i < videoJSONArray.size(); i++)
                videoModelArrayList.add(LsonUtil.fromJson(videoJSONArray.getAsJsonObject(i), ListVideoModel.class));
        }
        return videoModelArrayList;
    }
}
