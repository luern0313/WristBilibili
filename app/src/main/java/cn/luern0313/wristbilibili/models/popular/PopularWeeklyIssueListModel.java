package cn.luern0313.wristbilibili.models.popular;

import java.util.ArrayList;

import cn.luern0313.lson.annotation.field.LsonPath;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 被 luern0313 创建于 2021/3/3.
 */

@Getter
@Setter
@ToString
public class PopularWeeklyIssueListModel
{
    @LsonPath
    private ArrayList<PopularWeeklyIssueModel> list;

    @LsonPath("list[*].name")
    private String[] titleList;

    @Getter
    @Setter
    public static class PopularWeeklyIssueModel
    {
        @LsonPath
        private String name;

        @LsonPath
        private int number;

        @LsonPath
        private String subject;
    }
}
