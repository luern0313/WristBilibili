package cn.luern0313.wristbilibili.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.liulishuo.filedownloader.FileDownloader;

import java.io.IOException;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.StatisticsApi;
import cn.luern0313.wristbilibili.fragment.AnimationTimelineFragment;
import cn.luern0313.wristbilibili.fragment.DownloadFragment;
import cn.luern0313.wristbilibili.fragment.DynamicFragment;
import cn.luern0313.wristbilibili.fragment.FavorBoxFragment;
import cn.luern0313.wristbilibili.fragment.HistoryFragment;
import cn.luern0313.wristbilibili.fragment.RankingFragment;
import cn.luern0313.wristbilibili.fragment.RecommendFragment;
import cn.luern0313.wristbilibili.fragment.SearchFragment;
import cn.luern0313.wristbilibili.fragment.SettingFragment;
import cn.luern0313.wristbilibili.fragment.WatchlaterFragment;
import cn.luern0313.wristbilibili.service.DownloadService;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;

/**
 * Created by liupe on 2018/10/25.
 * fragment.....
 */

public class MainActivity extends AppCompatActivity
{
    Context ctx;

    Intent serviceIntent;
    private FragmentManager fm;
    private FragmentTransaction transaction;
    DisplayMetrics dm;

    TextView titleText;
    ImageView titleImg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ctx = this;

        dm = getResources().getDisplayMetrics();
        fm = getSupportFragmentManager();
        transaction = fm.beginTransaction();
        transaction.replace(R.id.main_frame, DynamicFragment.newInstance(true, SharedPreferencesUtil.getString(SharedPreferencesUtil.mid, "")));
        transaction.commit();

        titleText = findViewById(R.id.main_title_title);
        titleImg = findViewById(R.id.main_title_extraicon);

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
                intent.putExtra("text", getResources().getString(R.string.update));
                startActivity(intent);
            }
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }

        if(SharedPreferencesUtil.contains(SharedPreferencesUtil.mid) && (!SharedPreferencesUtil.getString(SharedPreferencesUtil.mid, "").equals("")))
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        StatisticsApi.Statistics(SharedPreferencesUtil.getString(SharedPreferencesUtil.mid, "no login"));
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        //if((int) (System.currentTimeMillis() / 1000) > 1590940980)
            //finish();

        //startActivity(VideoActivity.getActivityIntent(ctx, "78732000", ""));

        /*Intent intent = new Intent(ctx, ArticleActivity.class);
        intent.putExtra("article_id", "4807000");
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
            fm = getSupportFragmentManager();
            transaction = fm.beginTransaction();
            switch (data.getIntExtra("activity", 0))
            {
                case 1:
                    transaction.replace(R.id.main_frame, DynamicFragment.newInstance(true, SharedPreferencesUtil.getString(SharedPreferencesUtil.mid, "")));
                    titleText.setText(getResources().getString(R.string.menu_dynamic));
                    titleText.setTextSize(14);
                    break;
                case 2:
                    transaction.replace(R.id.main_frame, new RecommendFragment());
                    titleText.setText(getResources().getString(R.string.menu_recommend));
                    titleText.setTextSize(14);
                    break;
                case 3:
                    transaction.replace(R.id.main_frame, new RankingFragment());
                    titleText.setText(getResources().getString(R.string.menu_ranking));
                    titleText.setTextSize(14);
                    break;
                case 4:
                    transaction.replace(R.id.main_frame, new AnimationTimelineFragment());
                    titleText.setText(getResources().getString(R.string.menu_remind));
                    titleText.setTextSize(13);
                    break;
                case 5:
                    transaction.replace(R.id.main_frame, new DownloadFragment());
                    titleText.setText(getResources().getString(R.string.menu_download));
                    titleText.setTextSize(13);
                    break;
                case 6:
                    transaction.replace(R.id.main_frame, new SearchFragment());
                    titleText.setText(getResources().getString(R.string.menu_search));
                    titleText.setTextSize(14);
                    break;
                case 7:
                    transaction.replace(R.id.main_frame, FavorBoxFragment.newInstance(SharedPreferencesUtil.getString(SharedPreferencesUtil.mid, "")));
                    titleText.setText(getResources().getString(R.string.menu_collect));
                    titleText.setTextSize(14);
                    break;
                case 8:
                    transaction.replace(R.id.main_frame, new WatchlaterFragment());
                    titleText.setText(getResources().getString(R.string.menu_watchlater));
                    titleText.setTextSize(13);
                    break;
                case 9:
                    transaction.replace(R.id.main_frame, new HistoryFragment());
                    titleText.setText(getResources().getString(R.string.menu_history));
                    titleText.setTextSize(14);
                    break;
                case 10:
                    transaction.replace(R.id.main_frame, new SettingFragment());
                    titleText.setText(getResources().getString(R.string.menu_setting));
                    titleText.setTextSize(14);
                    break;
            }
            transaction.commit();
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
}
