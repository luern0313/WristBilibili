package cn.luern0313.wristbilibili.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
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
import java.util.Date;
import java.util.Locale;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.RankingAdapter;
import cn.luern0313.wristbilibili.api.RankingApi;
import cn.luern0313.wristbilibili.api.VideoDetailsApi;
import cn.luern0313.wristbilibili.models.RankingModel;
import cn.luern0313.wristbilibili.models.VideoModel;
import cn.luern0313.wristbilibili.ui.MainActivity;
import cn.luern0313.wristbilibili.ui.OtherUserActivity;
import cn.luern0313.wristbilibili.ui.TextActivity;
import cn.luern0313.wristbilibili.ui.VideodetailsActivity;
import cn.luern0313.wristbilibili.util.ImageDownloaderUtil;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

/**
 * 被 luern0313 创建于 2020/1/11.
 */

public class RankingFragment extends Fragment
{
    Context ctx;
    View rootLayout;
    RankingAdapter rankingAdapter;
    RankingAdapter.RankingAdapterListener adapterListener;
    VideoDetailsApi videoDetailsApi;
    VideoModel videoModel;

    ListView uiListView;
    WaveSwipeRefreshLayout uiWaveSwipeRefreshLayout;
    View uiPickUpView;
    View uiLoadingView;

    RankingApi rankingApi;
    ArrayList<RankingModel> rankingVideoArrayList = new ArrayList<>();
    ArrayList<ArrayList<String>> pickUpHashMap = new ArrayList<>();
    String today;
    int pn = 1;
    boolean isLoading = true;
    Bitmap bitmapPickUpUpFace;
    Bitmap bitmapPickUpVideoCover;

    Handler handler = new Handler();
    Runnable runnableUi;
    Runnable runnableNoWeb;
    Runnable runnableNoMore;
    Runnable runnableNoWebH;
    Runnable runnablePickNodata;
    Runnable runnablePick;
    Runnable runnablePickUi;
    Runnable runnablePickImg;

    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, final Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_ranking, container, false);
        rankingApi = new RankingApi(MainActivity.sharedPreferences.getString("mid", ""),
                                    MainActivity.sharedPreferences.getString("cookies", ""),
                                    MainActivity.sharedPreferences.getString("csrf", ""));

        uiPickUpView = inflater.inflate(R.layout.widget_ranking_pickup, null, false);
        uiLoadingView = inflater.inflate(R.layout.widget_loading, null, false);
        uiListView = rootLayout.findViewById(R.id.rk_listview);
        uiListView.addHeaderView(uiPickUpView);
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
                uiPickUpView.setVisibility(View.GONE);
            }
        };

        runnablePick = new Runnable()
        {
            @Override
            public void run()
            {
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                today = format.format(new Date(System.currentTimeMillis()));
                for(int i = 0; i < pickUpHashMap.size(); i++)
                {
                    if(pickUpHashMap.get(i).get(0).equals(today))
                    {
                        videoDetailsApi = new VideoDetailsApi(MainActivity.sharedPreferences.getString("cookies", ""),
                                                              MainActivity.sharedPreferences.getString("csrf", ""),
                                                              MainActivity.sharedPreferences.getString("mid", ""),
                                                              MainActivity.sharedPreferences.getString("access_key", ""),
                                                              pickUpHashMap.get(i).get(1));
                        new Thread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    videoModel = videoDetailsApi.getVideoDetails();
                                    handler.post(runnablePickUi);
                                }
                                catch (IOException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                }
            }
        };

        runnablePickUi = new Runnable()
        {
            @Override
            public void run()
            {
                uiPickUpView.findViewById(R.id.rk_pu_lay).setVisibility(View.VISIBLE);
                ((TextView) uiPickUpView.findViewById(R.id.rk_pu_video_title)).setText(videoModel.video_title);
                ((TextView) uiPickUpView.findViewById(R.id.rk_pu_video_up_name)).setText(videoModel.video_up_name);
                ((TextView) uiPickUpView.findViewById(R.id.rk_pu_date_date)).setText(today.substring(4, 6) + "月" + today.substring(6, 8) + "日");
                ((TextView) uiPickUpView.findViewById(R.id.rk_pu_video_play)).setText(videoModel.video_play);
                ((TextView) uiPickUpView.findViewById(R.id.rk_pu_video_danmaku)).setText(videoModel.video_danmaku);

                if(MainActivity.sharedPreferences.getBoolean("firstPickUp", true))
                    uiPickUpView.findViewById(R.id.rk_pu_date_click).setVisibility(View.VISIBLE);

                Drawable playNumDrawable = ctx.getResources().getDrawable(R.drawable.icon_video_play_num);
                Drawable danmakuNumDrawable = ctx.getResources().getDrawable(R.drawable.icon_video_danmu_num);
                playNumDrawable.setBounds(0,0,24,24);
                danmakuNumDrawable.setBounds(0,0,24,24);
                ((TextView) uiPickUpView.findViewById(R.id.rk_pu_video_play)).setCompoundDrawables(playNumDrawable,null, null,null);
                ((TextView) uiPickUpView.findViewById(R.id.rk_pu_video_danmaku)).setCompoundDrawables(danmakuNumDrawable,null, null,null);

                uiPickUpView.findViewById(R.id.rk_pu_date).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        uiPickUpView.findViewById(R.id.rk_pu_date_click).setVisibility(View.GONE);
                        MainActivity.editor.putBoolean("firstPickUp", false);
                        MainActivity.editor.apply();
                        Intent intent = new Intent(ctx, TextActivity.class);
                        intent.putExtra("title", "说明");
                        intent.putExtra("text", "");
                        startActivity(intent);
                    }
                });

                uiPickUpView.findViewById(R.id.rk_pu_video_up).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(ctx, OtherUserActivity.class);
                        intent.putExtra("mid", videoModel.video_up_mid);
                        startActivity(intent);
                    }
                });

                uiPickUpView.findViewById(R.id.rk_pu_lay).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(ctx, VideodetailsActivity.class);
                        intent.putExtra("aid", videoModel.video_aid);
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

        uiWaveSwipeRefreshLayout.setRefreshing(true);
        getRanking();

        return rootLayout;
    }

    void getRanking()
    {
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

    void onViewClick(int viewId, int position)
    {
        if(viewId == R.id.rk_video_lay)
        {
            if(!rankingVideoArrayList.get(position).video_aid.equals(""))
            {
                Intent intent = new Intent(ctx, VideodetailsActivity.class);
                intent.putExtra("aid", rankingVideoArrayList.get(position).video_aid);
                startActivity(intent);
            }
        }
        else if(viewId == R.id.rk_video_video_up)
        {
            Intent intent = new Intent(ctx, OtherUserActivity.class);
            intent.putExtra("mid", rankingVideoArrayList.get(position).up_mid);
            startActivity(intent);
        }
    }
}
