package cn.luern0313.wristbilibili.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import cn.luern0313.lson.LsonUtil;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.models.BannerModel;
import cn.luern0313.wristbilibili.ui.UnsupportedLinkActivity;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;

/**
 * 被 luern0313 创建于 2021/1/20.
 */

public class BannerView extends FrameLayout
{
    private final Context ctx;
    private final View rootView;
    private BannerModel bannerModel;

    private final RelativeLayout bannerLay;
    private final ImageView bannerImage;
    private final TextView bannerText;

    private final float screenWidth;

    private boolean isHide;
    private boolean showTip;

    public BannerView(Context context)
    {
        this(context, null);
    }

    public BannerView(Context context, @Nullable AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public BannerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        this(context, attrs, defStyleAttr, 0);
    }

    public BannerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        ctx = context;

        rootView = View.inflate(context, R.layout.widget_banner, this);

        bannerLay = rootView.findViewById(R.id.banner_lay);
        bannerImage = rootView.findViewById(R.id.banner_img);
        bannerText = rootView.findViewById(R.id.banner_text);

        bannerText.setSelected(true);

        showTip = SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.bannerTip, true);
        if(showTip)
            rootView.findViewById(R.id.banner_tip).setVisibility(VISIBLE);

        DisplayMetrics outMetrics = new DisplayMetrics();
        ((Activity) ctx).getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        screenWidth = outMetrics.widthPixels;

        bannerImage.setOnLongClickListener(v -> {
            if(showTip)
            {
                rootView.findViewById(R.id.banner_tip).setVisibility(GONE);
                SharedPreferencesUtil.putBoolean(SharedPreferencesUtil.bannerTip, false);
                showTip = false;
            }
            modifyState(true, true);
            return true;
        });

        bannerText.setOnClickListener(v -> {
            modifyState(false, true);
        });

        modifyState(SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.bannerHide, false), false);
        updateBannerConf(LsonUtil.fromJson(LsonUtil.parse(SharedPreferencesUtil.getString(SharedPreferencesUtil.bannerConf, "{}")), BannerModel.class));
    }

    public void updateBannerConf(BannerModel bannerModel)
    {
        this.bannerModel = bannerModel;
        if(bannerModel != null && bannerModel.getCurrentTime() < bannerModel.getBannerEndTime())
        {
            bannerText.setText(bannerModel.getText());
            if(!isHide)
                downloadImage(bannerModel.getImageUrls(), 0, true);
            bannerImage.setOnClickListener(v -> {
                Intent intent = new Intent(ctx, UnsupportedLinkActivity.class);
                intent.putExtra("url", bannerModel.getJumpUrl());
                ctx.startActivity(intent);
            });
        }
        else
        {
            bannerText.setVisibility(GONE);
            bannerImage.setVisibility(GONE);
        }
    }

    private void downloadImage(String[] urls, int index, boolean isSmooth)
    {
        Glide.with(this).asBitmap().diskCacheStrategy(DiskCacheStrategy.DATA).load(urls[index]).into(new SimpleTarget<Bitmap>()
        {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition)
            {
                bannerImage.setImageBitmap(resource);
                bannerText.setVisibility(INVISIBLE);
                bannerImage.setVisibility(VISIBLE);
                if(isSmooth)
                {
                    ValueAnimator valueAnimator = new ValueAnimator();
                    valueAnimator.setValues(PropertyValuesHolder.ofInt("height",  bannerLay.getLayoutParams().height, (int) (screenWidth / resource.getWidth() * resource.getHeight())),
                                            PropertyValuesHolder.ofFloat("alpha", 0f, 1f));
                    valueAnimator.setDuration(300);
                    valueAnimator.addUpdateListener(animation -> {
                        ViewGroup.LayoutParams layoutParams = bannerLay.getLayoutParams();
                        layoutParams.height = (int) animation.getAnimatedValue("height");
                        bannerLay.setLayoutParams(layoutParams);
                        bannerImage.setAlpha((float) animation.getAnimatedValue("alpha"));
                    });
                    valueAnimator.addListener(new AnimatorListenerAdapter()
                    {
                        @Override
                        public void onAnimationEnd(Animator animation)
                        {
                            bannerText.setVisibility(GONE);
                        }
                    });
                    valueAnimator.start();
                }
                else
                {
                    ViewGroup.LayoutParams layoutParams = bannerLay.getLayoutParams();
                    layoutParams.height = (int) (screenWidth / resource.getWidth() * resource.getHeight());
                    bannerLay.setLayoutParams(layoutParams);
                }
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable)
            {
                if(index + 1 < urls.length)
                    downloadImage(urls, index + 1, isSmooth);
            }
        });
    }

    private void modifyState(boolean isHide, boolean isSmooth)
    {
        if(this.isHide != isHide || !isSmooth)
        {
            SharedPreferencesUtil.putBoolean(SharedPreferencesUtil.bannerHide, isHide);
            this.isHide = isHide;
            if(isHide)
            {
                if(isSmooth)
                {
                    ValueAnimator valueAnimator = new ValueAnimator();
                    valueAnimator.setValues(PropertyValuesHolder.ofInt("height", bannerLay.getLayoutParams().height, DataProcessUtil.dip2px(26)),
                                            PropertyValuesHolder.ofFloat("alpha", 0f, 1f));
                    valueAnimator.setDuration(300);
                    valueAnimator.addUpdateListener(animation -> {
                        ViewGroup.LayoutParams layoutParams = bannerLay.getLayoutParams();
                        layoutParams.height = (int) animation.getAnimatedValue("height");
                        bannerLay.setLayoutParams(layoutParams);
                        bannerImage.setAlpha(1f - (float) animation.getAnimatedValue("alpha"));
                        bannerText.setAlpha((float) animation.getAnimatedValue("alpha"));
                    });
                    valueAnimator.addListener(new AnimatorListenerAdapter()
                    {
                        @Override
                        public void onAnimationEnd(Animator animation)
                        {
                            bannerImage.setVisibility(GONE);
                            bannerText.setVisibility(VISIBLE);
                        }
                    });
                    valueAnimator.start();
                }
                else
                {
                    ViewGroup.LayoutParams layoutParams = bannerLay.getLayoutParams();
                    layoutParams.height = DataProcessUtil.dip2px(26);
                    bannerLay.setLayoutParams(layoutParams);
                    bannerImage.setVisibility(GONE);
                    bannerText.setVisibility(VISIBLE);
                }
            }
            else
            {
                if(bannerModel != null)
                    downloadImage(bannerModel.getImageUrls(), 0, isSmooth);
            }
        }
        else
            modifyState(isHide, false);
    }

    private void clearDiskCache()
    {
        if(Looper.myLooper() == Looper.getMainLooper())
            new Thread(() -> Glide.get(ctx).clearDiskCache()).start();
        else
            Glide.get(ctx).clearDiskCache();
    }
}
