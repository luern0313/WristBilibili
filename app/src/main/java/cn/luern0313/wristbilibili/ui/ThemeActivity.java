package cn.luern0313.wristbilibili.ui;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.ThemeAdapter;
import cn.luern0313.wristbilibili.util.ThemeUtil;

public class ThemeActivity extends AppCompatActivity
{
    private Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        ThemeUtil.changeTheme(this, ThemeUtil.getCurrentTheme());
        setContentView(R.layout.activity_theme);

        ctx = this;

        ThemeAdapter.ThemeAdapterListener themeAdapterListener = new ThemeAdapter.ThemeAdapterListener()
        {
            @Override
            public void onAnimate(View view, String name, int from, int to)
            {
                animate(view, name, from, to);
            }

            @Override
            public void onChangeTheme(ViewGroup group, int primary, int fore)
            {
                changeTheme(group, primary, fore);
            }
        };

        final RecyclerView recyclerView = findViewById(R.id.theme_list);

        ThemeAdapter themeAdapter = new ThemeAdapter(ctx, getLayoutInflater(), themeAdapterListener, recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(themeAdapter);
    }

    private void changeTheme(ViewGroup group, @ColorInt int primary, @ColorInt int fore)
    {
        int count = group.getChildCount();
        for (int i = 0; i < count; i++)
        {
            View v = group.getChildAt(i);
            if(v instanceof ViewGroup)
            {
                changeTheme((ViewGroup) v, primary, fore);
            }
            if(v.getId() == R.id.theme_title_layout)
            {
                animate(v, "backgroundColor", ((ColorDrawable) v.getBackground()).getColor(), primary);
            }
            else if (v.getId() == R.id.theme_item_name)
            {
                //noinspection ConstantConditions
                animate(v, "textColor", ((TextView) v).getTextColors().getDefaultColor(), fore);
            }
            else if (v.getId() == R.id.theme_item_check)
            {
                //noinspection ConstantConditions
                ((ImageView) v).getDrawable().applyTheme(getTheme());
            }
        }
    }

    private void animate(View view, String name, @ColorInt int from, @ColorInt int to)
    {
        ObjectAnimator animator = ObjectAnimator.ofArgb(view, name, from, to);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(500);
        animator.start();
    }

    @Override
    public void finish()
    {
        super.finish();
        AppCompatDelegate.setDefaultNightMode(ThemeUtil.getCurrentTheme().isDarkTheme() ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }
}
