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
import cn.luern0313.wristbilibili.models.VideoModel;

/**
 * 被 luern0313 创建于 2020/1/30.
 */

public class VideoPartAdapter extends RecyclerView.Adapter<VideoPartAdapter.ViewHolder>
{
    private VideoPartListener videoPartListener;
    private ArrayList<VideoModel.VideoPartModel> videoPartArrayList;

    public VideoPartAdapter(ArrayList<VideoModel.VideoPartModel> videoPartArrayList)
    {
        this.videoPartArrayList = videoPartArrayList;
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
        VideoModel.VideoPartModel videoPartModel = videoPartArrayList.get(position);
        holder.text.setText(videoPartModel.video_part_name);
        holder.lay.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                videoPartListener.onItemClick(v, position);
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return videoPartArrayList.size();
    }

    public void setOnItemClickListener(VideoPartListener videoPartListener)
    {
        this.videoPartListener = videoPartListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        RelativeLayout lay;
        TextView text;
        public ViewHolder(View itemView)
        {
            super(itemView);
            lay = itemView.findViewById(R.id.item_video_part);
            text = itemView.findViewById(R.id.item_video_part_text);
        }
    }

    public interface VideoPartListener
    {
        void onItemClick(View view, int position);
    }
}
