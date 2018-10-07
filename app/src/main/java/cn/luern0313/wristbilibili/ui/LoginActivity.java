package cn.luern0313.wristbilibili.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.UserLogin;


public class LoginActivity extends Activity
{
    Context ctx;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    ImageView loginQR;
    UserLogin userLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ctx = this;
        sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        loginQR = findViewById(R.id.login_qr);
        userLogin = new UserLogin();

        //loginQR.setImageBitmap(userLogin.getLoginQR());

        new Thread()
        {
            public void run()
            {
                try
                {
                    loginQR.setImageBitmap(userLogin.getLoginQR());
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }.start();

    }
}
