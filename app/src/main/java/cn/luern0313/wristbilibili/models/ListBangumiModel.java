package cn.luern0313.wristbilibili.models;

import org.json.JSONObject;

import java.io.Serializable;

import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.LruCacheUtil;

/**
 * 被 luern0313 创建于 2020/1/31.
 */

public class ListBangumiModel implements Serializable
{
    public String bangumi_title;
    public String bangumi_season_id;
    public String bangumi_cover;
    public String bangumi_play;
    public String bangumi_follow;
    public String bangumi_new;
    public String bangumi_score;

    public ListBangumiModel(JSONObject bangumi)
    {
        bangumi_title = bangumi.optString("title");
        bangumi_season_id = String.valueOf(bangumi.optInt("season_id"));
        bangumi_cover = bangumi.has("new_ep") ? LruCacheUtil.getImageUrl(bangumi.optJSONObject("new_ep").optString("cover")) : "";
        bangumi_play = DataProcessUtil.getView(bangumi.has("stat") ? bangumi.optJSONObject("stat").optInt("view") : 0);
        bangumi_follow = DataProcessUtil.getView(bangumi.has("stat") ? bangumi.optJSONObject("stat").optInt("follow") : 0);
        bangumi_new = bangumi.has("new_ep") ? bangumi.optJSONObject("new_ep").optString("index_show") : "";
        bangumi_score = bangumi.has("rating") ? String.valueOf(bangumi.optJSONObject("rating").optDouble("score")) : "";
    }
}
