package cn.luern0313.wristbilibili.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.text.Html;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import cn.luern0313.wristbilibili.R;

/**
 * 被 luern0313 创建于 2020/2/25.
 */
public class ReplyHtmlImageHandlerUtil implements Html.ImageGetter
{
    private Context ctx;
    private TextView container;
    private HashMap<String, Integer> emoteSize;

    public ReplyHtmlImageHandlerUtil(TextView text, HashMap<String, Integer> emoteSize)
    {
        this.ctx = MyApplication.getContext();
        this.container = text;
        this.emoteSize = emoteSize;
    }

    @Override
    public Drawable getDrawable(String source)
    {
        if(source == null) return null;

        if(isNumericZidai(source))
        {
            Drawable drawable = ResourcesCompat.getDrawable(ctx.getResources(), Integer.parseInt(source), null);
            drawable.setBounds(0, 0, (int)(1f * DataProcessUtil.sp2px(14) * drawable.getIntrinsicWidth() / drawable.getIntrinsicHeight()), DataProcessUtil.sp2px(14));
            return drawable;
        }
        else
        {
            if(source.indexOf("//") == 0) source = "http:" + source;
            if(source.endsWith(".webp")) source = source.substring(0, source.lastIndexOf("@"));

            if(LruCacheUtil.getLruCache().get(source) != null)
                return LruCacheUtil.getLruCache().get(source);
            else
            {
                final LevelListDrawable drawable = new LevelListDrawable();
                final String finalSource = source;
                int size = emoteSize.containsKey(source) ? emoteSize.get(source) : 1;

                drawable.addLevel(0, 0, ResourcesCompat.getDrawable(container.getResources(), R.drawable.img_default_article_img, null));
                drawable.setBounds(0, 0, size * DataProcessUtil.sp2px(18), size * DataProcessUtil.sp2px(18));

                Glide.with(ctx).asBitmap().load(source).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).placeholder(R.drawable.img_default_article_img).into(
                        new SimpleTarget<Bitmap>()
                        {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition)
                            {
                                BitmapDrawable bitmapDrawable = new BitmapDrawable(ctx.getResources(), resource);
                                if(LruCacheUtil.getLruCache().get(finalSource) == null) LruCacheUtil.getLruCache().put(finalSource, bitmapDrawable);

                                drawable.addLevel(1, 1, bitmapDrawable);
                                drawable.setLevel(1);
                                container.invalidate();
                                container.setText(container.getText());
                            }
                        });
                return drawable;
            }
        }
    }

    private static boolean isNumericZidai(String str)
    {
        for (int i = 0; i < str.length(); i++)
        {
            if (!Character.isDigit(str.charAt(i)))
                return false;
        }
        return true;
    }
}
