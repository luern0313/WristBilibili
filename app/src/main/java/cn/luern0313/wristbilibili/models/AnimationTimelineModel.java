package cn.luern0313.wristbilibili.models;

import java.util.ArrayList;

import cn.luern0313.lson.annotation.field.LsonBooleanFormatAsNumber;
import cn.luern0313.lson.annotation.field.LsonNumberOperations;
import cn.luern0313.lson.annotation.field.LsonPath;
import cn.luern0313.wristbilibili.util.json.ImageUrlFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 被 luern0313 创建于 2020/1/15.
 */

@Getter
@Setter
@ToString
public class AnimationTimelineModel
{
    @LsonPath
    private String date;

    @LsonBooleanFormatAsNumber(equal = 1)
    @LsonPath
    private boolean isToday;

    @LsonNumberOperations(operator = LsonNumberOperations.Operator.MINUS, number = 1)
    @LsonPath
    private int dayOfWeek;

    @LsonPath("seasons")
    private ArrayList<AnimationTimelineSeasonModel> seasonModelArrayList;

    @Getter
    @Setter
    public class AnimationTimelineSeasonModel
    {
        @LsonPath
        private String seasonId;

        @LsonPath
        private String title;

        @ImageUrlFormat
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

        public boolean isToday()
        {
            return isToday;
        }
    }
}
