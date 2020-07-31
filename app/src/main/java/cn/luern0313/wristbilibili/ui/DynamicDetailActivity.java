package cn.luern0313.wristbilibili.ui;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

public class DynamicDetailActivity extends BaseActivity implements DynamicDetailFragment.DynamicDetailFragmentListener
{
    Context ctx;
    LayoutInflater inflater;
    FragmentPagerAdapter pagerAdapter;
    DynamicApi dynamicApi;
    DynamicModel dynamicModel;

    ViewFlipper uiTitle;
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

        uiTitle = findViewById(R.id.dynamic_detail_title_title);
        uiViewPager = findViewById(R.id.dynamic_detail_viewpager);
        uiLoading = findViewById(R.id.dynamic_detail_loading_img);

        runnableUi = new Runnable()
        {
            @Override
            public void run()
            {
                findViewById(R.id.dynamic_detail_loading).setVisibility(View.GONE);
                findViewById(R.id.dynamic_detail_nothing).setVisibility(View.GONE);
                findViewById(R.id.dynamic_detail_noweb).setVisibility(View.GONE);

                uiViewPager.setAdapter(pagerAdapter);
                if(getIntent().hasExtra("page"))
                    uiViewPager.setCurrentItem(Integer.parseInt(getIntent().getStringExtra("page")));
            }
        };

        runnableNoWeb = new Runnable()
        {
            @Override
            public void run()
            {
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
            }
        };

        runnableNothing = new Runnable()
        {
            @Override
            public void run()
            {
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
                while(uiTitle.getDisplayedChild() != position)
                {
                    if(uiTitle.getDisplayedChild() < position)
                    {
                        uiTitle.setInAnimation(ctx, R.anim.slide_in_right);
                        uiTitle.setOutAnimation(ctx, R.anim.slide_out_left);
                        uiTitle.showNext();
                    }
                    else
                    {
                        uiTitle.setInAnimation(ctx, android.R.anim.slide_in_left);
                        uiTitle.setOutAnimation(ctx, android.R.anim.slide_out_right);
                        uiTitle.showPrevious();
                    }
                }
            }
        });

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    dynamicModel = dynamicApi.getDynamicDetail(getIntent().getStringExtra("dynamic_id"));
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
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
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
                        Toast.makeText(ctx, "操作失败，请检查网络...", Toast.LENGTH_SHORT).show();
                    }
                    finally
                    {
                        EventBus.getDefault().post(dynamicModel);
                        Looper.loop();
                    }
                }
            }).start();
        }
    }

    class DynamicDetailFragmentPagerAdapter extends FragmentPagerAdapter
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
    };
}
