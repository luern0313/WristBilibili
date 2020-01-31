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
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.BangumiRecommendAdapter;
import cn.luern0313.wristbilibili.adapter.ReplyAdapter;
import cn.luern0313.wristbilibili.adapter.VideoPartAdapter;
import cn.luern0313.wristbilibili.api.BangumiApi;
import cn.luern0313.wristbilibili.api.OnlineVideoApi;
import cn.luern0313.wristbilibili.api.ReplyApi;
import cn.luern0313.wristbilibili.models.BangumiModel;
import cn.luern0313.wristbilibili.models.BangumiRecommendModel;
import cn.luern0313.wristbilibili.models.ReplyModel;
import cn.luern0313.wristbilibili.service.DownloadService;

public class BangumiActivity extends Activity
{
    Context ctx;
    Intent intent;
    LayoutInflater inflater;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    BangumiApi bangumiApi;
    BangumiModel bangumiModel;
    ReplyApi replyApi;
    ArrayList<ReplyModel> replyArrayList;
    ArrayList<BangumiRecommendModel> bangumiRecommendModelArrayList;
    OnlineVideoApi onlineVideoApi;
    String seasonId;

    TextView uiTitle;
    ViewPager uiViewPager;
    ImageView uiLoadingImg;
    LinearLayout uiLoading;
    LinearLayout uiNoWeb;
    View layoutReplyLoading;

    RecyclerView uiDetailEpisodesRecyclerView;
    RecyclerView uiDetailSectionsRecyclerView;
    VideoPartAdapter episodesRecyclerViewAdapter;
    VideoPartAdapter sectionsRecyclerViewAdapter;
    ListView uiReplyListView;
    ReplyAdapter replyAdapter;
    ReplyAdapter.ReplyAdapterListener replyAdapterListener;
    ListView uiRecommendListView;
    BangumiRecommendAdapter bangumiRecommendAdapter;

    boolean isLogin = false;
    boolean isReplyLoading = true;
    int replyPage;

    AnimationDrawable loadingImgAnim;

    Handler handler = new Handler();
    Runnable runnableDetailUi;
    Runnable runnableDetailNoWeb;
    Runnable runnableDetailNodata;
    Runnable runnableDetailLoadingFin;
    Runnable runnableDetailSetIcon;
    Runnable runnableReplyUi;
    Runnable runnableReplyUpdate;
    Runnable runnableReplyMoreNomore;
    Runnable runnableReplyMoreErr;
    Runnable runnableRecommendUi;

    final private int RESULT_DETAIL_EPISODE = 101;
    final private int RESULT_DETAIL_OTHER = 102;
    final private int RESULT_DETAIL_DOWNLOAD = 103;
    final private int RESULT_DETAIL_SHARE = 104;
    final private int RESULT_REPLY_SEND = 201;

