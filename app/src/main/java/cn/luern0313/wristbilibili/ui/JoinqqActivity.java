package cn.luern0313.wristbilibili.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ExpandableListView;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.JoinQQAdapter;
import cn.luern0313.wristbilibili.util.ViewTouchListener;
import cn.luern0313.wristbilibili.widget.TitleView;

public class JoinQqActivity extends BaseActivity implements TitleView.TitleViewListener
{
    Context ctx;
    LayoutInflater inflater;

    View layoutJoinQQHeader;
    TitleView titleView;
    ExpandableListView expandableListView;
    JoinQQAdapter joinQQAdapter;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joinqq);
        ctx = this;
        inflater = getLayoutInflater();

        layoutJoinQQHeader = inflater.inflate(R.layout.widget_join_qq_header, null);
        titleView = findViewById(R.id.join_title);
        expandableListView = findViewById(R.id.join_listview);
        expandableListView.addHeaderView(layoutJoinQQHeader);
        expandableListView.setOnTouchListener(new ViewTouchListener(expandableListView, this));

        joinQQAdapter = new JoinQQAdapter(inflater, expandableListView);
        expandableListView.setAdapter(joinQQAdapter);
        expandableListView.expandGroup(0);

        expandableListView.setOnGroupExpandListener(groupPosition -> {
            int count = joinQQAdapter.getGroupCount();
            for (int i = 0; i < count; i++)
                if(i != groupPosition)
                    expandableListView.collapseGroup(i);
        });
    }

    public void clickReportBug(View view)
    {
        Intent intent = new Intent(ctx, DynamicDetailActivity.class);
        intent.putExtra("dynamic_id", "439275429539994370");
        startActivity(intent);
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
