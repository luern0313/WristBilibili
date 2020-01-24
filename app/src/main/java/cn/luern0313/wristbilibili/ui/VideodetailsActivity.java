package cn.luern0313.wristbilibili.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.util.LruCache;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
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
import cn.luern0313.wristbilibili.api.FavorBoxApi;
import cn.luern0313.wristbilibili.models.FavorBoxModel;
import cn.luern0313.wristbilibili.models.ListofVideoModel;
import cn.luern0313.wristbilibili.api.OnlineVideoApi;
import cn.luern0313.wristbilibili.api.ReplyApi;
import cn.luern0313.wristbilibili.api.VideoDetailsApi;
import cn.luern0313.wristbilibili.models.ReplyModel;
import cn.luern0313.wristbilibili.service.DownloadService;
import cn.luern0313.wristbilibili.widget.ImageDownloader;

public class VideodetailsActivity extends Activity
{
    Context ctx;
    Intent intent;
    LayoutInflater inflater;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    VideoDetailsApi videoDetail;
    OnlineVideoApi onlineVideoApi;
    ReplyApi replyApi;
    ArrayList<VideoDetailsApi.VideoPart> videoPartArrayList;
    ArrayList<ReplyModel> replyArrayList;
    ArrayList<ListofVideoModel> recommendList;

    View layoutSendReply;
    View layoutChangeMode;
    View layoutLoading;

    Handler handler = new Handler();
    Runnable runnableNoWeb;
    Runnable runnableUi;
    Runnable runnableImg;
    Runnable runnableSetface;
    Runnable runnableNodata;

    Runnable runnableVideoLoadingFin;

    Runnable runnableReply;
    Runnable runnableReplyAddlist;
    Runnable runnableRecommend;
    Runnable runnableMoreNomore;
    Runnable runnableMoreErr;
    Runnable runnableReplyUpdate;

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

    LinearLayout uiVideoPartLayout;
    ListView uiRelpyListView;
    ListView uiRecommendListView;
    mAdapter replyAdapter;

    boolean isLogin = false;
    int isLiked = 0;//012
    int isCoined = 0;
    boolean isFaved = false;
    int replyPage = 1;
    boolean isReplyLoading = true;

    final private int RESULT_VD_FAVOR = 101;
    final private int RESULT_VD_DOWNLOAD = 102;
    final private int RESULT_VD_PART = 103;
    final private int RESULT_VD_SHARE = 104;
    final private int RESULT_REPLY_SEND = 201;

