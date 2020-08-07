package cn.luern0313.wristbilibili.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.OnlineVideoApi;

public class PlayerActivity extends BaseActivity
{
    Context ctx;
    Intent intent;
    OnlineVideoApi onlineVideoApi;

    String title, aid, cid, url;
    int time;
    String[] url_backup;
    String danmaku;
    Handler handler = new Handler();
    Runnable runnableLoading, runnableLoadErr, runnableTimer;

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

        title = intent.getStringExtra("title");
        aid = intent.getStringExtra("aid");
        cid = intent.getStringExtra("cid");
        time = intent.getIntExtra("time", 0);
        onlineVideoApi = new OnlineVideoApi(aid, cid);

        uiLoadingimg = findViewById(R.id.player_loadingimg);
        uiLoadingtip = findViewById(R.id.player_loadingtip);

        uiLoadingimg.setImageResource(R.drawable.anim_videoloading);
        loadingImgAnim = (AnimationDrawable) uiLoadingimg.getDrawable();
        loadingImgAnim.start();

        runnableLoading = new Runnable()
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
                    intent.putExtra("url_backup", url_backup);
                    intent.putExtra("danmaku", danmaku);
                    intent.putExtra("title", title);
                    intent.putExtra("identity_name", getString(R.string.app_name));
                    intent.putExtra("time", time);
                    intent.putExtra("headers", OnlineVideoApi.getPlayerHeaders());
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
                        intent.putExtra("url_backup", url_backup);
                        intent.putExtra("danmaku", danmaku);
                        intent.putExtra("title", title);
                        intent.putExtra("identity_name", getString(R.string.app_name));
                        intent.putExtra("time", time);
                        intent.putExtra("headers", OnlineVideoApi.getPlayerHeaders());
                        startActivityForResult(intent, 0);
                    }
                    catch(Exception ee)
                    {
                        Toast.makeText(ctx, "你没有安装配套视频软件：腕上视频，请先前往应用商店下载！", Toast.LENGTH_LONG).show();
                    }

                }
            }
        };

        runnableLoadErr = new Runnable()
        {
            @Override
            public void run()
            {
                uiLoadingtip.setText("视频链接获取失败？！");
            }
        };

        runnableTimer = new Runnable()
        {
            @Override
            public void run()
            {
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            onlineVideoApi.playHistory(time, false);
                            time += 10;
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }).start();
                handler.postDelayed(this, 10000);
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
                    url_backup = onlineVideoApi.getVideoBackupUrl();
                    danmaku = onlineVideoApi.getDanmakuUrl();
                    handler.post(runnableLoading);
                    handler.post(runnableTimer);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    loadingImgAnim.stop();
                    handler.post(runnableLoadErr);
                }
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
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
                        String result = onlineVideoApi.playHistory(data.getIntExtra("time", 0),
                                                                   data.getBooleanExtra("isfin", false));
                        if(!result.equals(""))
                        {
                            Looper.prepare();
                            Toast.makeText(getApplicationContext(), "历史记录同步失败...", Toast.LENGTH_SHORT).show();
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
    }
}
