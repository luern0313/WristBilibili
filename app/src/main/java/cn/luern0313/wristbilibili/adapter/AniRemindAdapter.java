package cn.luern0313.wristbilibili.adapter;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.AnimationTimelineApi;
import cn.luern0313.wristbilibili.widget.ImageDownloader;

/**
 * 被 luern0313 创建于 2020/1/9.
 */

public class AniRemindAdapter extends BaseAdapter
{
    private LayoutInflater mInflater;

    private LruCache<String, BitmapDrawable> mImageCache;

    private ArrayList<AnimationTimelineApi.Anim> arList;
    public ListView listView;

    public AniRemindAdapter(LayoutInflater inflater, ArrayList<AnimationTimelineApi.Anim> arList, ListView listView)
    {
        mInflater = inflater;
        this.arList = arList;
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
        return arList.size();
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
        AnimationTimelineApi.Anim anim = arList.get(position);
        ViewHolder viewHolder;
        if(convertView == null)
        {
            convertView = mInflater.inflate(R.layout.item_aniremind, null);
            viewHolder = new ViewHolder();
            convertView.setTag(viewHolder);
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
        viewHolder.name.setText(anim.name);
        viewHolder.last.setText("更新至" + anim.lastEpisode);
        viewHolder.img.setImageResource(R.drawable.img_default_animation);
        viewHolder.time.setText(anim.time);

        if(anim.isfollow == 1) viewHolder.isfollow.setVisibility(View.VISIBLE);
        else viewHolder.isfollow.setVisibility(View.GONE);

        viewHolder.img.setTag(anim.coverUrl);
        BitmapDrawable c = setImageFormWeb(anim.coverUrl);
        if(c != null) viewHolder.img.setImageDrawable(c);
        return convertView;
    }

    class ViewHolder
    {
        ImageView img;
        TextView name;
        TextView isfollow;
        TextView last;
        TextView time;
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
}
