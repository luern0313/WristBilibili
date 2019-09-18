package cn.luern0313.wristbilibili.fragment;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.util.LruCache;
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

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.DownloadApi;
import cn.luern0313.wristbilibili.api.DownloadApi.DownloadItem;
import cn.luern0313.wristbilibili.widget.ImageDownloader;

public class Download extends Fragment
{
    Context ctx;
    View rootLayout;
    mAdapter mAdapter;

    ListView uiListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = this.getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_download, container, false);

        uiListView = rootLayout.findViewById(R.id.dl_listview);
        mAdapter = new mAdapter(inflater, DownloadApi.fakeData());
        uiListView.setAdapter(mAdapter);


        return rootLayout;
    }

    String getSize(long size)
    {
        String[] unit = new String[]{"KB", "MB", "GB", "TB"};
        long s = size * 10;
        int u = 0;
        while (s > 10240 && u < unit.length - 1)
        {
            s /= 1024;
            u++;
        }
        return String.valueOf(s / 10.0) + unit[u];
    }

    class mAdapter extends BaseAdapter
    {
        private LayoutInflater mInflater;

        private LruCache<String, BitmapDrawable> mImageCache;

        private ArrayList<DownloadItem> downloadItemList;

        public mAdapter(LayoutInflater inflater, ArrayList<DownloadItem> arList)
        {
            mInflater = inflater;
            this.downloadItemList = arList;

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
            return downloadItemList.size();
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
            if(downloadItemList.get(position).mode == 2) return 2;
            else return 1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup)
        {
            DownloadItem downloadItem = downloadItemList.get(position);
            int type = getItemViewType(position);
            ViewHolder viewHolder = null;
            if(convertView == null)
            {
                if(type == 2)
                {
                    convertView = mInflater.inflate(R.layout.widget_reply_changemode, null);
                    ((TextView) convertView.findViewById(R.id.item_reply_sign)).setText(downloadItem.title);
                }
                else
                {
                    convertView = mInflater.inflate(R.layout.item_dl_dling, null);
                    viewHolder = new ViewHolder();
                    convertView.setTag(viewHolder);
                    viewHolder.img = convertView.findViewById(R.id.dling_img);
                    viewHolder.title = convertView.findViewById(R.id.dling_title);
                    viewHolder.size = convertView.findViewById(R.id.dling_size);
                    viewHolder.time = convertView.findViewById(R.id.dling_time);
                    viewHolder.prog = convertView.findViewById(R.id.dling_prog);
                }
            }
            else
            {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if(type == 1)
            {
                viewHolder.title.setText(downloadItem.title);
                viewHolder.size.setText(getSize(downloadItem.size));
                viewHolder.img.setImageResource(R.drawable.img_default_animation);

                if(downloadItem.mode == 0)
                {
                    viewHolder.time.setVisibility(View.GONE);
                    viewHolder.prog.setVisibility(View.GONE);
                }
                else
                {
                    viewHolder.time.setText("1小时42分钟"); //fake data, remember to change
                    viewHolder.prog.setProgress(6); //fake data, remember to change
                }

                viewHolder.img.setTag(downloadItem.cover);
                BitmapDrawable c = setImageFormWeb(downloadItem.cover);
                if(c != null) viewHolder.img.setImageDrawable(c);
            }
            return convertView;
        }

        class ViewHolder
        {
            ImageView img;
            TextView title;
            TextView size;
            TextView time;
            ProgressBar prog;
        }

        BitmapDrawable setImageFormWeb(String url)
        {
            if(mImageCache.get(url) != null)
            {
                return mImageCache.get(url);
            }
            else
            {
                ImageTask it = new ImageTask();
                it.execute(url);
                return null;
            }
        }

        class ImageTask extends AsyncTask<String, Void, BitmapDrawable>
        {
            private String imageUrl;

            @Override
            protected BitmapDrawable doInBackground(String... params)
            {
                try
                {
                    imageUrl = params[0];
                    Bitmap bitmap = null;
                    bitmap = ImageDownloader.downloadImage(imageUrl);
                    BitmapDrawable db = new BitmapDrawable(uiListView.getResources(), bitmap);
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
                ImageView iv = uiListView.findViewWithTag(imageUrl);
                if(iv != null && result != null)
                {
                    iv.setImageDrawable(result);
                }
            }
        }
    }
}
