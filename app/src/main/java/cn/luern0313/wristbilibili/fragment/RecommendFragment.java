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
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.RecommendAdapter;
import cn.luern0313.wristbilibili.api.RecommendApi;
import cn.luern0313.wristbilibili.models.RecommendModel;
import cn.luern0313.wristbilibili.ui.VideoActivity;
import cn.luern0313.wristbilibili.util.ColorUtil;
import cn.luern0313.wristbilibili.util.ViewScrollListener;
import cn.luern0313.wristbilibili.util.ViewTouchListener;
import cn.luern0313.wristbilibili.widget.TitleView;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

/**
 * 被 luern0313 创建于 2020/1/15.
 */

public class RecommendFragment extends Fragment
{
    Context ctx;
    View rootLayout;
    private RecommendAdapter recommendAdapter;
    private RecommendAdapter.RecommendAdapterListener adapterListener;
    private ListView uiListView;
    private WaveSwipeRefreshLayout uiWaveSwipeRefreshLayout;
    private View uiLoadingView;
    private TitleView.TitleViewListener titleViewListener;

    private RecommendApi recommendApi;
    private final ArrayList<RecommendModel> recommendVideoArrayList = new ArrayList<>();
    private boolean isLoading = false;

    Handler handler = new Handler();
    private Runnable runnableUi, runnableNoWeb, runnableNoMore, RunnableNoWebH;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_recommend, container, false);
        recommendApi = new RecommendApi();

        uiLoadingView = inflater.inflate(R.layout.widget_loading, null, false);
        uiListView = rootLayout.findViewById(R.id.rc_listview);
        uiListView.addFooterView(uiLoadingView);
        uiWaveSwipeRefreshLayout = rootLayout.findViewById(R.id.rc_swipe);
        uiWaveSwipeRefreshLayout.setColorSchemeColors(Color.WHITE, Color.WHITE);
        uiWaveSwipeRefreshLayout.setWaveColor(ColorUtil.getColor(R.attr.colorPrimary, ctx));
        uiWaveSwipeRefreshLayout.setTopOffsetOfWave(getResources().getDimensionPixelSize(R.dimen.titleHeight));
        uiWaveSwipeRefreshLayout.setOnRefreshListener(() -> handler.post(() -> {
            isLoading = true;
            getRecommend(1);
        }));

        adapterListener = this::onViewClick;

        recommendAdapter = new RecommendAdapter(inflater, recommendVideoArrayList, uiListView, adapterListener);
        uiListView.setAdapter(recommendAdapter);

        runnableUi = () -> {
            rootLayout.findViewById(R.id.rc_noweb).setVisibility(View.GONE);
            uiListView.setVisibility(View.VISIBLE);
            uiWaveSwipeRefreshLayout.setRefreshing(false);
            isLoading = false;
            recommendAdapter.notifyDataSetChanged();
        };

        runnableNoWeb = () -> {
            uiWaveSwipeRefreshLayout.setRefreshing(false);
            rootLayout.findViewById(R.id.rc_noweb).setVisibility(View.VISIBLE);
            isLoading = false;
        };

        RunnableNoWebH = () -> {
            ((TextView) uiLoadingView.findViewById(R.id.wid_load_button)).setText(ctx.getString(R.string.main_tip_no_more_web));
            uiLoadingView.findViewById(R.id.wid_load_button).setVisibility(View.VISIBLE);
        };

        runnableNoMore = () -> ((TextView) uiLoadingView.findViewById(R.id.wid_load_text)).setText(ctx.getString(R.string.main_tip_no_more_data));

        uiListView.setOnScrollListener(new ViewScrollListener(this));
        uiListView.setOnTouchListener(new ViewTouchListener(uiListView, titleViewListener));

        uiListView.setVisibility(View.GONE);
        uiWaveSwipeRefreshLayout.setRefreshing(true);
        getRecommend(2);

        return rootLayout;
    }

    private void getRecommend(final int mode) //1上 2下
    {
        new Thread(() -> {
            try
            {
                ArrayList<RecommendModel> rankingModelArrayList = recommendApi.getRecommendVideo(mode == 1);
                if(rankingModelArrayList != null && rankingModelArrayList.size() != 0)
                {
                    if(mode == 1)
                    {
                        recommendVideoArrayList.add(0, new RecommendModel(1));
                        recommendVideoArrayList.addAll(0, rankingModelArrayList);
                    }
                    else
                        recommendVideoArrayList.addAll(rankingModelArrayList);
                    handler.post(runnableUi);
                }
                else
                {
                    handler.post(runnableNoMore);
                }
            }
            catch (IOException e)
            {
                handler.post(runnableNoWeb);
                e.printStackTrace();
            }
        }).start();
    }

    private void onViewClick(int viewId, int position)
    {
        if(viewId == R.id.rc_video)
        {
            if(!recommendVideoArrayList.get(position).getAid().equals(""))
            {
                Intent intent = VideoActivity.getActivityIntent(ctx, recommendVideoArrayList.get(position).getAid(), "");
                startActivity(intent);
            }
        }
        else if(viewId == R.id.widget_recommend_update_lay)
        {
            uiListView.smoothScrollToPositionFromTop(0, 0);
            uiWaveSwipeRefreshLayout.setRefreshing(true);
            isLoading = true;
            recommendVideoArrayList.remove(position);
            getRecommend(1);
        }
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        if(context instanceof TitleView.TitleViewListener)
            titleViewListener = (TitleView.TitleViewListener) context;
    }
}
