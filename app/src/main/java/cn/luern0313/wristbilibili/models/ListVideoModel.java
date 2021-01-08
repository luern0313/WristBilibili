package cn.luern0313.wristbilibili.models;

import cn.luern0313.lson.annotation.field.LsonPath;
import cn.luern0313.wristbilibili.util.json.ImageUrlFormat;
import cn.luern0313.wristbilibili.util.json.UrlFormat;
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
    private String aid;

    @LsonPath("bvid")
    private String bvid;

    @LsonPath("title")
    private String title;

    @UrlFormat
    @ImageUrlFormat
    @LsonPath("pic")
    private String cover;

    @ViewFormat
    @LsonPath({"stat.view", "play"})
    private String play;

    @ViewFormat
    @LsonPath({"stat.danmaku", "video_review"})
    private String danmaku;

    @LsonPath({"owner.name", "author"})
    private String ownerName;

    @LsonPath("duration")
    private int duration;

    @LsonPath("progress")
    private int progress;
}
