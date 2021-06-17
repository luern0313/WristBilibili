package cn.luern0313.wristbilibili.ui;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.fragment.ReplyFragment;
import cn.luern0313.wristbilibili.fragment.TailFragment;
import cn.luern0313.wristbilibili.widget.TitleView;

public class TailActivity extends BaseActivity
{
    public final static String[] TAIL_REPLY_ARRAY = new String[]{"506346600911397312", "506347021818194631"};

    private Context ctx;
    private FragmentPagerAdapter pagerAdapter;

    private TitleView titleView;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tail);
        ctx = this;

        titleView = findViewById(R.id.tail_title);
        viewPager = findViewById(R.id.tail_viewpager);

        pagerAdapter = new TailFragmentPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {
            }

            @Override
            public void onPageSelected(int position)
            {
                while(titleView.getDisplayedChild() != position)
                {
                    titleView.show();
                    if(titleView.getDisplayedChild() < position)
                    {
                        titleView.setInAnimation(ctx, R.anim.slide_in_right);
                        titleView.setOutAnimation(ctx, R.anim.slide_out_left);
                        titleView.showNext();
                    }
                    else
                    {
                        titleView.setInAnimation(ctx, android.R.anim.slide_in_left);
                        titleView.setOutAnimation(ctx, android.R.anim.slide_out_right);
                        titleView.showPrevious();
                    }
                }
            }
        });

        viewPager.setAdapter(pagerAdapter);
    }

    private static class TailFragmentPagerAdapter extends FragmentPagerAdapter
    {
        TailFragmentPagerAdapter(@NonNull FragmentManager fm, int behavior)
        {
            super(fm, behavior);
        }

        @Override
        public int getCount()
        {
            return 2;
        }

        @NonNull
        @Override
        public Fragment getItem(int position)
        {
            if(position == 0)
                return TailFragment.newInstance();
            else
                return ReplyFragment.newInstanceForTail();
        }
    }

    public void tailMarket()
    {
        viewPager.setCurrentItem(1, true);
    }
}
