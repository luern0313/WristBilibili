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
import cn.luern0313.wristbilibili.util.ViewTouchListener;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;
import cn.luern0313.wristbilibili.widget.ExceptionHandlerView;
import cn.luern0313.wristbilibili.widget.TitleView;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

/**
 * 被 liupe 创建于 2018/11/10.
 */

public class AnimationTimelineFragment extends Fragment
{
    private Context ctx;
    private View rootLayout;
    private ExceptionHandlerView exceptionHandlerView;
    private ListView listView;
    private WaveSwipeRefreshLayout waveSwipeRefreshLayout;

    public static boolean isLogin;
    private AnimationTimelineApi animationTimelineApi;
    private ArrayList<AnimationTimelineModel> animationTimelineList;
    private AnimationTimelineAdapter adapter;
    private AnimationTimelineAdapter.AnimationTimelineListener adapterListener;
    private TitleView.TitleViewListener titleViewListener;

    private final Handler handler = new Handler();
    private Runnable runnableUi;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_aniremind, container, false);
        exceptionHandlerView = rootLayout.findViewById(R.id.ar_exception);
        listView = rootLayout.findViewById(R.id.ar_listview);
        waveSwipeRefreshLayout = rootLayout.findViewById(R.id.ar_swipe);
        waveSwipeRefreshLayout.setColorSchemeColors(Color.WHITE, Color.WHITE);
        waveSwipeRefreshLayout.setWaveColor(ColorUtil.getColor(R.attr.colorPrimary, ctx));
        waveSwipeRefreshLayout.setTopOffsetOfWave(getResources().getDimensionPixelSize(R.dimen.titleHeight));
        waveSwipeRefreshLayout.setOnRefreshListener(() -> handler.post(() -> {
            if(isLogin)
            {
                listView.setVisibility(View.GONE);
                getAnimTimeline();
            }
            else waveSwipeRefreshLayout.setRefreshing(false);
        }));

        adapterListener = this::onViewClick;

        runnableUi = () -> {
            exceptionHandlerView.hideAllView();
            adapter = new AnimationTimelineAdapter(inflater, animationTimelineList, listView, adapterListener);
            listView.setAdapter(adapter);
            listView.setVisibility(View.VISIBLE);
        };

        listView.setOnTouchListener(new ViewTouchListener(listView, titleViewListener));

        isLogin = SharedPreferencesUtil.contains(SharedPreferencesUtil.cookies);
        if(isLogin)
        {
            waveSwipeRefreshLayout.setRefreshing(true);
            getAnimTimeline();
        }
        else
            exceptionHandlerView.noLogin();

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
                exceptionHandlerView.noWeb();
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
