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
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.FavorBoxApi;
import cn.luern0313.wristbilibili.models.FavorBoxModel;
import cn.luern0313.wristbilibili.ui.FavorvideoActivity;
import cn.luern0313.wristbilibili.ui.MainActivity;
import cn.luern0313.wristbilibili.widget.ImageDownloader;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

/**
 * 被 luern0313 创建于 2018/11/16.
 * 收藏的fragment
 * 畜生！你收藏了甚么！
 */

public class FavorBoxFragment extends Fragment
{
    Context ctx;
    FavorBoxApi favorBoxApi;
    ArrayList<FavorBoxModel> favourboxArrayList;

    View rootLayout;
    ListView favListView;
    WaveSwipeRefreshLayout waveSwipeRefreshLayout;

    public static boolean isLogin;

    Handler handler = new Handler();
    Runnable runnableUi;
    Runnable runnableNoWeb;
    Runnable runnableNodata;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_favor, container, false);
        favListView = rootLayout.findViewById(R.id.fav_listview);
        waveSwipeRefreshLayout = rootLayout.findViewById(R.id.fav_swipe);
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
                            favListView.setVisibility(View.GONE);
                            getFavorbox();
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
                rootLayout.findViewById(R.id.fav_nologin).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.fav_noweb).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.fav_nonthing).setVisibility(View.GONE);
                favListView.setAdapter(new mAdapter(inflater, favourboxArrayList));
                favListView.setVisibility(View.VISIBLE);
                waveSwipeRefreshLayout.setRefreshing(false);
            }
        };

        runnableNoWeb = new Runnable()
        {
            @Override
            public void run()
            {
                rootLayout.findViewById(R.id.fav_nologin).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.fav_noweb).setVisibility(View.VISIBLE);
                rootLayout.findViewById(R.id.fav_nonthing).setVisibility(View.GONE);
                favListView.setVisibility(View.GONE);
                waveSwipeRefreshLayout.setRefreshing(false);
            }
        };

        runnableNodata = new Runnable()
        {
            @Override
            public void run()
            {
                rootLayout.findViewById(R.id.fav_nologin).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.fav_noweb).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.fav_nonthing).setVisibility(View.VISIBLE);
                favListView.setVisibility(View.GONE);
                waveSwipeRefreshLayout.setRefreshing(false);
            }
        };

        isLogin = MainActivity.sharedPreferences.contains("cookies");
        if(isLogin)
        {
            waveSwipeRefreshLayout.setRefreshing(true);
            getFavorbox();
        }
        else
        {
            rootLayout.findViewById(R.id.fav_noweb).setVisibility(View.GONE);
            rootLayout.findViewById(R.id.fav_nologin).setVisibility(View.VISIBLE);
        }

        favListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Intent intent = new Intent(ctx, FavorvideoActivity.class);
                intent.putExtra("fid", favourboxArrayList.get(position).fid);
                startActivity(intent);
            }
        });

        return rootLayout;
    }

    void getFavorbox()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    favorBoxApi = new FavorBoxApi(MainActivity.sharedPreferences.getString("cookies", ""), MainActivity.sharedPreferences.getString("mid", ""));
                    favourboxArrayList = favorBoxApi.getFavorbox();
                    if(favourboxArrayList != null && favourboxArrayList.size() != 0)
                    {
                        handler.post(runnableUi);
                    }
                    else
                    {
                        handler.post(runnableNodata);
                    }
                }
                catch (NullPointerException e)
                {
                    handler.post(runnableNodata);
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

        private ArrayList<FavorBoxModel> favList;

        public mAdapter(LayoutInflater inflater, ArrayList<FavorBoxModel> favList)
        {
            mInflater = inflater;
            this.favList = favList;

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
                mAdapter.ImageTask it = new mAdapter.ImageTask();
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
                    BitmapDrawable db = new BitmapDrawable(favListView.getResources(), bitmap);
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
                ImageView iv = favListView.findViewWithTag(imageUrl);
                if(iv != null && result != null)
                {
                    iv.setImageDrawable(result);
                }
            }
        }
    }
}
