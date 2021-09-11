package cn.luern0313.wristbilibili.models;

import cn.luern0313.lson.annotation.field.LsonPath;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 被 luern0313 创建于 2021/1/28.
 */

@Getter
@Setter
@ToString
public class BaseModel<Model>
{
    @LsonPath
    private int code = -1;

    @LsonPath
    private boolean status;

    @LsonPath({"message", "msg"})
    private String message;

    @LsonPath({"data", "result"})
    private Model data;

    @LsonPath
    private int ttl;

    public boolean isSuccess()
    {
        return code == 0 || status;
    }
}
