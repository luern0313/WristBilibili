package cn.luern0313.wristbilibili.ui;

import android.animation.ObjectAnimator;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.util.ColorUtil;
import cn.luern0313.wristbilibili.util.ThemeUtil;

public class ThemeActivity extends AppCompatActivity
{
    private @ColorInt int primary;
    private @ColorInt int back;
    private @ColorInt int fore;

    private static class ThemeViewHolder extends RecyclerView.ViewHolder
    {
        TextView nameView;
        CardView colorView;
        View itemView;
        View checkView;

        public ThemeViewHolder(@NonNull View itemView)
        {
            super(itemView);
            this.itemView = itemView;
            nameView = itemView.findViewById(R.id.theme_item_name);
            colorView = itemView.findViewById(R.id.theme_item_icon);
            checkView = itemView.findViewById(R.id.theme_item_check);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        ThemeUtil.changeTheme(this, ThemeUtil.getCurrentTheme());

        setContentView(R.layout.activity_theme);

        final RecyclerView rv = findViewById(R.id.theme_list);
        rv.setLayoutManager(new LinearLayoutManager(this,
                RecyclerView.VERTICAL, false));
        rv.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.VERTICAL));
        rv.setAdapter(new RecyclerView.Adapter<ThemeViewHolder>()
        {
            @NonNull
            @Override
            public ThemeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                return new ThemeViewHolder(getLayoutInflater().inflate(R.layout.item_theme,
                        parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull final ThemeViewHolder holder, int position)
            {
                holder.checkView.setVisibility(ThemeUtil.getCurrentThemePos() == position ?
                        View.VISIBLE : View.INVISIBLE);
                holder.nameView.setText(ThemeUtil.themes[position].name);
                holder.colorView.setCardBackgroundColor(getResources().getColor(
                        ThemeUtil.themes[position].previewColor));
                final int finalPos = position;
                holder.itemView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        @ColorInt int prevBack = ColorUtil.getColor(android.R.attr.colorBackground,
                                R.color.activityBG, rv.getContext());
                        ThemeUtil.changeCurrentTheme(ThemeUtil.themes[finalPos]);
                        ThemeUtil.changeTheme(rv.getContext(),
                                ThemeUtil.getCurrentTheme());
                        @ColorInt int[] colors = ColorUtil.getColors(new int[] {
                                R.attr.colorPrimary,
                                android.R.attr.colorBackground,
                                android.R.attr.textColor
                        }, new int[] {
                                getResources().getColor(R.color.colorPrimary),
                                getResources().getColor(R.color.activityBG),
                                getResources().getColor(R.color.gray_77)
                        }, rv.getContext());
                        primary = colors[0];
                        back = colors[1];
                        fore = colors[2];
                        animate((ViewGroup) findViewById(R.id.theme_root), "backgroundColor",
                                prevBack, back);
                        changeTheme((ViewGroup) findViewById(R.id.theme_root));
                        notifyDataSetChanged();
                    }
                });
            }

            @Override
            public int getItemCount()
            {
                return ThemeUtil.themes.length;
            }
        });
    }

    private void changeTheme(ViewGroup group)
    {
        int count = group.getChildCount();
        for(int i = 0; i < count; i++)
        {
            View v = group.getChildAt(i);
            if(v instanceof ViewGroup)
            {
                changeTheme((ViewGroup) v);
            }
            switch (v.getId())
            {
                case R.id.theme_title_layout:
                    animate(v, "backgroundColor", ((ColorDrawable) v.getBackground()).getColor(), primary);
                    break;
                case R.id.theme_item_name:
                    assert v instanceof TextView;
                    animate(v, "textColor", ((TextView) v).getTextColors().getDefaultColor(), fore);
                    break;
            }
        }
    }

    private void animate(View view, String name, @ColorInt int from, @ColorInt int to)
    {
        ObjectAnimator animator = ObjectAnimator.ofArgb(view, name, from, to);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(300);
        animator.start();
    }
}
