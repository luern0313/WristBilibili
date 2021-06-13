package cn.luern0313.wristbilibili.models;

import java.util.ArrayList;

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
    @LsonPath("list[*]")
    private ArrayList<RankingVideoModel> videoModelArrayList;

    @LsonPath
    private String note;

    @Getter
    @Setter
    public static class RankingVideoModel
    {
        @LsonPath("aid")
        private String aid;

        @LsonPath("bvid")
        private String bvid;

        @LsonPath("title")
        private String title;

        @ImageUrlFormat
        @LsonPath("pic")
        private String pic;

        @LsonPath("score")
        private String score;

        @LsonPath("stat.view")
        private int play;

        @LsonPath("stat.danmaku")
        private int danmaku;

        @LsonPath("owner.mid")
        private String mid;

        @LsonPath("owner.name")
        private String name;

        @ImageUrlFormat
        @LsonPath("owner.face")
        private String face;
    }
}
