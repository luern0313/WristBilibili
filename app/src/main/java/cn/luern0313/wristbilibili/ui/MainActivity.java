package cn.luern0313.wristbilibili.ui;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.fragment.Download;
import cn.luern0313.wristbilibili.fragment.Dynamic;

/**
 * Created by liupe on 2018/10/25.
 */

public class MainActivity extends Activity
{
    Context ctx;
    public static SharedPreferences sharedPreferences;
    public static SharedPreferences.Editor editor;
    private FragmentManager fm;
    private FragmentTransaction transaction;
    DisplayMetrics dm;

    TextView titleText;
    ImageView titleImg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ctx = this;
        sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        dm = getResources().getDisplayMetrics();
        fm = getFragmentManager();
        transaction = fm.beginTransaction();
        transaction.replace(R.id.main_frame, new Dynamic());
        transaction.commit();

        titleText = findViewById(R.id.main_title_title);
        titleImg = findViewById(R.id.main_title_extraicon);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0 && resultCode == 0 && data != null)
        {
            fm = getFragmentManager();
            transaction = fm.beginTransaction();
            switch (data.getIntExtra("activity", 0))
            {
                case 1:
                    transaction.replace(R.id.main_frame, new Dynamic());
                    titleText.setText("动态");
                    titleText.setTextSize(14);
                    break;
                case 2:
                    transaction.replace(R.id.main_frame, new Dynamic());
                    titleText.setText("番剧提醒");
                    titleText.setTextSize(13);
                    break;
                case 3:
                    transaction.replace(R.id.main_frame, new Download());
                    titleText.setText("离线缓存");
                    titleText.setTextSize(13);
                    break;
            }
            transaction.commit();
        }
    }

    public void buttonTitle(final View view)
    {
        /*titleText.setVisibility(View.GONE);
        titleImg.setVisibility(View.GONE);

        final int viewHeight = view.getLayoutParams().height;

        ValueAnimator anim = ValueAnimator.ofInt(viewHeight, dm.heightPixels);
        anim.setDuration(500);
        anim.setRepeatCount(0);

        anim.addListener(new Animator.AnimatorListener()
        {
            @Override public void onAnimationStart(Animator animation) {}
            @Override public void onAnimationCancel(Animator animation) {}
            @Override public void onAnimationRepeat(Animator animation) {}
            @Override
            public void onAnimationEnd(Animator animation)
            {
                titleText.setVisibility(View.VISIBLE);
                titleImg.setVisibility(View.VISIBLE);
                view.getLayoutParams().height = viewHeight;
            }
        });
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                int currentValue = (Integer) animation.getAnimatedValue();
                view.getLayoutParams().height = currentValue;
                view.requestLayout();
            }
        });
        anim.start();*/
        Intent intent = new Intent(ctx, MenuActivity.class);
        startActivityForResult(intent, 0);
        overridePendingTransition(R.anim.anim_activity_in_down, 0);
    }
}
