package cn.luern0313.wristbilibili.fragment;

import android.app.Fragment;
import android.content.Context;
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
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.AnimationTimelineApi;
import cn.luern0313.wristbilibili.api.AnimationTimelineApi.Anim;
import cn.luern0313.wristbilibili.ui.MainActivity;
import cn.luern0313.wristbilibili.widget.ImageDownloader;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

/**
 * Created by liupe on 2018/11/10.
 */

public class AniRemind extends Fragment
{
    Context ctx;
    View rootLayout;
    ListView arListView;
    WaveSwipeRefreshLayout waveSwipeRefreshLayout;

    public static boolean isLogin;
    AnimationTimelineApi animationTimelineApi;
    ArrayList<Anim> animationTimelineList;

    Handler handler = new Handler();
    Runnable runnableUi;
    Runnable runnableNoWeb;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_aniremind, container, false);
        arListView = rootLayout.findViewById(R.id.ar_listview);
        waveSwipeRefreshLayout = rootLayout.findViewById(R.id.ar_swipe);
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
                            arListView.setVisibility(View.GONE);
                            getAnimTimeline();
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
                rootLayout.findViewById(R.id.ar_nologin).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.ar_noweb).setVisibility(View.GONE);
                arListView.setAdapter(new mAdapter(inflater, animationTimelineList));
                arListView.setVisibility(View.VISIBLE);
                waveSwipeRefreshLayout.setRefreshing(false);
            }
        };

        runnableNoWeb = new Runnable()
        {
            @Override
            public void run()
            {
                waveSwipeRefreshLayout.setRefreshing(false);
                rootLayout.findViewById(R.id.ar_noweb).setVisibility(View.VISIBLE);
                rootLayout.findViewById(R.id.ar_nologin).setVisibility(View.GONE);
            }
        };

        isLogin = MainActivity.sharedPreferences.contains("cookies");
        if(isLogin)
        {
            waveSwipeRefreshLayout.setRefreshing(true);
            getAnimTimeline();
        }
        else
        {
            rootLayout.findViewById(R.id.ar_noweb).setVisibility(View.GONE);
            rootLayout.findViewById(R.id.ar_nologin).setVisibility(View.VISIBLE);
        }

        return rootLayout;
    }

    void getAnimTimeline()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    animationTimelineApi = new AnimationTimelineApi(MainActivity.sharedPreferences.getString("cookies", ""));
                    animationTimelineList = animationTimelineApi.getAnimTimelineList();
                    handler.post(runnableUi);
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

        private ArrayList<Anim> arList;

        public mAdapter(LayoutInflater inflater, ArrayList<Anim> arList)
        {
            mInflater = inflater;
            this.arList = arList;

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
            Anim anim = arList.get(position);
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
                    BitmapDrawable db = new BitmapDrawable(arListView.getResources(), bitmap);
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
        }
    }
}
