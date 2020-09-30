package cn.luern0313.wristbilibili.util.json;

import java.lang.annotation.Annotation;

import cn.luern0313.lson.Deserialization;
import cn.luern0313.lson.util.DeserializationStringUtil;
import cn.luern0313.lson.util.TypeUtil;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.LruCacheUtil;

/**
 * 被 luern0313 创建于 2020/8/3.
 */

public class LsonAnnotationHandle implements Deserialization.LsonAnnotationListener
{
    @Override
    public Object handleAnnotation(Object value, Annotation annotation, TypeUtil fieldType)
    {
        if(annotation instanceof ImageUrlHandle)
        {
            if(((ImageUrlHandle) annotation).value() == -1)
                return ((DeserializationStringUtil) value).set(LruCacheUtil.getImageUrl(value.toString()));
            else
                return ((DeserializationStringUtil) value).set(LruCacheUtil.getImageUrl(value.toString(), ((ImageUrlHandle) annotation).value()));
        }
        else if(annotation instanceof UrlHandle)
            return ((DeserializationStringUtil) value).set(DataProcessUtil.handleUrl(value.toString()));
        else if(annotation instanceof ViewFormat)
            return ((DeserializationStringUtil) value).set(DataProcessUtil.getView(Integer.parseInt(value.toString())));
        return value;
    }
}
