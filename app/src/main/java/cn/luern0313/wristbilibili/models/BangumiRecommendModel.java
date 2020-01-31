package cn.luern0313.wristbilibili.models;

import org.json.JSONObject;

/**
 * 被 luern0313 创建于 2020/1/31.
 */

public class BangumiRecommendModel
{
    public String bangumi_title;
    public String bangumi_season_id;
    public String bangumi_cover;
    public String bangumi_play;
    public String bangumi_follow;
    public String bangumi_new;
    public String bangumi_score;
    public BangumiRecommendModel(JSONObject bangumi)
    {
        bangumi_title = bangumi.optString("title");
        bangumi_season_id = String.valueOf(bangumi.optInt("season_id"));
        bangumi_cover = bangumi.has("new_ep") ? bangumi.optJSONObject("new_ep").optString("cover") : "";
        bangumi_play = getView(bangumi.has("stat") ? bangumi.optJSONObject("stat").optInt("view") : 0);
        bangumi_follow = getView(bangumi.has("stat") ? bangumi.optJSONObject("stat").optInt("follow") : 0);
        bangumi_new = bangumi.has("new_ep") ? bangumi.optJSONObject("new_ep").optString("index_show") : "";
        bangumi_score = bangumi.has("rating") ? String.valueOf(bangumi.optJSONObject("rating").optDouble("score")) : "";
    }

    private static String getView(int view)
    {
        if(view > 10000) return view / 1000 / 10.0 + "万";
        else return String.valueOf(view);
    }
}
