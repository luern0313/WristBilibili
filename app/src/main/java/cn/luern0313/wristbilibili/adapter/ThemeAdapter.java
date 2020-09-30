package cn.luern0313.wristbilibili.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.util.ColorUtil;
import cn.luern0313.wristbilibili.util.ThemeUtil;

/**
 * 被 luern0313 创建于 2020/8/7.
 */

public class ThemeAdapter extends RecyclerView.Adapter<ThemeAdapter.ThemeViewHolder>
{
    private Context ctx;
    private LayoutInflater layoutInflater;
    private RecyclerView recyclerView;

    private ThemeAdapterListener themeAdapterListener;

    public ThemeAdapter(Context ctx, LayoutInflater layoutInflater, ThemeAdapterListener themeAdapterListener, RecyclerView recyclerView)
    {
        this.ctx = ctx;
        this.layoutInflater = layoutInflater;
        this.themeAdapterListener = themeAdapterListener;
        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public ThemeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        return new ThemeViewHolder(layoutInflater.inflate(R.layout.item_theme, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ThemeViewHolder holder, int position)
    {
        holder.checkView.setVisibility(ThemeUtil.getCurrentThemePos() == position ? View.VISIBLE : View.INVISIBLE);
        holder.nameView.setText(ThemeUtil.themes[position].getName());
        holder.colorView.setCardBackgroundColor(ctx.getResources().getColor(ThemeUtil.themes[position].getPreviewColor()));
        final int finalPos = position;

        holder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ThemeUtil.changeCurrentTheme(ThemeUtil.themes[finalPos]);
                ThemeUtil.changeTheme(ctx, ThemeUtil.getCurrentTheme());
                int primary = ColorUtil.getColor(R.attr.colorPrimary, ctx);
                int back = ColorUtil.getColor(android.R.attr.colorBackground, ctx);
                int fore = ColorUtil.getColor(android.R.attr.textColor, ctx);
                themeAdapterListener.onChangeTheme((ViewGroup) ((Activity) ctx).findViewById(R.id.theme_root), primary, fore);
                themeAdapterListener.onAnimate(recyclerView, "backgroundColor", ((ColorDrawable) recyclerView.getBackground()).getColor(), back);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return ThemeUtil.themes.length;
    }

    public static class ThemeViewHolder extends RecyclerView.ViewHolder
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

    public interface ThemeAdapterListener
    {
        void onAnimate(View view, String name, @ColorInt int from, @ColorInt int to);
        void onChangeTheme(ViewGroup group, @ColorInt int primary, @ColorInt int fore);
    }
}
