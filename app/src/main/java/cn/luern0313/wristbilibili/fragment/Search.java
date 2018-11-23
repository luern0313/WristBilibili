package cn.luern0313.wristbilibili.fragment;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.luern0313.wristbilibili.R;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

/**
 * Created by liupe on 2018/11/16.
 * 搜索。。
 */

public class Search extends Fragment
{
    Context ctx;

    View rootLayout;
    ListView seaListView;
    WaveSwipeRefreshLayout waveSwipeRefreshLayout;

    Handler handler = new Handler();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_search, container, false);
        seaListView = rootLayout.findViewById(R.id.sea_listview);
        waveSwipeRefreshLayout = rootLayout.findViewById(R.id.sea_swipe);
        View loadingView = inflater.inflate(R.layout.widget_dyloading, null);
        seaListView.addFooterView(loadingView);

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

                    }
                });
            }
        });
        return rootLayout;
    }

    class mAdapter extends BaseAdapter
    {
        private LayoutInflater mInflater;

        private LruCache<String, BitmapDrawable> mImageCache;

        private JSONArray seaList;

        public mAdapter(LayoutInflater inflater, JSONArray seaList)
        {
            mInflater = inflater;
            this.seaList = seaList;

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
            return seaList.length();
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
            JSONObject v = null;
            try {v = seaList.getJSONObject(position);}
            catch (JSONException e) {e.printStackTrace();}
            ViewHolder viewHolder;
            if(convertView == null)
            {
                convertView = mInflater.inflate(R.layout.item_aniremind, null);
                viewHolder = new ViewHolder();
                convertView.setTag(viewHolder);
                viewHolder.img = convertView.findViewById(R.id.vid_img);
                viewHolder.title = convertView.findViewById(R.id.vid_title);
                viewHolder.up = convertView.findViewById(R.id.vid_up);
                viewHolder.play = convertView.findViewById(R.id.vid_play);
            }
            else
            {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.img.setImageResource(R.drawable.img_default_vid);
            viewHolder.title.setText((String) getInfoFromJson(v, "title"));
            viewHolder.up.setText("UP:" + getInfoFromJson(v, "author"));
            viewHolder.play.setText("播放:" + getView((int) getInfoFromJson(v, "play")) + "  弹幕:" + getInfoFromJson(v, "video_review"));

            viewHolder.img.setTag((String) getInfoFromJson(v, "pic"));
            BitmapDrawable c = setImageFormWeb((String) getInfoFromJson(v, "pic"));
            if(c != null) viewHolder.img.setImageDrawable(c);
            return convertView;
        }

        class ViewHolder
        {
            ImageView img;
            TextView title;
            TextView up;
            TextView play;
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

        private String getView(int view)
        {
            if(view > 10000) return view / 1000 / 10.0 + "万";
            else return String.valueOf(view);
        }
        private Object getInfoFromJson(JSONObject json, String get)
        {
            try
            {
                return json.get(get);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            return null;
        }

        private JSONObject getJsonFromJson(JSONObject json, String get)
        {
            try
            {
                return json.getJSONObject(get);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            return null;
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
                    bitmap = downloadImage();
                    BitmapDrawable db = new BitmapDrawable(seaListView.getResources(), bitmap);
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
                ImageView iv = seaListView.findViewWithTag(imageUrl);
                if(iv != null && result != null)
                {
                    iv.setImageDrawable(result);
                }
            }

            /**
             * 根据url从网络上下载图片
             *
             * @return
             */
            private Bitmap downloadImage() throws IOException
            {
                HttpURLConnection con = null;
                Bitmap bitmap = null;
                URL url = new URL(imageUrl);
                con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(5 * 1000);
                con.setReadTimeout(10 * 1000);
                bitmap = BitmapFactory.decodeStream(con.getInputStream());
                if(con != null)
                {
                    con.disconnect();
                }
                return bitmap;
            }
        }

    }
}
