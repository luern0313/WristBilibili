package cn.luern0313.wristbilibili.adapter;

import android.app.Activity;
import android.content.Context;
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

    private ThemeAdapterListener themeAdapterListener;

    public ThemeAdapter(Context ctx, LayoutInflater layoutInflater, ThemeAdapterListener themeAdapterListener)
    {
        this.ctx = ctx;
        this.layoutInflater = layoutInflater;
        this.themeAdapterListener = themeAdapterListener;
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
        holder.nameView.setText(ThemeUtil.themes[position].name);
        holder.colorView.setCardBackgroundColor(ctx.getResources().getColor(ThemeUtil.themes[position].previewColor));
        final int finalPos = position;
        holder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                @ColorInt int prevBack = ColorUtil.getColor(android.R.attr.colorBackground, R.color.activityBG, ctx);
                ThemeUtil.changeCurrentTheme(ThemeUtil.themes[finalPos]);
                ThemeUtil.changeTheme(ctx, ThemeUtil.getCurrentTheme());
                @ColorInt int[] colors = ColorUtil.getColors(
                        new int[]{R.attr.colorPrimary, android.R.attr.colorBackground, android.R.attr.textColor},
                        new int[]{ctx.getResources().getColor(R.color.colorPrimary),
                                ctx.getResources().getColor(R.color.activityBG),
                                ctx.getResources().getColor(R.color.gray_77)}, ctx);
                int primary = colors[0];
                int back = colors[1];
                int fore = colors[2];
                themeAdapterListener.onAnimate(((Activity) ctx).findViewById(R.id.theme_list), "backgroundColor", prevBack, back);
                themeAdapterListener.onChangeTheme((ViewGroup) ((Activity) ctx).findViewById(R.id.theme_root), primary, fore);
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
