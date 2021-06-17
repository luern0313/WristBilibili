package cn.luern0313.wristbilibili.adapter;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.core.content.res.ResourcesCompat;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.models.popular.PopularHistoryModel;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.ImageTaskUtil;
import cn.luern0313.wristbilibili.util.LruCacheUtil;

/**
 * 被 luern0313 创建于 2020/1/15.
 */

public class PopularHistoryAdapter extends BaseAdapter
{
    private final LayoutInflater mInflater;

    private final PopularHistoryAdapterListener popularHistoryAdapterListener;

    private final ArrayList<PopularHistoryModel.PopularHistoryVideoModel> popularHistoryVideoModelArrayList;
    private final ListView listView;

    public PopularHistoryAdapter(LayoutInflater inflater, ArrayList<PopularHistoryModel.PopularHistoryVideoModel> popularHistoryVideoModelArrayList, ListView listView, PopularHistoryAdapterListener popularHistoryAdapterListener)
    {
        mInflater = inflater;
        this.popularHistoryVideoModelArrayList = popularHistoryVideoModelArrayList;
        this.listView = listView;
        this.popularHistoryAdapterListener = popularHistoryAdapterListener;
    }

    @Override
    public int getCount()
    {
        return popularHistoryVideoModelArrayList.size();
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
        PopularHistoryModel.PopularHistoryVideoModel popularVideoModel = popularHistoryVideoModelArrayList.get(position);

        ViewHolder viewHolder = null;
        if(convertView == null)
        {
            convertView = mInflater.inflate(R.layout.item_popular_history, null);
            viewHolder = new ViewHolder();
            viewHolder.layout = convertView.findViewById(R.id.popular_history_item_video);
            viewHolder.video_img = convertView.findViewById(R.id.popular_history_item_video_img);
            viewHolder.video_time = convertView.findViewById(R.id.popular_history_item_video_time);
            viewHolder.video_title = convertView.findViewById(R.id.popular_history_item_video_video_title);
            viewHolder.video_play = convertView.findViewById(R.id.popular_history_item_video_video_play);
            viewHolder.video_danmaku = convertView.findViewById(R.id.popular_history_item_video_video_danmaku);
            viewHolder.video_reason = convertView.findViewById(R.id.popular_history_item_video_video_reason);
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.video_img.setImageResource(R.drawable.img_default_vid);
        viewHolder.video_time.setText(popularVideoModel.getTime());
        viewHolder.video_title.setText(popularVideoModel.getTitle());
        viewHolder.video_play.setText(popularVideoModel.getView());
        viewHolder.video_danmaku.setText(popularVideoModel.getDanmaku());
        if(popularVideoModel.getReason() != null && !popularVideoModel.getReason().equals(""))
        {
            viewHolder.video_reason.setVisibility(View.VISIBLE);
            viewHolder.video_reason.setText(popularVideoModel.getReason());
        }
        else
            viewHolder.video_reason.setVisibility(View.GONE);

        Drawable playNumDrawable = ResourcesCompat.getDrawable(convertView.getResources(), R.drawable.icon_number_play, null);
        Drawable danmakuNumDrawable = ResourcesCompat.getDrawable(convertView.getResources(), R.drawable.icon_number_danmu, null);
        playNumDrawable.setBounds(0, 0, DataProcessUtil.dip2px(10), DataProcessUtil.dip2px(10));
        danmakuNumDrawable.setBounds(0, 0, DataProcessUtil.dip2px(10), DataProcessUtil.dip2px(10));
        viewHolder.video_play.setCompoundDrawables(playNumDrawable, null, null, null);
        viewHolder.video_danmaku.setCompoundDrawables(danmakuNumDrawable, null, null, null);

        viewHolder.layout.setOnClickListener(onViewClick(position));

        viewHolder.video_img.setTag(popularVideoModel.getImg());
        BitmapDrawable i = setImageFormWeb(popularVideoModel.getImg());
        if(i != null) viewHolder.video_img.setImageDrawable(i);

        return convertView;
    }

    private View.OnClickListener onViewClick(final int position)
    {
        return v -> popularHistoryAdapterListener.onClick(v.getId(), position);
    }

    private static class ViewHolder
    {
        RelativeLayout layout;
        ImageView video_img;
        TextView video_time;
        TextView video_title;
        TextView video_play;
        TextView video_danmaku;
        TextView video_reason;
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

    public interface PopularHistoryAdapterListener
    {
        void onClick(int viewId, int position);
    }
}
