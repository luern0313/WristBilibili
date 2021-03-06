package cn.luern0313.wristbilibili.models;

import java.io.Serializable;

import cn.luern0313.lson.annotation.field.LsonPath;
import cn.luern0313.wristbilibili.util.json.ImageUrlFormat;
import cn.luern0313.wristbilibili.util.json.ViewFormat;
import lombok.Getter;
import lombok.Setter;

/**
 * 被 luern0313 创建于 2020/1/31.
 */

@Getter
@Setter
public class ListBangumiModel implements Serializable
{
    @LsonPath("title")
    private String title;

    @LsonPath("season_id")
    private String seasonId;

    @ImageUrlFormat
    @LsonPath("new_ep.cover")
    private String cover;

    @ViewFormat
    @LsonPath("stat.view")
    private String play;

    @ViewFormat
    @LsonPath("stat.follow")
    private String follow;

    @LsonPath("new_ep.index_show")
    private String newEp;

    @LsonPath("rating.score")
    private String score;
}
