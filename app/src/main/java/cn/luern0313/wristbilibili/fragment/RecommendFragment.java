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
import cn.luern0313.wristbilibili.widget.ExceptionHandlerView;
import cn.luern0313.wristbilibili.widget.TitleView;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

/**
 * 被 luern0313 创建于 2020/1/15.
 */

public class RecommendFragment extends Fragment implements ViewScrollListener.CustomScrollResult
{
    private Context ctx;
    private View rootLayout;
    private RecommendAdapter recommendAdapter;
    private RecommendAdapter.RecommendAdapterListener adapterListener;

    private ExceptionHandlerView exceptionHandlerView;
    private ListView listView;
    private WaveSwipeRefreshLayout waveSwipeRefreshLayout;
    private View loadingView;
    private TitleView.TitleViewListener titleViewListener;

    private RecommendApi recommendApi;
    private final ArrayList<RecommendModel> recommendVideoArrayList = new ArrayList<>();
    private boolean isLoading = true;

    private final Handler handler = new Handler();
    private Runnable runnableUi, runnableNoMore, runnableMoreNoWeb;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_recommend, container, false);
        recommendApi = new RecommendApi();

        loadingView = inflater.inflate(R.layout.widget_loading, null, false);
        exceptionHandlerView = rootLayout.findViewById(R.id.rc_exception);
        listView = rootLayout.findViewById(R.id.rc_listview);
        listView.addFooterView(loadingView);
        waveSwipeRefreshLayout = rootLayout.findViewById(R.id.rc_swipe);
        waveSwipeRefreshLayout.setColorSchemeColors(Color.WHITE, Color.WHITE);
        waveSwipeRefreshLayout.setWaveColor(ColorUtil.getColor(R.attr.colorPrimary, ctx));
        waveSwipeRefreshLayout.setTopOffsetOfWave(getResources().getDimensionPixelSize(R.dimen.titleHeight));
        waveSwipeRefreshLayout.setOnRefreshListener(() -> handler.post(() -> {
            getRecommend(1);
        }));

        adapterListener = this::onViewClick;

        recommendAdapter = new RecommendAdapter(inflater, recommendVideoArrayList, listView, adapterListener);
        listView.setAdapter(recommendAdapter);

        runnableUi = () -> {

            listView.setVisibility(View.VISIBLE);
            waveSwipeRefreshLayout.setRefreshing(false);
            isLoading = false;
            recommendAdapter.notifyDataSetChanged();
        };

        runnableMoreNoWeb = () -> {
            ((TextView) loadingView.findViewById(R.id.wid_load_button)).setText(ctx.getString(R.string.main_tip_no_more_web));
            loadingView.findViewById(R.id.wid_load_button).setVisibility(View.VISIBLE);
        };

        runnableNoMore = () -> ((TextView) loadingView.findViewById(R.id.wid_load_text)).setText(ctx.getString(R.string.main_tip_no_more_data));

        loadingView.findViewById(R.id.wid_load_button).setOnClickListener(v -> {
            ((TextView) loadingView.findViewById(R.id.wid_load_button)).setText(getString(R.string.main_tip_no_more_data_loading));
            loadingView.findViewById(R.id.wid_load_button).setVisibility(View.GONE);
            getRecommend(1);
        });

        listView.setOnScrollListener(new ViewScrollListener(this));
        listView.setOnTouchListener(new ViewTouchListener(listView, titleViewListener));

        listView.setVisibility(View.GONE);
        waveSwipeRefreshLayout.setRefreshing(true);
        getRecommend(2);

        return rootLayout;
    }

    private void getRecommend(final int mode) //1上 2下
    {
        isLoading = true;
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
                    handler.post(runnableNoMore);
            }
            catch (IOException e)
            {
                if(recommendVideoArrayList.size() == 0)
                    exceptionHandlerView.noWeb();
                else
                    handler.post(runnableMoreNoWeb);
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
                Intent intent = new Intent(ctx, VideoActivity.class);
                intent.putExtra(VideoActivity.ARG_AID, recommendVideoArrayList.get(position).getAid());
                startActivity(intent);
            }
        }
        else if(viewId == R.id.widget_recommend_update_lay)
        {
            listView.smoothScrollToPositionFromTop(0, 0);
            waveSwipeRefreshLayout.setRefreshing(true);
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

    @Override
    public boolean rule()
    {
        return !isLoading;
    }

    @Override
    public void result()
    {
        getRecommend(2);
    }
}
