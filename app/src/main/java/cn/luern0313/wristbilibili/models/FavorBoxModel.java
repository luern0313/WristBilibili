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
    public FavorBoxModel(JSONObject box)
    {
        title = box.optString("title");
        count = String.valueOf(box.optInt("media_count"));
        see = box.optInt("state") % 2 == 0;
        fid = String.valueOf(box.opt("fid"));
        id = String.valueOf(box.opt("id"));
    }
}
