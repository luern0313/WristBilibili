package cn.luern0313.wristbilibili.fragment;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.RankingAdapter;
import cn.luern0313.wristbilibili.api.RankingApi;
import cn.luern0313.wristbilibili.models.RankingModel;
import cn.luern0313.wristbilibili.ui.MainActivity;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

/**
 * 被 luern0313 创建于 2020/1/11.
 */

public class RankingFragment extends Fragment
{
    Context ctx;
    View rootLayout;
    RankingAdapter rankingAdapter;
    ListView uiListView;
    WaveSwipeRefreshLayout uiWaveSwipeRefreshLayout;
    View uiPickUpView;
    View uiLoadingView;

    RankingApi rankingApi;
    ArrayList<RankingModel> rankingVideoArrayList = new ArrayList<>();
    int pn = 0;
    boolean isLoading = false;

    Handler handler = new Handler();
    Runnable runnableUi;
    Runnable runnableNoWeb;

    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_ranking, container, false);
        rankingApi = new RankingApi(MainActivity.sharedPreferences.getString("mid", ""),
                                    MainActivity.sharedPreferences.getString("cookies", ""),
                                    MainActivity.sharedPreferences.getString("csrf", ""));

        uiPickUpView = inflater.inflate(R.layout.widget_ranking_pickup, null, false);
        uiPickUpView = inflater.inflate(R.layout.widget_loading, null, false);
        uiListView = rootLayout.findViewById(R.id.rk_listview);
        uiListView.addHeaderView(uiPickUpView);
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
                        rankingVideoArrayList = new ArrayList<>();
                        pn = 0;
                        uiListView.setVisibility(View.GONE);
                        getRanking();
                    }
                });
            }
        });
        rankingAdapter = new RankingAdapter(inflater, rankingVideoArrayList, uiListView);
        uiListView.setAdapter(rankingAdapter);

        runnableUi = new Runnable()
        {
            @Override
            public void run()
            {
                rootLayout.findViewById(R.id.rk_noweb).setVisibility(View.GONE);
                uiListView.setVisibility(View.VISIBLE);
                uiWaveSwipeRefreshLayout.setRefreshing(false);
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
                    rankingVideoArrayList.addAll(rankingApi.getRankingVideo(pn));
                    pn++;
                    handler.post(runnableUi);
                }
                catch (IOException e)
                {
                    handler.post(runnableNoWeb);
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
