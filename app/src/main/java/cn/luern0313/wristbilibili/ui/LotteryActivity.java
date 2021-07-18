package cn.luern0313.wristbilibili.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.fragment.LotteryFragment;
import cn.luern0313.wristbilibili.widget.TitleView;

public class LotteryActivity extends BaseActivity implements TitleView.TitleViewListener
{
    public static final String ARG_LOTTERY_ID = "id";

    private Context ctx;
    private Intent intent;

    private TitleView titleView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lottery);
        ctx = this;
        intent = getIntent();

        titleView = findViewById(R.id.lottery_title);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.lottery_frame, LotteryFragment.newInstance(intent.getStringExtra(ARG_LOTTERY_ID)));
        transaction.commit();
    }

    @Override
    public boolean hideTitle()
    {
        return titleView.hide();
    }

    @Override
    public boolean showTitle()
    {
        return titleView.show();
    }
}