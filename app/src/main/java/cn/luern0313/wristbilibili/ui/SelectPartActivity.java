package cn.luern0313.wristbilibili.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.SelectPartAdapter;
import cn.luern0313.wristbilibili.util.ListViewTouchListener;
import cn.luern0313.wristbilibili.widget.TitleView;

public class SelectPartActivity extends BaseActivity implements TitleView.TitleViewListener
{
    Context ctx;
    Intent inIntent;
    LayoutInflater inflater;
    SelectPartAdapter.SelectPartListener selectPartListener;

    String title;
    String tip;
    String[] options;
    String[] optionsId;

    TitleView titleView;
    ListView listView;
    View selectPartTipView;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_part);
        ctx = this;
        inIntent = getIntent();
        inflater = getLayoutInflater();

        setResult(-1, inIntent);

        title = inIntent.hasExtra("title") ? inIntent.getStringExtra("title") : getString(R.string.select_title);
        tip = inIntent.getStringExtra("tip");
        options = inIntent.hasExtra("options_name") ? inIntent.getStringArrayExtra("options_name") : new String[0];
        optionsId = inIntent.hasExtra("options_id") ? inIntent.getStringArrayExtra("options_id") : new String[0];

        titleView = findViewById(R.id.sp_title);
        titleView.setTitle(title);
        listView = findViewById(R.id.sp_list);
        if(tip != null)
        {
            selectPartTipView = inflater.inflate(R.layout.widget_select_part_tip, null);
            ((TextView) selectPartTipView.findViewById(R.id.sp_tip_text)).setText(tip);
            listView.addHeaderView(selectPartTipView, null, false);
            listView.setHeaderDividersEnabled(false);
        }

        selectPartListener = this::onClick;

        listView.setOnTouchListener(new ListViewTouchListener(listView, (TitleView.TitleViewListener) ctx));

        SelectPartAdapter mAdapter = new SelectPartAdapter(getLayoutInflater(), options, selectPartListener);

        listView.setAdapter(mAdapter);
    }

    private void onClick(int id, int position)
    {
        if(optionsId.length > position)
        {
            inIntent.putExtra("option_id", optionsId[position]);
            inIntent.putExtra("option_position", position);
            inIntent.putExtra("option_name", options[position]);
            setResult(0, inIntent);
            finish();
        }
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