    private DownloadService.MyBinder myBinder;
    private BangumiDownloadServiceConnection connection = new BangumiDownloadServiceConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bangumi);
        ctx = this;
        intent = getIntent();
        sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        seasonId = intent.getStringExtra("season_id");

        inflater = getLayoutInflater();
        layoutReplyLoading = inflater.inflate(R.layout.widget_loading, null);

        uiTitle = findViewById(R.id.bgm_title_title);
        uiViewPager = findViewById(R.id.bgm_viewpager);
        uiViewPager.setOffscreenPageLimit(2);
        uiLoadingImg = findViewById(R.id.bgm_loading_img);
        uiLoading = findViewById(R.id.bgm_loading);
        uiNoWeb = findViewById(R.id.bgm_noweb);

        isLogin = !sharedPreferences.getString("cookies", "").equals("");
        bangumiApi = new BangumiApi(sharedPreferences.getString("cookies", ""),
                                    sharedPreferences.getString("mid", ""),
                                    sharedPreferences.getString("csrf", ""),
                                    sharedPreferences.getString("access_key", ""),
                                    seasonId);

        uiLoadingImg.setImageResource(R.drawable.anim_loading);
        loadingImgAnim = (AnimationDrawable) uiLoadingImg.getDrawable();
        loadingImgAnim.start();
        uiLoading.setVisibility(View.VISIBLE);

        replyAdapterListener = new ReplyAdapter.ReplyAdapterListener()
        {
            @Override
            public void onClick(int viewId, int position)
            {
                onReplyViewClick(viewId, position);
            }
        };

        runnableDetailUi = new Runnable()
        {
            @Override
            public void run()
            {
                ((TextView) findViewById(R.id.bgm_detail_title)).setText(bangumiModel.bangumi_title);
                if(bangumiModel.bangumi_score.equals("")) findViewById(R.id.bgm_detail_score).setVisibility(View.GONE);
                else ((TextView) findViewById(R.id.bgm_detail_score)).setText(bangumiModel.bangumi_score);
                ((TextView) findViewById(R.id.bgm_detail_play)).setText(bangumiModel.bangumi_play);
                ((TextView) findViewById(R.id.bgm_detail_like)).setText(bangumiModel.bangumi_like);
                ((TextView) findViewById(R.id.bgm_detail_series)).setText(bangumiModel.bangumi_series);
                if(bangumiModel.bangumi_needvip.equals("")) findViewById(R.id.bgm_detail_needvip).setVisibility(View.GONE);
                else ((TextView) findViewById(R.id.bgm_detail_needvip)).setText(bangumiModel.bangumi_needvip);

                Drawable playNumDrawable = getResources().getDrawable(R.drawable.icon_video_play_num);
                Drawable danmakuNumDrawable = getResources().getDrawable(R.drawable.icon_video_like_num);
                playNumDrawable.setBounds(0, 0, 24, 24);
                danmakuNumDrawable.setBounds(0, 0, 24, 24);
                ((TextView) findViewById(R.id.bgm_detail_play)).setCompoundDrawables(playNumDrawable,
                                                                                     null, null,
                                                                                     null);
                ((TextView) findViewById(R.id.bgm_detail_like)).setCompoundDrawables(
                        danmakuNumDrawable, null, null, null);

                setBangumiIcon();

                if(bangumiModel.bangumi_episodes.size() != 0)
                {
                    findViewById(R.id.bgm_detail_video_part_layout).setVisibility(View.VISIBLE);
                    ((TextView) findViewById(R.id.bgm_detail_video_part_text)).setText("正片-共" + String.valueOf(bangumiModel.bangumi_episodes.size()) + "话");
                    LinearLayoutManager layoutManager = new LinearLayoutManager(BangumiActivity.super.getParent());
                    layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                    uiDetailEpisodesRecyclerView.setLayoutManager(layoutManager);
                    episodesRecyclerViewAdapter = new VideoPartAdapter(bangumiModel.bangumi_episodes, bangumiModel,
                                                                       1);
                    episodesRecyclerViewAdapter.setOnItemClickListener(new VideoPartAdapter.OnItemClickListener()
                    {
                        @Override
                        public void onItemClick(View view, int position)
                        {
                            clickBangumiDetailEpisode(position);
                        }
                    });
                    uiDetailEpisodesRecyclerView.setAdapter(episodesRecyclerViewAdapter);
                    if(bangumiModel.bangumi_user_progress_mode == 1)
                        ((LinearLayoutManager) uiDetailEpisodesRecyclerView.getLayoutManager()).
                                scrollToPositionWithOffset(bangumiModel.bangumi_user_progress_position,
                                                           0);
                }

                if(bangumiModel.bangumi_sections.size() != 0)
                {
                    findViewById(R.id.bgm_detail_video_other_layout).setVisibility(View.VISIBLE);
                    ((TextView) findViewById(R.id.bgm_detail_video_other_text)).setText(bangumiModel.bangumi_section_name);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(BangumiActivity.super.getParent());
                    layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                    uiDetailSectionsRecyclerView.setLayoutManager(layoutManager);
                    sectionsRecyclerViewAdapter = new VideoPartAdapter(bangumiModel.bangumi_sections, bangumiModel, 2);
                    sectionsRecyclerViewAdapter.setOnItemClickListener(new VideoPartAdapter.OnItemClickListener()
                    {
                        @Override
                        public void onItemClick(View view, int position)
                        {
                            clickBangumiDetailOther(position);
                        }
                    });
                    uiDetailSectionsRecyclerView.setAdapter(sectionsRecyclerViewAdapter);
                    if(bangumiModel.bangumi_user_progress_mode == 2)
                        ((LinearLayoutManager) uiDetailSectionsRecyclerView.getLayoutManager()).
                                scrollToPositionWithOffset(bangumiModel.bangumi_user_progress_position,
                                                           0);
                }

                if(bangumiModel.bangumi_seasons.size() > 1)
                {
                    findViewById(R.id.bgm_detail_video_season_layout).setVisibility(View.VISIBLE);
                    LinearLayout episodesLinearLayout = findViewById(R.id.bgm_detail_video_season);
                    for (int i = 0; i < bangumiModel.bangumi_seasons.size(); i++)
                        episodesLinearLayout.addView(getVideoSeasonButton(bangumiModel.bangumi_seasons.get(i)));
                }

                uiLoading.setVisibility(View.GONE);
                uiNoWeb.setVisibility(View.GONE);

                getReply();
                getRecommend();
            }
        };

        runnableDetailNoWeb = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    uiLoading.setVisibility(View.GONE);
                    uiNoWeb.setVisibility(View.VISIBLE);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableDetailNodata = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    findViewById(R.id.bgm_novideo).setVisibility(View.VISIBLE);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableDetailLoadingFin = new Runnable()
        {
            @Override
            public void run()
            {
                findViewById(R.id.bgm_detail_loading).setVisibility(View.GONE);
            }
        };

        runnableDetailSetIcon = new Runnable()
        {

            @Override
            public void run()
            {
                try
                {
                    setBangumiIcon();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableReplyUi = new Runnable()
        {
            @Override
            public void run()
            {
                isReplyLoading = false;
                replyAdapter = new ReplyAdapter(inflater, uiReplyListView, replyArrayList, replyApi.isShowFloor(), replyAdapterListener);
                uiReplyListView.setAdapter(replyAdapter);
            }
        };

        runnableReplyUpdate = new Runnable()
        {
            @Override
            public void run()
            {
                isReplyLoading = false;
                replyAdapter.notifyDataSetChanged();
            }
        };

        runnableReplyMoreNomore = new Runnable()
        {
            @Override
            public void run()
            {
                isReplyLoading = false;
                ((TextView) layoutReplyLoading.findViewById(R.id.wid_load_text)).setText("  没有更多了...");
            }
        };

        runnableReplyMoreErr = new Runnable()
        {
            @Override
            public void run()
            {
                isReplyLoading = false;
                ((TextView) layoutReplyLoading.findViewById(R.id.wid_load_text)).setText("好像没有网络...\n检查下网络？");
                layoutReplyLoading.findViewById(R.id.wid_load_button).setVisibility(View.VISIBLE);
            }
        };

        runnableRecommendUi = new Runnable()
        {
            @Override
            public void run()
            {
                bangumiRecommendAdapter = new BangumiRecommendAdapter(inflater, uiRecommendListView, bangumiRecommendModelArrayList);
                uiRecommendListView.setAdapter(bangumiRecommendAdapter);
            }
        };

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
                    View v = inflater.inflate(R.layout.viewpager_bangumi_detail, null);
                    v.setTag(0);

                    uiDetailEpisodesRecyclerView = v.findViewById(R.id.bgm_detail_video_part);
                    uiDetailSectionsRecyclerView = v.findViewById(R.id.bgm_detail_video_other);

                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                bangumiModel = bangumiApi.getBangumiInfo();
                                if(bangumiModel != null)
                                {
                                    handler.post(runnableDetailUi);
                                }
                                else
                                {
                                    handler.post(runnableDetailNodata);
                                }
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                                handler.post(runnableDetailNoWeb);
                            }
                        }
                    }).start();
                    container.addView(v);
                    return 0;
                }
                else if(position == 1)
                {
                    View v = inflater.inflate(R.layout.viewpager_bangumi_reply, null);
                    v.setTag(1);

                    uiReplyListView = v.findViewById(R.id.bgm_reply_listview);
                    uiReplyListView.setEmptyView(v.findViewById(R.id.bgm_reply_nothing));
                    uiReplyListView.addHeaderView(inflater.inflate(R.layout.widget_reply_sendreply, null), null, true);
                    uiReplyListView.addFooterView(layoutReplyLoading, null, true);
                    uiReplyListView.setHeaderDividersEnabled(false);
                    uiReplyListView.setOnScrollListener(new AbsListView.OnScrollListener()
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

                    container.addView(v);
                    return 1;
                }
                else
                {
                    View v = inflater.inflate(R.layout.viewpager_bangumi_recommend, null);
                    v.setTag(2);

                    uiRecommendListView = v.findViewById(R.id.bgm_recommend_listview);
                    uiRecommendListView.setEmptyView(v.findViewById(R.id.bgm_recommend_nothing));
                    uiRecommendListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                    {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                        {
                            Intent intent = new Intent(ctx, BangumiActivity.class);
                            intent.putExtra("season_id", bangumiRecommendModelArrayList.get(position).bangumi_season_id);
                            startActivity(intent);
                        }
                    });

                    container.addView(v);
                    return 2;
                }
            }
        };

        uiViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
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
                if(position == 0) titleAnim("番剧详情");
                else if(position == 1) titleAnim("番剧评论");
                else if(position == 2) titleAnim("相关推荐");
            }
        });

        uiViewPager.setAdapter(pagerAdapter);
    }

    private void onReplyViewClick(int viewId, int position)
    {
        final ReplyModel replyModel = replyArrayList.get(position);
        if(viewId == R.id.item_reply_head)
        {
            Intent intent = new Intent(ctx, OtherUserActivity.class);
            intent.putExtra("mid", replyModel.getUserMid());
            startActivity(intent);
        }
        else if(viewId == R.id.item_reply_like)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    String va = replyModel.likeReply(replyModel.getReplyId(), replyModel.isReplyLike() ? 0 : 1, "1");
                    if(va.equals("")) handler.post(runnableReplyUpdate);
                    else
                    {
                        Looper.prepare();
                        Toast.makeText(ctx, (replyModel.isReplyLike() ? "取消" : "点赞") + "失败：\n" + va, Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }
            }).start();
        }
        else if(viewId == R.id.item_reply_dislike)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    String va = replyModel.hateReply(replyModel.getReplyId(), replyModel.isReplyDislike() ? 0 : 1, "1");
                    if(va.equals("")) handler.post(runnableReplyUpdate);
                    else
                    {
                        Looper.prepare();
                        Toast.makeText(ctx, (replyModel.isReplyDislike() ? "取消" : "点踩") + "失败：\n" + va, Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }
            }).start();
        }
        else if(viewId == R.id.item_reply_reply)
        {
            Intent rintent = new Intent(ctx, CheckreplyActivity.class);
            rintent.putExtra("oid", intent.getStringExtra("aid"));
            rintent.putExtra("type", "1");
            rintent.putExtra("root", replyModel.getReplyId());
            startActivity(rintent);
        }
    }

    void setBangumiIcon()
    {
        findViewById(R.id.bgm_detail_loading).setVisibility(View.GONE);
        if(bangumiModel.bangumi_user_is_follow)
        {
            ((ImageView) findViewById(R.id.bgm_detail_bt_follow)).setImageResource(R.drawable.icon_vdd_do_follow_yes);
            ((TextView) findViewById(R.id.bgm_detail_bt_follow_text)).setText("已追番");
        }
        else
        {
            ((ImageView) findViewById(R.id.bgm_detail_bt_follow)).setImageResource(R.drawable.icon_vdd_do_follow_no);
            ((TextView) findViewById(R.id.bgm_detail_bt_follow_text)).setText("追番");
        }
    }

    void getReply()
    {
        replyPage = 1;
        isReplyLoading = true;
        replyApi = new ReplyApi(sharedPreferences.getString("cookies", ""),
                                sharedPreferences.getString("csrf", ""),
                                bangumiModel.bangumi_user_progress_aid, "1");
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
                    handler.post(runnableReplyUi);
                }
                catch (IOException | NullPointerException e)
                {
                    e.printStackTrace();
                    replyArrayList = new ArrayList<>();
                    handler.post(runnableReplyUi);
                }
            }
        }).start();
    }

    void getMoreReply()
    {
        if(replyApi == null || !replyApi.getOid().equals(bangumiModel.bangumi_user_progress_aid))
            getReply();
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
                        handler.post(runnableReplyUpdate);
                    }
                    else
                    {
                        handler.post(runnableReplyMoreNomore);
                    }
                }
                catch (IOException e)
                {
                    handler.post(runnableReplyMoreErr);
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public void getRecommend()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    bangumiRecommendModelArrayList = bangumiApi.getBangumiRecommend();
                    handler.post(runnableRecommendUi);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    void clickBangumiDetailEpisode(int position)
    {
        BangumiModel.BangumiEpisodeModel ep = bangumiModel.bangumi_episodes.get(position);
        bangumiModel.bangumi_user_progress_epid = ep.bangumi_episode_id;
        bangumiModel.bangumi_user_progress_mode = 1;
        bangumiModel.bangumi_user_progress_position = position;
        bangumiModel.bangumi_user_progress_aid = ep.bangumi_episode_aid;
        episodesRecyclerViewAdapter.notifyDataSetChanged();
        sectionsRecyclerViewAdapter.notifyDataSetChanged();
        getReply();
        Intent intent = new Intent(ctx, PlayerActivity.class);
        intent.putExtra("title", "第" + ep.bangumi_episode_title + "话 " + ep.bangumi_episode_title_long);
        intent.putExtra("aid", ep.bangumi_episode_aid);
        intent.putExtra("cid", ep.bangumi_episode_cid);
        startActivity(intent);
    }

    void clickBangumiDetailOther(int position)
    {
        BangumiModel.BangumiEpisodeModel ss = bangumiModel.bangumi_sections.get(position);
        bangumiModel.bangumi_user_progress_epid = ss.bangumi_episode_id;
        bangumiModel.bangumi_user_progress_mode = 2;
        bangumiModel.bangumi_user_progress_position = position;
        bangumiModel.bangumi_user_progress_aid = ss.bangumi_episode_aid;
        episodesRecyclerViewAdapter.notifyDataSetChanged();
        sectionsRecyclerViewAdapter.notifyDataSetChanged();
        getReply();
        Intent intent = new Intent(ctx, PlayerActivity.class);
        intent.putExtra("title", ss.bangumi_episode_title_long.equals("") ? ss.bangumi_episode_title : ss.bangumi_episode_title_long);
        intent.putExtra("aid", ss.bangumi_episode_aid);
        intent.putExtra("cid", ss.bangumi_episode_cid);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, final Intent data)
    {
        if(resultCode != 0) return;
        if(requestCode == RESULT_DETAIL_EPISODE)
        {
            clickBangumiDetailEpisode(data.getIntExtra("option_position", 0));
        }
        else if(requestCode == RESULT_DETAIL_OTHER)
        {
            clickBangumiDetailOther(data.getIntExtra("option_position", 0));
        }
        else if(requestCode == RESULT_DETAIL_DOWNLOAD)
        {
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
                                                            data.getStringExtra("option_id").split("，")[0],
                                                            data.getStringExtra("option_id").split("，")[1]);
                        onlineVideoApi.connectionVideoUrl();
                        handler.post(runnableDetailLoadingFin);
                        connection.downloadVideo(data.getStringExtra("option_name") + " - " + bangumiModel.bangumi_title,
                                                 data.getStringExtra("option_id").split("，")[0],
                                                 data.getStringExtra("option_id").split("，")[1]);
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
        }
        else if(requestCode == RESULT_DETAIL_SHARE)
        {
            //TODO type待确定
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        String result = bangumiApi.shareBangumi(data.getStringExtra("text"));
                        if(result.equals(""))
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
        }
    }

    public void clickBangumiDetail(View view)
    {
        Intent intent = new Intent(ctx, TextActivity.class);
        intent.putExtra("title", "番剧信息");
        intent.putExtra("text", getBangumiInfo());
        startActivity(intent);
    }

    public void clickBangumiFollow(View view)
    {
        findViewById(R.id.bgm_detail_loading).setVisibility(View.VISIBLE);
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    String result = bangumiApi.followBangumi(!bangumiModel.bangumi_user_is_follow);
                    handler.post(runnableDetailLoadingFin);
                    handler.post(runnableDetailSetIcon);
                    if(!"".equals(result))
                    {
                        Looper.prepare();
                        Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void clickBangumiCover(View view)
    {
        Intent intent = new Intent(ctx, ImgActivity.class);
        intent.putExtra("imgUrl", new String[]{bangumiModel.bangumi_cover});
        startActivity(intent);
    }

    public void clickBangumiDownload(View view)
    {
        String[] videoPartNames = new String[bangumiModel.bangumi_episodes.size() + bangumiModel.bangumi_sections.size()];
        String[] videoPartCids = new String[bangumiModel.bangumi_episodes.size() + bangumiModel.bangumi_sections.size()];
        for(int i = 0; i < bangumiModel.bangumi_episodes.size(); i++)
            videoPartNames[i] = bangumiModel.bangumi_episodes.get(i).position + "：" + bangumiModel.bangumi_episodes.get(i).bangumi_episode_title_long;
        for(int i = bangumiModel.bangumi_episodes.size(); i < videoPartNames.length; i++)
            videoPartNames[i] = bangumiModel.bangumi_sections.get(i).bangumi_episode_title_long.equals("") ? bangumiModel.bangumi_sections.get(i).bangumi_episode_title : bangumiModel.bangumi_sections.get(i).bangumi_episode_title_long;
        for(int i = 0; i < bangumiModel.bangumi_episodes.size(); i++)
            videoPartCids[i] = bangumiModel.bangumi_episodes.get(i).bangumi_episode_aid + "，" + bangumiModel.bangumi_episodes.get(i).bangumi_episode_cid;
        for(int i = bangumiModel.bangumi_episodes.size(); i < videoPartNames.length; i++)
            videoPartCids[i] = bangumiModel.bangumi_sections.get(i).bangumi_episode_aid + "，" + bangumiModel.bangumi_sections.get(i).bangumi_episode_cid;
        Intent intent = new Intent(ctx, SelectPartActivity.class);
        intent.putExtra("title", "分集下载");
        intent.putExtra("tip", "选择要下载的分集");
        intent.putExtra("options_name", videoPartNames);
        intent.putExtra("options_id", videoPartCids);
        startActivityForResult(intent, RESULT_DETAIL_DOWNLOAD);
    }

    public void clickBangumiShare(View view)
    {
        Intent intent = new Intent(ctx, SendDynamicActivity.class);
        intent.putExtra("share_title", bangumiModel.bangumi_title);
        intent.putExtra("share_img", bangumiModel.bangumi_cover_small);
        startActivityForResult(intent, RESULT_DETAIL_SHARE);
    }

    public void clickBangumiMorePart(View view)
    {
        String[] videoPartNames = new String[bangumiModel.bangumi_episodes.size()];
        for(int i = 0; i < bangumiModel.bangumi_episodes.size(); i++)
            videoPartNames[i] = "第" + bangumiModel.bangumi_episodes.get(i).bangumi_episode_title + "话 " + bangumiModel.bangumi_episodes.get(i).bangumi_episode_title_long;
        Intent intent = new Intent(ctx, SelectPartActivity.class);
        intent.putExtra("title", "选集");
        intent.putExtra("options_name", videoPartNames);
        startActivityForResult(intent, RESULT_DETAIL_EPISODE);
    }

    public void clickBangumiMoreOther(View view)
    {
        String[] videoPartNames = new String[bangumiModel.bangumi_sections.size()];
        for(int i = 0; i < bangumiModel.bangumi_sections.size(); i++)
            videoPartNames[i] = bangumiModel.bangumi_sections.get(i).bangumi_episode_title_long.equals("") ? bangumiModel.bangumi_sections.get(i).bangumi_episode_title : bangumiModel.bangumi_sections.get(i).bangumi_episode_title_long;
        Intent intent = new Intent(ctx, SelectPartActivity.class);
        intent.putExtra("title", "选集");
        intent.putExtra("options_name", videoPartNames);
        startActivityForResult(intent, RESULT_DETAIL_OTHER);
    }

    TextView getVideoSeasonButton(final BangumiModel.BangumiSeasonModel season)
    {
        TextView textView = new TextView(ctx);
        textView.setWidth(120);
        textView.setBackgroundResource(seasonId.equals(season.bangumi_season_id) ? R.drawable.selector_bg_bangumi_episode_now : R.drawable.selector_bg_bangumi_episode);
        textView.setPadding(12, 6, 12, 6);
        textView.setText(season.bangumi_season_title);
        textView.setLines(1);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setTextSize(14);
        textView.setTextColor(getResources().getColor(seasonId.equals(season.bangumi_season_id) ? R.color.mainColor : R.color.gray_77));
        textView.setOnClickListener(seasonId.equals(season.bangumi_season_id) ? null : new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(ctx, BangumiActivity.class);
                intent.putExtra("season_id", season.bangumi_season_id);
                startActivity(intent);
            }
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 4, 0);
        textView.setLayoutParams(lp);
        return textView;
    }

    String getBangumiInfo()
    {
        ArrayList<String> detail_info = new ArrayList<>();
        detail_info.add("<b><big><font color=\"#000000\">" + bangumiModel.bangumi_title + "</font></big></b>");
        detail_info.add("");
        detail_info.add(bangumiModel.bangumi_detail_typename + " | " + bangumiModel.bangumi_detail_areas.toString());
        detail_info.add(bangumiModel.bangumi_detail_publish_date);
        detail_info.add(bangumiModel.bangumi_detail_publish_ep);
        detail_info.add("风格：" + bangumiModel.bangumi_detail_styles.toString());
        detail_info.add("");
        detail_info.add("<big><font color=\"#000000\">简介</font></big>");
        detail_info.add(bangumiModel.bangumi_detail_evaluate);
        detail_info.add("");
        detail_info.add("<big><font color=\"#000000\">" + bangumiModel.bangumi_detail_actor_title + "</font></big>");
        detail_info.add(bangumiModel.bangumi_detail_actor_info.replaceAll("\n", "<br/>"));
        detail_info.add("");
        detail_info.add("<big><font color=\"#000000\">" + bangumiModel.bangumi_detail_staff_title + "</font></big>");
        detail_info.add(bangumiModel.bangumi_detail_staff_info.replaceAll("\n", "<br/>"));
        return join(detail_info, "<br/>");
    }

    class BangumiDownloadServiceConnection implements ServiceConnection
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

        void downloadVideo(String title, String aid, String cid)
        {
            String result = myBinder.startDownload(aid, cid, title,
                                                   bangumiModel.bangumi_cover_small,
                                                   onlineVideoApi.getVideoUrl(),
                                                   onlineVideoApi.getDanmakuUrl());
            Looper.prepare();
            if(result.equals("")) Toast.makeText(ctx, "已添加至下载列表", Toast.LENGTH_SHORT).show();
            else Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
            Looper.loop();
        }
    }

    void titleAnim(final String title)
    {
        ObjectAnimator alpha = ObjectAnimator.ofFloat(uiTitle, "alpha", 1f, 0f);
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
                uiTitle.setText(title);
                uiTitle.setAlpha(1);
            }
        });
    }

    String join(ArrayList arrayList, String split)
    {
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < arrayList.size(); i++)
            stringBuilder.append(arrayList.get(i)).append(i == 0 ? "" : split);
        return stringBuilder.toString();
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
