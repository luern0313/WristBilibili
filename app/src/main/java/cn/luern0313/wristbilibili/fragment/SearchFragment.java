package cn.luern0313.wristbilibili.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.SearchApi;
import cn.luern0313.wristbilibili.ui.MainActivity;
import cn.luern0313.wristbilibili.ui.VideodetailsActivity;
import cn.luern0313.wristbilibili.widget.ImageDownloader;

/**
 * Created by liupe on 2018/11/16.
 * 搜索。。
 */

public class SearchFragment extends Fragment
{
    Context ctx;
    SearchApi searchApi;

    View rootLayout;
    TextView seaHotWordText;
    GridView seaHotWordList;
    ListView seaListView;
    View loadingView;
    EditText seaEdittext;
    TextView inButton;
    TextView seaButton;
    ImageView seaLoadImg;

    Handler handler = new Handler();
    Runnable runnableHotWord;
    Runnable runnableHotWordErr;
    Runnable runnableNoweb;
    Runnable runnableUi;
    Runnable runnableAddlist;
    Runnable runnableNoWebH;
    Runnable runnableNoresult;
    Runnable runnableNomore;

    String[] hotWordArray;
    ArrayList<JSONObject> searchResult;
    HotWordAdapter hotwordAdapter;
    mAdapter mAdapter;

    boolean isLoading = true;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_search, container, false);
        seaHotWordText = rootLayout.findViewById(R.id.sea_hotwordtext);
        seaHotWordList = rootLayout.findViewById(R.id.sea_hotword);
        seaListView = rootLayout.findViewById(R.id.sea_listview);
        loadingView = inflater.inflate(R.layout.widget_loading, null);
        seaEdittext = rootLayout.findViewById(R.id.sea_edittext);
        inButton = rootLayout.findViewById(R.id.sea_inbutton);
        seaButton = rootLayout.findViewById(R.id.sea_seabutton);
        seaLoadImg = rootLayout.findViewById(R.id.sea_searching_img);

        runnableHotWord = new Runnable()
        {
            @Override
            public void run()
            {
                hotwordAdapter = new HotWordAdapter(ctx, android.R.layout.simple_list_item_1, hotWordArray);
                seaHotWordText.setText("大家都在搜");
                seaHotWordList.setAdapter(hotwordAdapter);
            }
        };

        runnableHotWordErr = new Runnable()
        {
            @Override
            public void run()
            {
                seaHotWordText.setText("搜索热词加载失败");
            }
        };

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
                ((TextView) loadingView.findViewById(R.id.wid_load_text)).setText("好像没有网络...\n检查下网络？");
                loadingView.findViewById(R.id.wid_load_button).setVisibility(View.VISIBLE);
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
                ((TextView) loadingView.findViewById(R.id.wid_load_text)).setText("  没有更多了...");
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

        loadingView.findViewById(R.id.wid_load_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((TextView) loadingView.findViewById(R.id.wid_load_text)).setText(" 加载中. . .");
                loadingView.findViewById(R.id.wid_load_button).setVisibility(View.GONE);
                getMoreSearch();
            }
        });

        inButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent voiceInputIntent = new Intent("com.mobvoi.ticwear.action.SPEECH");
                voiceInputIntent.putExtra("start_mode", "start_mode_with_voice_input");
                try
                {
                    startActivityForResult(voiceInputIntent, 0);
                }
                catch (Exception e)
                {
                    Toast.makeText(ctx, "抱歉，该手表不支持语音输入", Toast.LENGTH_SHORT).show();
                }
            }
        });

        seaButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getSearch();
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

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    //获取搜索关键词
                    hotWordArray = SearchApi.getHotWord();
                    if(hotWordArray != null && hotWordArray.length != 0)
                        handler.post(runnableHotWord);
                    else handler.post(runnableHotWordErr);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    handler.post(runnableHotWordErr);
                }
            }
        }).start();

        seaListView.addFooterView(loadingView);

        return rootLayout;
    }

    private class HotWordAdapter extends ArrayAdapter
    {
        HotWordAdapter(Context context, int resource, Object[] objects)
        {
            super(context, resource, objects);
        }

        MyListener listener = new MyListener();

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            String str = (String) getItem(position);//通过position获取当前要赋值的内容
            if(convertView == null)
            {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            }
            TextView tv = convertView.findViewById(android.R.id.text1);
            tv.setText(str);
            tv.setTextSize(12);
            tv.setOnClickListener(listener);
            return convertView;
        }

        //MyListener类继承OnClickListener，用来监听每个Item的点击事件
        private class MyListener implements View.OnClickListener
        {
            @Override
            public void onClick(View v)
            {
                seaEdittext.setText(((TextView) v).getText());
                getSearch();
            }
        }
    }

    void getSearch()
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0)
        {
            if(data != null)
            {
                String result = data.getExtras().getString("speech_content");
                if(result.endsWith("。")) result = result.substring(0, result.length() - 1);
                seaEdittext.setText(result);
                getSearch();
            }
            else
            {
                Toast.makeText(ctx, "识别失败，请重试", Toast.LENGTH_SHORT).show();
            }
        }
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
                    bitmap = ImageDownloader.downloadImage(imageUrl);
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
        }
    }
}
