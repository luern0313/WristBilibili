package cn.luern0313.wristbilibili.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.OnlineVideoApi;

public class PlayerActivity extends Activity
{
    Context ctx;
    Intent intent;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    OnlineVideoApi onlineVideoApi;

    String title;
    String aid;
    String part;
    String cid;
    String url;
    String danmaku;
    Handler handler = new Handler();
    Runnable runnLoading;
    Runnable runnLoadErr;

    ImageView uiLoadingimg;
    TextView uiLoadingtip;
    AnimationDrawable loadingImgAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_player);
        ctx = this;
        intent = getIntent();
        sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        title = intent.getStringExtra("title");
        aid = intent.getStringExtra("aid");
        part = intent.getStringExtra("part");
        cid = intent.getStringExtra("cid");
        onlineVideoApi = new OnlineVideoApi(sharedPreferences.getString("cookies", ""), sharedPreferences.getString("csrf", ""), sharedPreferences.getString("mid", ""), aid, part, cid);

        uiLoadingimg = findViewById(R.id.player_loadingimg);
        uiLoadingtip = findViewById(R.id.player_loadingtip);

        uiLoadingimg.setImageResource(R.drawable.anim_videoloading);
        loadingImgAnim = (AnimationDrawable) uiLoadingimg.getDrawable();
        loadingImgAnim.start();

        runnLoading = new Runnable()
        {
            @Override
            public void run()
            {
                findViewById(R.id.player_loading).setVisibility(View.GONE);

                try
                {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("cn.luern0313.wristvideoplayer", "cn.luern0313.wristvideoplayer.ui.PlayerActivity"));
                    intent.putExtra("mode", 1);
                    intent.putExtra("url", url);
                    intent.putExtra("danmaku", danmaku);
                    intent.putExtra("title", title);
                    startActivityForResult(intent, 0);
                }
                catch(Exception e)
                {
                    try
                    {
                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName("cn.luern0313.wristvideoplayer_free", "cn.luern0313.wristvideoplayer_free.ui.PlayerActivity"));
                        intent.putExtra("mode", 1);
                        intent.putExtra("url", url);
                        intent.putExtra("danmaku", danmaku);
                        intent.putExtra("title", title);
                        startActivityForResult(intent, 0);
                    }
                    catch(Exception ee)
                    {
                        Toast.makeText(ctx, "你没有安装配套视频软件：腕上视频，请先前往应用商店下载！", Toast.LENGTH_LONG).show();
                    }

                }
            }
        };

        runnLoadErr = new Runnable()
        {
            @Override
            public void run()
            {
                uiLoadingtip.setText("视频链接获取失败？！");
            }
        };

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    onlineVideoApi.connectionVideoUrl();
                    url = onlineVideoApi.getVideoUrl();
                    danmaku = onlineVideoApi.getDanmakuUrl();
                    handler.post(runnLoading);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    loadingImgAnim.stop();
                    handler.post(runnLoadErr);
                }
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data)
    {
        if(requestCode == 0 && data != null)
        {
            findViewById(R.id.player_history).setVisibility(View.VISIBLE);
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(!onlineVideoApi.playHistory(data.getIntExtra("time", 0), data.getBooleanExtra("isfin", false)))
                        {
                            Looper.prepare();
                            Toast.makeText(getApplicationContext(), "历史记录同步失败...", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    finally
                    {
                        finish();
                    }
                }
            }).start();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
