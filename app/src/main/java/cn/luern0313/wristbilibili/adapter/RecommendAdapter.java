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

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.models.RecommendModel;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.ImageTaskUtil;
import cn.luern0313.wristbilibili.util.LruCacheUtil;

/**
 * 被 luern0313 创建于 2020/1/15.
 */

public class RecommendAdapter extends BaseAdapter
{
    private LayoutInflater mInflater;

    private RecommendAdapterListener recommendAdapterListener;

    private ArrayList<RecommendModel> rcList;
    private ListView listView;

    public RecommendAdapter(LayoutInflater inflater, ArrayList<RecommendModel> rcList, ListView listView, RecommendAdapterListener recommendAdapterListener)
    {
        mInflater = inflater;
        this.rcList = rcList;
        this.listView = listView;
        this.recommendAdapterListener = recommendAdapterListener;
    }

    @Override
    public int getCount()
    {
        return rcList.size();
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
        return rcList.get(position).getMode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup)
    {
        RecommendModel recommendVideo = rcList.get(position);
        int type = getItemViewType(position);

        ViewHolder viewHolder = null;
        if(convertView == null)
        {
            if(type == 0)
            {
                convertView = mInflater.inflate(R.layout.item_recommend, null);
                viewHolder = new ViewHolder();
                viewHolder.layout = convertView.findViewById(R.id.rc_video);
                viewHolder.video_img = convertView.findViewById(R.id.rc_video_img);
                viewHolder.video_time = convertView.findViewById(R.id.rc_video_time);
                viewHolder.video_title = convertView.findViewById(R.id.rc_video_video_title);
                viewHolder.video_play = convertView.findViewById(R.id.rc_video_video_play);
                viewHolder.video_danmaku = convertView.findViewById(R.id.rc_video_video_danmaku);
                viewHolder.video_reason = convertView.findViewById(R.id.rc_video_video_reason);
                viewHolder.video_lable = convertView.findViewById(R.id.rc_video_video_label);
                convertView.setTag(viewHolder);
            }
            else
            {
                convertView = mInflater.inflate(R.layout.widget_recommend_update, null);
            }
        }
        else
        {
            if(type == 0)
                viewHolder = (ViewHolder) convertView.getTag();
        }

        if(type == 0)
        {
            viewHolder.video_img.setImageResource(R.drawable.img_default_vid);
            viewHolder.video_time.setText(recommendVideo.getTime());
            viewHolder.video_title.setText(recommendVideo.getTitle());
            viewHolder.video_play.setText(recommendVideo.getData1Text());
            viewHolder.video_danmaku.setText(recommendVideo.getData2Text());
            viewHolder.video_lable.setText(recommendVideo.getLabel());
            if(recommendVideo.getRecommendReason() != null && !recommendVideo.getRecommendReason().equals(""))
            {
                viewHolder.video_reason.setVisibility(View.VISIBLE);
                viewHolder.video_lable.setVisibility(View.GONE);
                viewHolder.video_reason.setText(recommendVideo.getRecommendReason());
            }
            else
            {
                viewHolder.video_reason.setVisibility(View.GONE);
                viewHolder.video_lable.setVisibility(View.VISIBLE);
            }

            Drawable playNumDrawable = convertView.getResources().getDrawable(R.drawable.icon_number_play);
            Drawable danmakuNumDrawable = convertView.getResources().getDrawable(R.drawable.icon_number_danmu);
            playNumDrawable.setBounds(0, 0, DataProcessUtil.dip2px(10), DataProcessUtil.dip2px(10));
            danmakuNumDrawable.setBounds(0, 0, DataProcessUtil.dip2px(10), DataProcessUtil.dip2px(10));
            viewHolder.video_play.setCompoundDrawables(playNumDrawable, null, null, null);
            viewHolder.video_danmaku.setCompoundDrawables(danmakuNumDrawable, null, null, null);

            viewHolder.layout.setOnClickListener(onViewClick(position));

            viewHolder.video_img.setTag(recommendVideo.getImg());
            BitmapDrawable i = setImageFormWeb(recommendVideo.getImg());
            if(i != null) viewHolder.video_img.setImageDrawable(i);
        }
        else
        {
            (convertView.findViewById(R.id.widget_recommend_update_lay)).setOnClickListener(onViewClick(position));
        }
        return convertView;
    }

    private View.OnClickListener onViewClick(final int position)
    {
        return v -> recommendAdapterListener.onClick(v.getId(), position);
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
        TextView video_lable;
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

    public interface RecommendAdapterListener
    {
        void onClick(int viewId, int position);
    }
}
