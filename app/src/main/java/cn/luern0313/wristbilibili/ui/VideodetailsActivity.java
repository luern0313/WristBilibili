package cn.luern0313.wristbilibili.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
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
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.OthersUser;
import cn.luern0313.wristbilibili.api.ReplyApi;
import cn.luern0313.wristbilibili.api.VideoDetails;

public class VideodetailsActivity extends Activity
{
    Context ctx;
    Intent intent;
    LayoutInflater inflater;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    VideoDetails videoDetail;
    ReplyApi replyApi;
    ArrayList<ReplyApi.reply> replyArrayList;

    View layoutSendReply;
    View layoutChangeMode;
    View layoutLoading;

    Handler handler = new Handler();
    Runnable runnableNoWeb;
    Runnable runnableUi;
    Runnable runnableImg;
    Runnable runnableSetface;
    Runnable runnableNodata;
    Runnable runnableReply;

    AnimationDrawable loadingImgAnim;
    Bitmap videoUpFace;

    TextView titleTextView;
    ViewPager viewPager;

    ImageView uiLoadingImg;
    LinearLayout uiLoading;
    LinearLayout uiNoWeb;
    TextView uiLikeText;
    TextView uiCoinText;
    TextView uiFavText;
    ImageView uiLikeImg;
    ImageView uiCoinImg;
    ImageView uiFavImg;
    ImageView uiDislikeImg;
    LinearLayout uiLikeLay;
    LinearLayout uiCoinLay;
    LinearLayout uiFavLay;
    LinearLayout uiDislikeLay;

    ListView uiRelpyListView;

