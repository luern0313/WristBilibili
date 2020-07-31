package cn.luern0313.wristbilibili.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import java.io.File;

import androidx.appcompat.app.AppCompatActivity;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;

public class LogsoffActivity extends BaseActivity
{
    Context ctx;
    LinearLayout nologinLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logsoff);

        ctx = this;
        nologinLayout = findViewById(R.id.logs_nologin);

        boolean isLogin = SharedPreferencesUtil.contains(SharedPreferencesUtil.cookies);
        if(!isLogin) nologinLayout.setVisibility(View.VISIBLE);
    }

    public void clickLogsoff(View view)
    {
        new File(getFilesDir(), "head.png").delete();
        SharedPreferencesUtil.removeValue(SharedPreferencesUtil.cookies);
        SharedPreferencesUtil.removeValue(SharedPreferencesUtil.mid);
        SharedPreferencesUtil.removeValue(SharedPreferencesUtil.csrf);

        SharedPreferencesUtil.removeValue(SharedPreferencesUtil.userName);
        SharedPreferencesUtil.removeValue(SharedPreferencesUtil.userCoin);
        SharedPreferencesUtil.removeValue(SharedPreferencesUtil.userLV);
        SharedPreferencesUtil.removeValue(SharedPreferencesUtil.userVip);

        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
