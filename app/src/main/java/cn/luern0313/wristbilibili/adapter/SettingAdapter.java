package cn.luern0313.wristbilibili.adapter;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.appcompat.widget.SwitchCompat;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.models.SettingModel;
import cn.luern0313.wristbilibili.util.MyApplication;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;

/**
 * 被 luern0313 创建于 2021/1/19.
 */

public class SettingAdapter extends BaseAdapter
{
    private final Context ctx;
    private final LayoutInflater mInflater;

    private final ArrayList<SettingModel> settingModelArrayList;

    private final SettingAdapterListener settingAdapterListener;
    private final ListView listView;

    public SettingAdapter(LayoutInflater inflater, ArrayList<SettingModel> settingModelArrayList, ListView listView, SettingAdapterListener settingAdapterListener)
    {
        this.ctx = MyApplication.getContext();
        this.mInflater = inflater;
        this.settingModelArrayList = settingModelArrayList;
        this.listView = listView;
        this.settingAdapterListener = settingAdapterListener;
    }

    @Override
    public int getCount()
    {
        return settingModelArrayList.size();
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

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup)
    {
        ViewHolder viewHolder;
        SettingModel settingModel = settingModelArrayList.get(position);
        if(convertView == null)
        {
            convertView = mInflater.inflate(R.layout.item_setting_item, null);
            viewHolder = new ViewHolder();
            convertView.setTag(viewHolder);
            viewHolder.lay = convertView.findViewById(R.id.setting_item_lay);
            viewHolder.text = convertView.findViewById(R.id.setting_item_text);
            viewHolder.text_tip = convertView.findViewById(R.id.setting_item_text_desc);
            viewHolder.extra = convertView.findViewById(R.id.setting_item_extra);
            viewHolder.switch_compat = convertView.findViewById(R.id.setting_item_switch);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.text.setText(settingModel.getName());
        viewHolder.text_tip.setVisibility(View.GONE);
        if(settingModel.getMode() == 0)
        {
            viewHolder.extra.setVisibility(View.GONE);
            viewHolder.switch_compat.setVisibility(View.GONE);

            viewHolder.lay.setOnClickListener(onViewClick(position));
        }
        else if(settingModel.getMode() == 1)
        {
            viewHolder.extra.setVisibility(View.VISIBLE);
            viewHolder.switch_compat.setVisibility(View.GONE);

            viewHolder.lay.setOnClickListener(onViewClick(position));
        }
        else if(settingModel.getMode() == 2)
        {
            viewHolder.extra.setVisibility(View.GONE);
            viewHolder.switch_compat.setVisibility(View.VISIBLE);

            if(settingModel.getSpName().equals("screenRound"))
                viewHolder.switch_compat.setChecked(SharedPreferencesUtil.getBoolean(
                        SharedPreferencesUtil.screenRound, !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                                ctx.getResources().getConfiguration().isScreenRound())));

            if(Build.VERSION.SDK_INT < settingModel.getLimitApi())
            {
                viewHolder.switch_compat.setEnabled(false);
                viewHolder.text_tip.setVisibility(View.VISIBLE);
                viewHolder.text_tip.setText(settingModel.getLimitTip());
            }

            viewHolder.lay.setOnClickListener(onSwitchChange(position, settingModel));
        }

        return convertView;
    }

    private static class ViewHolder
    {
        RelativeLayout lay;
        TextView text;
        TextView text_tip;
        ImageView extra;
        SwitchCompat switch_compat;
    }

    private View.OnClickListener onViewClick(final int position)
    {
        return v -> settingAdapterListener.onItemClick(position);
    }

    private View.OnClickListener onSwitchChange(final int position, final SettingModel settingModel)
    {
        return v -> {
            if(Build.VERSION.SDK_INT >= settingModel.getLimitApi())
            {
                boolean s = !((SwitchCompat) v.findViewById(R.id.setting_item_switch)).isChecked();
                if(settingAdapterListener.onSwitchChange(position, s))
                    ((SwitchCompat) v.findViewById(R.id.setting_item_switch)).setChecked(s);
            }
        };
    }

    public interface SettingAdapterListener
    {
        void onItemClick(int position);
        boolean onSwitchChange(int position, boolean s);
    }
}
