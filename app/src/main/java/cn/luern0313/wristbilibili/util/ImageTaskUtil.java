package cn.luern0313.wristbilibili.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;

/**
 * 被 luern0313 创建于 2020/1/14.
 */

public class ImageTaskUtil extends AsyncTask<String, Void, BitmapDrawable>
{
    private String imageUrl;
    private View view;
    private Resources listViewResources;
    private LruCache<String, BitmapDrawable> mImageCache;


    public ImageTaskUtil(View view, LruCache<String, BitmapDrawable> mImageCache)
    {
        super();
        this.view = view;
        this.listViewResources = view.getResources();
        this.mImageCache = mImageCache;
    }

    @Override
    protected BitmapDrawable doInBackground(String... params)
    {
        try
        {
            imageUrl = params[0];
            Bitmap bitmap = null;
            bitmap = ImageDownloaderUtil.downloadImage(imageUrl);
            BitmapDrawable db = new BitmapDrawable(listViewResources, bitmap);
            // 如果本地还没缓存该图片，就缓存
            if(mImageCache.get(imageUrl) == null && bitmap != null)
            {
                mImageCache.put(imageUrl, db);
            }
            return db;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(BitmapDrawable result)
    {
        // 通过Tag找到我们需要的ImageView，如果该ImageView所在的item已被移出页面，就会直接返回null
        ImageView iv = view.findViewWithTag(imageUrl);
        if(iv != null && result != null)
        {
            iv.setImageDrawable(result);
        }
    }
}
