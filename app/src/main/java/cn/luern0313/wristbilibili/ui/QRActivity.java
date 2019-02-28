package cn.luern0313.wristbilibili.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.OnlineWatchApi;
import cn.luern0313.wristbilibili.widget.QRCodeUtil;

public class QRActivity extends Activity
{
    Context ctx;
    ImageView qrImageView;
    OnlineWatchApi onlineWatchApi;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String url;
    Thread getOnlineWatchUrlThread;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        //initData();
        initView();
        /*qrImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(onlineWatchApi.getPlayURL())) {
                    Toast.makeText(QRActivity.this,"等一会再点",Toast.LENGTH_SHORT).show();
                }
                if (onlineWatchApi.getPlayURL().startsWith("###")){
                    new AlertDialog.Builder(QRActivity.this)
                            .setTitle("阿欧，出错了")
                            .setMessage(onlineWatchApi.getPlayURL())
                            .setCancelable(false)
                            .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    QRActivity.this.finish();
                                }
                            })
                            .create().show();
                } else {
                    Log.d("解析结果", onlineWatchApi.getPlayURL());
                    Intent intent = new Intent(QRActivity.this, OnlinePlayActivity.class);
                    intent.putExtra("url", onlineWatchApi.getPlayURL());
                    startActivity(intent);
                    QRActivity.this.finish();
                }
            }
        });*/
    }

    /*初始化基本数据*/
    private void initData()
    {
        ctx = this;
        sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        getOnlineWatchUrlThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                onlineWatchApi = new OnlineWatchApi(sharedPreferences.getString("cookies", ""), sharedPreferences.getString("csrf", ""), sharedPreferences.getString("mid", ""), url);
                Log.d("视频在线播放", onlineWatchApi.toString());
            }
        });
        getOnlineWatchUrlThread.start();
    }

    /*初始化View并绑定控件*/
    private void initView()
    {
        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        qrImageView = findViewById(R.id.qr_qr);
        qrImageView.setImageBitmap(QRCodeUtil.createQRCodeBitmap(url, 96, 96));
    }

}
