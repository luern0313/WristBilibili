package cn.luern0313.wristbilibili.util.json;

import java.lang.annotation.Annotation;

import cn.luern0313.lson.LsonUtil;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.LruCacheUtil;

/**
 * 被 luern0313 创建于 2020/8/3.
 */

public class LsonAnnotationHandle implements LsonUtil.LsonAnnotationListener
{
    @Override
    public Object handleAnnotation(Object value, Annotation annotation)
    {
        if(annotation instanceof ImageUrlHandle)
        {
            if(((ImageUrlHandle) annotation).value() == -1)
                return LruCacheUtil.getImageUrl((String) value);
            else
                return LruCacheUtil.getImageUrl((String) value, ((ImageUrlHandle) annotation).value());
        }
        else if(annotation instanceof UrlHandle)
            return DataProcessUtil.handleUrl((String) value);
        else if(annotation instanceof ViewFormat)
            return DataProcessUtil.getView(Integer.parseInt((String) value));
        return null;
    }
}
