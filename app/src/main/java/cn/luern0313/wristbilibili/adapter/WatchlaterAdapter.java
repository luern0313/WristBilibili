package cn.luern0313.wristbilibili.adapter;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import androidx.collection.LruCache;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.models.WatchLaterModel;
import cn.luern0313.wristbilibili.util.ImageDownloaderUtil;

/**
 * 被 luern0313 创建于 2020/4/30.
 */
public class WatchlaterAdapter extends BaseAdapter
{
    private LayoutInflater mInflater;
    private ListView listView;

    private LruCache<String, BitmapDrawable> mImageCache;

    private ArrayList<WatchLaterModel> wlList;

    public WatchlaterAdapter(LayoutInflater inflater, ArrayList<WatchLaterModel> wlList, ListView listView)
    {
        mInflater = inflater;
        this.wlList = wlList;
        this.listView = listView;

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
        return wlList.size();
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
        ViewHolder viewHolder;
        if(convertView == null)
        {
            convertView = mInflater.inflate(R.layout.item_list_video, null);
            viewHolder = new ViewHolder();
            convertView.setTag(viewHolder);
            viewHolder.title = convertView.findViewById(R.id.item_list_video_title);
            viewHolder.img = convertView.findViewById(R.id.item_list_video_img);
            viewHolder.up = convertView.findViewById(R.id.item_list_video_up);
            viewHolder.play = convertView.findViewById(R.id.item_list_video_play);
            viewHolder.pro = convertView.findViewById(R.id.item_list_video_pro);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        WatchLaterModel video = wlList.get(position);
        viewHolder.title.setText(video.title);
        viewHolder.img.setImageResource(R.drawable.img_default_vid);
        viewHolder.up.setText("UP : " + video.up);
        viewHolder.play.setText("播放 : " + video.play + "  弹幕 : " + video.danmaku);
        viewHolder.pro.setVisibility(View.VISIBLE);
        viewHolder.pro.setProgress((int) (video.progress * 100.0 / video.duration));

        viewHolder.img.setTag(video.cover);
        final BitmapDrawable c = setImageFormWeb(video.cover);
        if(c != null) viewHolder.img.setImageDrawable(c);
        return convertView;
    }

    class ViewHolder
    {
        TextView title;
        ImageView img;
        TextView up;
        TextView play;
        ProgressBar pro;
    }

    BitmapDrawable setImageFormWeb(String url)
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

    class ImageTask extends AsyncTask<String, Void, BitmapDrawable>
    {
        private String imageUrl;
        private Resources listViewResources;

        ImageTask(ListView listView)
        {
            listViewResources = listView.getResources();
        }

        @Override
        protected BitmapDrawable doInBackground(String... params)
        {
            try
            {
                imageUrl = params[0];
                Bitmap bitmap = null;
                bitmap = ImageDownloaderUtil.downloadImage(imageUrl);
                BitmapDrawable db = new BitmapDrawable(listViewResources, bitmap);
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
}
