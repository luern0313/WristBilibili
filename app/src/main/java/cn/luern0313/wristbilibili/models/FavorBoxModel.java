package cn.luern0313.wristbilibili.models;

import cn.luern0313.lson.annotation.LsonAddSuffix;
import cn.luern0313.lson.annotation.LsonPath;
import cn.luern0313.wristbilibili.util.json.ImageUrlHandle;
import lombok.Getter;
import lombok.Setter;

/**
 * 被 luern0313 创建于 2020/1/14.
 */

@Getter
@Setter
public class FavorBoxModel
{
    private int mode = 0;

    @LsonPath("name")
    private String title;

    @LsonPath("count")
    private String count;

    @LsonPath("state")
    private int see;

    @LsonPath("fav_box")
    private String fid;

    @LsonAddSuffix("31")
    @LsonPath("fav_box")
    private String id;

    @ImageUrlHandle
    @LsonPath("videos[0].pic")
    private String img;

    public FavorBoxModel() { }

    public FavorBoxModel(int mode)
    {
        this.mode = mode;
    }
}
