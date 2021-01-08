package cn.luern0313.wristbilibili.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.IOException;

import cn.luern0313.wristbilibili.R;

/**
 * 被 luern0313 创建于 2020/1/14.
 */

public class ImageTaskUtil extends AsyncTask<String, Void, BitmapDrawable>
{
    private String imageUrl;
    private final View view;
    private final Resources listViewResources;
    private final int width;
    private final boolean isImgActivity;

    public ImageTaskUtil(View view)
    {
        this(view, 170);
    }

    public ImageTaskUtil(View view, int width)
    {
        this(view, width, false);
    }

    public ImageTaskUtil(View view, int width, boolean isImgActivity)
    {
        super();
        this.view = view;
        this.listViewResources = view.getResources();
        this.width = width;
        this.isImgActivity = isImgActivity;
    }

    @Override
    protected BitmapDrawable doInBackground(String... params)
    {
        try
        {
            imageUrl = params[0];
            Bitmap bitmap = ImageDownloaderUtil.downloadImage(imageUrl, width);
            BitmapDrawable db = new BitmapDrawable(listViewResources, bitmap);
            if(LruCacheUtil.getLruCache().get(imageUrl) == null && bitmap != null)
            {
                LruCacheUtil.getLruCache().put(imageUrl, db);
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
        ImageView iv = null;
        if(!isImgActivity)
           iv = view.findViewWithTag(imageUrl);
        else
        {
            LinearLayout linearLayout = view.findViewWithTag(imageUrl);
            if(linearLayout != null && result != null)
                iv = linearLayout.findViewById(R.id.vp_imageView);
        }
        if(iv != null && result != null)
            iv.setImageDrawable(result);
    }
}
