package cn.luern0313.wristbilibili.models;

import cn.luern0313.lson.annotation.field.LsonPath;
import cn.luern0313.wristbilibili.util.json.ImageUrlHandle;
import lombok.Getter;
import lombok.Setter;

/**
 * 被 luern0313 创建于 2020/1/15.
 */

@Getter
@Setter
public class RecommendModel
{
    private int mode = 0;

    @LsonPath("param")
    private String aid;

    @LsonPath("title")
    private String title;

    @ImageUrlHandle
    @LsonPath("cover")
    private String img;

    @LsonPath("cover_right_text")
    private String time;

    @LsonPath("cover_left_icon_1")
    private int data1Icon;

    @LsonPath("cover_left_text_1")
    private String data1Text;

    @LsonPath("cover_left_icon_2")
    private int data2Icon;

    @LsonPath("cover_left_text_2")
    private String data2Text;

    @LsonPath("rcmd_reason")
    private String recommendReason;

    @LsonPath("desc_button.text")
    private String label;

    public RecommendModel()
    {
    }

    public RecommendModel(int mode)
    {
        this.mode = mode;
    }
}
