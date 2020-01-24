package cn.luern0313.wristbilibili.models;

import org.json.JSONObject;

/**
 * 被 luern0313 创建于 2020/1/15.
 */

public class AnimationTimelineModel
{
    public String season_id;
    public String name;
    public String coverUrl;
    public String lastEpisode;
    public int isfollow;
    public String time;
    public AnimationTimelineModel(JSONObject anim, String day)
    {
        season_id = String.valueOf(anim.optInt("season_id"));
        name = anim.optString("title");
        coverUrl = anim.optString("square_cover");
        lastEpisode = anim.optString("pub_index");
        isfollow = (int) anim.opt("follow");
        time = day + anim.optString("pub_time");
    }
}
