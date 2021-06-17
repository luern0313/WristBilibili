package cn.luern0313.wristbilibili.models.popular;

import java.util.ArrayList;

import cn.luern0313.lson.annotation.field.LsonPath;
import cn.luern0313.wristbilibili.util.json.TimeFormat;
import cn.luern0313.wristbilibili.util.json.ViewFormat;
import lombok.Getter;
import lombok.Setter;

/**
 * 被 luern0313 创建于 2021/3/3.
 */

@Getter
@Setter
public class PopularHistoryModel
{
    @LsonPath
    private ArrayList<PopularHistoryVideoModel> list;

    @LsonPath
    private String explain;

    @Getter
    @Setter
    public static class PopularHistoryVideoModel
    {
        @LsonPath
        private String aid;

        @LsonPath
        private String bvid;

        @LsonPath
        private String title;

        @LsonPath("pic")
        private String img;

        @TimeFormat
        @LsonPath("duration")
        private String time;

        @LsonPath("owner.name")
        private String ownerName;

        @LsonPath("owner.mid")
        private String ownerMid;

        @LsonPath("owner.face")
        private String ownerFace;

        @LsonPath("achievement")
        private String reason;

        @ViewFormat
        @LsonPath("stat.view")
        private String view;

        @ViewFormat
        @LsonPath("stat.danmaku")
        private String danmaku;
    }
}
