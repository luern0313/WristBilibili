package cn.luern0313.wristbilibili.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.models.BangumiModel;

/**
 * 被 luern0313 创建于 2020/1/30.
 */

public class BangumiEpisodeAdapter extends RecyclerView.Adapter<BangumiEpisodeAdapter.ViewHolder>
{
    private OnItemClickListener itemClickListener;
    private final ArrayList<BangumiModel.BangumiEpisodeModel> bangumiEpisodeArrayList;
    private final BangumiModel bangumiModel;
    private final int mode;

    public BangumiEpisodeAdapter(ArrayList<BangumiModel.BangumiEpisodeModel> bangumiEpisodeArrayList, BangumiModel bangumiModel, int mode)
    {
        this.bangumiEpisodeArrayList = bangumiEpisodeArrayList;
        this.bangumiModel = bangumiModel;
        this.mode = mode;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vd_videopart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position)
    {
        BangumiModel.BangumiEpisodeModel bangumiEpisodeModel = bangumiEpisodeArrayList.get(position);
        holder.text.setText(BangumiModel.getTitle(mode, bangumiModel, bangumiEpisodeModel, "\n"));
        holder.vip.setVisibility(bangumiEpisodeModel.getEpisodeVip().equals("") ? View.GONE : View.VISIBLE);
        holder.lay.setOnClickListener(v -> itemClickListener.onItemClick(v, position));

        if(bangumiModel.getUserProgressMode() == mode && bangumiModel.getUserProgressPosition() == position)
            holder.lay.setBackgroundResource(R.drawable.selector_bg_bangumi_episode_now);
        else
            holder.lay.setBackgroundResource(R.drawable.selector_bg_bangumi_episode);
    }

    @Override
    public int getItemCount()
    {
        return bangumiEpisodeArrayList.size();
    }

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener)
    {
        this.itemClickListener = mOnItemClickListener;
    }

    public interface OnItemClickListener
    {
        void onItemClick(View view, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        RelativeLayout lay;
        TextView text;
        TextView vip;
        public ViewHolder(View itemView)
        {
            super(itemView);
            lay = itemView.findViewById(R.id.item_video_part);
            text = itemView.findViewById(R.id.item_video_part_text);
            vip = itemView.findViewById(R.id.item_video_part_vip);
        }
    }
}
