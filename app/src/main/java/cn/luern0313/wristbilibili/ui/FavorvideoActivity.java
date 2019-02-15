package cn.luern0313.wristbilibili.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.FavorVideoApi;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

public class FavorvideoActivity extends Activity
{
    Context ctx;
    Intent intent;
    LayoutInflater inflater;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    FavorVideoApi favorVideoApi;

    ArrayList<JSONObject> favorvideoList;
    String fid;

    mAdapter adapter;
    ListView favvListView;
    WaveSwipeRefreshLayout waveSwipeRefreshLayout;
    View loadingView;

    Handler handler = new Handler();
    Runnable runnableUi;
    Runnable runnableNoWeb;
    Runnable runnableNoWebH;
    Runnable runnableNodata;
    Runnable runnableAddlist;
    Runnable runnableNomore;

    int page = 0;
    boolean isLoading = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorvideo);
        ctx = this;
        intent = getIntent();
        inflater = getLayoutInflater();
        sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        fid = intent.getStringExtra("fid");

        loadingView = inflater.inflate(R.layout.widget_dyloading, null);
        favvListView = findViewById(R.id.favv_listview);
        waveSwipeRefreshLayout = findViewById(R.id.favv_swipe);
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
                        favvListView.setVisibility(View.GONE);
                        getFavorVideo();
                    }
                });
            }
        });

        runnableUi = new Runnable()
        {
            @Override
            public void run()
            {
                isLoading = false;
                findViewById(R.id.favv_noweb).setVisibility(View.GONE);
                findViewById(R.id.favv_nonthing).setVisibility(View.GONE);
                favvListView.setVisibility(View.VISIBLE);

                waveSwipeRefreshLayout.setRefreshing(false);
                adapter = new mAdapter(inflater, favorvideoList);
                favvListView.setAdapter(adapter);
            }
        };

        runnableNoWeb = new Runnable()
        {
            @Override
            public void run()
            {
                findViewById(R.id.favv_noweb).setVisibility(View.VISIBLE);
                findViewById(R.id.favv_nonthing).setVisibility(View.GONE);
                favvListView.setVisibility(View.GONE);
            }
        };

        runnableNoWebH = new Runnable()
        {
            @Override
            public void run()
            {
                ((TextView) loadingView.findViewById(R.id.dyload_text)).setText("好像没有网络...\n检查下网络？");
                loadingView.findViewById(R.id.dyload_button).setVisibility(View.VISIBLE);
            }
        };

        runnableNomore = new Runnable()
        {
            @Override
            public void run()
            {
                ((TextView) loadingView.findViewById(R.id.dyload_text)).setText("  没有更多了...");
            }
        };

        runnableAddlist = new Runnable()
        {
            @Override
            public void run()
            {
                isLoading = false;
                adapter.notifyDataSetChanged();
            }
        };

        loadingView.findViewById(R.id.dyload_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((TextView) loadingView.findViewById(R.id.dyload_text)).setText(" 加载中. . .");
                loadingView.findViewById(R.id.dyload_button).setVisibility(View.GONE);
                getMoreFavorVideo();
            }
        });

        favvListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if(position < favorvideoList.size())
                {
                    Intent intent = new Intent(ctx, VideodetailsActivity.class);
                    intent.putExtra("aid", String.valueOf(favorvideoList.get(position).opt("aid")));
                    startActivity(intent);
                }
            }
        });

        favvListView.setOnScrollListener(new AbsListView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState)
            {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
            {
                if(visibleItemCount + firstVisibleItem == totalItemCount && !isLoading)
                {
                    getMoreFavorVideo();
                }
            }
        });

        waveSwipeRefreshLayout.setRefreshing(true);
        favvListView.addFooterView(loadingView);
        getFavorVideo();
    }

    void getFavorVideo()
    {
        isLoading = true;
        favorVideoApi = new FavorVideoApi(sharedPreferences.getString("cookies", ""), sharedPreferences.getString("mid", ""), fid);
        page = 1;
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    favorvideoList = favorVideoApi.getFavorvideo(page);
                    if(favorvideoList != null && favorvideoList.size() != 0)
                        handler.post(runnableUi);
                    else handler.post(runnableNodata);
                }
                catch (IOException e)
                {
                    handler.post(runnableNoWeb);
                    e.printStackTrace();
                }
            }
        }).start();
    }

    void getMoreFavorVideo()
    {
        isLoading = true;
        page++;
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    ArrayList<JSONObject> arrayList = favorVideoApi.getFavorvideo(page);
                    if(arrayList != null && arrayList.size() != 0)
                    {
                        favorvideoList.addAll(arrayList);
                        handler.post(runnableAddlist);
                    }
                    else handler.post(runnableNomore);
                }
                catch (IOException e)
                {
                    page--;
                    handler.post(runnableNoWebH);
                    e.printStackTrace();
                }
            }
        }).start();
    }

    class mAdapter extends BaseAdapter
    {
        private LayoutInflater mInflater;

        private LruCache<String, BitmapDrawable> mImageCache;

        private ArrayList<JSONObject> favList;

        public mAdapter(LayoutInflater inflater, ArrayList<JSONObject> favList)
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
            JSONObject v = favList.get(position);
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
            }
            else
            {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.img.setImageResource(R.drawable.img_default_vid);
            viewHolder.title.setText((String) getInfoFromJson(v, "title"));
            viewHolder.up.setText("UP : " + getInfoFromJson(getJsonFromJson(v, "owner"), "name"));
            viewHolder.play.setText("播放 : " + getView((int) getInfoFromJson(getJsonFromJson(v, "stat"), "view")) + "  弹幕 : " + getInfoFromJson(getJsonFromJson(v, "stat"), "danmaku"));

            viewHolder.img.setTag(getInfoFromJson(v, "pic"));
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
                mAdapter.ImageTask it = new mAdapter.ImageTask();
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
                    BitmapDrawable db = new BitmapDrawable(favvListView.getResources(), bitmap);
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
                ImageView iv = favvListView.findViewWithTag(imageUrl);
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
                int realWith = 136;
                int realHeight = 136;

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
