package cn.luern0313.wristbilibili.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import java.io.File;

import cn.luern0313.wristbilibili.R;

public class LogsoffActivity extends AppCompatActivity
{
    Context ctx;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    LinearLayout nologinLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logsoff);

        ctx = this;
        sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        nologinLayout = findViewById(R.id.logs_nologin);

        boolean isLogin = MainActivity.sharedPreferences.contains("cookies");
        if(!isLogin) nologinLayout.setVisibility(View.VISIBLE);
    }

    public void clickLogsoff(View view)
    {
        new File(getFilesDir(), "head.png").delete();
        editor.remove("cookies");
        editor.remove("mid");
        editor.remove("csrf");

        editor.remove("userName");
        editor.remove("userCoin");
        editor.remove("userLV");
        editor.remove("userVip");
        editor.commit();

        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
