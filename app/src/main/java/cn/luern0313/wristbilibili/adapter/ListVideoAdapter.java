package cn.luern0313.wristbilibili.adapter;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.models.ListVideoModel;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.ImageTaskUtil;
import cn.luern0313.wristbilibili.util.LruCacheUtil;

/**
 * 被 luern0313 创建于 2020/2/2.
 */

public class ListVideoAdapter extends BaseAdapter
{
    private final LayoutInflater mInflater;

    private final ListVideoAdapterListener listVideoAdapterListener;
    private final ListView listView;

    private final ArrayList<ListVideoModel> videoList;

    private final boolean isShowProgress;

    public ListVideoAdapter(LayoutInflater inflater, ArrayList<ListVideoModel> videoList, boolean isShowProgress, ListView listView, ListVideoAdapterListener listVideoAdapterListener)
    {
        this.mInflater = inflater;
        this.videoList = videoList;
        this.isShowProgress = isShowProgress;
        this.listView = listView;
        this.listVideoAdapterListener = listVideoAdapterListener;
    }

    @Override
    public int getCount()
    {
        return videoList.size();
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
        ListVideoModel video = videoList.get(position);
        ViewHolder viewHolder;
        if(convertView == null)
        {
            convertView = mInflater.inflate(R.layout.item_list_video, null);
            viewHolder = new ViewHolder();
            convertView.setTag(viewHolder);
            viewHolder.lay = convertView.findViewById(R.id.item_list_video_lay);
            viewHolder.img = convertView.findViewById(R.id.item_list_video_img);
            viewHolder.progress = convertView.findViewById(R.id.item_list_video_pro);
            viewHolder.title = convertView.findViewById(R.id.item_list_video_title);
            viewHolder.up = convertView.findViewById(R.id.item_list_video_up);
            viewHolder.play = convertView.findViewById(R.id.item_list_video_play);
            viewHolder.danmaku = convertView.findViewById(R.id.item_list_video_danmaku);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Drawable upDrawable = convertView.getResources().getDrawable(R.drawable.icon_video_up);
        Drawable playNumDrawable = convertView.getResources().getDrawable(R.drawable.icon_number_play);
        Drawable danmakuNumDrawable = convertView.getResources().getDrawable(R.drawable.icon_number_danmu);
        upDrawable.setBounds(0, 0, DataProcessUtil.dip2px(10), DataProcessUtil.dip2px(10));
        playNumDrawable.setBounds(0,0, DataProcessUtil.dip2px(10), DataProcessUtil.dip2px(10));
        danmakuNumDrawable.setBounds(0,0, DataProcessUtil.dip2px(10), DataProcessUtil.dip2px(10));
        viewHolder.up.setCompoundDrawables(upDrawable,null, null,null);
        viewHolder.play.setCompoundDrawables(playNumDrawable,null, null,null);
        viewHolder.danmaku.setCompoundDrawables(danmakuNumDrawable,null, null,null);

        viewHolder.img.setImageResource(R.drawable.img_default_vid);
        viewHolder.title.setText(video.getTitle());
        viewHolder.up.setText(video.getOwnerName());
        viewHolder.play.setText(video.getPlay());
        viewHolder.danmaku.setText(video.getDanmaku());

        if(isShowProgress)
        {
            viewHolder.progress.setVisibility(View.VISIBLE);
            viewHolder.progress.setProgress((int) (video.getProgress() * 100.0 / video.getDuration()));
        }
        else
            viewHolder.progress.setVisibility(View.GONE);

        viewHolder.lay.setOnClickListener(onViewClick(position));
        viewHolder.lay.setOnLongClickListener(onViewLongClick(position));

        viewHolder.img.setTag(video.getCover());
        BitmapDrawable c = setImageFormWeb(video.getCover());
        if(c != null) viewHolder.img.setImageDrawable(c);
        return convertView;
    }

    private static class ViewHolder
    {
        RelativeLayout lay;
        ImageView img;
        ProgressBar progress;
        TextView title;
        TextView up;
        TextView play;
        TextView danmaku;
    }

    private View.OnClickListener onViewClick(final int position)
    {
        return v -> listVideoAdapterListener.onListVideoAdapterClick(v.getId(), position);
    }

    private View.OnLongClickListener onViewLongClick(final int position)
    {
        return v -> {
            listVideoAdapterListener.onListVideoAdapterLongClick(v.getId(), position);
            return true;
        };
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

    public interface ListVideoAdapterListener
    {
        void onListVideoAdapterClick(int viewId, int position);
        void onListVideoAdapterLongClick(int viewId, int position);
    }
}
