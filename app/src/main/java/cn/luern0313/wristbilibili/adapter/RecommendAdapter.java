package cn.luern0313.wristbilibili.adapter;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.models.RecommendModel;
import cn.luern0313.wristbilibili.widget.ImageDownloader;

/**
 * 被 luern0313 创建于 2020/1/15.
 */

public class RecommendAdapter extends BaseAdapter
{
    private LayoutInflater mInflater;

    private LruCache<String, BitmapDrawable> mImageCache;
    private RecommendAdapterListener recommendAdapterListener;

    private ArrayList<RecommendModel> rcList;
    public ListView listView;

    public RecommendAdapter(LayoutInflater inflater, ArrayList<RecommendModel> rcList, ListView listView, RecommendAdapterListener recommendAdapterListener)
    {
        mInflater = inflater;
        this.rcList = rcList;
        this.listView = listView;
        this.recommendAdapterListener = recommendAdapterListener;

        int maxCache = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxCache / 8;
        mImageCache = new LruCache<String, BitmapDrawable>(cacheSize)
        {
            @Override
            protected int sizeOf(String key, BitmapDrawable value)
            {
                try
                {
                    return value.getBitmap().getByteCount();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                return 0;
            }
        };
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
    public View getView(int position, View convertView, ViewGroup viewGroup)
    {
        RecommendModel recommendVideo = rcList.get(position);
        ViewHolder viewHolder;
        if(convertView == null)
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
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.video_img.setImageResource(R.drawable.img_default_vid);
        viewHolder.video_time.setText(recommendVideo.video_time);
        viewHolder.video_title.setText(recommendVideo.video_title);
        viewHolder.video_play.setText(recommendVideo.video_data_1_text);
        viewHolder.video_danmaku.setText(recommendVideo.video_data_2_text);
        viewHolder.video_lable.setText(recommendVideo.video_lable);
        if(!recommendVideo.video_recommend_reason.equals(""))
        {
            viewHolder.video_reason.setVisibility(View.VISIBLE);
            viewHolder.video_lable.setVisibility(View.GONE);
            viewHolder.video_reason.setText(recommendVideo.video_recommend_reason);
        }
        else
        {
            viewHolder.video_reason.setVisibility(View.GONE);
            viewHolder.video_lable.setVisibility(View.VISIBLE);
        }

        Drawable playNumDrawable = convertView.getResources().getDrawable(R.drawable.icon_video_play_num);
        Drawable danmakuNumDrawable = convertView.getResources().getDrawable(R.drawable.icon_video_danmu_num);
        playNumDrawable.setBounds(0,0,24,24);
        danmakuNumDrawable.setBounds(0,0,24,24);
        viewHolder.video_play.setCompoundDrawables(playNumDrawable,null, null,null);
        viewHolder.video_danmaku.setCompoundDrawables(danmakuNumDrawable,null, null,null);

        viewHolder.layout.setOnClickListener(onViewClick(position));

        viewHolder.video_img.setTag(recommendVideo.video_img);
        BitmapDrawable i = setImageFormWeb(recommendVideo.video_img);
        if(i != null) viewHolder.video_img.setImageDrawable(i);
        return convertView;
    }

    private View.OnClickListener onViewClick(final int position)
    {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                recommendAdapterListener.onClick(v.getId(), position);
            }
        };
    }

    class ViewHolder
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

    private String getView(int view)
    {
        if(view > 10000) return view / 1000 / 10.0 + "万";
        else return String.valueOf(view);
    }

    private BitmapDrawable setImageFormWeb(String url)
    {
        if(mImageCache.get(url) != null)
        {
            return mImageCache.get(url);
        }
        else
        {
            ImageTask it = new ImageTask(listView);
            it.execute(url);
            return null;
        }
    }

    private class ImageTask extends AsyncTask<String, Void, BitmapDrawable>
    {
        private String imageUrl;
        private ListView listView;

        ImageTask(ListView listView)
        {
            super();
            this.listView = listView;
        }

        @Override
        protected BitmapDrawable doInBackground(String... params)
        {
            try
            {
                imageUrl = params[0];
                Bitmap bitmap = null;
                bitmap = ImageDownloader.downloadImage(imageUrl);
                BitmapDrawable db = new BitmapDrawable(listView.getResources(), bitmap);
                // 如果本地还没缓存该图片，就缓存
                if(mImageCache.get(imageUrl) == null && bitmap != null)
                {
                    mImageCache.put(imageUrl, db);
                }
                return db;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(BitmapDrawable result)
        {
            // 通过Tag找到我们需要的ImageView，如果该ImageView所在的item已被移出页面，就会直接返回null
            ImageView iv = listView.findViewWithTag(imageUrl);
            if(iv != null && result != null)
            {
                iv.setImageDrawable(result);
            }
        }
    }

    public interface RecommendAdapterListener
    {
        void onClick(int viewId, int position);
    }
}
