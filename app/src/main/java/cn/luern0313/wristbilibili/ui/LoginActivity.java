package cn.luern0313.wristbilibili.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.List;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.UserLoginApi;
import cn.luern0313.wristbilibili.fragment.AniRemindFragment;
import cn.luern0313.wristbilibili.fragment.DynamicFragment;
import okhttp3.Response;

public class LoginActivity extends Activity
{
    Context ctx;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    TextView uiChangeMode;
    ImageView loginQR;
    UserLoginApi userLoginApi;
    Bitmap QRImage;
    LinearLayout uiPwLayout;

    EditText uiLoginUser;
    EditText uiLoginPw;
    Button uiLoginButton;

    Handler UIhandler = new Handler();
    Handler timeoutHandler = new Handler();
    Runnable runnableUi;
    Runnable runnableDone;
    Runnable runnableTimeout;
    Runnable runnableLoginDone;
    Thread getLoginThread;

    Boolean stopFlag = false;
    int loginMode = 1;//0 pw 1 qr

    Intent reusltIntent = new Intent();

    String err;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_login);

        ctx = this;
        sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        uiChangeMode = findViewById(R.id.login_change_mode);
        loginQR = findViewById(R.id.login_qr);
        uiPwLayout = findViewById(R.id.login_pw);

        uiLoginUser = findViewById(R.id.login_pw_user_input);
        uiLoginPw = findViewById(R.id.login_pw_pw_input);
        uiLoginButton = findViewById(R.id.login_pw_login);
        userLoginApi = new UserLoginApi();

        reusltIntent.putExtra("isLogin", false);
        setResult(0, reusltIntent);

        new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    QRImage = userLoginApi.getLoginQR();
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
                stopFlag = true;
                findViewById(R.id.login_qrerr).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.login_tip)).setText("二维码失效，请重进登录界面刷新");
            }
        };

        runnableLoginDone = new Runnable()
        {
            @Override
            public void run()
            {
                if(err.equals(""))
                {
                    Intent intent1 = new Intent(ctx, FollowmeActivity.class);
                    startActivity(intent1);
                    Intent intent2 = new Intent(ctx, SueActivity.class);
                    startActivity(intent2);
                    reusltIntent.putExtra("isLogin", true);
                    setResult(0, reusltIntent);
                    finish();
                }
                else
                    Toast.makeText(ctx, err, Toast.LENGTH_LONG).show();
            }
        };

        uiChangeMode.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(loginMode == 1)
                {
                    uiPwLayout.setVisibility(View.VISIBLE);
                    uiChangeMode.setText("扫码登录 >");
                    loginMode = 0;
                }
                else
                {
                    uiPwLayout.setVisibility(View.GONE);
                    uiChangeMode.setText("账号密码登录 >");
                    loginMode = 1;
                }
            }
        });

        uiLoginButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(uiLoginUser.getText().toString().equals(""))
                    Toast.makeText(ctx, "用户名为空！", Toast.LENGTH_SHORT).show();
                else if(uiLoginPw.getText().toString().equals(""))
                    Toast.makeText(ctx, "密码为空！", Toast.LENGTH_SHORT).show();
                else
                {
                    login_pw(uiLoginUser.getText().toString(), uiLoginPw.getText().toString());
                }
            }
        });

        getLoginThread = new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    JSONObject loginJson;
                    while (!stopFlag)//不要停下来啊（指登录）
                    {
                        timeoutHandler.postDelayed(runnableTimeout, 170000);
                        Response response = userLoginApi.getLoginState();
                        loginJson = new JSONObject(response.body().string());
                        if(!(Boolean) loginJson.get("status"))
                        {
                            if((int) loginJson.get("data") == -5) UIhandler.post(runnableDone);
                        }
                        else if((Boolean) loginJson.get("status"))
                        {
                            timeoutHandler.removeCallbacks(runnableTimeout);
                            String cookies = "";
                            List<String> cookiesList = response.headers("Set-Cookie");
                            for (int i = 0; i < cookiesList.size(); i++)
                                cookies += cookiesList.get(i).split("; ")[0] + "; ";
                            cookies = cookies.substring(0, cookies.length() - 2);

                            editor.putString("cookies", cookies);
                            editor.putString("mid", getInfoFromCookie("DedeUserID", cookies));
                            editor.putString("csrf", getInfoFromCookie("bili_jct", cookies));
                            editor.commit();
                            stopFlag = true;

                            DynamicFragment.isLogin = true;
                            AniRemindFragment.isLogin = true;

                            Intent intent1 = new Intent(ctx, FollowmeActivity.class);
                            startActivity(intent1);
                            Intent intent2 = new Intent(ctx, SueActivity.class);
                            startActivity(intent2);
                            reusltIntent.putExtra("isLogin", true);
                            setResult(0, reusltIntent);
                            finish();
                            overridePendingTransition(R.anim.anim_activity_out_right, 0);
                        }
                        Thread.sleep(3000);
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

    private void login_pw(final String user, final String pw)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                err = userLoginApi.Login(user, pw);
                UIhandler.post(runnableLoginDone);
            }
        }).start();
    }

    private String getInfoFromCookie(String name, String cookie)
    {
        String[] cookies = cookie.split("; ");
        for(String i : cookies)
        {
            if(i.contains(name + "="))
                return i.substring(name.length() + 1);
        }
        return "";
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        stopFlag = true;
        timeoutHandler.removeCallbacks(runnableTimeout);
    }
}
