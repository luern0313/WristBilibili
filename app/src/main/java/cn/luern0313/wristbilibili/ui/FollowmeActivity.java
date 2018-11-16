package cn.luern0313.wristbilibili.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.luern0313.wristbilibili.R;

/**
 * Created by liupe on 2018/11/11.
 * 关注我！投硬币！给好评！
 */

public class FollowmeActivity extends Activity
{
    Context ctx;

    CardView cardView;
    RelativeLayout cardViewLay;
    TextView cardViewText;
    LinearLayout likeView;
    LinearLayout coinView;
    ImageView likeViewImg;
    ImageView coinViewImg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followme);
        ctx = this;

        Toast.makeText(ctx, "这只是一个界面，不会真的关注", Toast.LENGTH_LONG).show();
        cardView = findViewById(R.id.fme_card);
        cardViewLay = findViewById(R.id.fme_card_lay);
        cardViewText = findViewById(R.id.fme_card_button);
        likeView = findViewById(R.id.fme_like);
        coinView = findViewById(R.id.fme_coin);
        likeViewImg = findViewById(R.id.fme_like_img);
        coinViewImg = findViewById(R.id.fme_coin_img);

        cardView.setOnTouchListener(new View.OnTouchListener()
        {

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    cardViewText.setText("已关注");
                    cardViewText.setBackgroundResource(R.drawable.shape_anre_followbgyes);
                }
                return true;
            }
        });

        likeView.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    likeViewImg.setImageResource(R.drawable.icon_like_yes);
                }
                return true;
            }
        });

        coinView.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    coinViewImg.setImageResource(R.drawable.icon_coin_yes);
                }
                return true;
            }
        });
    }
}
