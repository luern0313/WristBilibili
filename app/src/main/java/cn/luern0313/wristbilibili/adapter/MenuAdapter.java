package cn.luern0313.wristbilibili.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.luern0313.lson.LsonUtil;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.ui.MainActivity;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.MyApplication;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;
import cn.luern0313.wristbilibili.widget.StripeDrawable;

/**
 * 被 luern0313 创建于 2021/1/19.
 */

public class MenuAdapter extends BaseAdapter
{
    private final Context ctx;
    private final LayoutInflater mInflater;

    private final MenuAdapterListener menuAdapterListener;
    private final ListView listView;

    private final TypedValue rippleTypedValue;
    private final int[] rippleAttribute;
    private final StripeDrawable stripeDrawable;

    private final int[] sort;
    private final int current;
    private final boolean isMenu;

    public MenuAdapter(LayoutInflater inflater, int[] sort, int current, boolean isMenu, ListView listView, MenuAdapterListener menuAdapterListener)
    {
        this.ctx = MyApplication.getContext();
        this.mInflater = inflater;
        this.sort = sort;
        this.current = current;
        this.isMenu = isMenu;
        this.listView = listView;
        this.menuAdapterListener = menuAdapterListener;

        rippleTypedValue = new TypedValue();
        ctx.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, rippleTypedValue, true);
        rippleAttribute = new int[] { android.R.attr.selectableItemBackground};

        stripeDrawable = new StripeDrawable();
    }

    @Override
    public int getCount()
    {
        return ctx.getResources().getStringArray(R.array.menu_title).length;
    }

    @Override
    public Object getItem(int position)
    {
        return position;
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    public void replace(int from, int to)
    {
        int fromIndex = sort[MainActivity.currentPage];
        int direction = to > from ? 1 : -1;
        int temp = sort[from];
        for(int i = from; i != to; i += direction)
            sort[i] = sort[i + direction];
        sort[to] = temp;
        this.notifyDataSetChanged();
        MainActivity.currentPage = DataProcessUtil.getPositionInList(sort, fromIndex);
        SharedPreferencesUtil.putString(SharedPreferencesUtil.menuSort, LsonUtil.toJson(sort));
    }

    public void reset()
    {
        int fromIndex = sort[MainActivity.currentPage];
        for (int i = 0; i < sort.length; i++)
            sort[i] = i;
        this.notifyDataSetChanged();
        MainActivity.currentPage = DataProcessUtil.getPositionInList(sort, fromIndex);
        SharedPreferencesUtil.removeValue(SharedPreferencesUtil.menuSort);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup)
    {
        ViewHolder viewHolder;
        if(convertView == null)
        {
            convertView = mInflater.inflate(R.layout.item_menu, null);
            viewHolder = new ViewHolder();
            convertView.setTag(viewHolder);
            viewHolder.lay = convertView.findViewById(R.id.item_menu_lay);
            viewHolder.img = convertView.findViewById(R.id.item_menu_img);
            viewHolder.title = convertView.findViewById(R.id.item_menu_title);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        TypedArray typedArray = ctx.getResources().obtainTypedArray(R.array.menu_icon);
        if(isMenu && current != position)
        {
            viewHolder.lay.setOnClickListener(onViewClick(position));
            viewHolder.lay.setClickable(true);
            viewHolder.lay.setFocusable(true);
            TypedArray rippleTypedArray = ctx.getTheme().obtainStyledAttributes(rippleTypedValue.resourceId, rippleAttribute);
            Drawable rippleDrawable = rippleTypedArray.getDrawable(0);
            rippleTypedArray.recycle();
            viewHolder.lay.setBackground(rippleDrawable);
        }
        else
        {
            viewHolder.lay.setOnClickListener(null);
            viewHolder.lay.setClickable(false);
            viewHolder.lay.setFocusable(false);
            viewHolder.lay.setBackgroundResource(0);
            if(isMenu)
                viewHolder.lay.setBackground(stripeDrawable);
        }
        viewHolder.img.setImageResource(typedArray.getResourceId(sort[position], 0));
        viewHolder.title.setText(ctx.getResources().getStringArray(R.array.menu_title)[sort[position]]);
        typedArray.recycle();
        return convertView;
    }

    private static class ViewHolder
    {
        RelativeLayout lay;
        ImageView img;
        TextView title;
    }

    private View.OnClickListener onViewClick(final int position)
    {
        return v -> menuAdapterListener.onItemClick(position);
    }

    public interface MenuAdapterListener
    {
        void onItemClick(int position);
    }
}
