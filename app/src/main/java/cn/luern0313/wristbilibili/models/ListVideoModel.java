package cn.luern0313.wristbilibili.models;

import org.json.JSONObject;

import cn.luern0313.wristbilibili.util.DataProcessUtil;

/**
 * 被 luern0313 创建于 2020/2/2.
 */

public class ListVideoModel
{
    public String video_aid;
    public String video_bvid;
    public String video_title;
    public String video_cover;
    public String video_play;
    public String video_danmaku;
    public String video_owner_name;

    public ListVideoModel(JSONObject video, int mode)
    {
        if(mode == 0)
        {
            video_aid = video.optString("aid");
            video_bvid = video.optString("bvid");
            video_title = video.optString("title");
            video_cover = DataProcessUtil.handleUrl(video.optString("pic"));

            JSONObject stat = video.has("stat") ? video.optJSONObject("stat") : new JSONObject();
            video_play = DataProcessUtil.getView(stat.optInt("view"));
            video_danmaku = DataProcessUtil.getView(stat.optInt("danmaku"));

            JSONObject owner = video.has("owner") ? video.optJSONObject("owner") : new JSONObject();
            video_owner_name = owner.optString("name");
        }
        else
        {
            video_aid = video.optString("aid");
            video_bvid = video.optString("bvid");
            video_title = video.optString("title");
            video_cover = DataProcessUtil.handleUrl(video.optString("pic"));

            video_play = DataProcessUtil.getView(video.optInt("play"));
            video_danmaku = DataProcessUtil.getView(video.optInt("video_review"));

            video_owner_name = video.optString("author");
        }
    }
}
