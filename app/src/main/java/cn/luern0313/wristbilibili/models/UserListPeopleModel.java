package cn.luern0313.wristbilibili.models;

import java.io.Serializable;

import cn.luern0313.lson.annotation.LsonDateFormat;
import cn.luern0313.lson.annotation.LsonPath;
import cn.luern0313.wristbilibili.util.json.ImageUrlHandle;
import lombok.Getter;
import lombok.Setter;

/**
 * 被 luern0313 创建于 2020/1/15.
 */

@Getter
@Setter
public class UserListPeopleModel implements Serializable
{
    @LsonPath("mid")
    private String uid;

    @LsonPath("uname")
    private String name;

    @ImageUrlHandle
    @LsonPath("face")
    private String face;

    @LsonPath("sign")
    private String sign;

    @LsonDateFormat("yy-MM-dd HH:mm")
    @LsonPath("mtime")
    private String mtime;

    @LsonPath("official_verify.type")
    private String verifyType;

    @LsonPath("official_verify.desc")
    private String verifyName;

    @LsonPath("vip.viptype")
    private int vip;
}
