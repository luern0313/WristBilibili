package cn.luern0313.wristbilibili.models;

import org.json.JSONObject;

/**
 * 被 luern0313 创建于 2020/1/15.
 */

public class RankingModel
{
    public String video_aid;
    public String video_title;
    public String video_pic;
    public String video_score;
    public int video_play;
    public int video_danmaku;
    public String up_mid;
    public String up_name;
    public String up_face;
    public RankingModel(JSONObject jsonObject)
    {
        video_aid = "av".equals(jsonObject.optString("goto")) ? jsonObject.optString("param") : "";
        video_title = jsonObject.optString("title");
        video_pic = jsonObject.optString("cover");
        video_score = String.valueOf(jsonObject.optInt("pts"));
        video_play = jsonObject.optInt("play");
        video_danmaku = jsonObject.optInt("danmaku");
        up_mid = String.valueOf(jsonObject.optInt("mid"));
        up_name = jsonObject.optString("name");
        up_face = jsonObject.optString("face");
    }
}
