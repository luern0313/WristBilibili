package cn.luern0313.wristbilibili.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.RankingAdapter;
import cn.luern0313.wristbilibili.api.RankingApi;
import cn.luern0313.wristbilibili.api.VideoApi;
import cn.luern0313.wristbilibili.models.RankingModel;
import cn.luern0313.wristbilibili.models.VideoModel;
import cn.luern0313.wristbilibili.ui.TextActivity;
import cn.luern0313.wristbilibili.ui.UserActivity;
import cn.luern0313.wristbilibili.ui.VideoActivity;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.ImageDownloaderUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

/**
 * 被 luern0313 创建于 2020/1/11.
 */

public class RankingFragment extends Fragment
{
    Context ctx;
    View rootLayout;
    private RankingAdapter rankingAdapter;
    private RankingAdapter.RankingAdapterListener adapterListener;
    private VideoApi videoDetailsApi;
    private VideoModel videoModel;

    private ListView uiListView;
    private WaveSwipeRefreshLayout uiWaveSwipeRefreshLayout;
    private View uiPickUpView;
    private View uiLoadingView;

    private RankingApi rankingApi;
    private ArrayList<RankingModel> rankingVideoArrayList = new ArrayList<>();
    private LinkedHashMap<Integer, String> pickUpHashMap = new LinkedHashMap<>();
    private String pickupday;
    private int pn = 1;
    private boolean isLoading = true;
    private Bitmap bitmapPickUpUpFace;
    private Bitmap bitmapPickUpVideoCover;