    boolean isLogin = false;
    int isLiked = 0;//012
    int isCoined = 0;
    boolean isFaved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videodetails);

        ctx = this;
        intent = getIntent();
        sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        inflater = getLayoutInflater();
        layoutSendReply = inflater.inflate(R.layout.widget_reply_sendreply, null);
        layoutChangeMode = inflater.inflate(R.layout.widget_reply_changemode, null);
        layoutLoading = inflater.inflate(R.layout.widget_dyloading, null);
        titleTextView = findViewById(R.id.vd_title_title);
        viewPager = findViewById(R.id.vd_viewpager);

        isLogin = !MainActivity.sharedPreferences.getString("cookies", "").equals("");
        videoDetail = new VideoDetails(sharedPreferences.getString("cookies", ""), sharedPreferences.getString("csrf", ""), sharedPreferences.getString("mid", ""), intent.getStringExtra("aid"));
        replyApi = new ReplyApi(sharedPreferences.getString("cookies", ""), sharedPreferences.getString("csrf", ""), intent.getStringExtra("aid"), "1");

        runnableNoWeb = new Runnable()
        {
            @Override
            public void run()
            {
                uiLoading.setVisibility(View.GONE);
                uiNoWeb.setVisibility(View.VISIBLE);
            }
        };

        runnableUi = new Runnable()
        {
            @Override
            public void run()
            {
                ((TextView) findViewById(R.id.vd_video_title)).setText(videoDetail.getVideoTitle());
                ((TextView) findViewById(R.id.vd_video_play)).setText("播放:" + videoDetail.getVideoPlay() + "  弹幕:" + videoDetail.getVideoDanmaku());
                ((TextView) findViewById(R.id.vd_video_time)).setText(videoDetail.getVideoupTime() + "  AV" + videoDetail.getVideoAid());
                ((TextView) findViewById(R.id.vd_video_details)).setText(videoDetail.getVideoDetail());
                ((TextView) findViewById(R.id.vd_card_name)).setText(videoDetail.getVideoUpName());
                ((TextView) findViewById(R.id.vd_card_sen)).setText(videoDetail.getVideoUpSign());
                uiLikeText.setText(videoDetail.getVideoLike());
                uiCoinText.setText(videoDetail.getVideoCoin());
                uiFavText.setText(videoDetail.getVideoFav());
                isLiked = videoDetail.getSelfLiked();
                isCoined = videoDetail.getSelfCoined();
                isFaved = videoDetail.getSelfFaved();

                setIcon();

                uiLoading.setVisibility(View.GONE);
                uiNoWeb.setVisibility(View.GONE);
            }
        };

        runnableImg = new Runnable()
        {
            @Override
            public void run()
            {
                setIcon();
            }
        };

        runnableSetface = new Runnable()
        {
            @Override
            public void run()
            {
                ((ImageView) findViewById(R.id.vd_card_head)).setImageBitmap(videoUpFace);
            }
        };

        runnableNodata = new Runnable()
        {
            @Override
            public void run()
            {
                findViewById(R.id.vd_novideo).setVisibility(View.VISIBLE);
            }
        };

        runnableReply = new Runnable()
        {
            @Override
            public void run()
            {
                mAdapter replyAdapter = new mAdapter(inflater, replyArrayList);
                uiRelpyListView.setAdapter(replyAdapter);
            }
        };

        PagerAdapter pagerAdapter = new PagerAdapter()
        {
            @Override
            public int getCount()
            {
                return 2;
            }

            @Override
            public boolean isViewFromObject(View view, Object object)
            {
                return view.getTag().equals(object);
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object)
            {
                container.removeView(container.findViewWithTag(object));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position)
            {
                if(position == 0)
                {
                    View v = inflater.inflate(R.layout.viewpager_vd_vd, null);
                    v.setTag(0);

                    uiLoadingImg = v.findViewById(R.id.vd_loading_img);
                    uiLoading = v.findViewById(R.id.vd_loading);
                    uiNoWeb = v.findViewById(R.id.vd_noweb);
                    uiLikeText = v.findViewById(R.id.vd_like_text);
                    uiCoinText = v.findViewById(R.id.vd_coin_text);
                    uiFavText = v.findViewById(R.id.vd_fav_text);
                    uiLikeImg = v.findViewById(R.id.vd_like_img);
                    uiCoinImg = v.findViewById(R.id.vd_coin_img);
                    uiFavImg = v.findViewById(R.id.vd_fav_img);
                    uiDislikeImg = v.findViewById(R.id.vd_dislike_img);
                    uiLikeLay = v.findViewById(R.id.vd_like);
                    uiCoinLay = v.findViewById(R.id.vd_coin);
                    uiFavLay = v.findViewById(R.id.vd_fav);
                    uiDislikeLay = v.findViewById(R.id.vd_dislike);

                    uiLoadingImg.setImageResource(R.drawable.anim_loading);
                    loadingImgAnim = (AnimationDrawable) uiLoadingImg.getDrawable();
                    loadingImgAnim.start();
                    uiLoading.setVisibility(View.VISIBLE);

                    v.findViewById(R.id.vd_card).setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Intent intent = new Intent(ctx, OtheruserActivity.class);
                            intent.putExtra("mid", videoDetail.getVideoUpAid());
                            startActivity(intent);
                        }
                    });

                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                if(videoDetail.getVideoDetails())
                                {
                                    handler.post(runnableUi);
                                    videoUpFace = videoDetail.getVideoUpFace();
                                    handler.post(runnableSetface);
                                }
                                else
                                {
                                    handler.post(runnableNodata);
                                }
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                                handler.post(runnableNoWeb);
                            }
                        }
                    }).start();
                    container.addView(v);
                    return 0;
                }
                else
                {
                    View v = inflater.inflate(R.layout.viewpager_vd_reply, null);
                    v.setTag(1);

                    uiRelpyListView = v.findViewById(R.id.vd_reply_listview);
                    uiRelpyListView.addHeaderView(layoutSendReply, null, true);
                    uiRelpyListView.addFooterView(layoutLoading, null, true);
                    uiRelpyListView.setHeaderDividersEnabled(false);
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                replyArrayList = replyApi.getReply(1, "2", 5);
                                replyArrayList.add(replyApi.new reply(1));
                                replyArrayList.addAll(replyApi.getReply(1, "0", 0));
                                handler.post(runnableReply);
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    container.addView(v);
                    return 1;
                }
            }
        };

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {
            }

            @Override
            public void onPageSelected(int position)
            {
                if(position == 0) titleAnim("详情");
                else if(position == 1) titleAnim("评论");
            }
        });

        viewPager.setAdapter(pagerAdapter);
    }

    void titleAnim(String title)
    {
        titleTextView.setText(title);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(titleTextView, "alpha", 1f, 0f);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(alpha).after(2000);
        animatorSet.setDuration(500);
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {
            }

            @Override
            public void onAnimationCancel(Animator animation)
            {
            }

            @Override
            public void onAnimationRepeat(Animator animation)
            {
            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                titleTextView.setText("视频");
                titleTextView.setAlpha(1);
            }
        });
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
            return 2;
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
                viewHolder.liken.setText(String.valueOf(v.getReplyBeLiked()));
                viewHolder.replyn.setText(String.valueOf(v.getReplyBeReply()));

                if(v.isReplyLike()) viewHolder.likei.setImageResource(R.drawable.icon_liked);
                else viewHolder.likei.setImageResource(R.drawable.icon_like);
                if(v.isReplyDislike())
                    viewHolder.dislikei.setImageResource(R.drawable.icon_disliked);
                else viewHolder.dislikei.setImageResource(R.drawable.icon_dislike);
                if(v.getUserVip() == 2) viewHolder.name.setTextColor(getResources().getColor(R.color.mainColor));
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
                ImageView iv = uiRelpyListView.findViewWithTag(imageUrl);
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

    void setIcon()
    {
        uiLikeText.setText(videoDetail.getVideoLike());
        uiCoinText.setText(videoDetail.getVideoCoin());
        uiFavText.setText(videoDetail.getVideoFav());
        uiLikeImg.setImageResource(R.drawable.icon_vdd_do_like_no);
        uiCoinImg.setImageResource(R.drawable.icon_vdd_do_coin_no);
        uiFavImg.setImageResource(R.drawable.icon_vdd_do_fav_no);
        uiDislikeImg.setImageResource(R.drawable.icon_vdd_do_dislike_no);
        if(isLiked == 1)//赞
        {
            uiLikeImg.setImageResource(R.drawable.icon_vdd_do_like_yes);
            uiDislikeImg.setImageResource(R.drawable.icon_vdd_do_dislike_no);
        }
        else if(isLiked == 2)
        {
            uiLikeImg.setImageResource(R.drawable.icon_vdd_do_like_no);
            uiDislikeImg.setImageResource(R.drawable.icon_vdd_do_dislike_yes);
        }
        else
        {
            uiLikeImg.setImageResource(R.drawable.icon_vdd_do_like_no);
            uiDislikeImg.setImageResource(R.drawable.icon_vdd_do_dislike_no);
        }
        if(isCoined > 0) uiCoinImg.setImageResource(R.drawable.icon_vdd_do_coin_yes);
        else uiCoinImg.setImageResource(R.drawable.icon_vdd_do_coin_no);
        if(isFaved) uiFavImg.setImageResource(R.drawable.icon_vdd_do_fav_yes);
        else uiFavImg.setImageResource(R.drawable.icon_vdd_do_fav_no);
    }

    public void clickCover(View view)
    {
        Intent intent = new Intent(ctx, ImgActivity.class);
        intent.putExtra("imgUrl", new String[]{videoDetail.getVideoFace()});
        startActivity(intent);
    }

    public void clickPlay(View view)
    {
        Intent intent = new Intent(ctx, QRActivity.class);
        intent.putExtra("url", "https://www.bilibili.com/video/av" + videoDetail.getVideoAid());
        startActivity(intent);
    }

    public void clickCoverLater(View view)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if(videoDetail.playLater())
                    {
                        Looper.prepare();
                        Toast.makeText(ctx, "已添加至稍后再看", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                    else
                    {
                        Looper.prepare();
                        Toast.makeText(ctx, "未成功添加至稍后观看！请检查网络再试", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(ctx, "未成功添加至稍后观看！请检查网络再试", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }
        }).start();
    }

    public void clickHistory(View view)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if(videoDetail.playHistory())
                    {
                        Looper.prepare();
                        Toast.makeText(ctx, "已添加至历史记录！你可以在历史记录找到", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                    else
                    {
                        Looper.prepare();
                        Toast.makeText(ctx, "未成功添加至历史记录！请检查网络再试", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(ctx, "未成功添加至历史记录！请检查网络再试", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }
        }).start();
    }

    public void clickLike(View view)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if(isLiked == 1)
                    {
                        videoDetail.likeVideo(2);
                        isLiked = 0;
                        videoDetail.setSelfLiked(-1);
                        Looper.prepare();
                        Toast.makeText(ctx, "已取消喜欢...", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        videoDetail.likeVideo(1);
                        isLiked = 1;
                        videoDetail.setSelfLiked(1);
                        Looper.prepare();
                        Toast.makeText(ctx, "已喜欢！这个视频会被更多人看到！", Toast.LENGTH_SHORT).show();
                    }
                    handler.post(runnableImg);
                    Looper.loop();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(ctx, "喜欢失败...请检查你的网络..", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }
        }).start();
    }

    public void clickCoin(View view)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if(videoDetail.getVideoCopyright() == 1)  //1原创
                    {
                        if(isCoined < 2)
                        {
                            isCoined++;
                            videoDetail.coinVideo(1);
                            videoDetail.setSelfCoined(1);
                            Looper.prepare();
                            Toast.makeText(ctx, "你投了一个硬币！再次点击可以再次投币！", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Looper.prepare();
                            Toast.makeText(ctx, "最多投两个硬币...", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else  //2转载
                    {
                        if(isCoined < 1)
                        {
                            isCoined++;
                            videoDetail.coinVideo(1);
                            videoDetail.setSelfCoined(1);
                            Looper.prepare();
                            Toast.makeText(ctx, "你投了一个硬币！本稿件最多投一个硬币", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Looper.prepare();
                            Toast.makeText(ctx, "本稿件最多投一个硬币...", Toast.LENGTH_SHORT).show();
                        }
                    }
                    handler.post(runnableImg);
                    Looper.loop();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(ctx, "投币失败！请检查你的网络..", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }
        }).start();
    }

    public void clickFav(final View view)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if(isFaved)
                    {
                        isFaved = false;
                        videoDetail.favCancalVideo();
                        videoDetail.setSelfFaved(-1);
                        Looper.prepare();
                        Toast.makeText(ctx, "已取消收藏！", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        isFaved = true;
                        videoDetail.favVideo();
                        videoDetail.setSelfFaved(1);
                        Looper.prepare();
                        Toast.makeText(ctx, "已收藏至默认收藏夹！\n(别问我为什么不能选择别的..懒...)", Toast.LENGTH_SHORT).show();
                    }
                    handler.post(runnableImg);
                    Looper.loop();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(ctx, "收藏失败！请检查你的网络..", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }
        }).start();
    }

    public void clickDislike(View view)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if(isLiked == 2)
                    {
                        videoDetail.likeVideo(4);
                        isLiked = 0;
                        Looper.prepare();
                        Toast.makeText(ctx, "取消点踩成功！", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        videoDetail.likeVideo(3);
                        isLiked = 2;
                        Looper.prepare();
                        Toast.makeText(ctx, "点踩成功！", Toast.LENGTH_SHORT).show();
                    }
                    handler.post(runnableImg);
                    Looper.loop();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(ctx, "点踩失败！请检查你的网络..", Toast.LENGTH_SHORT).show();
                    Looper.loop();
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
                    try
                    {
                        if(videoDetail.sendReply(data.getStringExtra("text")))
                        {
                            Looper.prepare();
                            Toast.makeText(ctx, "发送成功！", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                        else
                        {
                            Looper.prepare();
                            Toast.makeText(ctx, "发送失败，可能是短时间发送过多？", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        Looper.prepare();
                        Toast.makeText(ctx, "评论发送失败。。请检查网络？", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }
            }).start();
        }
    }
}
