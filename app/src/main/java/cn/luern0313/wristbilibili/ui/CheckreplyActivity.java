package cn.luern0313.wristbilibili.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.zip.Inflater;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.ReplyApi;

public class CheckreplyActivity extends Activity
{
    Context ctx;
    LayoutInflater inflater;
    Intent intent;
    ReplyApi replyApi;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ArrayList<ReplyApi.reply> replyArrayList;
    mAdapter replyAdapter;

    AnimationDrawable loadingImgAnim;
    Bitmap videoUpFace;

    Handler handler = new Handler();
    Runnable runnableReply;
    Runnable runnableReplyUpdate;
    Runnable runnableMoreNomore;
    Runnable runnableMoreErr;

    View layoutSendReply;
    View layoutLoading;
    LinearLayout uiLoading;
    ImageView uiLoadingImg;
    ListView uiListview;
    LinearLayout uiNothing;

    boolean isReplyLoading = true;
    int replyPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkreply);
        ctx = this;
        inflater = getLayoutInflater();
        intent = getIntent();
        sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        replyApi = new ReplyApi(sharedPreferences.getString("cookies", ""), sharedPreferences.getString("csrf", ""), intent.getStringExtra("oid"), intent.getStringExtra("type"));

        layoutSendReply = inflater.inflate(R.layout.widget_reply_sendreply, null);
        layoutLoading = inflater.inflate(R.layout.widget_dyloading, null);
        uiLoading = findViewById(R.id.cr_loading);
        uiLoadingImg = findViewById(R.id.cr_loading_img);
        uiListview = findViewById(R.id.cr_reply_listview);
        uiNothing = findViewById(R.id.cr_reply_nothing);

        uiLoadingImg.setImageResource(R.drawable.anim_loading);
        loadingImgAnim = (AnimationDrawable) uiLoadingImg.getDrawable();
        loadingImgAnim.start();

        runnableReply = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Log.i("bilibili", "runnablie！！");
                    isReplyLoading = false;
                    uiLoading.setVisibility(View.GONE);

                    replyAdapter = new mAdapter(inflater, replyArrayList);
                    uiListview.setVisibility(View.VISIBLE);
                    uiListview.setAdapter(replyAdapter);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableReplyUpdate = new Runnable()
        {
            @Override
            public void run()
            {
                replyAdapter.notifyDataSetChanged();
            }
        };

        runnableMoreNomore = new Runnable()
        {
            @Override
            public void run()
            {
                ((TextView) layoutLoading.findViewById(R.id.dyload_text)).setText("  没有更多了...");
            }
        };

        runnableMoreErr = new Runnable()
        {
            @Override
            public void run()
            {
                ((TextView) layoutLoading.findViewById(R.id.dyload_text)).setText("好像没有网络...\n检查下网络？");
                layoutLoading.findViewById(R.id.dyload_button).setVisibility(View.VISIBLE);
                isReplyLoading = false;
            }
        };

        uiListview.setEmptyView(findViewById(R.id.cr_reply_nothing));
        uiListview.addHeaderView(layoutSendReply, null, true);
        uiListview.addFooterView(layoutLoading, null, true);
        uiListview.setHeaderDividersEnabled(false);
        uiListview.setOnScrollListener(new AbsListView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState)
            {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
            {
                if(visibleItemCount + firstVisibleItem == totalItemCount && !isReplyLoading)
                {
                    getMoreReply();
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
                    replyArrayList = new ArrayList<>();
                    if(intent.getStringExtra("root").equals(""))
                    {
                        replyArrayList.add(replyApi.new reply(1));
                        replyArrayList.addAll(replyApi.getReply(1, "2", 5, ""));
                        replyArrayList.add(replyApi.new reply(2));
                        replyArrayList.addAll(replyApi.getReply(1, "0", 0, ""));
                        handler.post(runnableReply);
                    }
                    else
                    {
                        replyArrayList.add(replyApi.new reply(2));
                        replyArrayList.addAll(replyApi.getReply(1, "0", 0, intent.getStringExtra("root")));
                        handler.post(runnableReply);
                    }
                }
                catch (IOException | NullPointerException e)
                {
                    e.printStackTrace();
                    replyArrayList = new ArrayList<>();
                    handler.post(runnableReply);
                }
            }
        }).start();
    }

    void getMoreReply()
    {
        isReplyLoading = true;
        replyPage++;
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    ArrayList<ReplyApi.reply> r = replyApi.getReply(replyPage, "0", 0, intent.getStringExtra("root"));
                    if(r != null && r.size() != 0)
                    {
                        replyArrayList.addAll(r);
                        isReplyLoading = false;
                        handler.post(runnableReplyUpdate);
                    }
                    else
                    {
                        handler.post(runnableMoreNomore);
                    }
                }
                catch (IOException e)
                {
                    handler.post(runnableMoreErr);
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void clickSendReply(View view)
    {
        Intent replyIntent = new Intent(ctx, ReplyActivity.class);
        replyIntent.putExtra("oid", intent.getStringExtra("aid"));
        replyIntent.putExtra("type", "1");
        startActivityForResult(replyIntent, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0 && resultCode == 0 && (!data.getStringExtra("text").equals("")))
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    String r = replyApi.sendReply(intent.getStringExtra("root"), data.getStringExtra("text"));
                    if(r.equals(""))
                    {
                        Looper.prepare();
                        Toast.makeText(ctx, "发送成功！", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                    else
                    {
                        Looper.prepare();
                        Toast.makeText(ctx, "发送失败：\n" + r, Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }
            }).start();
        }
    }

    class mAdapter extends BaseAdapter
    {
        private LayoutInflater mInflater;

        private LruCache<String, BitmapDrawable> mImageCache;

        private ArrayList<ReplyApi.reply> replyList;

        public mAdapter(LayoutInflater inflater, ArrayList<ReplyApi.reply> replyList)
        {
            mInflater = inflater;
            this.replyList = replyList;

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
            return replyList.size();
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
            return 3;
        }

        @Override
        public int getItemViewType(int position)
        {
            return replyList.get(position).getMode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup)
        {
            final ReplyApi.reply v = replyList.get(position);
            ViewHolder viewHolder = null;
            if(convertView == null)
            {
                switch (getItemViewType(position))
                {
                    case 0:
                        convertView = mInflater.inflate(R.layout.item_vd_reply, null);
                        viewHolder = new ViewHolder();
                        convertView.setTag(viewHolder);
                        viewHolder.img = convertView.findViewById(R.id.item_reply_head);
                        viewHolder.name = convertView.findViewById(R.id.item_reply_name);
                        viewHolder.time = convertView.findViewById(R.id.item_reply_time);
                        viewHolder.text = convertView.findViewById(R.id.item_reply_text);
                        viewHolder.like = convertView.findViewById(R.id.item_reply_like);
                        viewHolder.likei = convertView.findViewById(R.id.item_reply_like_i);
                        viewHolder.liken = convertView.findViewById(R.id.item_reply_like_n);
                        viewHolder.dislike = convertView.findViewById(R.id.item_reply_dislike);
                        viewHolder.dislikei = convertView.findViewById(R.id.item_reply_dislike_i);
                        viewHolder.reply = convertView.findViewById(R.id.item_reply_reply);
                        viewHolder.replyn = convertView.findViewById(R.id.item_reply_reply_n);
                        break;

                    case 1:
                        convertView = mInflater.inflate(R.layout.widget_reply_changemode, null);
                        ((TextView) convertView.findViewById(R.id.item_reply_sign)).setText("热门评论");
                        break;

                    case 2:
                        convertView = mInflater.inflate(R.layout.widget_reply_changemode, null);
                        ((TextView) convertView.findViewById(R.id.item_reply_sign)).setText("最新评论");
                        break;
                }
            }
            else
            {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if(getItemViewType(position) == 0)
            {
                viewHolder.img.setImageResource(R.drawable.img_default_avatar);
                viewHolder.name.setText(v.getUserName());
                viewHolder.time.setText(v.getReplyTime() + "   " + v.getReplyFloor(replyApi.isShowFloor()) + "   LV" + v.getUserLv());
                viewHolder.text.setText(v.getReplyText());
                viewHolder.liken.setText(v.getReplyBeLiked());
                viewHolder.replyn.setText(v.getReplyBeReply());

                if(!intent.getStringExtra("root").equals(""))
                {
                    viewHolder.dislike.setVisibility(View.INVISIBLE);
                    viewHolder.reply.setVisibility(View.INVISIBLE);
                }

                if(v.isReplyLike()) viewHolder.likei.setImageResource(R.drawable.icon_liked);
                else viewHolder.likei.setImageResource(R.drawable.icon_like);
                if(v.isReplyDislike())
                    viewHolder.dislikei.setImageResource(R.drawable.icon_disliked);
                else viewHolder.dislikei.setImageResource(R.drawable.icon_dislike);
                if(v.getUserVip() == 2)
                    viewHolder.name.setTextColor(getResources().getColor(R.color.mainColor));
                else viewHolder.name.setTextColor(getResources().getColor(R.color.textColor4));

                viewHolder.img.setTag(v.getUserHead());
                BitmapDrawable h = setImageFormWeb(v.getUserHead());
                if(h != null) viewHolder.img.setImageDrawable(h);

                viewHolder.img.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Intent intent = new Intent(ctx, OtheruserActivity.class);
                        intent.putExtra("mid", v.getUserMid());
                        startActivity(intent);
                    }
                });

                viewHolder.like.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        new Thread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                String va = v.likeReply(v.getReplyId(), v.isReplyLike() ? 0 : 1, intent.getStringExtra("type"));
                                if(va.equals("")) handler.post(runnableReplyUpdate);
                                else
                                {
                                    Looper.prepare();
                                    Toast.makeText(ctx, (v.isReplyLike() ? "取消" : "点赞") + "失败：\n" + va, Toast.LENGTH_SHORT).show();
                                    Looper.loop();
                                }
                            }
                        }).start();
                    }
                });

                viewHolder.dislike.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        new Thread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                String va = v.hateReply(v.getReplyId(), v.isReplyDislike() ? 0 : 1, intent.getStringExtra("type"));
                                if(va.equals("")) handler.post(runnableReplyUpdate);
                                else
                                {
                                    Looper.prepare();
                                    Toast.makeText(ctx, (v.isReplyDislike() ? "取消" : "点踩") + "失败：\n" + va, Toast.LENGTH_SHORT).show();
                                    Looper.loop();
                                }
                            }
                        }).start();
                    }
                });

                viewHolder.reply.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Intent rintent = new Intent(ctx, CheckreplyActivity.class);
                        rintent.putExtra("oid", intent.getStringExtra("oid"));
                        rintent.putExtra("type", intent.getStringExtra("type"));
                        rintent.putExtra("root", v.getReplyId());
                        startActivity(rintent);
                    }
                });
            }
            return convertView;
        }

        class ViewHolder
        {
            ImageView img;
            TextView name;
            TextView time;
            TextView text;
            LinearLayout like;
            ImageView likei;
            TextView liken;
            LinearLayout dislike;
            ImageView dislikei;
            LinearLayout reply;
            TextView replyn;
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
                    BitmapDrawable db = new BitmapDrawable(getResources(), bitmap);
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
                ImageView iv = uiListview.findViewWithTag(imageUrl);
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
