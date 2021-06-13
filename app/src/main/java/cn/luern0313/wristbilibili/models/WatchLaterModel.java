package cn.luern0313.wristbilibili.models;

import cn.luern0313.lson.annotation.field.LsonPath;
import cn.luern0313.wristbilibili.util.json.ImageUrlFormat;
import cn.luern0313.wristbilibili.util.json.ViewFormat;
import lombok.Getter;
import lombok.Setter;

/**
 * 被 luern0313 创建于 2020/1/15.
 */

@Getter
@Setter
public class WatchLaterModel
{
    @LsonPath("aid")
    private String aid;

    @LsonPath("title")
    private String title;

    @LsonPath("owner.name")
    private String up;

    @ImageUrlFormat
    @LsonPath("pic")
    private String cover;

    @LsonPath("duration")
    private int duration;

    @LsonPath("progress")
    private int progress;

    @ViewFormat
    @LsonPath("stat.view")
    private String play;

    @ViewFormat
    @LsonPath("stat.danmaku")
    private String danmaku;
}
