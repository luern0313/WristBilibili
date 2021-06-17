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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.PopularHistoryAdapter;
import cn.luern0313.wristbilibili.api.PopularHistoryApi;
import cn.luern0313.wristbilibili.models.popular.PopularHistoryModel;
import cn.luern0313.wristbilibili.ui.VideoActivity;
import cn.luern0313.wristbilibili.util.ColorUtil;
import cn.luern0313.wristbilibili.util.ViewTouchListener;
import cn.luern0313.wristbilibili.widget.ExceptionHandlerView;
import cn.luern0313.wristbilibili.widget.TitleView;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

/**
 * 被 luern0313 创建于 2020/1/11.
 */

public class PopularHistoryFragment extends Fragment
{
    private Context ctx;
    private View rootLayout;
    private PopularHistoryAdapter popularHistoryAdapter;
    private PopularHistoryApi popularHistoryApi;
    private TitleView.TitleViewListener titleViewListener;

    private ListView listView;
    private WaveSwipeRefreshLayout waveSwipeRefreshLayout;
    private ExceptionHandlerView exceptionHandlerView;

    private final Handler handler = new Handler();
    private Runnable runnableUi;

    public PopularHistoryFragment() { }

    public static PopularHistoryFragment newInstance()
    {
        return new PopularHistoryFragment();
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
        rootLayout = inflater.inflate(R.layout.fragment_popular_history, container, false);
        popularHistoryApi = new PopularHistoryApi();

        exceptionHandlerView = rootLayout.findViewById(R.id.popular_history_exception);
        listView = rootLayout.findViewById(R.id.popular_history_listview);
        waveSwipeRefreshLayout = rootLayout.findViewById(R.id.popular_history_swipe);
        waveSwipeRefreshLayout.setColorSchemeColors(Color.WHITE, Color.WHITE);
        waveSwipeRefreshLayout.setWaveColor(ColorUtil.getColor(R.attr.colorPrimary, ctx));
        waveSwipeRefreshLayout.setTopOffsetOfWave(getResources().getDimensionPixelSize(R.dimen.titleHeight));
        waveSwipeRefreshLayout.setOnRefreshListener(() -> handler.post(() -> {
            listView.setVisibility(View.GONE);
            getPopularHistoryVideo();
        }));

        PopularHistoryAdapter.PopularHistoryAdapterListener adapterListener = this::onViewClick;

        runnableUi = () -> {
            listView.setVisibility(View.VISIBLE);
            waveSwipeRefreshLayout.setRefreshing(false);

            popularHistoryAdapter = new PopularHistoryAdapter(inflater, popularHistoryApi.popularHistoryVideoModelArrayList, listView, adapterListener);
            listView.setAdapter(popularHistoryAdapter);
        };

        listView.setOnTouchListener(new ViewTouchListener(listView, titleViewListener));

        listView.setVisibility(View.GONE);
        waveSwipeRefreshLayout.setRefreshing(true);
        getPopularHistoryVideo();

        return rootLayout;
    }

    private void getPopularHistoryVideo()
    {
        new Thread(() -> {
            try
            {
                popularHistoryApi.getPopularHistoryVideo();
                if(popularHistoryApi.popularHistoryVideoModelArrayList.size() > 0)
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

    private void onViewClick(int viewId, int position)
    {
        PopularHistoryModel.PopularHistoryVideoModel popularVideoModel = popularHistoryApi.popularHistoryVideoModelArrayList.get(position);
        if(viewId == R.id.popular_history_item_video)
        {
            Intent intent = new Intent(ctx, VideoActivity.class);
            intent.putExtra(VideoActivity.ARG_AID, popularVideoModel.getAid());
            intent.putExtra(VideoActivity.ARG_BVID, popularVideoModel.getBvid());
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
}
