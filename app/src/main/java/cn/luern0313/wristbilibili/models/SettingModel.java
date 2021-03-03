package cn.luern0313.wristbilibili.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import cn.luern0313.lson.annotation.field.LsonPath;
import lombok.Getter;
import lombok.Setter;

/**
 * 被 luern0313 创建于 2021/2/10.
 */

@Getter
@Setter
public class SettingModel implements Serializable
{
    @LsonPath
    private String name;

    @LsonPath
    private int mode;

    @LsonPath("goto")
    private String gotoClass;

    @LsonPath
    private String spName;

    @LsonPath
    private int limitApi;

    @LsonPath
    private String limitTip;

    @LsonPath
    private HashMap<String, String> parameter;

    @LsonPath
    private ArrayList<SettingModel> sub;
}
