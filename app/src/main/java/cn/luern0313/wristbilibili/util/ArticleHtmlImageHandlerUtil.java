package cn.luern0313.wristbilibili.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.text.Html;
import android.util.LruCache;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.models.article.ArticleImageModel;

/**
 * 被 luern0313 创建于 2020/2/25.
 */
public class ArticleHtmlImageHandlerUtil implements Html.ImageGetter
{
    private Context ctx;
    private TextView container;
    private LruCache<String, BitmapDrawable> lruCache;
    private int width;
    private ArticleImageModel articleImageModel;

    public ArticleHtmlImageHandlerUtil(Context ctx, LruCache<String, BitmapDrawable> lruCache, TextView text, int width, ArticleImageModel articleImageModel)
    {
        this.ctx = ctx;
        this.container = text;
        this.lruCache = lruCache;
        this.width = width;
        this.articleImageModel = articleImageModel;
    }

    @Override
    public Drawable getDrawable(String source)
    {
        if(source == null) return null;

        if(source.indexOf("//") == 0)
            source = "http:" + source;
        if(source.endsWith(".webp"))
            source = source.substring(0, source.lastIndexOf("@"));

        if(lruCache.get(source) != null) return lruCache.get(source);
        else {
            final LevelListDrawable drawable = new LevelListDrawable();
            final String finalSource = source;
            int defLeft = articleImageModel.article_image_width < width ? (int) ((width - articleImageModel.article_image_width) * 1.0 / 2) : 0;
            int defRight = articleImageModel.article_image_width < width ? (defLeft + articleImageModel.article_image_width) : width;
            int defBottom = articleImageModel.article_image_width < width ? articleImageModel.article_image_height : (int) (articleImageModel.article_image_height * 1.0 / articleImageModel.article_image_width * width);
            drawable.addLevel(0, 0, container.getResources().getDrawable(R.drawable.img_default_article_img));
            drawable.setBounds(defLeft, 0, defRight, defBottom);
            Glide.with(ctx).asBitmap().load(source).diskCacheStrategy(DiskCacheStrategy.NONE).placeholder(R.drawable.img_default_article_img).into(
                    new SimpleTarget<Bitmap>()
                    {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition)
                        {
                            BitmapDrawable bitmapDrawable = new BitmapDrawable(resource);
                            if(lruCache.get(finalSource) == null) lruCache.put(finalSource, bitmapDrawable);
                            float img_width = resource.getWidth();
                            float img_height = resource.getHeight();
                            int left = img_width < width ? (int) ((width - img_width) / 2) : 0;
                            int right = (int) (img_width < width ? (left + img_width) : width);
                            int bottom = (int) (img_width < width ? img_height : img_height / img_width * width);
                            drawable.addLevel(1, 1, bitmapDrawable);
                            drawable.setBounds(left, 0, right, bottom);
                            drawable.setLevel(1);
                            container.invalidate();
                            container.setText(container.getText());
                        }
                    });
            return drawable;
        }
    }
}
