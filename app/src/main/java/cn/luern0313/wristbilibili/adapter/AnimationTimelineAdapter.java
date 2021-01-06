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

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.models.AnimationTimelineModel;
import cn.luern0313.wristbilibili.util.ImageTaskUtil;
import cn.luern0313.wristbilibili.util.LruCacheUtil;
import cn.luern0313.wristbilibili.util.MyApplication;

/**
 * 被 luern0313 创建于 2020/1/9.
 */

public class AnimationTimelineAdapter extends BaseAdapter
{
    private Context ctx;
    private LayoutInflater mInflater;

    private AnimationTimelineListener animationTimelineListener;

    private ArrayList<AnimationTimelineModel> arList;
    private ListView listView;

    public AnimationTimelineAdapter(LayoutInflater inflater, ArrayList<AnimationTimelineModel> arList, ListView listView, AnimationTimelineListener animationTimelineListener)
    {
        this.ctx = MyApplication.getContext();
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
        return size;
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
        AnimationTimelineModel.AnimationTimelineSeasonModel animationTimelineSeasonModel = null;
        int p = position;
        for (int i = 0; i < arList.size(); i++)
        {
            if(p >= arList.get(i).getSeasonModelArrayList().size())
                p -= arList.get(i).getSeasonModelArrayList().size();
            else
                animationTimelineSeasonModel = arList.get(i).getSeasonModelArrayList().get(p);
        }
        ViewHolder viewHolder;
        if(convertView == null)
        {
            convertView = mInflater.inflate(R.layout.item_aniremind, null);
            viewHolder = new ViewHolder();
            convertView.setTag(viewHolder);
            viewHolder.lay = convertView.findViewById(R.id.anre_lay);
            viewHolder.img = convertView.findViewById(R.id.anre_img);
            viewHolder.name = convertView.findViewById(R.id.anre_name);
            viewHolder.isfollow = convertView.findViewById(R.id.anre_isfollow);
            viewHolder.last = convertView.findViewById(R.id.anre_last);
            viewHolder.time = convertView.findViewById(R.id.anre_time);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.lay.setOnClickListener(onViewClick(position));
        viewHolder.name.setText(animationTimelineSeasonModel.getTitle());
        viewHolder.last.setText(String.format(ctx.getString(R.string.animation_timeline_update), animationTimelineSeasonModel.getLastEpisode()));
        viewHolder.img.setImageResource(R.drawable.img_default_animation);

        if(animationTimelineSeasonModel.getIsToday() == 1)
            viewHolder.time.setText(animationTimelineSeasonModel.getTime());
        else
            viewHolder.time.setText(String.format(ctx.getString(R.string.animation_timeline_date), animationTimelineSeasonModel.getDate(), animationTimelineSeasonModel.getTime()));

        if(animationTimelineSeasonModel.getIsFollow() == 1)
            viewHolder.isfollow.setVisibility(View.VISIBLE);
        else
            viewHolder.isfollow.setVisibility(View.GONE);

        viewHolder.img.setTag(animationTimelineSeasonModel.getCoverUrl());
        BitmapDrawable c = setImageFormWeb(animationTimelineSeasonModel.getCoverUrl());
        if(c != null) viewHolder.img.setImageDrawable(c);
        return convertView;
    }

    private static class ViewHolder
    {
        RelativeLayout lay;
        ImageView img;
        TextView name;
        TextView isfollow;
        TextView last;
        TextView time;
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
