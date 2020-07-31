package cn.luern0313.wristbilibili.ui;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.util.ThemeUtil;

public class ThemeActivity extends BaseActivity
{

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

        setContentView(R.layout.activity_theme);

        RecyclerView rv = findViewById(R.id.theme_list);
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
            public void onBindViewHolder(@NonNull ThemeViewHolder holder, int position)
            {
                holder.nameView.setText(ThemeUtil.themes[position].name);
                holder.colorView.setCardBackgroundColor(getResources().getColor(
                        ThemeUtil.themes[position].previewColor));
                holder.checkView.setVisibility(ThemeUtil.getCurrentThemePos() == position ?
                        View.VISIBLE : View.INVISIBLE);
                final int finalPos = position;
                holder.itemView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        ThemeUtil.changeCurrentTheme(ThemeUtil.themes[finalPos]);
                        ThemeUtil.changeTheme(getApplicationContext(), ThemeUtil.getCurrentTheme());
                        recreate();
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
}
