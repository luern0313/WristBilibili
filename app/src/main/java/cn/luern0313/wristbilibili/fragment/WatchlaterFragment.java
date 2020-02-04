package cn.luern0313.wristbilibili.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.WatchLaterApi;
import cn.luern0313.wristbilibili.models.WatchLaterModel;
import cn.luern0313.wristbilibili.ui.MainActivity;
import cn.luern0313.wristbilibili.ui.VideodetailsActivity;
import cn.luern0313.wristbilibili.util.ImageDownloaderUtil;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

/**
 * 被 luern0313 创建于 2019/8/31.
 * 稍后再看
 */

public class WatchlaterFragment extends Fragment
{
    Context ctx;
    View rootLayout;
    ListView wlListView;
    WaveSwipeRefreshLayout waveSwipeRefreshLayout;
    WatchLaterApi watchLaterApi;

    public static boolean isLogin;

    Handler handler = new Handler();
    Runnable runnableUi;
    Runnable runnableNoWeb;
    Runnable runnableNoData;

    ArrayList<WatchLaterModel> watchLaterVideoArrayList;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();

        rootLayout = inflater.inflate(R.layout.fragment_watchlater, container, false);
        wlListView = rootLayout.findViewById(R.id.wl_listview);
        waveSwipeRefreshLayout = rootLayout.findViewById(R.id.wl_swipe);
        waveSwipeRefreshLayout.setColorSchemeColors(Color.WHITE, Color.WHITE);
        waveSwipeRefreshLayout.setWaveColor(Color.argb(255, 250, 114, 152));
        waveSwipeRefreshLayout.setOnRefreshListener(new WaveSwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                handler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if(isLogin)
                        {
                            wlListView.setVisibility(View.GONE);
                            getWatchLater();
                        }
                        else waveSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });

        runnableUi = new Runnable()
        {
            @Override
            public void run()
            {
                rootLayout.findViewById(R.id.wl_nologin).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.wl_noweb).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.wl_nonthing).setVisibility(View.GONE);
                wlListView.setAdapter(new mAdapter(inflater, watchLaterVideoArrayList));
                wlListView.setVisibility(View.VISIBLE);
                waveSwipeRefreshLayout.setRefreshing(false);
            }
        };

        runnableNoWeb = new Runnable()
        {
            @Override
            public void run()
            {
                waveSwipeRefreshLayout.setRefreshing(false);
                rootLayout.findViewById(R.id.wl_noweb).setVisibility(View.VISIBLE);
                rootLayout.findViewById(R.id.wl_nologin).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.wl_nonthing).setVisibility(View.GONE);
            }
        };

        runnableNoData = new Runnable()
        {
            @Override
            public void run()
            {
                waveSwipeRefreshLayout.setRefreshing(false);
                rootLayout.findViewById(R.id.wl_nonthing).setVisibility(View.VISIBLE);
                rootLayout.findViewById(R.id.wl_noweb).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.wl_nologin).setVisibility(View.GONE);
            }
        };

        wlListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Intent intent = new Intent(ctx, VideodetailsActivity.class);
                intent.putExtra("aid", watchLaterVideoArrayList.get(position).aid);
                startActivity(intent);
            }
        });

        isLogin = MainActivity.sharedPreferences.contains("cookies");
        if(isLogin)
        {
            waveSwipeRefreshLayout.setRefreshing(true);
            getWatchLater();
        }
        else
        {
            rootLayout.findViewById(R.id.wl_noweb).setVisibility(View.GONE);
            rootLayout.findViewById(R.id.wl_nologin).setVisibility(View.VISIBLE);
        }

        return rootLayout;
    }

    void getWatchLater()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    watchLaterApi = new WatchLaterApi(MainActivity.sharedPreferences.getString("cookies", ""),
                        MainActivity.sharedPreferences.getString("csrf", ""),
                        MainActivity.sharedPreferences.getString("mid", ""));
                    watchLaterVideoArrayList = watchLaterApi.getWatchLater();
                    if(watchLaterVideoArrayList != null && watchLaterVideoArrayList.size() != 0)
                    {
                        handler.post(runnableUi);
                    }
                    else
                    {
                        handler.post(runnableNoData);
                    }
                }
                catch (NullPointerException e)
                {
                    handler.post(runnableNoData);
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    handler.post(runnableNoWeb);
                    e.printStackTrace();
                }
            }
        }).start();
    }

    class mAdapter extends BaseAdapter
    {
        private LayoutInflater mInflater;

        private LruCache<String, BitmapDrawable> mImageCache;

        private ArrayList<WatchLaterModel> wlList;

        public mAdapter(LayoutInflater inflater, ArrayList<WatchLaterModel> wlList)
        {
            mInflater = inflater;
            this.wlList = wlList;

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
                convertView = mInflater.inflate(R.layout.item_favor_video, null);
                viewHolder = new ViewHolder();
                convertView.setTag(viewHolder);
                viewHolder.title = convertView.findViewById(R.id.vid_title);
                viewHolder.img = convertView.findViewById(R.id.vid_img);
                viewHolder.up = convertView.findViewById(R.id.vid_up);
                viewHolder.play = convertView.findViewById(R.id.vid_play);
                viewHolder.pro = convertView.findViewById(R.id.vid_pro);
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
                    bitmap = ImageDownloaderUtil.downloadImage(imageUrl);
                    BitmapDrawable db = new BitmapDrawable(wlListView.getResources(), bitmap);
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
                ImageView iv = wlListView.findViewWithTag(imageUrl);
                if(iv != null && result != null)
                {
                    iv.setImageDrawable(result);
                }
            }
        }
    }
}
