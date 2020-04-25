package cn.luern0313.wristbilibili.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;

import androidx.fragment.app.Fragment;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.AnimationTimelineAdapter;
import cn.luern0313.wristbilibili.api.AnimationTimelineApi;
import cn.luern0313.wristbilibili.models.AnimationTimelineModel;
import cn.luern0313.wristbilibili.ui.BangumiActivity;
import cn.luern0313.wristbilibili.ui.MainActivity;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

/**
 * 被 liupe 创建于 2018/11/10.
 */

public class AnimationTimelineFragment extends Fragment
{
    Context ctx;
    View rootLayout;
    ListView arListView;
    WaveSwipeRefreshLayout waveSwipeRefreshLayout;

    public static boolean isLogin;
    AnimationTimelineApi animationTimelineApi;
    ArrayList<AnimationTimelineModel> animationTimelineList;
    AnimationTimelineAdapter adapter;
    AnimationTimelineAdapter.AnimationTimelineListener adapterListener;

    Handler handler = new Handler();
    Runnable runnableUi;
    Runnable runnableNoWeb;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_aniremind, container, false);
        arListView = rootLayout.findViewById(R.id.ar_listview);
        waveSwipeRefreshLayout = rootLayout.findViewById(R.id.ar_swipe);
        waveSwipeRefreshLayout.setColorSchemeColors(Color.WHITE, Color.WHITE);
        waveSwipeRefreshLayout.setWaveColor(Color.argb(255, 250, 114, 152));
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
                            arListView.setVisibility(View.GONE);
                            getAnimTimeline();
                        }
                        else waveSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });

        adapterListener = new AnimationTimelineAdapter.AnimationTimelineListener()
        {
            @Override
            public void onClick(int viewId, int position)
            {
                onViewClick(viewId, position);
            }
        };

        runnableUi = new Runnable()
        {
            @Override
            public void run()
            {
                rootLayout.findViewById(R.id.ar_nologin).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.ar_noweb).setVisibility(View.GONE);
                adapter = new AnimationTimelineAdapter(inflater, animationTimelineList, arListView, adapterListener);
                arListView.setAdapter(adapter);
                arListView.setVisibility(View.VISIBLE);
                waveSwipeRefreshLayout.setRefreshing(false);
            }
        };

        runnableNoWeb = new Runnable()
        {
            @Override
            public void run()
            {
                waveSwipeRefreshLayout.setRefreshing(false);
                rootLayout.findViewById(R.id.ar_noweb).setVisibility(View.VISIBLE);
                rootLayout.findViewById(R.id.ar_nologin).setVisibility(View.GONE);
            }
        };

        isLogin = MainActivity.sharedPreferences.contains("cookies");
        if(isLogin)
        {
            waveSwipeRefreshLayout.setRefreshing(true);
            getAnimTimeline();
        }
        else
        {
            rootLayout.findViewById(R.id.ar_noweb).setVisibility(View.GONE);
            rootLayout.findViewById(R.id.ar_nologin).setVisibility(View.VISIBLE);
        }

        return rootLayout;
    }

    void getAnimTimeline()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    animationTimelineApi = new AnimationTimelineApi(MainActivity.sharedPreferences.getString("cookies", ""));
                    animationTimelineList = animationTimelineApi.getAnimTimelineList();
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

    void onViewClick(int id, int position)
    {
        Intent intent = new Intent(ctx, BangumiActivity.class);
        intent.putExtra("season_id", animationTimelineList.get(position).season_id);
        startActivity(intent);
    }
}
