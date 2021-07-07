package cn.luern0313.wristbilibili.util;

import android.content.Context;

import androidx.viewpager.widget.ViewPager;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.widget.TitleView;

/**
 * 被 luern 创建于 2021/7/4.
 */

public class TitleViewPagerChangeListener implements ViewPager.OnPageChangeListener
{
    Context ctx;
    TitleView uiTitleView;

    public TitleViewPagerChangeListener(Context ctx, TitleView uiTitleView)
    {
        this.ctx = ctx;
        this.uiTitleView = uiTitleView;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

    @Override
    public void onPageScrollStateChanged(int state) {}

    @Override
    public void onPageSelected(int position)
    {
        while(uiTitleView.getDisplayedChild() != position)
        {
            uiTitleView.show();
            if(uiTitleView.getDisplayedChild() < position && uiTitleView.hasNext())
            {
                uiTitleView.setInAnimation(ctx, R.anim.slide_in_right);
                uiTitleView.setOutAnimation(ctx, R.anim.slide_out_left);
                uiTitleView.showNext();
            }
            else if(uiTitleView.getDisplayedChild() > position && uiTitleView.hasPrevious())
            {
                uiTitleView.setInAnimation(ctx, android.R.anim.slide_in_left);
                uiTitleView.setOutAnimation(ctx, android.R.anim.slide_out_right);
                uiTitleView.showPrevious();
            }
        }
    }
}
