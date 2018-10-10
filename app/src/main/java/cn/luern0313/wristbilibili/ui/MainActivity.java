package cn.luern0313.wristbilibili.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import cn.luern0313.wristbilibili.R;

public class MainActivity extends Activity
{
    Context ctx;

    ListView mainListView;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ctx = this;

        mainListView = findViewById(R.id.main_list);
        mAdapter adapter = new mAdapter(getLayoutInflater());
        mainListView.setAdapter(adapter);
    }

    public void buttonTitle(View view)
    {
        Intent intent = new Intent(ctx, MenuActivity.class);
        startActivity(intent);
    }

    class mAdapter extends BaseAdapter
    {
        private LayoutInflater mInflater;

        private LruCache<String, BitmapDrawable> mImageCache;

        public mAdapter(LayoutInflater inflater)
        {
            mInflater = inflater;

            int maxCache = (int) Runtime.getRuntime().maxMemory();
            int cacheSize = maxCache / 8;
            mImageCache = new LruCache<String, BitmapDrawable>(cacheSize)
            {
                @Override
                protected int sizeOf(String key, BitmapDrawable value)
                {
                    return value.getBitmap().getByteCount();
                }
            };
        }

        @Override
        public int getCount()
        {
            return 8;
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
            //Log.i("part", position + "," + mImgurl.get(position));
            ViewHolder viewHolder;
            // 若无可重用的 view 则进行加载
            if(convertView == null)
            {
                convertView = mInflater.inflate(R.layout.item_liveitem, null);
                // 初始化 ViewHolder 方便重用
                viewHolder = new ViewHolder();
                convertView.setTag(viewHolder);
            }
            else
            { // 否则进行重用
                viewHolder = (ViewHolder) convertView.getTag();
            }

            /*if(mImgurl.size() != 0)
            {
                viewHolder.vImg.setTag(mImgurl.get(position));
                if(mImgurl.get(position).charAt(0) == 'h')
                {
                    if(mImageCache.get(mImgurl.get(position)) != null)
                    {
                        viewHolder.vImg.setImageDrawable(mImageCache.get(mImgurl.get(position)));
                    }
                    else
                    {
                        ImageTask it = new ImageTask();
                        it.execute(mImgurl.get(position));
                    }
                }
            }*/
            return convertView;
        }

        class ViewHolder
        {
            ImageView vImg;
            TextView vTitle;
            TextView vCount;
            TextView vCountt;
        }

        /*class ImageTask extends AsyncTask<String, Void, BitmapDrawable>
        {
            private String imageUrl;

            @Override
            protected BitmapDrawable doInBackground(String... params)
            {
                imageUrl = params[0];
                Bitmap bitmap = downloadImage();
                BitmapDrawable db = new BitmapDrawable(boxListview.getResources(), bitmap);
                // 如果本地还没缓存该图片，就缓存
                if(mImageCache.get(imageUrl) == null)
                {
                    mImageCache.put(imageUrl, db);
                }
                return db;
            }

            @Override
            protected void onPostExecute(BitmapDrawable result)
            {
                // 通过Tag找到我们需要的ImageView，如果该ImageView所在的item已被移出页面，就会直接返回null
                ImageView iv = boxListview.findViewWithTag(imageUrl);
                if(iv != null && result != null)
                {
                    iv.setImageDrawable(result);
                }
            }*/

            /**
             * 根据url从网络上下载图片
             *
             * @return
             */
            /*private Bitmap downloadImage()
            {
                HttpURLConnection con = null;
                Bitmap bitmap = null;
                try
                {
                    URL url = new URL(imageUrl);
                    con = (HttpURLConnection) url.openConnection();
                    con.setConnectTimeout(5 * 1000);
                    con.setReadTimeout(10 * 1000);
                    bitmap = BitmapFactory.decodeStream(con.getInputStream());
                } catch (MalformedURLException e)
                {
                    e.printStackTrace();
                } catch (IOException e)
                {
                    e.printStackTrace();
                } finally
                {
                    if(con != null)
                    {
                        con.disconnect();
                    }
                }

                return bitmap;
            }

        }*/

    }
}
