package cn.luern0313.wristbilibili.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.util.LruCache;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.UserDynamic;
import cn.luern0313.wristbilibili.ui.MainActivity;

public class Dynamic extends Fragment
{
    Context ctx;

    UserDynamic userDynamic;
    ArrayList<Object> dynamicList;

    ListView dyListView;
    private View rootLayout;

    Handler handler = new Handler();
    Runnable runnableUi;

    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        rootLayout = inflater.inflate(R.layout.fragment_dynamic, container, false);
        dyListView = rootLayout.findViewById(R.id.dy_listview);

        runnableUi = new Runnable()
        {
            @Override
            public void run()
            {
                mAdapter adapter = new mAdapter(inflater, dynamicList);
                dyListView.setAdapter(adapter);
            }
        };

        if(!MainActivity.sharedPreferences.getString("cookies", "").equals(""))
        {
            userDynamic = new UserDynamic(MainActivity.sharedPreferences.getString("cookies", ""), MainActivity.sharedPreferences.getString("mid", ""));
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        userDynamic.getDynamic();
                        dynamicList = userDynamic.getDynamicList();
                        handler.post(runnableUi);
                    }
                    catch (IOException e)
                    {
                        Looper.prepare();
                        Toast.makeText(ctx, "好像没有网络连接呢...", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        else
        {
            rootLayout.findViewById(R.id.dy_nologin).setVisibility(View.VISIBLE);
        }

        return rootLayout;
    }

    class mAdapter extends BaseAdapter
    {
        private LayoutInflater mInflater;

        private LruCache<String, BitmapDrawable> mImageCache;

        private ArrayList<Object> dyList;

        public mAdapter(LayoutInflater inflater, ArrayList<Object> dyList)
        {
            mInflater = inflater;
            this.dyList = dyList;

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
            return dyList.size();
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
            return 5;
        }

        @Override
        public int getItemViewType(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup)
        {
            ViewHolderOriText viewHolderOriText = null;
            ViewHolderOriVid viewHolderOriVid = null;
            ViewHolderShaText viewHolderShaText = null;
            ViewHolderShaVid viewHolderShaVid = null;
            ViewHolderUnktyp viewHolderUnktyp = null;
            int type = getItemViewType(position);

            // 若无可重用的 view 则进行加载
            if(convertView == null)
            {
                if(dyList.get(position) instanceof UserDynamic.cardOriginalVideo)
                {
                    convertView = mInflater.inflate(R.layout.item_news_original_video, null);
                    viewHolderOriVid = new ViewHolderOriVid();
                    convertView.setTag(viewHolderOriVid);
                }
                else if(dyList.get(position) instanceof UserDynamic.cardOriginalText)
                {
                    convertView = mInflater.inflate(R.layout.item_news_original_text, null);
                    viewHolderOriText = new ViewHolderOriText();
                    convertView.setTag(viewHolderOriText);
                }
                else if(dyList.get(position) instanceof UserDynamic.cardUnknow)
                {
                    convertView = mInflater.inflate(R.layout.item_news_unknowtype, null);
                    viewHolderUnktyp = new ViewHolderUnktyp();
                    convertView.setTag(viewHolderUnktyp);
                }
                else if(dyList.get(position) instanceof UserDynamic.cardShareVideo)
                {
                    convertView = mInflater.inflate(R.layout.item_news_share_video, null);
                    viewHolderShaVid = new ViewHolderShaVid();
                    convertView.setTag(viewHolderShaVid);
                }
                else if(dyList.get(position) instanceof UserDynamic.cardShareText)
                {
                    convertView = mInflater.inflate(R.layout.item_news_share_text, null);
                    viewHolderShaText = new ViewHolderShaText();
                    convertView.setTag(viewHolderShaText);
                }
            }
            else
            {
                switch (type)
                {
                    case 4:
                        viewHolderOriVid = (ViewHolderOriVid) convertView.getTag();
                        break;
                    case 3:
                        viewHolderOriText = (ViewHolderOriText) convertView.getTag();
                        break;
                    case 2:
                        viewHolderUnktyp = (ViewHolderUnktyp) convertView.getTag();
                        break;
                    case 1:
                        viewHolderShaVid = (ViewHolderShaVid) convertView.getTag();
                        break;
                    case 0:
                        viewHolderShaText = (ViewHolderShaText) convertView.getTag();
                        break;
                }
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

        class ViewHolderOriText
        {

        }

        class ViewHolderOriVid
        {

        }

        class ViewHolderShaText
        {

        }

        class ViewHolderShaVid
        {

        }

        class ViewHolderUnktyp
        {

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
