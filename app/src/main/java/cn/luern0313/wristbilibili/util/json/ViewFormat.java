package cn.luern0313.wristbilibili.util.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cn.luern0313.lson.LsonDefinedAnnotation;


/**
 * 被 luern0313 创建于 2020/7/31.
 */

@LsonDefinedAnnotation(applyTypeWhiteList = String.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ViewFormat
{

}
