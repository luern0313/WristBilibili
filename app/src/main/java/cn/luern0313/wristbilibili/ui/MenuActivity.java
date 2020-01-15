package cn.luern0313.wristbilibili.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.UserInfoApi;

public class MenuActivity extends Activity
{
    Context ctx;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    TextView uiUserName;
    TextView uiUserCoin;
    TextView uiUserLV;
    ImageView uiUserHead;
    ImageView uiUserVip;

    Handler handler = new Handler();
    Runnable runnableUi;

    Bitmap head;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        ctx = this;
        sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        uiUserName = findViewById(R.id.menu_username);
        uiUserCoin = findViewById(R.id.menu_usercoin);
        uiUserLV = findViewById(R.id.menu_userlv);
        uiUserHead = findViewById(R.id.menu_useric);
        uiUserVip = findViewById(R.id.menu_uservip);

        uiUserName.setText(sharedPreferences.getString("userName", "你还没登录呢~"));
        uiUserCoin.setText("硬币 : " + sharedPreferences.getString("userCoin", "0"));
        uiUserLV.setText("LV" + String.valueOf(sharedPreferences.getInt("userLV", 0)));
        uiUserVip.setVisibility(sharedPreferences.getBoolean("userVip", false) ? View.VISIBLE : View.GONE);
        try
        {
            uiUserHead.setImageBitmap(BitmapFactory.decodeStream(new FileInputStream(new File(getFilesDir(), "head.png"))));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        intent = new Intent();
        setResult(-1, intent);

        setUserInfo();

    }

    public void setUserInfo()
    {
        if(sharedPreferences.contains("cookies")) //是否登录（←错错错错错错错错！！cookie有时限！！！！）
        {
            final UserInfoApi userInfoApi = new UserInfoApi(sharedPreferences.getString("cookies", ""));
            runnableUi = new Runnable()
            {
                @Override
                public void run()
                {
                    uiUserName.setText(userInfoApi.getUserName());
                    uiUserCoin.setText("硬币 : " + userInfoApi.getUserCoin());
                    uiUserLV.setText("LV" + userInfoApi.getUserLV());
                    uiUserHead.setImageBitmap(head);
                    uiUserVip.setVisibility(userInfoApi.isVip() ? View.VISIBLE : View.GONE);

                    editor.putString("userName", userInfoApi.getUserName());
                    editor.putString("userCoin", userInfoApi.getUserCoin());
                    editor.putInt("userLV", userInfoApi.getUserLV());
                    editor.putBoolean("userVip", userInfoApi.isVip());
                    editor.commit();
                }
            };
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        int stat = userInfoApi.getUserInfo();
                        //0正常，-1网络问题，-2登录过期
                        if(stat == 0)
                        {
                            head = userInfoApi.getUserHead();
                            saveBitmap(head);
                            handler.post(runnableUi);
                        }
                        else if(stat == -2)
                        {
                            Looper.prepare();
                            Toast.makeText(getApplicationContext(), "您的登录信息已过期，请注销后重新登录", Toast.LENGTH_LONG).show();
                            Intent i = new Intent(ctx, LogsoffActivity.class);
                            startActivity(i);
                            Looper.loop();
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    protected void saveBitmap(Bitmap bt)
    {
        File file = new File(this.getFilesDir(), "head.png");
        FileOutputStream out = null;
        try
        {
            out = new FileOutputStream(file);
            bt.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0 && resultCode == 0)
        {
            if(data.getBooleanExtra("isLogin", false))
            {
                uiUserName.setText("登录中...");
                setUserInfo();
            }
        }
    }

    public void buutonUser(View view)  //个人信息/登录
    {
        if(!sharedPreferences.contains("cookies"))//是否登录的验证
        {
            Intent intent = new Intent(ctx, LoginActivity.class);
            startActivityForResult(intent, 0);
            overridePendingTransition(R.anim.anim_activity_in_left, 0);
        }
        else
        {
            Intent intent = new Intent(ctx, OtherUserActivity.class);
            intent.putExtra("mid", sharedPreferences.getString("mid", ""));
            startActivity(intent);
        }
    }

    public void buttonDynamic(View view)
    {
        intent.putExtra("activity", 1);
        setResult(0, intent);
        finish();
        overridePendingTransition(0, R.anim.anim_activity_out_up);
    }

    public void buttonRanking(View view)
    {
        intent.putExtra("activity", 2);
        setResult(0, intent);
        finish();
        overridePendingTransition(0, R.anim.anim_activity_out_up);
    }

    public void buttonRemind(View view)
    {
        intent.putExtra("activity", 3);
        setResult(0, intent);
        finish();
        overridePendingTransition(0, R.anim.anim_activity_out_up);
    }

    public void buttonDL(View view)
    {
        intent.putExtra("activity", 4);
        setResult(0, intent);
        finish();
        overridePendingTransition(0, R.anim.anim_activity_out_up);
    }

    public void buttonSearch(View view)
    {
        intent.putExtra("activity", 5);
        setResult(0, intent);
        finish();
        overridePendingTransition(0, R.anim.anim_activity_out_up);
    }

    public void buttonCollect(View view)
    {
        intent.putExtra("activity", 6);
        setResult(0, intent);
        finish();
        overridePendingTransition(0, R.anim.anim_activity_out_up);
    }

    public void buttonWatchlater(View view)
    {
        intent.putExtra("activity", 7);
        setResult(0, intent);
        finish();
        overridePendingTransition(0, R.anim.anim_activity_out_up);
    }

    public void buttonSetting(View view)
    {
        intent.putExtra("activity", 8);
        setResult(0, intent);
        finish();
        overridePendingTransition(0, R.anim.anim_activity_out_up);
    }
}
