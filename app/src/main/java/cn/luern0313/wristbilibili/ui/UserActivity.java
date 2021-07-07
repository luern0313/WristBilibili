package cn.luern0313.wristbilibili.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.UserApi;
import cn.luern0313.wristbilibili.fragment.DynamicFragment;
import cn.luern0313.wristbilibili.fragment.FavorBoxFragment;
import cn.luern0313.wristbilibili.fragment.user.UserBangumiFragment;
import cn.luern0313.wristbilibili.fragment.user.UserDetailFragment;
import cn.luern0313.wristbilibili.fragment.user.UserListPeopleFragment;
import cn.luern0313.wristbilibili.fragment.user.UserVideoFragment;
import cn.luern0313.wristbilibili.models.UserModel;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;
import cn.luern0313.wristbilibili.util.TitleViewPagerChangeListener;
import cn.luern0313.wristbilibili.widget.ExceptionHandlerView;
import cn.luern0313.wristbilibili.widget.TitleView;

/**
 * 被 luern0313 创建于 不知道什么时候.
 * 这个部分做的很！不！优！雅！！
 * 该api去获取去解析的直接在activity文件里完成了
 * 现在要拓展功能才知道当初不该偷懒
 *
 * ok现在好多了
 */
public class UserActivity extends BaseActivity implements UserDetailFragment.UserDetailFragmentListener, TitleView.TitleViewListener
{
    Context ctx;
    Intent intent;
    LayoutInflater inflater;
    FragmentPagerAdapter pagerAdapter;
    UserModel userModel;
    UserApi userApi;

    Handler handler = new Handler();
    Runnable runnableUi;

    TitleView uiTitleView;
    ExceptionHandlerView uiExceptionHandlerView;
    ViewPager uiViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        ctx = this;
        intent = getIntent();
        inflater = getLayoutInflater();

        userApi = new UserApi(intent.getStringExtra("mid"));
        uiTitleView = findViewById(R.id.user_title);
        uiExceptionHandlerView = findViewById(R.id.user_exception);
        uiViewPager = findViewById(R.id.user_viewpager);

        runnableUi = () -> {
            uiExceptionHandlerView.hideAllView();
            uiViewPager.setOffscreenPageLimit(userModel.getTab().size() - 1);

            for (int i = 1; i < userModel.getTab().size(); i++)
                uiTitleView.addTitle(userModel.getTab().get(i).get(1));
            uiViewPager.setAdapter(pagerAdapter);
        };

        pagerAdapter = new UserFragmentPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
                                                    SharedPreferencesUtil.getString(SharedPreferencesUtil.mid, ""));

        uiViewPager.addOnPageChangeListener(new TitleViewPagerChangeListener(ctx, uiTitleView));

        new Thread(() -> {
            try
            {
                userModel = userApi.getUserInfo();
                if(userModel != null)
                    handler.post(runnableUi);
                else
                    uiExceptionHandlerView.noData();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                uiExceptionHandlerView.noWeb();
            }
        }).start();
    }

    @Override
    public void onUserDetailFragmentViewClick(int viewId)
    {
        if(viewId == R.id.user_detail_follow)
        {
            new Thread(() -> {
                try
                {
                    if(!userModel.isUserFollow())
                    {
                        String result = userApi.follow();
                        if(result.equals(""))
                        {
                            userModel.setCardFansNum(userModel.getCardFansNum() + 1);
                            userModel.setUserFollow(true);
                            Looper.prepare();
                            Toast.makeText(ctx, getString(R.string.user_follow_follow_toast), Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Looper.prepare();
                            Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    {
                        String result = userApi.unfollow();
                        if(result.equals(""))
                        {
                            userModel.setCardFansNum(userModel.getCardFansNum() - 1);
                            userModel.setUserFollow(false);
                            Looper.prepare();
                            Toast.makeText(ctx, getString(R.string.user_follow_cancel_toast), Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Looper.prepare();
                            Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                catch (IOException e)
                {
                    Looper.prepare();
                    Toast.makeText(ctx, getString(R.string.main_error_unknown), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                finally
                {
                    EventBus.getDefault().post(userModel);
                    Looper.loop();
                }
            }).start();
        }
        else if(viewId == R.id.user_detail_video)
        {
            int position = getPositionFormTab("contribute");
            if(position != -1)
                uiViewPager.setCurrentItem(position, true);
        }
        else if(viewId == R.id.user_detail_bangumi)
        {
            int position = getPositionFormTab("bangumi");
            if(position != -1)
                uiViewPager.setCurrentItem(position, true);
        }
        else if(viewId == R.id.user_detail_favor)
        {
            int position = getPositionFormTab("favorite");
            if(position != -1)
                uiViewPager.setCurrentItem(position, true);
        }
        else if(viewId == R.id.user_detail_howfollow)
        {
            int position = getPositionFormTab("follow");
            if(position != -1)
                uiViewPager.setCurrentItem(position, true);
        }
        else if(viewId == R.id.user_detail_howfans)
        {
            int position = getPositionFormTab("fans");
            if(position != -1)
                uiViewPager.setCurrentItem(position, true);
        }
    }

    private int getPositionFormTab(String tabName)
    {
        for(int i = 0; i < userModel.getTab().size(); i++)
            if(userModel.getTab().get(i).get(0).equals(tabName))
                return i;
        return -1;
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

    class UserFragmentPagerAdapter extends FragmentPagerAdapter
    {
        private final String selfMid;

        UserFragmentPagerAdapter(@NonNull FragmentManager fm, int behavior, String selfMid)
        {
            super(fm, behavior);
            this.selfMid = selfMid;
        }

        @Override
        public int getCount()
        {
            return userModel.getTab().size();
        }

        @NonNull
        @Override
        public Fragment getItem(int position)
        {
            String n = userModel.getTab().get(position).get(0);
            switch (n)
            {
                case "home":
                    return UserDetailFragment.newInstance(userModel);
                case "dynamic":
                    return DynamicFragment.newInstance(false, userModel.getCardMid());
                case "contribute":
                    return UserVideoFragment.newInstance(userModel.getCardMid());
                case "bangumi":
                    return UserBangumiFragment.newInstance(userModel.getCardMid(), 1);
                case "movie":
                    return UserBangumiFragment.newInstance(userModel.getCardMid(), 2);
                case "favorite":
                    return FavorBoxFragment.newInstance(userModel.getCardMid(), userModel.getCardMid().equals(selfMid));
                case "follow":
                    return UserListPeopleFragment.newInstance(userModel.getCardMid(), 0);
                case "fans":
                    return UserListPeopleFragment.newInstance(userModel.getCardMid(), 1);
            }
            return null;
        }
    }
}
