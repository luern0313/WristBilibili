package cn.luern0313.wristbilibili.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.widget.LinearLayout;

import cn.luern0313.wristbilibili.R;

/**
 * Created by liupe on 2018/11/11.
 */

public class FollowmeActivity extends Activity
{
    Context ctx;

    CardView cardView;
    LinearLayout likeView;
    LinearLayout coinView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followme);
        ctx = this;

        cardView = findViewById(R.id.fme_card);
        likeView = findViewById(R.id.fme_like);
        coinView = findViewById(R.id.fme_coin);


    }
}
