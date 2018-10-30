package cn.luern0313.wristbilibili.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.util.LruCache;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.ui.MenuActivity;

public class Dynamic extends Fragment
{
    Context ctx;

    ListView mainListView;
    private View rootLayout;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        rootLayout = inflater.inflate(R.layout.fragment_dynamic, container, false);
        mainListView = rootLayout.findViewById(R.id.main_list);
        mAdapter adapter = new mAdapter(inflater);
        mainListView.setAdapter(adapter);

        return rootLayout;
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
            return 5;
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
                switch (type)
                {
                    case 4:
                        convertView = mInflater.inflate(R.layout.item_news_original_video, null);
                        viewHolderOriVid = new ViewHolderOriVid();
                        convertView.setTag(viewHolderOriVid);
                        break;
                    case 3:
                        convertView = mInflater.inflate(R.layout.item_news_original_text, null);
                        viewHolderOriText = new ViewHolderOriText();
                        convertView.setTag(viewHolderOriText);
                        break;
                    case 2:
                        convertView = mInflater.inflate(R.layout.item_news_unknowtype, null);
                        viewHolderUnktyp = new ViewHolderUnktyp();
                        convertView.setTag(viewHolderUnktyp);
                        break;
                    case 1:
                        convertView = mInflater.inflate(R.layout.item_news_share_video, null);
                        viewHolderShaVid = new ViewHolderShaVid();
                        convertView.setTag(viewHolderShaVid);
                        break;
                    case 0:
                        convertView = mInflater.inflate(R.layout.item_news_share_text, null);
                        viewHolderShaText = new ViewHolderShaText();
                        convertView.setTag(viewHolderShaText);
                        break;
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
