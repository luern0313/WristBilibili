package cn.luern0313.wristbilibili.adapter;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.models.RankingModel;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.ImageTaskUtil;
import cn.luern0313.wristbilibili.util.LruCacheUtil;
import cn.luern0313.wristbilibili.util.MyApplication;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 被 luern0313 创建于 2020/1/9.
 */

public class RankingAdapter extends BaseAdapter
{
    private Context ctx;
    private LayoutInflater mInflater;

    private RankingAdapterListener rankingAdapterListener;

    private ArrayList<RankingModel> rkList;
    private ListView listView;

    public RankingAdapter(LayoutInflater inflater, ArrayList<RankingModel> rkList, ListView listView, RankingAdapterListener rankingAdapterListener)
    {
        this.ctx = MyApplication.getContext();
        this.mInflater = inflater;
        this.rkList = rkList;
        this.listView = listView;
        this.rankingAdapterListener = rankingAdapterListener;
    }

    @Override
    public int getCount()
    {
        return rkList.size();
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
        RankingModel rankingVideo = rkList.get(position);
        ViewHolder viewHolder;
        if(convertView == null)
        {
            convertView = mInflater.inflate(R.layout.item_ranking_video, null);
            viewHolder = new ViewHolder();
            convertView.setTag(viewHolder);
            viewHolder.layout = convertView.findViewById(R.id.rk_video_lay);
            viewHolder.relativeLayout = convertView.findViewById(R.id.rk_video_lay_lay);
            viewHolder.up_layout = convertView.findViewById(R.id.rk_video_video_up);
            viewHolder.up_head = convertView.findViewById(R.id.rk_video_video_up_head);
            viewHolder.up_name = convertView.findViewById(R.id.rk_video_video_up_name);
            viewHolder.video_rank = convertView.findViewById(R.id.rk_video_rank);
            viewHolder.video_img = convertView.findViewById(R.id.rk_video_video_img);
            viewHolder.video_title = convertView.findViewById(R.id.rk_video_video_title);
            viewHolder.video_play = convertView.findViewById(R.id.rk_video_video_play);
            viewHolder.video_danmaku = convertView.findViewById(R.id.rk_video_video_danmaku);
            viewHolder.video_score = convertView.findViewById(R.id.rk_video_video_score);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Drawable playNumDrawable = convertView.getResources().getDrawable(R.drawable.icon_number_play);
        Drawable danmakuNumDrawable = convertView.getResources().getDrawable(R.drawable.icon_number_danmu);
        playNumDrawable.setBounds(0, 0, DataProcessUtil.dip2px(10), DataProcessUtil.dip2px(10));
        danmakuNumDrawable.setBounds(0, 0,DataProcessUtil.dip2px(10), DataProcessUtil.dip2px(10));
        viewHolder.video_play.setCompoundDrawables(playNumDrawable,null, null,null);
        viewHolder.video_danmaku.setCompoundDrawables(danmakuNumDrawable,null, null,null);

        viewHolder.up_name.setText(rankingVideo.getName());
        viewHolder.video_rank.setText(String.valueOf(position + 1));
        viewHolder.video_title.setText(rankingVideo.getTitle());
        viewHolder.video_play.setText(getView(rankingVideo.getPlay()));
        viewHolder.video_danmaku.setText(getView(rankingVideo.getDanmaku()));
        viewHolder.video_score.setText(String.format(ctx.getString(R.string.ranking_score), rankingVideo.getScore()));
        viewHolder.up_head.setImageResource(R.drawable.img_default_head);
        viewHolder.video_img.setImageResource(R.drawable.img_default_vid);

        viewHolder.layout.setOnClickListener(onViewClick(position));
        viewHolder.up_layout.setOnClickListener(onViewClick(position));

        switch(position + 1)
        {
            case 1:
                viewHolder.relativeLayout.setBackgroundResource(R.drawable.shape_bg_ranking_rank_1);
                viewHolder.video_rank.setTextColor(convertView.getResources().getColor(R.color.ranking_rank_1));
                break;
            case 2:
                viewHolder.relativeLayout.setBackgroundResource(R.drawable.shape_bg_ranking_rank_2);
                viewHolder.video_rank.setTextColor(convertView.getResources().getColor(R.color.ranking_rank_2));
                break;
            case 3:
                viewHolder.relativeLayout.setBackgroundResource(R.drawable.shape_bg_ranking_rank_3);
                viewHolder.video_rank.setTextColor(convertView.getResources().getColor(R.color.ranking_rank_3));
                break;
            default:
                viewHolder.relativeLayout.setBackgroundResource(0);
                viewHolder.video_rank.setTextColor(convertView.getResources().getColor(R.color.ranking_rank_other));
                break;
        }

        viewHolder.up_head.setTag(rankingVideo.getFace());
        BitmapDrawable c = setImageFormWeb(rankingVideo.getFace());
        if(c != null) viewHolder.up_head.setImageDrawable(c);

        viewHolder.video_img.setTag(rankingVideo.getPic());
        BitmapDrawable i = setImageFormWeb(rankingVideo.getPic());
        if(i != null) viewHolder.video_img.setImageDrawable(i);
        return convertView;
    }

    private View.OnClickListener onViewClick(final int position)
    {
        return v -> rankingAdapterListener.onClick(v.getId(), position);
    }

    private static class ViewHolder
    {
        LinearLayout layout;
        RelativeLayout relativeLayout;
        LinearLayout up_layout;
        CircleImageView up_head;
        TextView up_name;
        TextView video_rank;
        ImageView video_img;
        TextView video_title;
        TextView video_play;
        TextView video_danmaku;
        TextView video_score;
    }

    private String getView(int view)
    {
        if(view > 10000) return view / 1000 / 10.0 + "万";
        else return String.valueOf(view);
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

    public interface RankingAdapterListener
    {
        void onClick(int viewId, int position);
    }
}
