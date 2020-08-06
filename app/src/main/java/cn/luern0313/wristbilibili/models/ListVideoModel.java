package cn.luern0313.wristbilibili.models;

import cn.luern0313.lson.annotation.LsonPath;
import cn.luern0313.wristbilibili.util.json.ImageUrlHandle;
import cn.luern0313.wristbilibili.util.json.UrlHandle;
import cn.luern0313.wristbilibili.util.json.ViewFormat;
import lombok.Getter;
import lombok.Setter;

/**
 * 被 luern0313 创建于 2020/2/2.
 */

@Getter
@Setter
public class ListVideoModel
{
    @LsonPath("aid")
    private String videoAid;

    @LsonPath("bvid")
    private String videoBvid;

    @LsonPath("title")
    private String videoTitle;

    @UrlHandle
    @ImageUrlHandle
    @LsonPath("pic")
    private String videoCover;

    @ViewFormat
    @LsonPath({"stat.view", "play"})
    private String videoPlay;

    @ViewFormat
    @LsonPath({"stat.danmaku", "video_review"})
    private String videoDanmaku;

    @LsonPath({"owner.name", "author"})
    private String videoOwnerName;
}
