package cn.luern0313.wristbilibili.models;

import cn.luern0313.lson.annotation.field.LsonPath;
import cn.luern0313.wristbilibili.util.json.ImageUrlFormat;
import lombok.Getter;
import lombok.Setter;

/**
 * 被 luern0313 创建于 2020/1/15.
 */

@Getter
@Setter
public class RankingModel
{
    @LsonPath("goto")
    private String identification;

    @LsonPath("param")
    private String aid;

    @LsonPath("title")
    private String title;

    @ImageUrlFormat
    @LsonPath("cover")
    private String pic;

    @LsonPath("pts")
    private String score;

    @LsonPath("play")
    private int play;

    @LsonPath("danmaku")
    private int danmaku;

    @LsonPath("mid")
    private String mid;

    @LsonPath("name")
    private String name;

    @ImageUrlFormat
    @LsonPath("face")
    private String face;
}
