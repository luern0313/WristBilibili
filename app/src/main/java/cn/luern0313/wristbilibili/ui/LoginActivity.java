package cn.luern0313.wristbilibili.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
    Bitmap QRImage;

    Handler handler = new Handler();
    Runnable runnable;
    Handler UIhandler = new Handler();
    Runnable runnableUi;

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
                    QRImage = userLogin.getLoginQR();
                    UIhandler.post(runnableUi);

                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }.start();

        runnableUi = new Runnable()
        {
            @Override
            public void run()
            {
                //更新imageview
                loginQR.setImageBitmap(QRImage);
                handler.postDelayed(runnable, 3000);
            }
        };

        runnable = new Runnable()
        {
            @Override
            public void run()
            {
                new Thread()
                {
                    public void run()
                    {
                        try
                        {
                            Log.i("bilibili", userLogin.getLoginState());
                        } catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        //要做的事情，这里再次调用此Runnable对象，以实现每一秒实现一次的定时器操作
                        handler.postDelayed(this, 5000);
                    }
                }.start();
            }
        };
    }
}
