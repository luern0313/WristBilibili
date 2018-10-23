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
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.UserInfo;
import cn.luern0313.wristbilibili.widget.CircleImageView;

public class MenuActivity extends Activity
{
    Context ctx;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    TextView uiUserName;
    TextView uiUserCoin;
    TextView uiUserLV;
    CircleImageView uiUserHead;

    Handler handler = new Handler();
    Runnable runnableUi;
    Runnable runnableProg;

    Bitmap head;

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

        uiUserName.setText(sharedPreferences.getString("userName", "你还没登录呢~"));
        uiUserCoin.setText("硬币 : " + String.valueOf(sharedPreferences.getInt("userCoin", 0)));
        uiUserLV.setText("LV" + String.valueOf(sharedPreferences.getInt("userLV", 0)));
        try
        {
            uiUserHead.setImageBitmap(BitmapFactory.decodeStream(new FileInputStream(new File(getFilesDir(), "head.png"))));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        setUserInfo();

    }

    public void setUserInfo()
    {
        if(!sharedPreferences.getString("cookies", "").equals(""))//是否登录
        {
            final UserInfo userInfo = new UserInfo(sharedPreferences.getString("cookies", ""));
            findViewById(R.id.menu_headlod).setVisibility(View.VISIBLE);

            runnableUi = new Runnable()
            {
                @Override
                public void run()
                {
                    uiUserName.setText(userInfo.getUserName());
                    uiUserCoin.setText("硬币 : " + String.valueOf(userInfo.getUserCoin()));
                    uiUserLV.setText("LV" + userInfo.getUserLV());
                    uiUserHead.setImageBitmap(head);
                    findViewById(R.id.menu_headlod).setVisibility(View.GONE);

                    editor.putString("userName", userInfo.getUserName());
                    editor.putInt("userCoin", (int) userInfo.getUserCoin());
                    editor.putInt("userLV", userInfo.getUserLV());
                    editor.commit();
                }
            };
            runnableProg = new Runnable()
            {
                @Override
                public void run()
                {
                    findViewById(R.id.menu_headlod).setVisibility(View.GONE);
                }
            };
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        userInfo.getUserInfo();
                        head = userInfo.getUserHead();
                        saveBitmap(head);
                        handler.post(runnableUi);
                    }
                    catch (IOException e)
                    {
                        handler.post(runnableProg);
                        Looper.prepare();
                        Toast.makeText(ctx, "好像没有网络连接...", Toast.LENGTH_SHORT).show();
                        Looper.loop();
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
            if(data.getBooleanExtra("isLogin", false)) setUserInfo();
        }
    }

    public void buutonUser(View view)  //个人信息/登录
    {
        if(sharedPreferences.getString("cookies", "").equals(""))//是否登录的验证
        {
            Intent intent = new Intent(ctx, LoginActivity.class);
            startActivityForResult(intent, 0);
        }
    }

    public void buttonDL(View view)
    {
        Intent intent = new Intent(ctx, DownloadActivity.class);
        startActivity(intent);
    }
}
