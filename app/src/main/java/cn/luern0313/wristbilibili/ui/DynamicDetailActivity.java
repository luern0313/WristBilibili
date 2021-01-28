package cn.luern0313.wristbilibili.ui;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.DynamicApi;
import cn.luern0313.wristbilibili.api.ReplyApi;
import cn.luern0313.wristbilibili.fragment.DynamicDetailFragment;
import cn.luern0313.wristbilibili.fragment.ReplyFragment;
import cn.luern0313.wristbilibili.models.DynamicModel;
import cn.luern0313.wristbilibili.util.MyApplication;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;
import cn.luern0313.wristbilibili.widget.TitleView;

public class DynamicDetailActivity extends BaseActivity implements DynamicDetailFragment.DynamicDetailFragmentListener, TitleView.TitleViewListener
{
    Context ctx;
    FragmentPagerAdapter pagerAdapter;
    DynamicApi dynamicApi;
    DynamicModel.DynamicBaseModel dynamicModel;

    TitleView uiTitleView;
    ViewPager uiViewPager;
    ImageView uiLoading;

    Handler handler = new Handler();
    Runnable runnableUi, runnableNoWeb, runnableNothing;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_detail);
        ctx = MyApplication.getContext();
        dynamicApi = new DynamicApi(SharedPreferencesUtil.getString(SharedPreferencesUtil.mid, ""), false);

        uiTitleView = findViewById(R.id.dynamic_detail_title);
        uiViewPager = findViewById(R.id.dynamic_detail_viewpager);
        uiLoading = findViewById(R.id.dynamic_detail_loading_img);

        runnableUi = () -> {
            findViewById(R.id.dynamic_detail_loading).setVisibility(View.GONE);
            findViewById(R.id.dynamic_detail_nothing).setVisibility(View.GONE);
            findViewById(R.id.dynamic_detail_noweb).setVisibility(View.GONE);

            uiViewPager.setAdapter(pagerAdapter);
            if(getIntent().hasExtra("page"))
                uiViewPager.setCurrentItem(Integer.parseInt(getIntent().getStringExtra("page")));
        };

        runnableNoWeb = () -> {
            try
            {
                findViewById(R.id.dynamic_detail_loading).setVisibility(View.GONE);
                findViewById(R.id.dynamic_detail_nothing).setVisibility(View.GONE);
                findViewById(R.id.dynamic_detail_noweb).setVisibility(View.VISIBLE);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        };

        runnableNothing = () -> {
            try
            {
                findViewById(R.id.dynamic_detail_loading).setVisibility(View.GONE);
                findViewById(R.id.dynamic_detail_nothing).setVisibility(View.VISIBLE);
                findViewById(R.id.dynamic_detail_noweb).setVisibility(View.GONE);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        };

        uiLoading.setImageResource(R.drawable.anim_loading);
        AnimationDrawable loadingImgAnim = (AnimationDrawable) uiLoading.getDrawable();
        loadingImgAnim.start();

        pagerAdapter = new DynamicDetailFragmentPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        uiViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
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
                while(uiTitleView.getDisplayedChild() != position)
                {
                    uiTitleView.show();
                    if(uiTitleView.getDisplayedChild() < position)
                    {
                        uiTitleView.setInAnimation(ctx, R.anim.slide_in_right);
                        uiTitleView.setOutAnimation(ctx, R.anim.slide_out_left);
                        uiTitleView.showNext();
                    }
                    else
                    {
                        uiTitleView.setInAnimation(ctx, android.R.anim.slide_in_left);
                        uiTitleView.setOutAnimation(ctx, android.R.anim.slide_out_right);
                        uiTitleView.showPrevious();
                    }
                }
            }
        });

        new Thread(() -> {
            try
            {
                dynamicModel = dynamicApi.getDynamicDetail(getIntent().getStringExtra("dynamic_id"), getIntent().getStringExtra("type"));
                if(dynamicModel != null)
                    handler.post(runnableUi);
                else
                    handler.post(runnableNothing);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                handler.post(runnableNoWeb);
            }
        }).start();
    }

    @Override
    public void onDynamicDetailFragmentOnClick(int viewId)
    {
        if(viewId == R.id.item_dynamic_reply_lay)
        {
            uiViewPager.setCurrentItem(1, true);
        }
        else if(viewId == R.id.item_dynamic_like_lay)
        {
            new Thread(() -> {
                try
                {
                    String result = dynamicApi.likeDynamic(dynamicModel.getCardId(), dynamicModel.isCardUserLike() ? "2" : "1");
                    if(result.equals(""))
                    {
                        dynamicModel.setCardUserLike(!dynamicModel.isCardUserLike());
                        dynamicModel.setCardLikeNum(dynamicModel.getCardLikeNum() + 1);
                        Looper.prepare();
                    }
                    else
                    {
                        Looper.prepare();
                        Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(ctx, getString(R.string.main_error_web), Toast.LENGTH_SHORT).show();
                }
                finally
                {
                    EventBus.getDefault().post(dynamicModel);
                    Looper.loop();
                }
            }).start();
        }
    }

    @Override
    public boolean hideTitle()
    {
        return uiTitleView.hide();
    }

    @Override
    public boolean showTitle()
    {
        return uiTitleView.show();
    }

    private class DynamicDetailFragmentPagerAdapter extends FragmentPagerAdapter
    {
        DynamicDetailFragmentPagerAdapter(@NonNull FragmentManager fm, int behavior)
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
                return DynamicDetailFragment.newInstance(dynamicModel);
            else
                return ReplyFragment.newInstance(dynamicModel.getCardReplyId(), ReplyApi.typeMap.containsKey(dynamicModel.getCardType()) ?
                        ReplyApi.typeMap.get(dynamicModel.getCardType()) : "17", null, 0);
        }
    }
}
