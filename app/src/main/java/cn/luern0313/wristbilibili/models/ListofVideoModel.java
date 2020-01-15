package cn.luern0313.wristbilibili.models;

import org.json.JSONObject;

/**
 * 被 luern0313 创建于 2019/2/22.
 * 针对返回一个列表的视频的单个视频信息解析的api
 */

public class ListofVideoModel
{
    private JSONObject videoJson;
    private JSONObject videoUserJson;
    private JSONObject videoStatJson;

    public ListofVideoModel(JSONObject json)
    {
        this.videoJson = json;
        this.videoUserJson = json.optJSONObject("owner");
        this.videoStatJson = json.optJSONObject("stat");
    }

    public String getVideoAid()
    {
        return String.valueOf(videoJson.optInt("aid"));
    }

    public String getVideoTitle()
    {
        return videoJson.optString("title");
    }

    public String getVideoImg()
    {
        return "https:" + videoJson.optString("pic");
    }

    public String getOwnerName()
    {
        return videoUserJson.optString("name");
    }

    public String getVideoPlay()
    {
        int view = videoStatJson.optInt("view");
        if(view > 10000) return view / 1000 / 10.0 + "万";
        else return String.valueOf(view);
    }

    public String getVideoDanmaku()
    {
        int danmaku = videoStatJson.optInt("danmaku");
        if(danmaku > 10000) return danmaku / 1000 / 10.0 + "万";
        else return String.valueOf(danmaku);
    }
}
