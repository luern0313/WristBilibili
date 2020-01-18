package cn.luern0313.wristbilibili.models;

import org.json.JSONObject;

/**
 * 被 luern0313 创建于 2020/1/14.
 */

public class FavorBoxModel
{
    public String title;
    public String count;
    public boolean see;
    public String fid;
    public String id;
    public String img;
    public FavorBoxModel(JSONObject box)
    {
        title = box.optString("name");
        count = String.valueOf(box.optInt("count"));
        see = box.optInt("state") % 2 == 0;
        fid = String.valueOf(box.opt("fav_box"));
        id = String.valueOf(box.opt("fav_box")) + "31";
        img = box.optJSONArray("videos").optJSONObject(0).optString("pic");
    }
}
