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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.PopularAdapter;
import cn.luern0313.wristbilibili.api.PopularApi;
import cn.luern0313.wristbilibili.models.popular.PopularModel;
import cn.luern0313.wristbilibili.ui.PopularHistoryActivity;
import cn.luern0313.wristbilibili.ui.PopularWeeklyActivity;
import cn.luern0313.wristbilibili.ui.RankingActivity;
import cn.luern0313.wristbilibili.ui.VideoActivity;
import cn.luern0313.wristbilibili.util.ColorUtil;
import cn.luern0313.wristbilibili.util.ViewScrollListener;
import cn.luern0313.wristbilibili.util.ViewTouchListener;
import cn.luern0313.wristbilibili.widget.ExceptionHandlerView;
import cn.luern0313.wristbilibili.widget.TitleView;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

/**
 * 被 luern0313 创建于 2020/1/11.
 */

public class PopularFragment extends Fragment implements ViewScrollListener.CustomScrollResult, View.OnClickListener
{
    private Context ctx;
    private View rootLayout;
    private PopularAdapter popularAdapter;
    private PopularApi popularApi;
    private TitleView.TitleViewListener titleViewListener;

    private ExceptionHandlerView exceptionHandlerView;
    private ListView listView;
    private WaveSwipeRefreshLayout waveSwipeRefreshLayout;
    private View uiHeaderView, uiLoadingView;

    private boolean isLoading = true;
    private int pn;

    private final Handler handler = new Handler();
    private Runnable runnableUi, runnableMoreNothing, runnableMoreNoWeb, runnableMore;

    public PopularFragment() { }

    public static PopularFragment newInstance()
    {
        return new PopularFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
        {
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, final Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_popular, container, false);
        popularApi = new PopularApi();

        uiHeaderView = inflater.inflate(R.layout.widget_popular_header, null, false);
        uiLoadingView = inflater.inflate(R.layout.widget_loading, null, false);
        exceptionHandlerView = rootLayout.findViewById(R.id.popular_exception);
        listView = rootLayout.findViewById(R.id.popular_listview);
        listView.addFooterView(uiLoadingView);
        listView.addHeaderView(uiHeaderView);
        waveSwipeRefreshLayout = rootLayout.findViewById(R.id.popular_swipe);
        waveSwipeRefreshLayout.setColorSchemeColors(Color.WHITE, Color.WHITE);
        waveSwipeRefreshLayout.setWaveColor(ColorUtil.getColor(R.attr.colorPrimary, ctx));
        waveSwipeRefreshLayout.setTopOffsetOfWave(getResources().getDimensionPixelSize(R.dimen.titleHeight));
        waveSwipeRefreshLayout.setOnRefreshListener(() -> handler.post(() -> {
            listView.setVisibility(View.GONE);
            getPopularVideo();
        }));

        uiHeaderView.findViewById(R.id.popular_header_ranking).setOnClickListener(this);
        uiHeaderView.findViewById(R.id.popular_header_weekly).setOnClickListener(this);
        uiHeaderView.findViewById(R.id.popular_header_history).setOnClickListener(this);

        PopularAdapter.PopularAdapterListener adapterListener = this::onViewClick;

        runnableUi = () -> {
            isLoading = false;
            listView.setVisibility(View.VISIBLE);
            waveSwipeRefreshLayout.setRefreshing(false);

            popularAdapter = new PopularAdapter(inflater, popularApi.popularVideoModelArrayList, listView, adapterListener);
            listView.setAdapter(popularAdapter);
        };

        runnableMoreNoWeb = () -> {
            ((TextView) uiLoadingView.findViewById(R.id.wid_load_text)).setText(ctx.getString(R.string.main_tip_no_more_web));
            uiLoadingView.findViewById(R.id.wid_load_button).setVisibility(View.VISIBLE);
        };

        runnableMoreNothing = () -> ((TextView) uiLoadingView.findViewById(R.id.wid_load_text)).setText(ctx.getString(R.string.main_tip_no_more_data));

        runnableMore = () -> {
            isLoading = false;
            popularAdapter.notifyDataSetChanged();
        };

        listView.setOnTouchListener(new ViewTouchListener(listView, titleViewListener));
        listView.setOnScrollListener(new ViewScrollListener(this));

        listView.setVisibility(View.GONE);
        waveSwipeRefreshLayout.setRefreshing(true);
        getPopularVideo();

        return rootLayout;
    }

    private void getPopularVideo()
    {
        isLoading = true;
        new Thread(() -> {
            try
            {
                pn = 1;
                if(popularApi.getPopularVideo(pn))
                    handler.post(runnableUi);
                else
                    exceptionHandlerView.noData();
            }
            catch (IOException e)
            {
                exceptionHandlerView.noWeb();
                e.printStackTrace();
            }
        }).start();
    }

    private void getMorePopularVideo()
    {
        isLoading = true;
        new Thread(() -> {
            try
            {
                pn++;
                if(popularApi.getPopularVideo(pn))
                {
                    handler.post(runnableMore);
                    if(popularApi.noMore)
                        handler.post(runnableMoreNothing);
                }
                else
                    handler.post(runnableMoreNothing);
            }
            catch (IOException e)
            {
                handler.post(runnableMoreNoWeb);
                e.printStackTrace();
            }
        }).start();
    }


    private void onViewClick(int viewId, int position)
    {
        PopularModel.PopularVideoModel popularVideoModel = popularApi.popularVideoModelArrayList.get(position);
        if(viewId == R.id.popular_item_video)
        {
            Intent intent = new Intent(ctx, VideoActivity.class);
            intent.putExtra(VideoActivity.ARG_AID, popularVideoModel.getAid());
            intent.putExtra(VideoActivity.ARG_BVID, popularVideoModel.getBvid());
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View v)
    {
        if(v.getId() == R.id.popular_header_ranking)
        {
            Intent intent = new Intent(ctx, RankingActivity.class);
            startActivity(intent);
        }
        else if(v.getId() == R.id.popular_header_weekly)
        {
            Intent intent = new Intent(ctx, PopularWeeklyActivity.class);
            startActivity(intent);
        }
        else if(v.getId() == R.id.popular_header_history)
        {
            Intent intent = new Intent(ctx, PopularHistoryActivity.class);
            startActivity(intent);
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
        return !isLoading && !popularApi.noMore;
    }

    @Override
    public void result()
    {
        getMorePopularVideo();
    }

}
