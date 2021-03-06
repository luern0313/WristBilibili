package cn.luern0313.wristbilibili.adapter;

import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.util.ImageTaskUtil;
import cn.luern0313.wristbilibili.util.LruCacheUtil;

/**
 * 被 luern0313 创建于 2020/5/19.
 */
public class DynamicAlbumImgAdapter extends RecyclerView.Adapter<DynamicAlbumImgAdapter.ViewHolder>
{
    private RecyclerView recyclerView;
    private ArrayList<String> urlArrayList;

    private DynamicAlbumImgAdapterListener dynamicAlbumImgAdapterListener;

    public DynamicAlbumImgAdapter(ArrayList<String> urlArrayList, RecyclerView recyclerView, DynamicAlbumImgAdapterListener dynamicAlbumImgAdapterListener)
    {
        this.recyclerView = recyclerView;
        this.urlArrayList = urlArrayList;
        this.dynamicAlbumImgAdapterListener = dynamicAlbumImgAdapterListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder
    {
        private ImageView imageView;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_dynamic_album_img);
        }
    }

    @NonNull
    @Override
    public DynamicAlbumImgAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dynamic_album_img, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final DynamicAlbumImgAdapter.ViewHolder holder, final int position)
    {
        holder.imageView.setImageResource(R.drawable.img_default_animation);
        holder.imageView.setOnClickListener(v -> dynamicAlbumImgAdapterListener.onClick(position));

        holder.imageView.setTag(urlArrayList.get(position));
        BitmapDrawable b = setImageFormWeb(urlArrayList.get(position));
        if(b != null) holder.imageView.setImageDrawable(b);
    }

    @Override
    public int getItemCount()
    {
        return urlArrayList.size();
    }

    private BitmapDrawable setImageFormWeb(String url)
    {
        if(url != null && LruCacheUtil.getLruCache().get(url) != null)
            return LruCacheUtil.getLruCache().get(url);
        else
        {
            ImageTaskUtil it = new ImageTaskUtil(recyclerView);
            it.execute(url);
            return null;
        }
    }

    public interface DynamicAlbumImgAdapterListener
    {
        void onClick(int position);
    }
}
