package cn.luern0313.wristbilibili.fragment;

import android.annotation.SuppressLint;
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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.AnimationTimelineAdapter;
import cn.luern0313.wristbilibili.api.AnimationTimelineApi;
import cn.luern0313.wristbilibili.models.AnimationTimelineModel;
import cn.luern0313.wristbilibili.ui.BangumiActivity;
import cn.luern0313.wristbilibili.util.ColorUtil;
import cn.luern0313.wristbilibili.util.ListViewTouchListener;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;
import cn.luern0313.wristbilibili.widget.TitleView;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

/**
 * 被 liupe 创建于 2018/11/10.
 */

public class AnimationTimelineFragment extends Fragment
{
    Context ctx;
    View rootLayout;
    private ListView uiListView;
    private WaveSwipeRefreshLayout waveSwipeRefreshLayout;

    public static boolean isLogin;
    private AnimationTimelineApi animationTimelineApi;
    private ArrayList<AnimationTimelineModel> animationTimelineList;
    AnimationTimelineAdapter adapter;
    private AnimationTimelineAdapter.AnimationTimelineListener adapterListener;
    private TitleView.TitleViewListener titleViewListener;

    Handler handler = new Handler();
    private Runnable runnableUi, runnableNoWeb;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_aniremind, container, false);
        uiListView = rootLayout.findViewById(R.id.ar_listview);
        waveSwipeRefreshLayout = rootLayout.findViewById(R.id.ar_swipe);
        waveSwipeRefreshLayout.setColorSchemeColors(Color.WHITE, Color.WHITE);
        waveSwipeRefreshLayout.setWaveColor(ColorUtil.getColor(R.attr.colorPrimary, ctx));
        waveSwipeRefreshLayout.setOnRefreshListener(() -> handler.post(() -> {
            if(isLogin)
            {
                uiListView.setVisibility(View.GONE);
                getAnimTimeline();
            }
            else waveSwipeRefreshLayout.setRefreshing(false);
        }));

        adapterListener = this::onViewClick;

        runnableUi = () -> {
            rootLayout.findViewById(R.id.ar_nologin).setVisibility(View.GONE);
            rootLayout.findViewById(R.id.ar_noweb).setVisibility(View.GONE);
            adapter = new AnimationTimelineAdapter(inflater, animationTimelineList, uiListView, adapterListener);
            uiListView.setAdapter(adapter);
            uiListView.setVisibility(View.VISIBLE);
            waveSwipeRefreshLayout.setRefreshing(false);
        };

        runnableNoWeb = () -> {
            waveSwipeRefreshLayout.setRefreshing(false);
            rootLayout.findViewById(R.id.ar_noweb).setVisibility(View.VISIBLE);
            rootLayout.findViewById(R.id.ar_nologin).setVisibility(View.GONE);
        };

        uiListView.setOnTouchListener(new ListViewTouchListener(uiListView, titleViewListener));

        isLogin = SharedPreferencesUtil.contains(SharedPreferencesUtil.cookies);
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

    private void getAnimTimeline()
    {
        new Thread(() -> {
            try
            {
                animationTimelineApi = new AnimationTimelineApi();
                animationTimelineList = animationTimelineApi.getAnimTimelineList();
                handler.post(runnableUi);
            }
            catch (IOException e)
            {
                handler.post(runnableNoWeb);
                e.printStackTrace();
            }
        }).start();
    }

    private void onViewClick(int id, int position)
    {
        AnimationTimelineModel.AnimationTimelineSeasonModel animationTimelineSeasonModel = null;
        for (int i = 0; i < animationTimelineList.size(); i++)
        {
            if(position > animationTimelineList.get(i).getSeasonModelArrayList().size())
                position -= animationTimelineList.get(i).getSeasonModelArrayList().size();
            else
                animationTimelineSeasonModel = animationTimelineList.get(i).getSeasonModelArrayList().get(position);
        }
        Intent intent = new Intent(ctx, BangumiActivity.class);
        intent.putExtra("season_id", animationTimelineSeasonModel.getSeasonId());
        startActivity(intent);
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        if(context instanceof TitleView.TitleViewListener)
            titleViewListener = (TitleView.TitleViewListener) context;
    }
}
