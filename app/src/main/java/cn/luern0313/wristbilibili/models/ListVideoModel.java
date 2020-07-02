package cn.luern0313.wristbilibili.models;

import org.json.JSONObject;

import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.LruCacheUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * 被 luern0313 创建于 2020/2/2.
 */

@Getter
@Setter
public class ListVideoModel
{
    private String videoAid;
    private String videoBvid;
    private String videoTitle;
    private String videoCover;
    private String videoPlay;
    private String videoDanmaku;
    private String videoOwnerName;

    public ListVideoModel(JSONObject video, int mode)
    {
        if(mode == 0)
        {
            videoAid = video.optString("aid");
            videoBvid = video.optString("bvid");
            videoTitle = video.optString("title");
            videoCover = LruCacheUtil.getImageUrl(DataProcessUtil.handleUrl(video.optString("pic")));

            JSONObject stat = video.has("stat") ? video.optJSONObject("stat") : new JSONObject();
            videoPlay = DataProcessUtil.getView(stat.optInt("view"));
            videoDanmaku = DataProcessUtil.getView(stat.optInt("danmaku"));

            JSONObject owner = video.has("owner") ? video.optJSONObject("owner") : new JSONObject();
            videoOwnerName = owner.optString("name");
        }
        else
        {
            videoAid = video.optString("aid");
            videoBvid = video.optString("bvid");
            videoTitle = video.optString("title");
            videoCover = LruCacheUtil.getImageUrl(DataProcessUtil.handleUrl(video.optString("pic")));

            videoPlay = DataProcessUtil.getView(video.optInt("play"));
            videoDanmaku = DataProcessUtil.getView(video.optInt("video_review"));

            videoOwnerName = video.optString("author");
        }
    }
}
