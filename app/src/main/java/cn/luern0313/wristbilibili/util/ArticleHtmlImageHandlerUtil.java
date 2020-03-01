package cn.luern0313.wristbilibili.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.text.Html;
import android.util.Log;
import android.util.LruCache;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import cn.luern0313.wristbilibili.R;

/**
 * 被 luern0313 创建于 2020/2/25.
 */
public class ArticleHtmlImageHandlerUtil implements Html.ImageGetter
{
    private Context ctx;
    private TextView container;
    private int width;
    private LruCache<String, BitmapDrawable> lruCache;

    public ArticleHtmlImageHandlerUtil(Context ctx, LruCache<String, BitmapDrawable> lruCache, TextView text, int width)
    {
        this.ctx = ctx;
        this.container = text;
        this.width = width;
        this.lruCache = lruCache;
    }

    @Override
    public Drawable getDrawable(String source)
    {
        if(source == null) return null;
        if(source.indexOf("//") == 0)
            source = "http:" + source;
        if(source.endsWith(".webp"))
            source = source.substring(0, source.lastIndexOf("@"));

        Log.i("bilibili", source);

        if(lruCache.get(source) != null)
            return lruCache.get(source);
        else
        {
            final LevelListDrawable drawable = new LevelListDrawable();
            final String finalSource = source;

            Glide.with(ctx).asBitmap().load(source).placeholder(R.drawable.img_default_article_img).into(new SimpleTarget<Bitmap>()
            {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition)
                {
                    BitmapDrawable bitmapDrawable = new BitmapDrawable(resource);
                    if(lruCache.get(finalSource) == null)
                        lruCache.put(finalSource, bitmapDrawable);

                    float img_width = resource.getWidth();
                    float img_height = resource.getHeight();

                    int left = img_width < width ? (int) ((width - img_width) / 2) : 0;
                    int right = (int) (img_width < width ? (left + img_width) : width);
                    int bottom = (int) (img_width < width ? img_height : img_height / img_width * width);

                    drawable.addLevel(2, 2, bitmapDrawable);
                    drawable.setBounds(left, 0, right, bottom);
                    drawable.setLevel(2);
                    container.invalidate();
                    container.setText(container.getText());
                }

            });
            return drawable;
        }
    }
}
