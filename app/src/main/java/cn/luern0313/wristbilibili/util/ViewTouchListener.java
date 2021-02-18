package cn.luern0313.wristbilibili.util;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;

import cn.luern0313.wristbilibili.widget.TitleView;

/**
 * 被 luern0313 创建于 2021/1/21.
 */

public class ViewTouchListener implements View.OnTouchListener
{
    private final View view;
    private final TitleView.TitleViewListener titleViewListener;
    private final CustomViewListener customViewListener;
    private float endY;
    private boolean isDown;

    public ViewTouchListener(View view, TitleView.TitleViewListener titleViewListener)
    {
        this(view, titleViewListener, null);
    }

    public ViewTouchListener(View view, TitleView.TitleViewListener titleViewListener, CustomViewListener customViewListener)
    {
        this.view = view;
        this.titleViewListener = titleViewListener;
        this.customViewListener = customViewListener;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                endY = event.getY();
                isDown = true;
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isDown = false;
                break;

            case MotionEvent.ACTION_MOVE:
                float startY = event.getY();
                if(!isDown)
                {
                    endY = startY;
                    isDown = true;
                }
                if((startY - endY) < 0 && (view.canScrollVertically(-1) || view.canScrollVertically(1)))
                {
                    if(titleViewListener != null)
                        titleViewListener.hideTitle();
                    if(customViewListener != null)
                        customViewListener.hide();
                    endY = startY;
                }
                else if((startY - endY) > 0)
                {
                    if(titleViewListener != null)
                        titleViewListener.showTitle();
                    if(customViewListener != null)
                        customViewListener.show();
                    endY = startY;
                }
                break;
        }
        return false;
    }

    public interface CustomViewListener
    {
        void hide();
        void show();
    }
}
