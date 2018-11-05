package cn.luern0313.wristbilibili.fragment;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.util.LruCache;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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
        ctx = getActivity();
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
            if(dyList.get(position) instanceof UserDynamic.cardOriginalVideo) return 4;
            else if(dyList.get(position) instanceof UserDynamic.cardOriginalText) return 3;
            else if(dyList.get(position) instanceof UserDynamic.cardUnknow) return 2;
            else if(dyList.get(position) instanceof UserDynamic.cardShareVideo) return 1;
            else if(dyList.get(position) instanceof UserDynamic.cardShareText) return 0;
            return 2;
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

                        viewHolderOriVid.head = convertView.findViewById(R.id.liov_head);
                        viewHolderOriVid.name = convertView.findViewById(R.id.liov_name);
                        viewHolderOriVid.time = convertView.findViewById(R.id.liov_time);
                        viewHolderOriVid.text = convertView.findViewById(R.id.liov_text);
                        viewHolderOriVid.img = convertView.findViewById(R.id.liov_share_img);
                        viewHolderOriVid.imgtext = convertView.findViewById(R.id.liov_textimg);
                        viewHolderOriVid.title = convertView.findViewById(R.id.liov_title);
                        viewHolderOriVid.likebu = convertView.findViewById(R.id.liov_likebu);
                        viewHolderOriVid.like = convertView.findViewById(R.id.liov_like);
                        break;

                    case 3:
                        convertView = mInflater.inflate(R.layout.item_news_original_text, null);
                        viewHolderOriText = new ViewHolderOriText();
                        convertView.setTag(viewHolderOriText);

                        viewHolderOriText.head = convertView.findViewById(R.id.liot_head);
                        viewHolderOriText.name = convertView.findViewById(R.id.liot_name);
                        viewHolderOriText.time = convertView.findViewById(R.id.liot_time);
                        viewHolderOriText.text = convertView.findViewById(R.id.liot_text);
                        viewHolderOriText.textimg = convertView.findViewById(R.id.liot_textimg);
                        viewHolderOriText.likebu = convertView.findViewById(R.id.liot_likebu);
                        viewHolderOriText.like = convertView.findViewById(R.id.liot_like);
                        break;

                    case 2:
                        convertView = mInflater.inflate(R.layout.item_news_unknowtype, null);
                        viewHolderUnktyp = new ViewHolderUnktyp();
                        convertView.setTag(viewHolderUnktyp);

                        viewHolderUnktyp.head = convertView.findViewById(R.id.liuk_head);
                        viewHolderUnktyp.name = convertView.findViewById(R.id.liuk_name);
                        viewHolderUnktyp.time = convertView.findViewById(R.id.liuk_time);
                        break;

                    case 1:
                        convertView = mInflater.inflate(R.layout.item_news_share_video, null);
                        viewHolderShaVid = new ViewHolderShaVid();
                        convertView.setTag(viewHolderShaVid);

                        viewHolderShaVid.head = convertView.findViewById(R.id.lisv_head);
                        viewHolderShaVid.name = convertView.findViewById(R.id.lisv_name);
                        viewHolderShaVid.time = convertView.findViewById(R.id.lisv_time);
                        viewHolderShaVid.text = convertView.findViewById(R.id.lisv_text);
                        viewHolderShaVid.shead = convertView.findViewById(R.id.lisv_share_head);
                        viewHolderShaVid.sname = convertView.findViewById(R.id.lisv_share_name);
                        viewHolderShaVid.simg = convertView.findViewById(R.id.lisv_share_img);
                        viewHolderShaVid.simgtext = convertView.findViewById(R.id.lisv_share_imgtext);
                        viewHolderShaVid.stitle = convertView.findViewById(R.id.lisv_share_text);
                        viewHolderShaVid.likebu = convertView.findViewById(R.id.lisv_likebu);
                        viewHolderShaVid.like = convertView.findViewById(R.id.lisv_like);
                        break;

                    case 0:
                        convertView = mInflater.inflate(R.layout.item_news_share_text, null);
                        viewHolderShaText = new ViewHolderShaText();
                        convertView.setTag(viewHolderShaText);

                        viewHolderShaText.head = convertView.findViewById(R.id.list_head);
                        viewHolderShaText.name = convertView.findViewById(R.id.list_name);
                        viewHolderShaText.time = convertView.findViewById(R.id.list_time);
                        viewHolderShaText.text = convertView.findViewById(R.id.list_text);
                        viewHolderShaText.shead = convertView.findViewById(R.id.list_share_head);
                        viewHolderShaText.sname = convertView.findViewById(R.id.list_share_name);
                        viewHolderShaText.stext = convertView.findViewById(R.id.list_share_text);
                        viewHolderShaText.stextimg = convertView.findViewById(R.id.list_share_textimg);
                        viewHolderShaText.likebu = convertView.findViewById(R.id.list_likebu);
                        viewHolderShaText.like = convertView.findViewById(R.id.list_like);
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

            if(type == 4)
            {
                UserDynamic.cardOriginalVideo dy = (UserDynamic.cardOriginalVideo) dyList.get(position);
                viewHolderOriVid.name.setText(Html.fromHtml("<b>" + dy.getOwnerName() + "</b>投稿了视频"));
                viewHolderOriVid.time.setText(dy.getDynamicTime());
                if(!dy.getDynamic().equals(""))
                {
                    viewHolderOriVid.text.setVisibility(View.VISIBLE);
                    viewHolderOriVid.text.setText(dy.getDynamic());
                }
                else viewHolderOriVid.text.setVisibility(View.GONE);
                viewHolderOriVid.imgtext.setText(dy.getVideoDuration() + "  " + dy.getVideoView() + "观看");
                viewHolderOriVid.title.setText(dy.getVideoTitle());
                viewHolderOriVid.like.setText(String.valueOf(dy.getBeLiked()));
            }
            else if(type == 3)
            {
                UserDynamic.cardOriginalText dy = (UserDynamic.cardOriginalText) dyList.get(position);
                viewHolderOriText.name.setText(dy.getUserName());
                viewHolderOriText.time.setText(dy.getDynamicTime());
                viewHolderOriText.text.setText(dy.getDynamicText());
                if(!dy.getTextImgCount().equals("0"))
                {
                    viewHolderOriText.textimg.setVisibility(View.VISIBLE);
                    viewHolderOriText.textimg.setText("查看共" + dy.getTextImgCount() + "张图片");
                }
                else viewHolderOriText.textimg.setVisibility(View.GONE);
                viewHolderOriText.like.setText(String.valueOf(dy.getBeLiked()));
            }
            else if(type == 2)
            {
                UserDynamic.cardUnknow dy = (UserDynamic.cardUnknow) dyList.get(position);
                viewHolderUnktyp.name.setText(dy.getOwnerName());
                viewHolderUnktyp.time.setText(dy.getDynamicTime());
            }
            else if(type == 1)
            {
                UserDynamic.cardShareVideo dy = (UserDynamic.cardShareVideo) dyList.get(position);
                UserDynamic.cardOriginalVideo sdy = (UserDynamic.cardOriginalVideo) userDynamic.getDynamicClass(dy.getOriginalVideo(), 1);
                viewHolderShaVid.name.setText(dy.getUserName());
                viewHolderShaVid.time.setText(dy.getDynamicTime());
                viewHolderShaVid.text.setText(dy.getDynamicText());
                viewHolderShaVid.sname.setText(sdy.getOwnerName());
                viewHolderShaVid.simgtext.setText(sdy.getVideoDuration() + "  " + sdy.getVideoView() + "观看");
                viewHolderShaVid.stitle.setText(sdy.getVideoTitle());
                viewHolderShaVid.like.setText(String.valueOf(dy.getBeLiked()));
            }
            else if(type == 0)
            {
                UserDynamic.cardShareText dy = (UserDynamic.cardShareText) dyList.get(position);
                UserDynamic.cardOriginalText sdy = (UserDynamic.cardOriginalText) userDynamic.getDynamicClass(dy.getOriginalText(), 2);
                viewHolderShaText.name.setText(dy.getUserName());
                viewHolderShaText.time.setText(dy.getDynamicTime());
                viewHolderShaText.text.setText(dy.getDynamicText());
                viewHolderShaText.sname.setText(sdy.getUserName());
                viewHolderShaText.stext.setText(sdy.getDynamicText());
                if(!sdy.getTextImgCount().equals("0"))
                {
                    viewHolderShaText.stextimg.setVisibility(View.VISIBLE);
                    viewHolderShaText.stextimg.setText("查看共" + sdy.getTextImgCount() + "张图片");
                }
                viewHolderShaText.like.setText(String.valueOf(dy.getBeLiked()));

            }

            /*
            if(mImgurl.size() != 0)
            {
                viewHolder.vImg.setTag(mImgurl.get(position));
                    if(mImageCache.get(mImgurl.get(position)) != null)
                    {
                        viewHolder.vImg.setImageDrawable(mImageCache.get(mImgurl.get(position)));
                    }
                    else
                    {
                        ImageTask it = new ImageTask();
                        it.execute(mImgurl.get(position));
                    }
            }*/
            return convertView;
        }

        class ViewHolderOriVid
        {
            ImageView head;
            TextView name;
            TextView time;
            TextView text;
            ImageView img;
            TextView imgtext;
            TextView title;
            ImageView likebu;
            TextView like;
        }

        class ViewHolderOriText
        {
            ImageView head;
            TextView name;
            TextView time;
            TextView text;
            TextView textimg;
            ImageView likebu;
            TextView like;
        }

        class ViewHolderUnktyp
        {
            ImageView head;
            TextView name;
            TextView time;
        }

        class ViewHolderShaVid
        {
            ImageView head;
            TextView name;
            TextView time;
            TextView text;
            ImageView shead;
            TextView sname;
            ImageView simg;
            TextView simgtext;
            TextView stitle;
            ImageView likebu;
            TextView like;
        }

        class ViewHolderShaText
        {
            ImageView head;
            TextView name;
            TextView time;
            TextView text;
            ImageView shead;
            TextView sname;
            TextView stext;
            TextView stextimg;
            ImageView likebu;
            TextView like;
        }

        class ImageTask extends AsyncTask<String, Void, BitmapDrawable>
        {
            private String imageUrl;

            @Override
            protected BitmapDrawable doInBackground(String... params)
            {
                imageUrl = params[0];
                Bitmap bitmap = downloadImage();
                BitmapDrawable db = new BitmapDrawable(dyListView.getResources(), bitmap);
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
                ImageView iv = dyListView.findViewWithTag(imageUrl);
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
            private Bitmap downloadImage()
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
                }
                catch (MalformedURLException e)
                {
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    if(con != null)
                    {
                        con.disconnect();
                    }
                }
                return bitmap;
            }
        }

    }
}
