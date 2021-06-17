package cn.luern0313.wristbilibili.adapter;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.models.AnimationTimelineModel;
import cn.luern0313.wristbilibili.util.ColorUtil;
import cn.luern0313.wristbilibili.util.ImageTaskUtil;
import cn.luern0313.wristbilibili.util.LruCacheUtil;

/**
 * 被 luern0313 创建于 2020/1/9.
 */

public class AnimationTimelineAdapter extends BaseAdapter
{
    private final Context ctx;
    private final LayoutInflater mInflater;

    private final AnimationTimelineListener animationTimelineListener;

    private final ArrayList<AnimationTimelineModel> arList;
    private final ListView listView;

    public AnimationTimelineAdapter(LayoutInflater inflater, ArrayList<AnimationTimelineModel> arList, ListView listView, AnimationTimelineListener animationTimelineListener)
    {
        this.ctx = listView.getContext();
        this.mInflater = inflater;
        this.arList = arList;
        this.listView = listView;
        this.animationTimelineListener = animationTimelineListener;
    }

    @Override
    public int getCount()
    {
        int size = 0;
        for (int i = 0; i < arList.size(); i++)
            size += arList.get(i).getSeasonModelArrayList().size();
        return size + arList.size();
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
    public int getViewTypeCount()
    {
        return 2;
    }

    @Override
    public int getItemViewType(int position)
    {
        for (int i = 0; i < arList.size(); i++)
        {
            if(position >= arList.get(i).getSeasonModelArrayList().size() + 1)
                position -= arList.get(i).getSeasonModelArrayList().size() + 1;
            else
                return position == 0 ? 1 : 0;
        }
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup)
    {
        int type = getItemViewType(position);
        AnimationTimelineModel animationTimelineModel = null;
        AnimationTimelineModel.AnimationTimelineSeasonModel animationTimelineSeasonModel = null;
        int p = position;
        for (int i = 0; i < arList.size(); i++)
        {
            if(p >= arList.get(i).getSeasonModelArrayList().size() + 1)
                p -= arList.get(i).getSeasonModelArrayList().size() + 1;
            else
            {
                animationTimelineModel = arList.get(i);
                if(p != 0)
                    animationTimelineSeasonModel = animationTimelineModel.getSeasonModelArrayList().get(p - 1);
                break;
            }
        }
        AnimViewHolder animViewHolder = null;
        DateViewHolder dateViewHolder = null;
        if(convertView == null)
        {
            if(type == 0)
            {
                convertView = mInflater.inflate(R.layout.item_anim_remind, null);
                animViewHolder = new AnimViewHolder();
                convertView.setTag(animViewHolder);
                animViewHolder.lay = convertView.findViewById(R.id.item_anim_remind_lay);
                animViewHolder.img = convertView.findViewById(R.id.item_anim_remind_img);
                animViewHolder.name = convertView.findViewById(R.id.item_anim_remind_name);
                animViewHolder.follow = convertView.findViewById(R.id.item_anim_remind_follow);
                animViewHolder.last = convertView.findViewById(R.id.item_anim_remind_last);
                animViewHolder.time = convertView.findViewById(R.id.item_anim_remind_time);
            }
            else
            {
                convertView = mInflater.inflate(R.layout.item_anim_remid_date, null);
                dateViewHolder = new DateViewHolder();
                convertView.setTag(dateViewHolder);
                dateViewHolder.date = convertView.findViewById(R.id.item_anim_remind_date_date);
                dateViewHolder.empty = convertView.findViewById(R.id.item_anim_remind_date_empty);
            }
        }
        else
        {
            if(type == 0)
                animViewHolder = (AnimViewHolder) convertView.getTag();
            else
                dateViewHolder = (DateViewHolder) convertView.getTag();
        }

        if(type == 0)
        {
            animViewHolder.lay.setOnClickListener(onViewClick(position));
            animViewHolder.name.setText(animationTimelineSeasonModel.getTitle());
            animViewHolder.last.setText(animationTimelineSeasonModel.getLastEpisode());
            animViewHolder.img.setImageResource(R.drawable.img_default_animation);

            if(animationTimelineSeasonModel.isToday())
                animViewHolder.time.setText(animationTimelineSeasonModel.getTime());
            else
                animViewHolder.time.setText(String.format(ctx.getString(R.string.animation_timeline_date), animationTimelineSeasonModel.getDate(), animationTimelineSeasonModel.getTime()));

            if(animationTimelineSeasonModel.getIsFollow() == 1)
                animViewHolder.follow.setVisibility(View.VISIBLE);
            else
                animViewHolder.follow.setVisibility(View.GONE);

            animViewHolder.img.setTag(animationTimelineSeasonModel.getCoverUrl());
            BitmapDrawable c = setImageFormWeb(animationTimelineSeasonModel.getCoverUrl());
            if(c != null) animViewHolder.img.setImageDrawable(c);
        }
        else
        {
            dateViewHolder.date.setText(String.format(ctx.getString(
                    R.string.animation_timeline_date_date), animationTimelineModel.getDate(), ctx.getResources().getStringArray(
                            R.array.animation_timeline_day_of_week)[animationTimelineModel.getDayOfWeek()]));
            if(animationTimelineModel.getSeasonModelArrayList().size() == 0)
            {
                String[] array = ctx.getResources().getStringArray(R.array.animation_timeline_date_empty_array);
                dateViewHolder.empty.setText(array[new Random().nextInt(array.length)]);
                dateViewHolder.empty.setVisibility(View.VISIBLE);
            }
            else
                dateViewHolder.empty.setVisibility(View.GONE);

            if(animationTimelineModel.isToday())
                dateViewHolder.date.setTextColor(ColorUtil.getColor(R.attr.colorAccent, ctx));
            else
                dateViewHolder.date.setTextColor(ColorUtil.getColor(R.attr.colorTitleGrey, ctx));
        }
        return convertView;
    }

    private static class AnimViewHolder
    {
        RelativeLayout lay;
        ImageView img;
        TextView name;
        TextView follow;
        TextView last;
        TextView time;
    }

    private static class DateViewHolder
    {
        TextView date;
        TextView empty;
    }

    private View.OnClickListener onViewClick(final int position)
    {
        return v -> animationTimelineListener.onClick(v.getId(), position);
    }

    private BitmapDrawable setImageFormWeb(String url)
    {
        if(url != null && LruCacheUtil.getLruCache().get(url) != null)
            return LruCacheUtil.getLruCache().get(url);
        else
        {
            ImageTaskUtil it = new ImageTaskUtil(listView);
            it.execute(url);
            return null;
        }
    }

    public interface AnimationTimelineListener
    {
        void onClick(int viewId, int position);
    }
}
