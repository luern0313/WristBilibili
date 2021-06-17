package cn.luern0313.wristbilibili.api;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.lson.LsonUtil;
import cn.luern0313.lson.TypeReference;
import cn.luern0313.wristbilibili.models.AnimationTimelineModel;
import cn.luern0313.wristbilibili.models.BaseModel;
import cn.luern0313.wristbilibili.util.NetWorkUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;

/**
 * Created by liupe on 2018/11/10.
 * 番剧时间表
 */

public class AnimationTimelineApi
{
    private final ArrayList<String> webHeaders;
    public AnimationTimelineApi()
    {
        webHeaders = new ArrayList<String>(){{
            add("Cookie"); add(SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, ""));
            add("Referer"); add("https://www.bilibili.com/");
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_WEB);
        }};
    }

    public ArrayList<AnimationTimelineModel> getAnimTimelineList() throws IOException
    {
        String url = "https://bangumi.bilibili.com/web_api/timeline_global";
        BaseModel<ArrayList<AnimationTimelineModel>> baseModel = LsonUtil.fromJson(LsonUtil.parse(NetWorkUtil.get(url, webHeaders).body().string()), new TypeReference<BaseModel<ArrayList<AnimationTimelineModel>>>(){});
        if(baseModel.isSuccess())
            return baseModel.getData();
        return null;
    }
}
