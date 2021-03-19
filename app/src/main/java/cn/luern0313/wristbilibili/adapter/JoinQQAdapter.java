package cn.luern0313.wristbilibili.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.util.MyApplication;

/**
 * 被 luern0313 创建于 2020/4/25.
 */
public class JoinQQAdapter extends BaseExpandableListAdapter
{
    private final Context ctx;
    private final LayoutInflater inflater;

    private final ListView listView;

    public JoinQQAdapter(LayoutInflater inflater, ListView listView)
    {
        this.ctx = MyApplication.getContext();
        this.inflater = inflater;
        this.listView = listView;
    }

    @Override
    public int getGroupCount()
    {
        return ctx.getResources().getStringArray(R.array.join_qq_list).length;
    }

    @Override
    public int getChildrenCount(int groupPosition)
    {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition)
    {
        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition)
    {
        return null;
    }

    @Override
    public long getGroupId(int groupPosition)
    {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition)
    {
        return childPosition;
    }

    @Override
    public boolean hasStableIds()
    {
        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
    {
        NumberViewHolder numberViewHolder;
        if(convertView == null)
        {
            convertView = inflater.inflate(R.layout.item_join_number, parent, false);
            numberViewHolder = new NumberViewHolder();
            numberViewHolder.text = convertView.findViewById(R.id.item_join_num_text);
            numberViewHolder.expand = convertView.findViewById(R.id.item_join_num_expand);
            convertView.setTag(numberViewHolder);
        }
        else
        {
            numberViewHolder = (NumberViewHolder) convertView.getTag();
        }

        numberViewHolder.text.setText(Html.fromHtml(String.format(ctx.getString(groupPosition == 0 ? R.string.join_qq_number : R.string.join_qq_number_full), (char) (65 + getGroupCount() - groupPosition - 1), ctx.getResources().getStringArray(R.array.join_qq_list)[groupPosition])));

        if(isExpanded)
            numberViewHolder.expand.setRotation(180);
        else
            numberViewHolder.expand.setRotation(0);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
    {
        ImgViewHolder imgViewHolder;
        if(convertView == null)
        {
            convertView = inflater.inflate(R.layout.item_join_img, parent, false);
            imgViewHolder = new ImgViewHolder();
            imgViewHolder.img = convertView.findViewById(R.id.item_join_qq_img);
            convertView.setTag(imgViewHolder);
        }
        else
        {
            imgViewHolder = (ImgViewHolder) convertView.getTag();
        }

        try
        {
            imgViewHolder.img.setImageResource(R.drawable.class.getField("img_qq" + (getGroupCount() - groupPosition)).getInt(null));
        }
        catch (NoSuchFieldException | IllegalAccessException ignored)
        {
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition)
    {
        return true;
    }

    private static class NumberViewHolder
    {
        TextView text;
        ImageView expand;
    }

    private static class ImgViewHolder
    {
        ImageView img;
    }
}
