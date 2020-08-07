package cn.luern0313.wristbilibili.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;

import androidx.fragment.app.Fragment;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.WatchlaterAdapter;
import cn.luern0313.wristbilibili.api.WatchLaterApi;
import cn.luern0313.wristbilibili.models.WatchLaterModel;
import cn.luern0313.wristbilibili.ui.VideoActivity;
import cn.luern0313.wristbilibili.util.ColorUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

/**
 * 被 luern0313 创建于 2019/8/31.
 * 稍后再看
 */

public class WatchlaterFragment extends Fragment
{
    Context ctx;
    View rootLayout;
    private ListView wlListView;
    private WaveSwipeRefreshLayout waveSwipeRefreshLayout;
    private WatchLaterApi watchLaterApi;

    public static boolean isLogin;

    Handler handler = new Handler();
    private Runnable runnableUi, runnableNoWeb, runnableNoData;

    private ArrayList<WatchLaterModel> watchLaterVideoArrayList;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();

        rootLayout = inflater.inflate(R.layout.fragment_watchlater, container, false);
        wlListView = rootLayout.findViewById(R.id.wl_listview);
        waveSwipeRefreshLayout = rootLayout.findViewById(R.id.wl_swipe);
        waveSwipeRefreshLayout.setColorSchemeColors(Color.WHITE, Color.WHITE);
        waveSwipeRefreshLayout.setWaveColor(ColorUtil.getColor(R.attr.colorPrimary, getContext()));
        waveSwipeRefreshLayout.setOnRefreshListener(new WaveSwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                handler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if(isLogin)
                        {
                            wlListView.setVisibility(View.GONE);
                            getWatchLater();
                        }
                        else waveSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });

        runnableUi = new Runnable()
        {
            @Override
            public void run()
            {
                rootLayout.findViewById(R.id.wl_nologin).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.wl_noweb).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.wl_nonthing).setVisibility(View.GONE);
                wlListView.setAdapter(new WatchlaterAdapter(inflater, watchLaterVideoArrayList, wlListView));
                wlListView.setVisibility(View.VISIBLE);
                waveSwipeRefreshLayout.setRefreshing(false);
            }
        };

        runnableNoWeb = new Runnable()
        {
            @Override
            public void run()
            {
                waveSwipeRefreshLayout.setRefreshing(false);
                rootLayout.findViewById(R.id.wl_noweb).setVisibility(View.VISIBLE);
                rootLayout.findViewById(R.id.wl_nologin).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.wl_nonthing).setVisibility(View.GONE);
            }
        };

        runnableNoData = new Runnable()
        {
            @Override
            public void run()
            {
                waveSwipeRefreshLayout.setRefreshing(false);
                rootLayout.findViewById(R.id.wl_nonthing).setVisibility(View.VISIBLE);
                rootLayout.findViewById(R.id.wl_noweb).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.wl_nologin).setVisibility(View.GONE);
            }
        };

        wlListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                startActivity(VideoActivity
                                      .getActivityIntent(ctx, watchLaterVideoArrayList.get(position).aid, ""));
            }
        });

        isLogin = SharedPreferencesUtil.contains(SharedPreferencesUtil.cookies);
        if(isLogin)
        {
            waveSwipeRefreshLayout.setRefreshing(true);
            getWatchLater();
        }
        else
        {
            rootLayout.findViewById(R.id.wl_noweb).setVisibility(View.GONE);
            rootLayout.findViewById(R.id.wl_nologin).setVisibility(View.VISIBLE);
        }

        return rootLayout;
    }

    private void getWatchLater()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    watchLaterApi = new WatchLaterApi();
                    watchLaterVideoArrayList = watchLaterApi.getWatchLater();
                    if(watchLaterVideoArrayList != null && watchLaterVideoArrayList.size() != 0)
                    {
                        handler.post(runnableUi);
                    }
                    else
                    {
                        handler.post(runnableNoData);
                    }
                }
                catch (NullPointerException e)
                {
                    handler.post(runnableNoData);
                    e.printStackTrace();
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
