package cn.luern0313.wristbilibili.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.fragment.SettingFragment;
import cn.luern0313.wristbilibili.models.SettingModel;

public class SettingActivity extends BaseActivity
{
    Context ctx;
    Intent intent;

    public final static String ARG_SETTING_MODEL_LIST = "settingModelListArg";

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ctx = this;
        intent = getIntent();

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.setting_frame, SettingFragment.newInstance((ArrayList<SettingModel>) intent.getSerializableExtra(ARG_SETTING_MODEL_LIST)));
        transaction.commit();
    }
}