    Handler handler = new Handler();
    private Runnable runnableUi, runnableNoWeb, runnableNoMore, runnableNoWebH, runnablePickNodata;
    private Runnable runnablePick, runnablePickUi, runnablePickImg;

    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, final Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_ranking, container, false);
        rankingApi = new RankingApi();

        uiPickUpView = inflater.inflate(R.layout.widget_ranking_pickup, null, false);
        uiLoadingView = inflater.inflate(R.layout.widget_loading, null, false);
        uiListView = rootLayout.findViewById(R.id.rk_listview);
        uiListView.addFooterView(uiLoadingView);
        uiWaveSwipeRefreshLayout = rootLayout.findViewById(R.id.rk_swipe);
        uiWaveSwipeRefreshLayout.setColorSchemeColors(Color.WHITE, Color.WHITE);
        uiWaveSwipeRefreshLayout.setWaveColor(Color.argb(255, 250, 114, 152));
        uiWaveSwipeRefreshLayout.setOnRefreshListener(new WaveSwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                handler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        isLoading = true;
                        rankingVideoArrayList.clear();
                        pn = 1;
                        uiListView.setVisibility(View.GONE);
                        getRanking();
                    }
                });
            }
        });

        adapterListener = new RankingAdapter.RankingAdapterListener()
        {
            @Override
            public void onClick(int viewId, int position)
            {
                onViewClick(viewId, position);
            }
        };

        rankingAdapter = new RankingAdapter(inflater, rankingVideoArrayList, uiListView, adapterListener);
        uiListView.setAdapter(rankingAdapter);

        runnableUi = new Runnable()
        {
            @Override
            public void run()
            {
                rootLayout.findViewById(R.id.rk_noweb).setVisibility(View.GONE);
                uiListView.setVisibility(View.VISIBLE);
                uiWaveSwipeRefreshLayout.setRefreshing(false);
                isLoading = false;
                rankingAdapter.notifyDataSetChanged();
            }
        };

        runnableNoWeb = new Runnable()
        {
            @Override
            public void run()
            {
                uiWaveSwipeRefreshLayout.setRefreshing(false);
                rootLayout.findViewById(R.id.rk_noweb).setVisibility(View.VISIBLE);
                isLoading = false;
            }
        };

        runnableNoWebH = new Runnable()
        {
            @Override
            public void run()
            {
                ((TextView) uiLoadingView.findViewById(R.id.wid_load_button)).setText("好像没有网络...\n检查下网络？");
                uiLoadingView.findViewById(R.id.wid_load_button).setVisibility(View.VISIBLE);
            }
        };

        runnableNoMore = new Runnable()
        {
            @Override
            public void run()
            {
                ((TextView) uiLoadingView.findViewById(R.id.wid_load_text)).setText("  没有更多了...");
            }
        };

        runnablePickNodata = new Runnable()
        {
            @Override
            public void run()
            {
                uiPickUpView.findViewById(R.id.rk_pu_lay).setVisibility(View.GONE);
            }
        };

        runnablePick = new Runnable()
        {
            @Override
            public void run()
            {
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                int today_int = Integer.valueOf(format.format(new Date(System.currentTimeMillis())));
                ArrayList<Integer> dates = new ArrayList<>(pickUpHashMap.keySet());
                Collections.sort(dates);
                for(int i = dates.size() - 1; i >= 0; i--)
                {
                    if(dates.get(i) <= today_int)
                    {
                        pickupday = String.valueOf(dates.get(i));
                        videoDetailsApi = new VideoApi(pickUpHashMap.get(dates.get(i)), "");
                        new Thread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    videoModel = videoDetailsApi.getVideoDetails();
                                    if(videoModel != null)
                                        handler.post(runnablePickUi);
                                    else
                                        handler.post(runnablePickNodata);
                                }
                                catch (IOException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                        break;
                    }
                }
            }
        };

        runnablePickUi = new Runnable()
        {
            @Override
            public void run()
            {
                if(uiListView.getHeaderViewsCount() == 0) uiListView.addHeaderView(uiPickUpView);
                uiPickUpView.findViewById(R.id.rk_pu_lay).setVisibility(View.VISIBLE);
                ((TextView) uiPickUpView.findViewById(R.id.rk_pu_video_title)).setText(videoModel.video_title);
                ((TextView) uiPickUpView.findViewById(R.id.rk_pu_video_up_name)).setText(videoModel.video_up_name);
                ((TextView) uiPickUpView.findViewById(R.id.rk_pu_date_date)).setText(pickupday.substring(4, 6) + "月" + pickupday.substring(6, 8) + "日");
                ((TextView) uiPickUpView.findViewById(R.id.rk_pu_video_play)).setText(videoModel.video_play);
                ((TextView) uiPickUpView.findViewById(R.id.rk_pu_video_danmaku)).setText(videoModel.video_danmaku);

                if(SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.firstPickUp, true))
                    uiPickUpView.findViewById(R.id.rk_pu_date_click).setVisibility(View.VISIBLE);

                Drawable playNumDrawable = ctx.getResources().getDrawable(R.drawable.icon_video_play_num);
                Drawable danmakuNumDrawable = ctx.getResources().getDrawable(R.drawable.icon_video_danmu_num);
                playNumDrawable.setBounds(0, 0, DataProcessUtil.dip2px(10), DataProcessUtil.dip2px(10));
                danmakuNumDrawable.setBounds(0, 0, DataProcessUtil.dip2px(10), DataProcessUtil.dip2px(10));
                ((TextView) uiPickUpView.findViewById(R.id.rk_pu_video_play)).setCompoundDrawables(playNumDrawable,null, null,null);
                ((TextView) uiPickUpView.findViewById(R.id.rk_pu_video_danmaku)).setCompoundDrawables(danmakuNumDrawable,null, null,null);

                uiPickUpView.findViewById(R.id.rk_pu_date).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        uiPickUpView.findViewById(R.id.rk_pu_date_click).setVisibility(View.GONE);
                        SharedPreferencesUtil.putBoolean(SharedPreferencesUtil.firstPickUp, false);
                        Intent intent = new Intent(ctx, TextActivity.class);
                        intent.putExtra("title", "说明");
                        intent.putExtra("text", ("Pick Up视频说明\n" + "每天由用户推荐并投票选出一个精选视频，在排行榜的上方Pick Up栏推广展示一天\n" + "目的是让一些制作精良但播放不高的视频获得更多的曝光\n" + "（相关规则和投票系统正在制作中，目前推荐视频为手动设置，你可以在qq或b站私聊开发者推荐你喜欢的视频）\n" + "（相关要求：连续五天不能推荐同一名up主的视频，上榜视频非引战或有争议视频，播放量少的视频优先等）").replaceAll("\n", "<br/><br/>"));
                        startActivity(intent);
                    }
                });

                uiPickUpView.findViewById(R.id.rk_pu_video_up).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(ctx, UserActivity.class);
                        intent.putExtra("mid", videoModel.video_up_mid);
                        startActivity(intent);
                    }
                });

                uiPickUpView.findViewById(R.id.rk_pu_lay).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        new Thread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                rankingApi.clickPickUpVideo();
                            }
                        }).start();
                        startActivity(VideoActivity.getActivityIntent(ctx, videoModel.video_aid, ""));
                    }
                });

                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            bitmapPickUpUpFace = ImageDownloaderUtil.downloadImage(videoModel.video_up_face);
                            bitmapPickUpVideoCover = ImageDownloaderUtil.downloadImage(videoModel.video_cover);
                            handler.post(runnablePickImg);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        };

        runnablePickImg = new Runnable()
        {
            @Override
            public void run()
            {
                ((ImageView) uiPickUpView.findViewById(R.id.rk_pu_video_up_head)).setImageBitmap(bitmapPickUpUpFace);
                ((ImageView) uiPickUpView.findViewById(R.id.rk_pu_video_img)).setImageBitmap(bitmapPickUpVideoCover);
            }
        };

        uiListView.setOnScrollListener(new AbsListView.OnScrollListener()
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
                    getRanking();
                }
            }
        });

        uiListView.setVisibility(View.GONE);
        uiWaveSwipeRefreshLayout.setRefreshing(true);
        getRanking();

        return rootLayout;
    }

    private void getRanking()
    {
        uiPickUpView.findViewById(R.id.rk_pu_lay).setVisibility(View.GONE);
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    ArrayList<RankingModel> rankingModelArrayList = rankingApi.getRankingVideo(pn);
                    pickUpHashMap = rankingApi.getPickUpVideo();
                    if(rankingModelArrayList != null && rankingModelArrayList.size() != 0)
                    {
                        rankingVideoArrayList.addAll(rankingModelArrayList);
                        pn++;
                        handler.post(runnableUi);
                        if(pickUpHashMap == null || pickUpHashMap.size() == 0)
                            handler.post(runnablePickNodata);
                        else
                            handler.post(runnablePick);
                    }
                    else
                        handler.post(runnableNoMore);
                }
                catch (IOException e)
                {
                    handler.post(runnableNoWeb);
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void onViewClick(int viewId, int position)
    {
        if(viewId == R.id.rk_video_lay)
        {
            if(!rankingVideoArrayList.get(position).video_aid.equals(""))
                startActivity(VideoActivity
                                      .getActivityIntent(ctx, rankingVideoArrayList.get(position).video_aid, ""));
        }
        else if(viewId == R.id.rk_video_video_up)
        {
            Intent intent = new Intent(ctx, UserActivity.class);
            intent.putExtra("mid", rankingVideoArrayList.get(position).up_mid);
            startActivity(intent);
        }
    }
}
