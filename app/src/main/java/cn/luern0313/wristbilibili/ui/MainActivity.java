package cn.luern0313.wristbilibili.ui;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.fragment.AniRemind;
import cn.luern0313.wristbilibili.fragment.Download;
import cn.luern0313.wristbilibili.fragment.Dynamic;
import cn.luern0313.wristbilibili.fragment.FavorBox;
import cn.luern0313.wristbilibili.fragment.Search;
import cn.luern0313.wristbilibili.fragment.Setting;
import cn.luern0313.wristbilibili.fragment.Watchlater;
import cn.luern0313.wristbilibili.service.DownloadService;

/**
 * Created by liupe on 2018/10/25.
 * fragment.....
 */

public class MainActivity extends Activity
{
    Context ctx;
    public static SharedPreferences sharedPreferences;
    public static SharedPreferences.Editor editor;
    private FragmentManager fm;
    private FragmentTransaction transaction;
    DisplayMetrics dm;

    TextView titleText;
    ImageView titleImg;

    private DownloadService.MyBinder myBinder;
    private ServiceConnection connection = new ServiceConnection()
    {

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            myBinder = (DownloadService.MyBinder) service;
            //myBinder.startDownload();
        }
    };

    //TODO 稍后再看，一键三连
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ctx = this;
        sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        dm = getResources().getDisplayMetrics();
        fm = getFragmentManager();
        transaction = fm.beginTransaction();
        transaction.replace(R.id.main_frame, new Dynamic());
        transaction.commit();

        titleText = findViewById(R.id.main_title_title);
        titleImg = findViewById(R.id.main_title_extraicon);

        //新版本更新
        try
        {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), 0);
            if(sharedPreferences.getInt("ver", 0) < pi.versionCode)
            {
                editor.putInt("ver", pi.versionCode);
                editor.commit();
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0 && resultCode == 0 && data != null)
        {
            fm = getFragmentManager();
            transaction = fm.beginTransaction();
            switch (data.getIntExtra("activity", 0))
            {
                case 1:
                    transaction.replace(R.id.main_frame, new Dynamic());
                    titleText.setText("动态");
                    titleText.setTextSize(14);
                    break;
                case 2:
                    transaction.replace(R.id.main_frame, new AniRemind());
                    titleText.setText("追番提醒");
                    titleText.setTextSize(13);
                    break;
                case 3:
                    transaction.replace(R.id.main_frame, new Download());
                    titleText.setText("离线缓存");
                    titleText.setTextSize(13);
                    break;
                case 4:
                    transaction.replace(R.id.main_frame, new Search());
                    titleText.setText("搜索");
                    titleText.setTextSize(14);
                    break;
                case 5:
                    transaction.replace(R.id.main_frame, new FavorBox());
                    titleText.setText("收藏");
                    titleText.setTextSize(14);
                    break;
                case 6:
                    transaction.replace(R.id.main_frame, new Watchlater());
                    titleText.setText("稍后再看");
                    titleText.setTextSize(13);
                    break;
                case 7:
                    transaction.replace(R.id.main_frame, new Setting());
                    titleText.setText("设置");
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
}
