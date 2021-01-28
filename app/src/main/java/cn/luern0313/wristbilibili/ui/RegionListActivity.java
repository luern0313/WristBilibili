package cn.luern0313.wristbilibili.ui;

import android.content.Context;
import android.os.Bundle;

import cn.luern0313.wristbilibili.R;

public class RegionListActivity extends BaseActivity
{
    Context ctx;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_region_list);
        ctx = this;
    }
}