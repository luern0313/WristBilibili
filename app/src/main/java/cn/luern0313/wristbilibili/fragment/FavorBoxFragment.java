package cn.luern0313.wristbilibili.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.FavorBoxApi;
import cn.luern0313.wristbilibili.ui.FavorvideoActivity;
import cn.luern0313.wristbilibili.ui.MainActivity;
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
    ArrayList<FavorBoxApi.FavorBox> favourboxArrayList;

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

        private ArrayList<FavorBoxApi.FavorBox> favList;

        public mAdapter(LayoutInflater inflater, ArrayList<FavorBoxApi.FavorBox> favList)
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
            FavorBoxApi.FavorBox box = favList.get(position);
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
            //viewHolder.img.setImageResource(R.color.textColor);
            viewHolder.countt.setText(box.count);
            viewHolder.title.setText(box.title);
            viewHolder.see.setText(box.see ? "公开" : "私有");
            viewHolder.count.setText(box.count + "个视频");

            /*try
            {
                viewHolder.img.setTag(box.optJSONArray("cover").optJSONObject(0).opt("pic"));
                BitmapDrawable c = setImageFormWeb((String) box.optJSONArray("cover").optJSONObject(0).opt("pic"));
                if(c != null) viewHolder.img.setImageDrawable(c);
            }
            catch (Exception e)
            {
                viewHolder.img.setImageResource(R.drawable.img_default_vid);
            }*/
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
                    bitmap = downloadImage();
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

            /**
             * 获得需要压缩的比率
             *
             * @param options 需要传入已经BitmapFactory.decodeStream(is, null, options);
             * @return 返回压缩的比率，最小为1
             */
            public int getInSampleSize(BitmapFactory.Options options)
            {
                int inSampleSize = 1;
                int realWith = 140;
                int realHeight = 140;

                int outWidth = options.outWidth;
                int outHeight = options.outHeight;

                //获取比率最大的那个
                if(outWidth > realWith || outHeight > realHeight)
                {
                    int withRadio = Math.round(outWidth / realWith);
                    int heightRadio = Math.round(outHeight / realHeight);
                    inSampleSize = withRadio > heightRadio ? withRadio : heightRadio;
                }
                return inSampleSize;
            }

            /**
             * 根据输入流返回一个压缩的图片
             *
             * @param input 图片的输入流
             * @return 压缩的图片
             */
            public Bitmap getCompressBitmap(InputStream input)
            {
                //因为InputStream要使用两次，但是使用一次就无效了，所以需要复制两个
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try
                {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = input.read(buffer)) > -1)
                    {
                        baos.write(buffer, 0, len);
                    }
                    baos.flush();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                //复制新的输入流
                InputStream is = new ByteArrayInputStream(baos.toByteArray());
                InputStream is2 = new ByteArrayInputStream(baos.toByteArray());

                //只是获取网络图片的大小，并没有真正获取图片
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(is, null, options);
                //获取图片并进行压缩
                options.inSampleSize = getInSampleSize(options);
                options.inJustDecodeBounds = false;
                return BitmapFactory.decodeStream(is2, null, options);
            }

            /**
             * 根据url从网络上下载图片
             *
             * @return 图片
             */
            private Bitmap downloadImage() throws IOException
            {
                HttpURLConnection con = null;
                Bitmap bitmap = null;
                URL url = new URL(imageUrl);
                con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(5 * 1000);
                con.setReadTimeout(10 * 1000);
                bitmap = getCompressBitmap(con.getInputStream());
                if(con != null)
                {
                    con.disconnect();
                }
                return bitmap;
            }
        }

    }
}
