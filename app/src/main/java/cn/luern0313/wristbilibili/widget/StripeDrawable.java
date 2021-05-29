package cn.luern0313.wristbilibili.widget;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.MyApplication;
import cn.luern0313.wristbilibili.util.ThemeUtil;

/**
 * 被 luern0313 创建于 2021/3/6.
 */

public class StripeDrawable extends Drawable implements Animatable, Drawable.Callback
{
    private final int STRIPE_WIDTH = DataProcessUtil.dip2px(14);
    private final int SAFE_AREA = DataProcessUtil.dip2px(100);
    private final int angle = 35;

    private int width;
    private int height;
    private int startOffset;
    private int endOffset;

    private final ValueAnimator valueAnimator;

    private final Paint paint;

    public StripeDrawable()
    {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(STRIPE_WIDTH);
        paint.setColor(MyApplication.getContext().getResources().getColor(R.color.black));
        paint.setAlpha(!ThemeUtil.getCurrentTheme().isDarkTheme() ? 30 : 50);
        paint.setStrokeCap(Paint.Cap.SQUARE);

        valueAnimator = ValueAnimator.ofFloat(0, STRIPE_WIDTH * 2);
        valueAnimator.setDuration(1500);
        valueAnimator.addUpdateListener(animation -> {
            startOffset = ((Float) animation.getAnimatedValue()).intValue();
            invalidateSelf();
        });
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
    }

    @Override
    public void draw(@NonNull Canvas canvas)
    {
        for(int i = -SAFE_AREA + startOffset; i < width + SAFE_AREA; i += STRIPE_WIDTH * 2)
            canvas.drawLine(i, 0, i + endOffset, height, paint);
    }

    @Override
    protected void onBoundsChange(Rect bounds)
    {
        super.onBoundsChange(bounds);
        if (isRunning())
            stop();
        width = bounds.width();
        height = bounds.height();
        endOffset = (int) (Math.tan(Math.toRadians(angle)) * height);
        start();
    }

    @Override
    public void setAlpha(int alpha)
    {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter)
    {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity()
    {
        return PixelFormat.TRANSPARENT;
    }

    @Override
    public void start()
    {
        valueAnimator.start();
    }

    @Override
    public void stop()
    {
        valueAnimator.end();
    }

    @Override
    public boolean isRunning()
    {
        return valueAnimator.isRunning();
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable who)
    {
        final Callback callback = getCallback();
        if(callback != null)
        {
            callback.invalidateDrawable(this);
        }
    }

    @Override
    public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when)
    {
        final Callback callback = getCallback();
        if(callback != null)
        {
            callback.scheduleDrawable(this, what, when);
        }
    }

    @Override
    public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what)
    {
        final Callback callback = getCallback();
        if(callback != null)
        {
            callback.unscheduleDrawable(this, what);
        }
    }
}