    private DownloadService.MyBinder myBinder;
    private VideoDownloadServiceConnection connection = new VideoDownloadServiceConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videodetails);

        ctx = this;
        intent = getIntent();
        sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        Intent serviceIntent = new Intent(ctx, DownloadService.class);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);

        inflater = getLayoutInflater();
        layoutSendReply = inflater.inflate(R.layout.widget_reply_sendreply, null);
        layoutLoading = inflater.inflate(R.layout.widget_loading, null);
        layoutChangeMode = inflater.inflate(R.layout.widget_reply_changemode, null);

        titleTextView = findViewById(R.id.vd_title_title);
        viewPager = findViewById(R.id.vd_viewpager);
        viewPager.setOffscreenPageLimit(2);
        uiLoadingImg = findViewById(R.id.vd_loading_img);
        uiLoading = findViewById(R.id.vd_loading);
        uiNoWeb = findViewById(R.id.vd_noweb);

        isLogin = !sharedPreferences.getString("cookies", "").equals("");
        videoDetail = new VideoDetailsApi(sharedPreferences.getString("cookies", ""),
                                          sharedPreferences.getString("csrf", ""),
                                          sharedPreferences.getString("mid", ""),
                                          intent.getStringExtra("aid"));
        replyApi = new ReplyApi(sharedPreferences.getString("cookies", ""),
                                sharedPreferences.getString("csrf", ""),
                                intent.getStringExtra("aid"), "1");

        uiLoadingImg.setImageResource(R.drawable.anim_loading);
        loadingImgAnim = (AnimationDrawable) uiLoadingImg.getDrawable();
        loadingImgAnim.start();
        uiLoading.setVisibility(View.VISIBLE);

        if(sharedPreferences.getBoolean("tip_vd", true)) findViewById(R.id.vd_tip).setVisibility(View.VISIBLE);

        runnableNoWeb = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    uiLoading.setVisibility(View.GONE);
                    uiNoWeb.setVisibility(View.VISIBLE);
                    isReplyLoading = false;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableUi = new Runnable()
        {
            @Override
            public void run()
            {
                try
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
                    videoPartArrayList = videoDetail.getVideoPartList();

                    if(videoDetail.getVideoPartSize() > 1)
                    {
                        findViewById(R.id.vd_video_part_layout).setVisibility(View.VISIBLE);
                        findViewById(R.id.vd_bt_play).setVisibility(View.GONE);
                        ((TextView) findViewById(R.id.vd_video_part_text)).setText("共" + String.valueOf(videoDetail.getVideoPartSize()) + "P");
                        for(int i = 0; i < videoPartArrayList.size(); i++)
                        {
                            if(i < 30)
                                uiVideoPartLayout.addView(getVideoPartButton(videoPartArrayList.get(i)));
                            else
                            {
                                TextView textView = new TextView(ctx);
                                textView.setWidth(90);
                                textView.setBackgroundResource(R.drawable.selector_bg_vd_videopart);
                                textView.setPadding(12, 6, 12, 6);
                                textView.setText("查看\n更多");
                                textView.setLines(2);
                                textView.setGravity(Gravity.CENTER);
                                textView.setEllipsize(TextUtils.TruncateAt.END);
                                textView.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        clickMorePart(null);
                                    }
                                });
                                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                lp.setMargins(0, 0, 4, 0);
                                textView.setLayoutParams(lp);
                                uiVideoPartLayout.addView(textView);
                                break;
                            }
                        }
                    }
                    else uiVideoPartLayout.setVisibility(View.GONE);

                    setIcon();
                    isReplyLoading = false;

                    uiLoading.setVisibility(View.GONE);
                    uiNoWeb.setVisibility(View.GONE);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableImg = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    setIcon();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableSetface = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    ((ImageView) findViewById(R.id.vd_card_head)).setImageBitmap(videoUpFace);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableNodata = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    findViewById(R.id.vd_novideo).setVisibility(View.VISIBLE);
                    isReplyLoading = false;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableVideoLoadingFin = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    findViewById(R.id.vd_vd_loading).setVisibility(View.GONE);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableReply = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    replyAdapter = new mAdapter(inflater, replyArrayList);
                    uiRelpyListView.setAdapter(replyAdapter);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableReplyAddlist = new Runnable()
        {
            @Override
            public void run()
            {
                replyAdapter.notifyDataSetChanged();
            }
        };

        runnableRecommend = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    rAdapter recommendAdapter = new rAdapter(inflater, recommendList);
                    uiRecommendListView.setAdapter(recommendAdapter);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableMoreNomore = new Runnable()
        {
            @Override
            public void run()
            {
                ((TextView) layoutLoading.findViewById(R.id.wid_load_text)).setText("  没有更多了...");
            }
        };

        runnableMoreErr = new Runnable()
        {
            @Override
            public void run()
            {
                ((TextView) layoutLoading.findViewById(R.id.wid_load_text)).setText("好像没有网络...\n检查下网络？");
                layoutLoading.findViewById(R.id.wid_load_button).setVisibility(View.VISIBLE);
                isReplyLoading = false;
            }
        };

        runnableReplyUpdate = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    replyAdapter.notifyDataSetChanged();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        layoutLoading.findViewById(R.id.wid_load_button).setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        ((TextView) layoutLoading.findViewById(R.id.wid_load_text)).setText(" 加载中. . .");
                        layoutLoading.findViewById(R.id.wid_load_button).setVisibility(View.GONE);
                        getMoreReply();
                    }
                });

        final PagerAdapter pagerAdapter = new PagerAdapter()
        {
            @Override
            public int getCount()
            {
                return 3;
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
                    uiVideoPartLayout = v.findViewById(R.id.vd_video_part);

                    v.findViewById(R.id.vd_card).setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Intent intent = new Intent(ctx, OtherUserActivity.class);
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
                else if(position == 1)
                {
                    View v = inflater.inflate(R.layout.viewpager_vd_reply, null);
                    v.setTag(1);

                    uiRelpyListView = v.findViewById(R.id.vd_reply_listview);
                    uiRelpyListView.setEmptyView(v.findViewById(R.id.vd_reply_nothing));
                    uiRelpyListView.addHeaderView(layoutSendReply, null, true);
                    uiRelpyListView.addFooterView(layoutLoading, null, true);
                    uiRelpyListView.setHeaderDividersEnabled(false);
                    uiRelpyListView.setOnScrollListener(new AbsListView.OnScrollListener()
                    {
                        @Override
                        public void onScrollStateChanged(AbsListView view, int scrollState)
                        {
                        }

                        @Override
                        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
                        {
                            if(visibleItemCount + firstVisibleItem == totalItemCount && !isReplyLoading && isLogin)
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
                                replyArrayList.add(new ReplyModel(1));
                                replyArrayList.addAll(replyApi.getReply(1, "2", 5, ""));
                                replyArrayList.add(new ReplyModel(2));
                                replyArrayList.addAll(replyApi.getReply(1, "0", 0, ""));
                                handler.post(runnableReply);
                            }
                            catch (IOException | NullPointerException e)
                            {
                                e.printStackTrace();
                                replyArrayList = new ArrayList<>();
                                handler.post(runnableReply);
                            }
                        }
                    }).start();
                    container.addView(v);
                    return 1;
                }
                else
                {
                    View v = inflater.inflate(R.layout.viewpager_vd_recommend, null);
                    v.setTag(2);

                    uiRecommendListView = v.findViewById(R.id.vd_recommend_listview);
                    uiRecommendListView.setEmptyView(v.findViewById(R.id.vd_recommend_nothing));
                    uiRecommendListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                    {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                        {
                            Intent intent = new Intent(ctx, VideodetailsActivity.class);
                            intent.putExtra("aid", String.valueOf(
                                    recommendList.get(position).getVideoAid()));
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
                                recommendList = videoDetail.getRecommendVideos();
                                handler.post(runnableRecommend);
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                                recommendList = new ArrayList<>();
                                handler.post(runnableRecommend);
                            }
                        }
                    }).start();
                    container.addView(v);
                    return 2;
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
                if(position == 0) titleAnim("视频详情");
                else if(position == 1) titleAnim("视频评论");
                else if(position == 2) titleAnim("相关推荐");
            }
        });

        viewPager.setAdapter(pagerAdapter);
    }

    TextView getVideoPartButton(final VideoDetailsApi.VideoPart part)
    {
        TextView textView = new TextView(ctx);
        textView.setWidth(170);
        textView.setBackgroundResource(R.drawable.selector_bg_vd_videopart);
        textView.setPadding(12, 6, 12, 6);
        textView.setText(part.getPartName());
        textView.setLines(2);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(ctx, PlayerActivity.class);
                intent.putExtra("title", part.getPartName() + " - " + videoDetail.getVideoTitle());
                intent.putExtra("aid", videoDetail.getVideoAid());
                intent.putExtra("part", String.valueOf(part.getPartNum()));
                intent.putExtra("cid", String.valueOf(part.getPartCid()));
                startActivity(intent);
            }
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 4, 0);
        textView.setLayoutParams(lp);
        return textView;
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
                    ArrayList<ReplyModel> r = replyApi.getReply(replyPage, "0", 0, "");
                    if(r != null && r.size() != 0)
                    {
                        replyArrayList.addAll(r);
                        isReplyLoading = false;
                        handler.post(runnableReplyAddlist);
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

    void titleAnim(final String title)
    {
        ObjectAnimator alpha = ObjectAnimator.ofFloat(titleTextView, "alpha", 1f, 0f);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(alpha);
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
                titleTextView.setText(title);
                titleTextView.setAlpha(1);
            }
        });
    }

    public void clickVdTip(View view)
    {
        findViewById(R.id.vd_tip).setVisibility(View.GONE);
        editor.putBoolean("tip_vd", false);
        editor.commit();
    }

    class mAdapter extends BaseAdapter
    {
        private LayoutInflater mInflater;

        private LruCache<String, BitmapDrawable> mImageCache;

        private ArrayList<ReplyModel> replyList;

        public mAdapter(LayoutInflater inflater, ArrayList<ReplyModel> replyList)
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
            final ReplyModel v = replyList.get(position);
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

                if(v.isReplyLike()) viewHolder.likei.setImageResource(R.drawable.icon_liked);
                else viewHolder.likei.setImageResource(R.drawable.icon_like);
                if(v.isReplyDislike()) viewHolder.dislikei.setImageResource(
                        R.drawable.icon_disliked);
                else viewHolder.dislikei.setImageResource(R.drawable.icon_dislike);
                if(v.getUserVip() == 2) viewHolder.name.setTextColor(
                        getResources().getColor(R.color.mainColor));
                else viewHolder.name.setTextColor(getResources().getColor(R.color.black));

                viewHolder.img.setTag(v.getUserHead());
                BitmapDrawable h = setImageFormWeb(v.getUserHead());
                if(h != null) viewHolder.img.setImageDrawable(h);

                viewHolder.img.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Intent intent = new Intent(ctx, OtherUserActivity.class);
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
                                String va = v.likeReply(v.getReplyId(), v.isReplyLike() ? 0 : 1, "1");
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
                                String va = v.hateReply(v.getReplyId(), v.isReplyDislike() ? 0 : 1, "1");
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
                        rintent.putExtra("oid", intent.getStringExtra("aid"));
                        rintent.putExtra("type", "1");
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
                    bitmap = ImageDownloader.downloadImage(imageUrl);
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
        }

    }

    class rAdapter extends BaseAdapter
    {
        private LayoutInflater mInflater;

        private LruCache<String, BitmapDrawable> mImageCache;

        private ArrayList<ListofVideoModel> recommendList;

        public rAdapter(LayoutInflater inflater, ArrayList<ListofVideoModel> recommendList)
        {
            mInflater = inflater;
            this.recommendList = recommendList;

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
            return recommendList.size();
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
            final ListofVideoModel v = recommendList.get(position);
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
            viewHolder.title.setText(v.getVideoTitle());
            viewHolder.up.setText("UP : " + v.getOwnerName());
            viewHolder.play.setText("播放 : " + v.getVideoPlay() + "  弹幕 : " + v.getVideoDanmaku());

            viewHolder.img.setTag(v.getVideoImg());
            BitmapDrawable h = setImageFormWeb(v.getVideoImg());
            if(h != null) viewHolder.img.setImageDrawable(h);

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
                ImageView iv = uiRecommendListView.findViewWithTag(imageUrl);
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


    public void clickMorePart(View view)
    {
        String[] videoPartNames = new String[videoPartArrayList.size()];
        String[] videoPartCids = new String[videoPartArrayList.size()];
        for(int i = 0; i < videoPartArrayList.size(); i++)
            videoPartNames[i] = videoPartArrayList.get(i).getPartName();
        for(int i = 0; i < videoPartArrayList.size(); i++)
            videoPartCids[i] = String.valueOf(videoPartArrayList.get(i).getPartCid());
        Intent intent = new Intent(ctx, SelectPartActivity.class);
        intent.putExtra("title", "分P");
        intent.putExtra("options_name", videoPartNames);
        intent.putExtra("options_id", videoPartCids);
        startActivityForResult(intent, RESULT_VD_PART);
    }

    public void clickCover(View view)
    {
        Intent intent = new Intent(ctx, ImgActivity.class);
        intent.putExtra("imgUrl", new String[]{videoDetail.getVideoFace()});
        startActivity(intent);
    }

    public void clickPlay(View view)
    {
        Intent intent = new Intent(ctx, PlayerActivity.class);
        intent.putExtra("title", videoDetail.getVideoTitle());
        intent.putExtra("aid", videoDetail.getVideoAid());
        intent.putExtra("part", "1");
        intent.putExtra("cid", videoDetail.getVideoCid());
        startActivity(intent);
    }

    public void clickDownload(View view)
    {
        String[] videoPartNames = new String[videoPartArrayList.size()];
        String[] videoPartCids = new String[videoPartArrayList.size()];
        for(int i = 0; i < videoPartArrayList.size(); i++)
            videoPartNames[i] = videoPartArrayList.get(i).getPartName();
        for(int i = 0; i < videoPartArrayList.size(); i++)
            videoPartCids[i] = String.valueOf(videoPartArrayList.get(i).getPartCid());
        Intent intent = new Intent(ctx, SelectPartActivity.class);
        intent.putExtra("title", "分P下载");
        intent.putExtra("tip", "选择要下载的分P");
        intent.putExtra("options_name", videoPartNames);
        intent.putExtra("options_id", videoPartCids);
        startActivityForResult(intent, RESULT_VD_DOWNLOAD);
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

    public void clickShare(View view)
    {
        Intent intent = new Intent(ctx, SendDynamicActivity.class);
        intent.putExtra("is_share", true);
        intent.putExtra("share_dyid", videoDetail.getVideoAid());
        intent.putExtra("share_up", videoDetail.getVideoUpName());
        intent.putExtra("share_img", videoDetail.getVideoFace());
        intent.putExtra("share_title", videoDetail.getVideoTitle());
        startActivityForResult(intent, RESULT_VD_SHARE);
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
        findViewById(R.id.vd_vd_loading).setVisibility(View.VISIBLE);
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    FavorBoxApi favorBoxApi = new FavorBoxApi(
                            sharedPreferences.getString("cookies", ""),
                            sharedPreferences.getString("mid", ""));
                    ArrayList<FavorBoxModel> favorBoxArrayList = favorBoxApi.getFavorbox();
                    String[] favorBoxNames = new String[favorBoxArrayList.size()];
                    for (int i = 0; i < favorBoxArrayList.size(); i++)
                        favorBoxNames[i] = favorBoxArrayList.get(i).title;
                    String[] favorBoxIds = new String[favorBoxArrayList.size()];
                    for (int i = 0; i < favorBoxArrayList.size(); i++)
                        favorBoxIds[i] = favorBoxArrayList.get(i).id;
                    Intent intent = new Intent(ctx, SelectPartActivity.class);
                    intent.putExtra("title", "收藏");
                    intent.putExtra("tip", "选择收藏夹");
                    intent.putExtra("options_name", favorBoxNames);
                    intent.putExtra("options_id", favorBoxIds);
                    startActivityForResult(intent, RESULT_VD_FAVOR);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(ctx, "收藏失败！请检查你的网络..", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
                finally
                {
                    handler.post(runnableVideoLoadingFin);
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
        startActivityForResult(replyIntent, RESULT_REPLY_SEND);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != 0) return;
        switch (requestCode)
        {
            case RESULT_VD_FAVOR:
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            String result = videoDetail.favVideo(data.getStringExtra("option_id"));
                            Looper.prepare();
                            if("success".equals(result))
                            {
                                isFaved = true;
                                videoDetail.setSelfFaved(1);
                                Toast.makeText(ctx, "已收藏至 " + data.getStringExtra("option_name") + " 收藏夹！", Toast.LENGTH_SHORT).show();
                                handler.post(runnableImg);
                            }
                            else Toast.makeText(ctx, "错误：" + result, Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }).start();
                break;

            case RESULT_VD_DOWNLOAD:
                findViewById(R.id.vd_vd_loading).setVisibility(View.VISIBLE);
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            onlineVideoApi = new OnlineVideoApi(sharedPreferences.getString("cookies", ""),
                                                                sharedPreferences.getString("csrf", ""),
                                                                sharedPreferences.getString("mid", ""),
                                                                videoDetail.getVideoAid(),
                                                                String.valueOf(data.getIntExtra("option_position", 0) + 1),
                                                                data.getStringExtra("option_id"));
                            onlineVideoApi.connectionVideoUrl();
                            handler.post(runnableVideoLoadingFin);
                            connection.setVideoPartData(data.getStringExtra("option_name") + " - " + videoDetail.getVideoTitle(), data.getStringExtra("option_id"));
                            connection.downloadVideo();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                            Looper.prepare();
                            Toast.makeText(ctx, "网络连接失败，请检查网络", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    }
                }).start();
                break;

            case RESULT_VD_PART:
                Intent intent = new Intent(ctx, PlayerActivity.class);
                intent.putExtra("title", data.getStringExtra("option_name") + " - " + videoDetail.getVideoTitle());
                intent.putExtra("aid", videoDetail.getVideoAid());
                intent.putExtra("part", String.valueOf(data.getIntExtra("option_position", 1)));
                intent.putExtra("cid", data.getStringExtra("option_id"));
                startActivity(intent);
                break;

            case RESULT_VD_SHARE:
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            if(videoDetail.shareVideo(data.getStringExtra("text")))
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
                            Toast.makeText(ctx, "分享视频失败。。请检查网络？", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    }
                }).start();
                break;

            case RESULT_REPLY_SEND:
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
                break;
        }
    }

    class VideoDownloadServiceConnection implements ServiceConnection
    {
        String title;
        String cid;

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            myBinder = (DownloadService.MyBinder) service;
        }

        void setVideoPartData(String title, String cid)
        {
            this.title = title;
            this.cid = cid;
        }

        void downloadVideo()
        {
            String result = myBinder.startDownload(videoDetail.getVideoAid(), cid, title,
                                                   videoDetail.getVideoFace(),
                                                   onlineVideoApi.getVideoUrl(),
                                                   onlineVideoApi.getDanmakuUrl());
            Looper.prepare();
            if(result.equals("")) Toast.makeText(ctx, "已添加至下载列表", Toast.LENGTH_SHORT).show();
            else Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
            Looper.loop();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        try
        {
            unbindService(connection);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
