package cn.luern0313.wristbilibili.models;

import org.json.JSONObject;

import cn.luern0313.wristbilibili.util.LruCacheUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * 被 luern0313 创建于 2020/1/14.
 */

@Getter
@Setter
public class FavorBoxModel
{
    private int favorBoxMode;
    private String favorBoxTitle;
    private String favorBoxCount;
    private boolean favorBoxSee;
    private String favorBoxFid;
    private String favorBoxId;
    private String favorBoxImg;
    public FavorBoxModel(JSONObject box)
    {
        favorBoxMode = 0;
        favorBoxTitle = box.optString("name");
        favorBoxCount = String.valueOf(box.optInt("count"));
        favorBoxSee = box.optInt("state") % 2 == 0;
        favorBoxFid = String.valueOf(box.opt("fav_box"));
        favorBoxId = box.opt("fav_box") + "31";
        favorBoxImg = box.has("videos") && box.optJSONArray("videos") != null ?
                LruCacheUtil.getImageUrl(box.optJSONArray("videos").optJSONObject(0).optString("pic")) : "";
    }

    public FavorBoxModel(int mode)
    {
        favorBoxMode = mode;
    }
}
