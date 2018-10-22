package cn.luern0313.wristbilibili.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.List;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.UserLogin;
import okhttp3.Response;

public class LoginActivity extends Activity
{
    Context ctx;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    ImageView loginQR;
    UserLogin userLogin;
    Bitmap QRImage;

    Handler UIhandler = new Handler();
    Runnable runnableUi;
    Runnable runnableDone;
    Runnable runnableTimeout;
    Thread getLoginThread;

    Boolean stopFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_login);

        ctx = this;
        sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        loginQR = findViewById(R.id.login_qr);
        userLogin = new UserLogin();

        new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    QRImage = userLogin.getLoginQR();
                    UIhandler.post(runnableUi);
                    getLoginThread.start();
                } catch (ConnectException | UnknownHostException e)
                {
                    Looper.prepare();
                    Toast.makeText(ctx, "好像没有网络连接呢...", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                    e.printStackTrace();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }).start();

        runnableUi = new Runnable()
        {
            @Override
            public void run()
            {
                loginQR.setImageBitmap(QRImage);
            }
        };

        runnableDone = new Runnable()
        {
            @Override
            public void run()
            {
                findViewById(R.id.login_qrdone).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.login_tip)).setText("已扫描，请在手机上继续操作");
            }
        };
        runnableTimeout = new Runnable()
        {
            @Override
            public void run()
            {
                findViewById(R.id.login_qrerr).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.login_tip)).setText("二维码失效，请重进登录界面刷新");
            }
        };

        getLoginThread = new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    int times = 0;
                    JSONObject loginJson;
                    while (!stopFlag)//不要停下来啊（指登录）
                    {
                        Response response = userLogin.getLoginState();
                        loginJson = new JSONObject(response.body().string());
                        if(!(Boolean) loginJson.get("status"))
                        {
                            if((int) loginJson.get("data") == -5) UIhandler.post(runnableDone);
                        }
                        else if((Boolean) loginJson.get("status"))
                        {
                            String cookies = "";
                            List<String> cookiesList = response.headers("Set-Cookie");
                            for (int i = 0; i < cookiesList.size(); i++)
                                cookies += cookiesList.get(i).split("; ")[0] + "; ";
                            cookies = cookies.substring(0, cookies.length() - 2);
                            editor.putString("cookies", cookies);
                            editor.commit();
                            stopFlag = true;
                            finish();
                        }
                        Thread.sleep(3000);
                        times++;
                        if(times >= 55)//我的极限是三分钟
                        {
                            stopFlag = true;
                            UIhandler.post(runnableTimeout);
                        }
                    }
                } catch (ConnectException | UnknownHostException e)
                {
                    Looper.prepare();
                    Toast.makeText(ctx, "好像没有网络连接呢...", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                    e.printStackTrace();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        stopFlag = true;
    }
}
