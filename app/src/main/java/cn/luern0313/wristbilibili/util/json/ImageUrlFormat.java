package cn.luern0313.wristbilibili.util.json;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cn.luern0313.lson.annotation.LsonDefinedAnnotation;
import cn.luern0313.wristbilibili.util.LruCacheUtil;

/**
 * 被 luern0313 创建于 2020/7/31.
 */

@LsonDefinedAnnotation(config = ImageUrlFormat.ImageUrlHandleConfig.class, acceptableDeserializationType = LsonDefinedAnnotation.AcceptableType.STRING, acceptableSerializationType = LsonDefinedAnnotation.AcceptableType.STRING)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ImageUrlFormat
{
    int value() default -1;

    class ImageUrlHandleConfig implements LsonDefinedAnnotation.LsonDefinedAnnotationConfig
    {
        @Override
        public Object deserialization(Object value, Annotation annotation, Object object)
        {
            if(((ImageUrlFormat) annotation).value() == -1)
                return LruCacheUtil.getImageUrl(value.toString());
            else
                return LruCacheUtil.getImageUrl(value.toString(), ((ImageUrlFormat) annotation).value());
        }

        @Override
        public Object serialization(Object value, Annotation annotation, Object object)
        {
            return value;
        }
    }
}
