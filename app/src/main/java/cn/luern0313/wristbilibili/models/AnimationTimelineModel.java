package cn.luern0313.wristbilibili.models;

import java.util.ArrayList;

import cn.luern0313.lson.annotation.field.LsonPath;
import cn.luern0313.wristbilibili.util.json.ImageUrlHandle;
import lombok.Getter;
import lombok.Setter;

/**
 * 被 luern0313 创建于 2020/1/15.
 */

@Getter
@Setter
public class AnimationTimelineModel
{
    @LsonPath("date")
    private String date;

    @LsonPath("is_today")
    private int isToday;

    @LsonPath("seasons")
    private ArrayList<AnimationTimelineSeasonModel> seasonModelArrayList;

    @Getter
    @Setter
    public class AnimationTimelineSeasonModel
    {
        @LsonPath("season_id")
        private String seasonId;

        @LsonPath("title")
        private String title;

        @ImageUrlHandle
        @LsonPath("square_cover")
        private String coverUrl;

        @LsonPath("pub_index")
        private String lastEpisode;

        @LsonPath("follow")
        private int isFollow;

        @LsonPath("pub_time")
        private String time;

        public String getDate()
        {
            return date;
        }

        public int getIsToday()
        {
            return isToday;
        }
    }
}
