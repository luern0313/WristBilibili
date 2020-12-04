package cn.luern0313.wristbilibili.api;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.lson.LsonUtil;
import cn.luern0313.lson.element.LsonArray;
import cn.luern0313.lson.element.LsonObject;
import cn.luern0313.wristbilibili.models.AnimationTimelineModel;
import cn.luern0313.wristbilibili.util.NetWorkUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;

/**
 * Created by liupe on 2018/11/10.
 * 番剧时间表
 */

public class AnimationTimelineApi
{
    private ArrayList<String> webHeaders;
    public AnimationTimelineApi()
    {
        webHeaders = new ArrayList<String>(){{
            add("Cookie"); add(SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, ""));
            add("Referer"); add("https://www.bilibili.com/anime");
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_WEB);
        }};
    }

    public ArrayList<AnimationTimelineModel> getAnimTimelineList() throws IOException
    {
        String url = "https://bangumi.bilibili.com/web_api/timeline_global";
        LsonObject result = LsonUtil.parseAsObject(NetWorkUtil.get(url, webHeaders).body().string());
        ArrayList<AnimationTimelineModel> animationTimelineModelArrayList = new ArrayList<>();
        if(result.getAsInt("code", -1) == 0)
        {
            LsonArray animArray = result.getAsJsonArray("result");
            for (int i = 6; i >= 0; i--)
                animationTimelineModelArrayList.add(LsonUtil.fromJson(animArray.getAsJsonObject(i), AnimationTimelineModel.class));
        }
        return animationTimelineModelArrayList;
    }
}
