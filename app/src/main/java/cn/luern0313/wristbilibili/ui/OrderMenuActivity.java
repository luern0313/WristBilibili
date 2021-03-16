package cn.luern0313.wristbilibili.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.mobeta.android.dslv.DragSortListView;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.MenuAdapter;
import cn.luern0313.wristbilibili.widget.TitleView;

public class OrderMenuActivity extends BaseActivity
{
    private Context ctx;
    private LayoutInflater inflater;
    private MenuAdapter menuAdapter;

    private DragSortListView.DropListener dropListener;

    private View layoutHeader;
    private View layoutFooter;
    private TitleView titleView;
    private DragSortListView listView;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_menu);
        ctx = this;
        inflater = getLayoutInflater();

        dropListener = (from, to) -> {
            if(from != to)
                menuAdapter.replace(from, to);
        };

        layoutHeader = inflater.inflate(R.layout.widget_order_menu_header, null);
        layoutFooter = inflater.inflate(R.layout.widget_order_menu_footer, null);
        titleView = new TitleView(ctx);
        titleView.setMode(TitleView.MODE_BACK);
        titleView.setTitle(getString(R.string.order_menu_title));
        listView = findViewById(R.id.order_menu_listview);

        layoutFooter.findViewById(R.id.order_menu_footer_button).setOnClickListener(v -> menuAdapter.reset());

        listView.setDropListener(dropListener);
        listView.addHeaderView(titleView);
        listView.addHeaderView(layoutHeader);
        listView.addFooterView(layoutFooter);
        menuAdapter = new MenuAdapter(inflater, MenuActivity.getMenuSort(), 0, false, listView, null);
        listView.setAdapter(menuAdapter);
    }
}