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
import cn.luern0313.wristbilibili.util.TitleViewPagerChangeListener;
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

        viewPager.addOnPageChangeListener(new TitleViewPagerChangeListener(ctx, titleView));

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
