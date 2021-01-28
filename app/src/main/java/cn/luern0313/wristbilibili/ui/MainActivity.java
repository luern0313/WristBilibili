package cn.luern0313.wristbilibili.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import com.liulishuo.filedownloader.FileDownloader;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.RegionApi;
import cn.luern0313.wristbilibili.api.StatisticsApi;
import cn.luern0313.wristbilibili.fragment.AnimationTimelineFragment;
import cn.luern0313.wristbilibili.fragment.DownloadFragment;
import cn.luern0313.wristbilibili.fragment.DynamicFragment;
import cn.luern0313.wristbilibili.fragment.FavorBoxFragment;
import cn.luern0313.wristbilibili.fragment.HistoryFragment;
import cn.luern0313.wristbilibili.fragment.RankingFragment;
import cn.luern0313.wristbilibili.fragment.RecommendFragment;
import cn.luern0313.wristbilibili.fragment.RegionListFragment;
import cn.luern0313.wristbilibili.fragment.SearchFragment;
import cn.luern0313.wristbilibili.fragment.SettingFragment;
import cn.luern0313.wristbilibili.fragment.WatchlaterFragment;
import cn.luern0313.wristbilibili.service.DownloadService;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;
import cn.luern0313.wristbilibili.widget.TitleView;

/**
 * Created by liupe on 2018/10/25.
 * fragment.....
 */

public class MainActivity extends BaseActivity implements TitleView.TitleViewListener
{
    Context ctx;

    Intent serviceIntent;
    private FragmentManager fm;
    private FragmentTransaction transaction;
    DisplayMetrics dm;

    TitleView titleView;

    private final static Class<?>[] menuFragment = new Class[]{
            DynamicFragment.class, RecommendFragment.class, RankingFragment.class,
            AnimationTimelineFragment.class, RegionListFragment.class, DownloadFragment.class,
            SearchFragment.class, FavorBoxFragment.class, WatchlaterFragment.class,
            HistoryFragment.class, SettingFragment.class
    };
    private final static Object[][] menuFragmentParameter = new Object[][]{
            {true, SharedPreferencesUtil.getString(SharedPreferencesUtil.mid, "")},
            {}, {}, {}, {}, {}, {},
            {SharedPreferencesUtil.getString(SharedPreferencesUtil.mid, ""), true},
            {}, {}, {}
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ctx = this;

        titleView = findViewById(R.id.main_title);
        dm = getResources().getDisplayMetrics();
        fm = getSupportFragmentManager();
        transaction = fm.beginTransaction();
        switchFragment(0);

        FileDownloader.setup(this);

        //新版本更新
        try
        {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), 0);
            if(SharedPreferencesUtil.getInt(SharedPreferencesUtil.ver, 0) < pi.versionCode)
            {
                if(SharedPreferencesUtil.getInt(SharedPreferencesUtil.ver, 0) < 13 && SharedPreferencesUtil.contains(SharedPreferencesUtil.cookies))
                {
                    Toast.makeText(ctx, "抱歉，因为登录功能更新，您需要重新登录，否则某些功能将不可用。", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(ctx, LogsoffActivity.class);
                    startActivity(intent);
                }
                SharedPreferencesUtil.putInt(SharedPreferencesUtil.ver, pi.versionCode);

                if(SharedPreferencesUtil.contains(SharedPreferencesUtil.cookies))
                    startActivity(new Intent(ctx, FollowmeActivity.class));
                Intent intent = new Intent(ctx, TextActivity.class);
                intent.putExtra("title", "更新日志");
                intent.putExtra("text", getString(R.string.update));
                startActivity(intent);
            }
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }

        if(SharedPreferencesUtil.contains(SharedPreferencesUtil.mid) && (!SharedPreferencesUtil.getString(SharedPreferencesUtil.mid, "").equals("")))
        {
            new Thread(() -> {
                try
                {
                    StatisticsApi.Statistics(SharedPreferencesUtil.getString(SharedPreferencesUtil.mid, "no login"));
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }).start();
        }

        //Toast.makeText(ctx, "请注意，你正在使用测试版本", Toast.LENGTH_LONG).show();
        //if((int) (System.currentTimeMillis() / 1000) > 1593791999)
        //    finish();

        //startActivity(VideoActivity.getActivityIntent(ctx, "78732000", ""));
        new Thread(() -> {
            try
            {
                new RegionApi().getRegionList();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }).start();

        /*Intent intent = new Intent(ctx, UserActivity.class);
        intent.putExtra("mid", "476315827");
        startActivity(intent);

        intent = new Intent(ctx, LotteryActivity.class);
        intent.putExtra(LotteryActivity.ARG_LOTTERY_ID, "474964958500946284");
        startActivity(intent);*/

        serviceIntent = new Intent(this, DownloadService.class);
        startService(serviceIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0 && resultCode == 0 && data != null)
        {
            switchFragment(data.getIntExtra("activity", 0));
        }
    }

    private void switchFragment(int choose)
    {
        try
        {
            showTitle();
            fm = getSupportFragmentManager();
            transaction = fm.beginTransaction();
            if(menuFragmentParameter[choose].length > 0)
                transaction.replace(R.id.main_frame, (Fragment) menuFragment[choose].getDeclaredMethod("newInstance", DataProcessUtil.getParameterTypes(menuFragmentParameter[choose])).invoke(null, menuFragmentParameter[choose]));
            else
                transaction.replace(R.id.main_frame, (Fragment) menuFragment[choose].newInstance());
            titleView.setTitle(getResources().getStringArray(R.array.menu_title)[choose]);
            transaction.commit();
        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e)
        {
            e.printStackTrace();
        }
    }

    public void buttonTitle(final View view)
    {
        Intent intent = new Intent(ctx, MenuActivity.class);
        startActivityForResult(intent, 0);
        overridePendingTransition(R.anim.anim_activity_in_down, 0);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        stopService(serviceIntent);
    }

    @Override
    public boolean hideTitle()
    {
        return titleView.hide();
    }

    @Override
    public boolean showTitle()
    {
        return titleView.show();
    }
}
