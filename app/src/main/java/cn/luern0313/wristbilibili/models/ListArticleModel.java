package cn.luern0313.wristbilibili.models;

import cn.luern0313.lson.annotation.LsonDateFormat;
import cn.luern0313.lson.annotation.LsonPath;
import cn.luern0313.wristbilibili.util.json.ImageUrlHandle;
import cn.luern0313.wristbilibili.util.json.ViewFormat;
import lombok.Getter;
import lombok.Setter;

/**
 * 被 luern0313 创建于 2020/6/24.
 */

@Getter
@Setter
public class ListArticleModel
{
    @LsonPath("id")
    private String id;

    @LsonPath("title")
    private String title;

    @ImageUrlHandle
    @LsonPath("image_urls")
    private String[] img;

    @LsonPath("summary")
    private String desc;

    @LsonPath("author.name")
    private String up;

    @ViewFormat
    @LsonPath("stats.view")
    private String view;

    @LsonDateFormat("MM-dd HH:mm")
    @LsonPath("publish_time")
    private String time;
}
