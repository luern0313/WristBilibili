package cn.luern0313.wristbilibili.util;

import android.graphics.drawable.BitmapDrawable;
import android.util.LruCache;

/**
 * 被 luern0313 创建于 2020/6/23.
 */

public class LruCacheUtil
{
    private static LruCache<String, BitmapDrawable> mImageCache = new LruCache<String, BitmapDrawable>((int) Runtime.getRuntime().maxMemory() / 3)
    {
        @Override
        protected int sizeOf(String key, BitmapDrawable value)
        {
            try
            {
                return value.getBitmap().getByteCount();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return 0;
        }
    };

    public static String getImageUrl(String url)
    {
        return getImageUrl(url, 170);
    }

    public static String getImageUrl(String url, int width)
    {
        if(url == null || url.equals(""))
            return url;
        url = url.replaceFirst("@(?:\\d+?[a-z]+?_)*\\d+?[a-z]+?\\.[a-z]+$", "");
        url += "@" + width + "w_1e_1c." + url.split("\\.")[url.split("\\.").length - 1];
        return url;
    }

    public static LruCache<String, BitmapDrawable> getLruCache()
    {
        return mImageCache;
    }
}
