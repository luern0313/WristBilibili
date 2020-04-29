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
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import androidx.collection.LruCache;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.models.FavorBoxModel;
import cn.luern0313.wristbilibili.util.ImageDownloaderUtil;

/**
 * 被 luern0313 创建于 2020/4/25.
 */
public class FavorBoxAdapter extends BaseAdapter
{
    private LayoutInflater mInflater;

    private LruCache<String, BitmapDrawable> mImageCache;
    private FavorBoxAdapterListener favorBoxAdapterListener;

    private ArrayList<FavorBoxModel> favList;
    private ListView listView;

    public FavorBoxAdapter(LayoutInflater inflater, ArrayList<FavorBoxModel> favList, ListView listView, FavorBoxAdapterListener favorBoxAdapterListener)
    {
        mInflater = inflater;
        this.favList = favList;
        this.listView = listView;
        this.favorBoxAdapterListener = favorBoxAdapterListener;

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
        FavorBoxModel box = favList.get(position);
        ViewHolder viewHolder;
        if(convertView == null)
        {
            convertView = mInflater.inflate(R.layout.item_favor_box, null);
            viewHolder = new ViewHolder();
            convertView.setTag(viewHolder);
            viewHolder.lay = convertView.findViewById(R.id.favor_lay);
            viewHolder.img = convertView.findViewById(R.id.favor_img);
            viewHolder.countt = convertView.findViewById(R.id.favor_countt);
            viewHolder.title = convertView.findViewById(R.id.favor_title);
            viewHolder.see = convertView.findViewById(R.id.favor_see);
            viewHolder.count = convertView.findViewById(R.id.favor_count);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.img.setImageResource(R.drawable.img_default_vid);
        viewHolder.countt.setText(box.count);
        viewHolder.title.setText(box.title);
        viewHolder.see.setText(box.see ? "公开" : "私有");
        viewHolder.count.setText(box.count + "个视频");
        viewHolder.lay.setOnClickListener(onViewClick(position));

        try
        {
            viewHolder.img.setTag(box.img);
            BitmapDrawable c = setImageFormWeb(box.img);
            if(c != null) viewHolder.img.setImageDrawable(c);
        }
        catch (Exception e)
        {
            viewHolder.img.setImageResource(R.drawable.img_default_vid);
        }
        return convertView;
    }

    class ViewHolder
    {
        RelativeLayout lay;
        ImageView img;
        TextView countt;
        TextView title;
        TextView see;
        TextView count;
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

    private View.OnClickListener onViewClick(final int position)
    {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                favorBoxAdapterListener.onClick(v.getId(), position);
            }
        };
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
            ImageView iv = listView.findViewWithTag(imageUrl);
            if(iv != null && result != null)
            {
                iv.setImageDrawable(result);
            }
        }
    }

    public interface FavorBoxAdapterListener
    {
        void onClick(int viewId, int position);
    }
}
