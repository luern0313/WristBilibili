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
import cn.luern0313.wristbilibili.adapter.PopularWeeklyAdapter;
import cn.luern0313.wristbilibili.api.PopularWeeklyApi;
import cn.luern0313.wristbilibili.models.popular.PopularWeeklyVideoListModel;
import cn.luern0313.wristbilibili.ui.SelectPartActivity;
import cn.luern0313.wristbilibili.ui.VideoActivity;
import cn.luern0313.wristbilibili.util.ColorUtil;
import cn.luern0313.wristbilibili.util.ViewTouchListener;
import cn.luern0313.wristbilibili.widget.ExceptionHandlerView;
import cn.luern0313.wristbilibili.widget.TitleView;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

/**
 * 被 luern0313 创建于 2020/1/11.
 */

public class PopularWeeklyFragment extends Fragment
{
    private Context ctx;
    private View rootLayout;
    private PopularWeeklyAdapter popularWeeklyAdapter;
    private PopularWeeklyApi popularWeeklyApi;
    private TitleView.TitleViewListener titleViewListener;

    private ListView listView;
    private WaveSwipeRefreshLayout waveSwipeRefreshLayout;
    private ExceptionHandlerView exceptionHandlerView;
    private View headerView;

    private final Handler handler = new Handler();
    private Runnable runnableUi;

    private int issue;

    private final int RESULT_POPULAR_WEEKLY_SELECT_ISSUE = 101;

    public PopularWeeklyFragment() { }

    public static PopularWeeklyFragment newInstance()
    {
        return new PopularWeeklyFragment();
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
        rootLayout = inflater.inflate(R.layout.fragment_popular_weekly, container, false);
        popularWeeklyApi = new PopularWeeklyApi();

        headerView = inflater.inflate(R.layout.widget_popular_weekly_header, null, false);
        exceptionHandlerView = rootLayout.findViewById(R.id.popular_weekly_exception);
        listView = rootLayout.findViewById(R.id.popular_weekly_listview);
        listView.addHeaderView(headerView);
        waveSwipeRefreshLayout = rootLayout.findViewById(R.id.popular_weekly_swipe);
        waveSwipeRefreshLayout.setColorSchemeColors(Color.WHITE, Color.WHITE);
        waveSwipeRefreshLayout.setWaveColor(ColorUtil.getColor(R.attr.colorPrimary, ctx));
        waveSwipeRefreshLayout.setTopOffsetOfWave(getResources().getDimensionPixelSize(R.dimen.titleHeight));
        waveSwipeRefreshLayout.setOnRefreshListener(() -> handler.post(() -> {
            listView.setVisibility(View.GONE);
            getPopularWeeklyIssue();
        }));

        PopularWeeklyAdapter.PopularWeeklyAdapterListener adapterListener = this::onViewClick;

        headerView.findViewById(R.id.popular_weekly_header_issue).setOnClickListener(v -> {
            Intent intent = new Intent(ctx, SelectPartActivity.class);
            intent.putExtra("title", getString(R.string.popular_weekly_issue_title));
            intent.putExtra("tip", getString(R.string.popular_weekly_update_cycle));
            intent.putExtra("options_name", popularWeeklyApi.popularWeeklyIssueListModel.getTitleList());
            startActivityForResult(intent, RESULT_POPULAR_WEEKLY_SELECT_ISSUE);
        });

        runnableUi = () -> {
            listView.setVisibility(View.VISIBLE);
            waveSwipeRefreshLayout.setRefreshing(false);
            setView();

            popularWeeklyAdapter = new PopularWeeklyAdapter(inflater, popularWeeklyApi.popularWeeklyVideoListModel.getList(), listView, adapterListener);
            listView.setAdapter(popularWeeklyAdapter);
        };

        listView.setOnTouchListener(new ViewTouchListener(listView, titleViewListener));

        listView.setVisibility(View.GONE);
        waveSwipeRefreshLayout.setRefreshing(true);
        getPopularWeeklyIssue();

        return rootLayout;
    }

    private void setView()
    {
        ((TextView) headerView.findViewById(R.id.popular_weekly_header_issue_text)).setText(popularWeeklyApi.popularWeeklyVideoListModel.getLabel());
        ((TextView) headerView.findViewById(R.id.popular_weekly_header_title)).setText(popularWeeklyApi.popularWeeklyVideoListModel.getTitle());
        ((TextView) headerView.findViewById(R.id.popular_weekly_header_desc)).setText(popularWeeklyApi.popularWeeklyVideoListModel.getDesc());
    }

    private void getPopularWeeklyIssue()
    {
        new Thread(() -> {
            try
            {
                popularWeeklyApi.getPopularWeeklyIssueList();
                if(popularWeeklyApi.popularWeeklyIssueListModel.getList() != null && popularWeeklyApi.popularWeeklyIssueListModel.getList().size() > 0)
                {
                    issue = popularWeeklyApi.popularWeeklyIssueListModel.getList().get(0).getNumber();
                    getPopularWeeklyVideo();
                }
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

    private void getPopularWeeklyVideo()
    {
        new Thread(() -> {
            try
            {
                popularWeeklyApi.getPopularWeeklyVideoList(issue);
                if(popularWeeklyApi.popularWeeklyVideoListModel.getList() != null && popularWeeklyApi.popularWeeklyVideoListModel.getList().size() > 0)
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != 0 || data == null) return;
        switch (requestCode)
        {
            case RESULT_POPULAR_WEEKLY_SELECT_ISSUE:
            {
                waveSwipeRefreshLayout.setRefreshing(true);
                issue = popularWeeklyApi.popularWeeklyIssueListModel.getList().get(data.getIntExtra("option_position", 0)).getNumber();
                getPopularWeeklyVideo();
            }
        }
    }

    private void onViewClick(int viewId, int position)
    {
        PopularWeeklyVideoListModel.PopularWeeklyVideoModel popularVideoModel = popularWeeklyApi.popularWeeklyVideoListModel.getList().get(position);
        if(viewId == R.id.popular_weekly_item_video)
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
