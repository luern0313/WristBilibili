package cn.luern0313.wristbilibili.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.fragment.LotteryFragment;

public class LotteryActivity extends BaseActivity
{
    public static final String ARG_LOTTERY_ID = "id";

    Context ctx;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lottery);
        ctx = this;
        intent = getIntent();

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.lottery_frame, LotteryFragment.newInstance(intent.getStringExtra(ARG_LOTTERY_ID)));
        transaction.commit();
    }
}