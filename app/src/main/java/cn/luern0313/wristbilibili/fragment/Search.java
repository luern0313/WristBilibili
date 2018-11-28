package cn.luern0313.wristbilibili.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import cn.luern0313.wristbilibili.api.SearchApi;
import cn.luern0313.wristbilibili.api.VideoDetails;
import cn.luern0313.wristbilibili.ui.MainActivity;
import cn.luern0313.wristbilibili.ui.VideodetailsActivity;

/**
 * Created by liupe on 2018/11/16.
 * 搜索。。
 */

public class Search extends Fragment
{
    Context ctx;
    SearchApi searchApi;

    View rootLayout;
    ListView seaListView;
    View loadingView;
    EditText seaEdittext;
    TextView seaButton;
    ImageView seaLoadImg;

    Handler handler = new Handler();
    Runnable runnableNoweb;
    Runnable runnableUi;
    Runnable runnableAddlist;
    Runnable runnableNoWebH;
    Runnable runnableNoresult;
    Runnable runnableNomore;

    ArrayList<JSONObject> searchResult;
    mAdapter mAdapter;

    boolean isLoading = true;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_search, container, false);
        seaListView = rootLayout.findViewById(R.id.sea_listview);
        loadingView = inflater.inflate(R.layout.widget_dyloading, null);
        seaEdittext = rootLayout.findViewById(R.id.sea_edittext);
        seaButton = rootLayout.findViewById(R.id.sea_seabutton);
        seaLoadImg = rootLayout.findViewById(R.id.sea_searching_img);

        runnableNoweb = new Runnable()
        {
            @Override
            public void run()
            {
                rootLayout.findViewById(R.id.sea_noweb).setVisibility(View.VISIBLE);
                rootLayout.findViewById(R.id.sea_searching).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.sea_nonthing).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.sea_listview).setVisibility(View.GONE);
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

        runnableUi = new Runnable()
        {
            @Override
            public void run()
            {
                mAdapter = new mAdapter(inflater, searchResult);
                seaListView.setAdapter(mAdapter);
                rootLayout.findViewById(R.id.sea_noweb).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.sea_searching).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.sea_nonthing).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.sea_listview).setVisibility(View.VISIBLE);
            }
        };

        runnableNoresult = new Runnable()
        {
            @Override
            public void run()
            {
                rootLayout.findViewById(R.id.sea_noweb).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.sea_searching).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.sea_nonthing).setVisibility(View.VISIBLE);
                rootLayout.findViewById(R.id.sea_listview).setVisibility(View.GONE);
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
                mAdapter.notifyDataSetChanged();
            }
        };

        loadingView.findViewById(R.id.dyload_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((TextView) loadingView.findViewById(R.id.dyload_text)).setText(" 加载中. . .");
                loadingView.findViewById(R.id.dyload_button).setVisibility(View.GONE);
                getMoreSearch();
            }
        });

        seaButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                InputMethodManager inputMethodManager = (InputMethodManager) ctx.getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(seaEdittext.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                isLoading = true;
                searchApi = new SearchApi(MainActivity.sharedPreferences.getString("cookies", ""), seaEdittext.getText().toString());

                seaLoadImg.setImageResource(R.drawable.anim_searching);
                AnimationDrawable loadingImgAnim = (AnimationDrawable) seaLoadImg.getDrawable();
                loadingImgAnim.start();

                rootLayout.findViewById(R.id.sea_noweb).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.sea_searching).setVisibility(View.VISIBLE);
                rootLayout.findViewById(R.id.sea_nonthing).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.sea_listview).setVisibility(View.GONE);
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            searchResult = searchApi.getSearchResult();
                            if(searchResult != null && searchResult.size() != 0) handler.post(runnableUi);
                            else handler.post(runnableNoresult);
                            isLoading = false;
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                            handler.post(runnableNoweb);
                        }
                    }
                }).start();
            }
        });

        seaListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if(position < searchResult.size())
                {
                    Intent intent = new Intent(ctx, VideodetailsActivity.class);
                    intent.putExtra("aid", String.valueOf(searchResult.get(position).optInt("aid")));
                    startActivity(intent);
                }
            }
        });

        seaListView.setOnScrollListener(new AbsListView.OnScrollListener()
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
                    isLoading = true;
                    //((TextView) loadingView.findViewById(R.id.dyload_text)).setText(" 加载中...");
                    getMoreSearch();
                }
            }
        });

        seaListView.addFooterView(loadingView);

        return rootLayout;
    }

    void getMoreSearch()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    ArrayList<JSONObject> arrayList = searchApi.getSearchResult();
                    if(arrayList.size() != 0)
                    {
                        searchResult.addAll(arrayList);
                        isLoading = false;
                        handler.post(runnableAddlist);
                    }
                    else
                    {
                        handler.post(runnableNomore);
                    }
                }
                catch (IOException e)
                {
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

        private ArrayList<JSONObject> seaList;

        public mAdapter(LayoutInflater inflater, ArrayList<JSONObject> seaList)
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
            return seaList.size();
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
            JSONObject v = seaList.get(position);
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
            viewHolder.up.setText("UP : " + getInfoFromJson(v, "author"));
            viewHolder.play.setText("播放 : " + getView((int) getInfoFromJson(v, "play")) + "  弹幕 : " + getInfoFromJson(v, "video_review"));

            viewHolder.img.setTag("https:" + getInfoFromJson(v, "pic"));
            BitmapDrawable c = setImageFormWeb("https:" + getInfoFromJson(v, "pic"));
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
             * 获得需要压缩的比率
             *
             * @param options 需要传入已经BitmapFactory.decodeStream(is, null, options);
             * @return 返回压缩的比率，最小为1
             */
            public int getInSampleSize(BitmapFactory.Options options) {
                int inSampleSize = 1;
                int realWith = 68;
                int realHeight = 44;

                int outWidth = options.outWidth;
                int outHeight = options.outHeight;

                //获取比率最大的那个
                if (outWidth > realWith || outHeight > realHeight) {
                    int withRadio = Math.round(outWidth / realWith);
                    int heightRadio = Math.round(outHeight / realHeight);
                    inSampleSize = withRadio > heightRadio ? withRadio : heightRadio;
                }
                return inSampleSize;
            }

            /**
             * 根据输入流返回一个压缩的图片
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
