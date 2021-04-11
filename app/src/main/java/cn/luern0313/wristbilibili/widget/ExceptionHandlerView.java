package cn.luern0313.wristbilibili.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import cn.luern0313.wristbilibili.R;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

/**
 * 被 luern0313 创建于 2021/3/14.
 */

public class ExceptionHandlerView extends FrameLayout
{
    private final Context ctx;
    private final View rootView;
    private final LinearLayout viewLoading;
    private final LinearLayout viewLoadingProgress;
    private final LinearLayout viewLogin;
    private final LinearLayout viewWeb;
    private final LinearLayout viewData;

    private int waveSwipeRefreshLayoutId;
    private WaveSwipeRefreshLayout waveSwipeRefreshLayout;

    private final Handler handler = new Handler();
    private final Runnable runnableLoadingStart, runnableLoadingEnd, runnableProgressLoadingStart, runnableProgressLoadingEnd, runnableNoLogin, runnableNoWeb, runnableNoData, runnableHideAll;

    public ExceptionHandlerView(Context context)
    {
        this(context, null);
    }

    public ExceptionHandlerView(Context context, @Nullable AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public ExceptionHandlerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        this(context, attrs, defStyleAttr, 0);
    }

    public ExceptionHandlerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        ctx = context;

        rootView = View.inflate(context, R.layout.widget_exception_handler, this);
        viewLoading = rootView.findViewById(R.id.exception_loading);
        viewLoadingProgress = rootView.findViewById(R.id.exception_loading_progress);
        viewLogin = rootView.findViewById(R.id.exception_login);
        viewWeb = rootView.findViewById(R.id.exception_web);
        viewData = rootView.findViewById(R.id.exception_data);

        runnableLoadingStart = this::loadingStart;
        runnableLoadingEnd = this::loadingEnd;
        runnableProgressLoadingStart = this::progressLoadingStart;
        runnableProgressLoadingEnd = this::progressLoadingEnd;
        runnableNoLogin = this::noLogin;
        runnableNoWeb = this::noWeb;
        runnableNoData = this::noData;
        runnableHideAll = this::hideAllView;

        initStyle(context, attrs);
    }

    public void loadingStart()
    {
        if(isMainThread())
        {
            viewLoading.setVisibility(VISIBLE);
            ((AnimationDrawable) ((ImageView) viewLoading.findViewById(R.id.exception_loading_img)).getDrawable()).start();
        }
        else
            handler.post(runnableLoadingStart);
    }

    public void loadingEnd()
    {
        if(isMainThread())
        {
            if(waveSwipeRefreshLayout != null)
                waveSwipeRefreshLayout.setRefreshing(false);
            viewLoading.setVisibility(GONE);
            ((AnimationDrawable) ((ImageView) viewLoading.findViewById(R.id.exception_loading_img)).getDrawable()).stop();
        }
        else
            handler.post(runnableLoadingEnd);
    }

    public void progressLoadingStart()
    {
        if(isMainThread())
            viewLoadingProgress.setVisibility(VISIBLE);
        else
            handler.post(runnableLoadingStart);
    }

    public void progressLoadingEnd()
    {
        if(isMainThread())
        {
            if(waveSwipeRefreshLayout != null)
                waveSwipeRefreshLayout.setRefreshing(false);
            viewLoadingProgress.setVisibility(GONE);
        }
        else
            handler.post(runnableLoadingEnd);
    }

    public void noLogin()
    {
        if(isMainThread())
        {
            hideAllView();
            viewLogin.setVisibility(VISIBLE);
        }
        else
            handler.post(runnableNoLogin);
    }

    public void noWeb()
    {
        if(isMainThread())
        {
            hideAllView();
            viewWeb.setVisibility(VISIBLE);
        }
        else
            handler.post(runnableNoWeb);
    }

    public void noData()
    {
        if(isMainThread())
        {
            hideAllView();
            viewData.setVisibility(VISIBLE);
        }
        else
            handler.post(runnableNoData);
    }

    public void hideAllView()
    {
        if(isMainThread())
        {
            if(waveSwipeRefreshLayout != null)
                waveSwipeRefreshLayout.setRefreshing(false);
            viewLoading.setVisibility(GONE);
            viewLogin.setVisibility(GONE);
            viewWeb.setVisibility(GONE);
            viewData.setVisibility(GONE);
        }
        else
            handler.post(runnableHideAll);
    }

    private boolean isMainThread()
    {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    @Override
    public void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        if(waveSwipeRefreshLayoutId != 0 && ctx instanceof Activity)
            waveSwipeRefreshLayout = ((Activity) ctx).getWindow().findViewById(waveSwipeRefreshLayoutId);
    }

    private void initStyle(Context context, AttributeSet attrs)
    {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExceptionHandlerView);
        waveSwipeRefreshLayoutId = typedArray.getResourceId(R.styleable.ExceptionHandlerView_wave_swipe_refresh_view, 0);
        if(typedArray.getBoolean(R.styleable.ExceptionHandlerView_show_loading, false))
            loadingStart();
        else
            loadingEnd();
        typedArray.recycle();
    }
}
