package cn.luern0313.wristbilibili.ui;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import cn.luern0313.wristbilibili.R;

public class OnlinePlayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_play);
        TextView tv = findViewById(R.id.tv_online_play);
        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(this, "参数错误！", Toast.LENGTH_SHORT).show();
            return;
        }
       tv.setText(url);
    }
}
