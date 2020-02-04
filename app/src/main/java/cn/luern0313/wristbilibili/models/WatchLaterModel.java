package cn.luern0313.wristbilibili.models;

import org.json.JSONObject;

import cn.luern0313.wristbilibili.util.DataProcessUtil;

/**
 * 被 luern0313 创建于 2020/1/15.
 */

public class WatchLaterModel
{
    public String aid;
    public String title;
    public String up;
    public String cover;
    public int duration;
    public int progress;
    public String play;
    public String danmaku;

    public WatchLaterModel(JSONObject warchlater)
    {
        this.aid = String.valueOf(warchlater.optInt("aid", 0));
        this.title = warchlater.optString("title", "");
        this.up = warchlater.optJSONObject("owner").optString("name", "");
        this.cover = warchlater.optString("pic", "");
        this.duration = warchlater.optInt("duration", 0);
        this.progress = warchlater.optInt("progress", 0);
        this.play = DataProcessUtil.getView(warchlater.optJSONObject("stat").optInt("view", 0));
        this.danmaku = DataProcessUtil.getView(warchlater.optJSONObject("stat").optInt("danmaku", 0));
    }
}
