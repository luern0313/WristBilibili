package cn.luern0313.wristbilibili.adapter;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.LruCache;
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
import cn.luern0313.wristbilibili.models.FavorVideoModel;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.ImageDownloaderUtil;

/**
 * 被 luern0313 创建于 2020/2/2.
 */

public class FavorVideoAdapter extends BaseAdapter
{
    private LayoutInflater mInflater;

    private LruCache<String, BitmapDrawable> mImageCache;
    private ListView listView;

    private ArrayList<FavorVideoModel> favList;

    public FavorVideoAdapter(LayoutInflater inflater, ArrayList<FavorVideoModel> favList, ListView listView)
    {
        mInflater = inflater;
        this.favList = favList;
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
        return favList.size();
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
        FavorVideoModel video = favList.get(position);
        ViewHolder viewHolder;
        if(convertView == null)
        {
            convertView = mInflater.inflate(R.layout.item_favor_video, null);
            viewHolder = new ViewHolder();
            convertView.setTag(viewHolder);
            viewHolder.img = convertView.findViewById(R.id.vid_img);
            viewHolder.title = convertView.findViewById(R.id.vid_title);
            viewHolder.up = convertView.findViewById(R.id.vid_up);
            viewHolder.play = convertView.findViewById(R.id.vid_play);
            viewHolder.danmaku = convertView.findViewById(R.id.vid_danmaku);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Drawable upDrawable = convertView.getResources().getDrawable(R.drawable.icon_video_up);
        Drawable playNumDrawable = convertView.getResources().getDrawable(R.drawable.icon_video_play_num);
        Drawable danmakuNumDrawable = convertView.getResources().getDrawable(R.drawable.icon_video_danmu_num);
        upDrawable.setBounds(0, 0, DataProcessUtil.dip2px(listView.getContext(), 10), DataProcessUtil.dip2px(listView.getContext(), 10));
        playNumDrawable.setBounds(0,0, DataProcessUtil.dip2px(listView.getContext(), 10), DataProcessUtil.dip2px(listView.getContext(), 10));
        danmakuNumDrawable.setBounds(0,0, DataProcessUtil.dip2px(listView.getContext(), 10), DataProcessUtil.dip2px(listView.getContext(), 10));
        viewHolder.up.setCompoundDrawables(upDrawable,null, null,null);
        viewHolder.play.setCompoundDrawables(playNumDrawable,null, null,null);
        viewHolder.danmaku.setCompoundDrawables(danmakuNumDrawable,null, null,null);

        viewHolder.img.setImageResource(R.drawable.img_default_vid);
        viewHolder.title.setText(video.video_title);
        viewHolder.up.setText(video.video_owner_name);
        viewHolder.play.setText(video.video_play);
        viewHolder.danmaku.setText(video.video_danmaku);

        viewHolder.img.setTag(video.video_cover);
        BitmapDrawable c = setImageFormWeb(video.video_cover);
        if(c != null) viewHolder.img.setImageDrawable(c);
        return convertView;
    }

    class ViewHolder
    {
        ImageView img;
        TextView title;
        TextView up;
        TextView play;
        TextView danmaku;
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

    class ImageTask extends AsyncTask<String, Void, BitmapDrawable>
    {
        private String imageUrl;
        private Resources listViewResources;

        ImageTask(ListView listView)
        {
            this.listViewResources = listView.getResources();
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
