package cn.luern0313.wristbilibili.models;

import org.json.JSONObject;

import lombok.Getter;
import lombok.Setter;

/**
 * 被 luern0313 创建于 2020/1/15.
 */

@Getter
@Setter
public class AnimationTimelineModel
{
    private String seasonId;
    private String name;
    private String coverUrl;
    private String lastEpisode;
    private int isFollow;
    private String time;
    public AnimationTimelineModel(JSONObject anim, String day)
    {
        seasonId = String.valueOf(anim.optInt("season_id"));
        name = anim.optString("title");
        coverUrl = anim.optString("square_cover");
        lastEpisode = anim.optString("pub_index");
        isFollow = (int) anim.opt("follow");
        time = day + anim.optString("pub_time");
    }
}
