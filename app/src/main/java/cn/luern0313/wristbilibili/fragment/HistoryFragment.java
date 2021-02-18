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
import androidx.fragment.app.Fragment;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.ListVideoAdapter;
import cn.luern0313.wristbilibili.api.HistoryApi;
import cn.luern0313.wristbilibili.models.ListVideoModel;
import cn.luern0313.wristbilibili.ui.VideoActivity;
import cn.luern0313.wristbilibili.util.ColorUtil;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.ListViewTouchListener;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;
import cn.luern0313.wristbilibili.util.ViewScrollListener;
import cn.luern0313.wristbilibili.util.ViewTouchListener;
import cn.luern0313.wristbilibili.widget.TitleView;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

public class HistoryFragment extends Fragment
{
    Context ctx;
    View rootLayout;
    private WaveSwipeRefreshLayout waveSwipeRefreshLayout;
    private HistoryApi historyApi;

    private View uiLoadingView;
    private ListVideoAdapter listVideoAdapter;
    private ListVideoAdapter.ListVideoAdapterListener listVideoAdapterListener;
    private ListView uiListView;
    private TitleView.TitleViewListener titleViewListener;

    private final ArrayList<ListVideoModel> historyArrayList = new ArrayList<>();

    public static boolean isLogin;
    private boolean isLoading = true;

    private final Handler handler = new Handler();
    private Runnable runnableUi, runnableNoWeb, runnableNoData, runnableMore, runnableMoreNoData, runnableMoreNoWeb;

    private int pn;

    public HistoryFragment() { }

    public static HistoryFragment newInstance()
    {
        return new HistoryFragment();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();

        rootLayout = inflater.inflate(R.layout.fragment_history, container, false);
        uiListView = rootLayout.findViewById(R.id.history_listview);
        uiLoadingView = inflater.inflate(R.layout.widget_loading, null, false);
        uiListView.addFooterView(uiLoadingView);
        waveSwipeRefreshLayout = rootLayout.findViewById(R.id.history_swipe);
        waveSwipeRefreshLayout.setColorSchemeColors(Color.WHITE, Color.WHITE);
        waveSwipeRefreshLayout.setWaveColor(ColorUtil.getColor(R.attr.colorPrimary, ctx));
        waveSwipeRefreshLayout.setTopOffsetOfWave(DataProcessUtil.dip2px(33));
        waveSwipeRefreshLayout.setOnRefreshListener(() -> handler.post(() -> {
            if(isLogin)
            {
                uiListView.setVisibility(View.GONE);
                getHistory();
            }
            else waveSwipeRefreshLayout.setRefreshing(false);
        }));

        listVideoAdapterListener = new ListVideoAdapter.ListVideoAdapterListener()
        {
            @Override
            public void onListVideoAdapterClick(int viewId, int position)
            {
                Intent intent;
                if(historyArrayList.get(position).getBvid() != null && !historyArrayList.get(position).getBvid().equals(""))
                    intent = VideoActivity.getActivityIntent(ctx, "", historyArrayList.get(position).getBvid());
                else
                    intent = VideoActivity.getActivityIntent(ctx, historyArrayList.get(position).getAid(), "");
                startActivity(intent);
            }

            @Override
            public void onListVideoAdapterLongClick(int viewId, int position)
            {

            }
        };

        runnableUi = () -> {
            isLoading = false;
            rootLayout.findViewById(R.id.history_nologin).setVisibility(View.GONE);
            rootLayout.findViewById(R.id.history_noweb).setVisibility(View.GONE);
            rootLayout.findViewById(R.id.history_nonthing).setVisibility(View.GONE);

            listVideoAdapter = new ListVideoAdapter(inflater, historyArrayList, true, uiListView, listVideoAdapterListener);
            uiListView.setAdapter(listVideoAdapter);
            uiListView.setVisibility(View.VISIBLE);
            waveSwipeRefreshLayout.setRefreshing(false);
        };

        runnableNoWeb = () -> {
            waveSwipeRefreshLayout.setRefreshing(false);
            rootLayout.findViewById(R.id.history_noweb).setVisibility(View.VISIBLE);
            rootLayout.findViewById(R.id.history_nologin).setVisibility(View.GONE);
            rootLayout.findViewById(R.id.history_nonthing).setVisibility(View.GONE);
        };

        runnableNoData = () -> {
            waveSwipeRefreshLayout.setRefreshing(false);
            rootLayout.findViewById(R.id.history_nonthing).setVisibility(View.VISIBLE);
            rootLayout.findViewById(R.id.history_noweb).setVisibility(View.GONE);
            rootLayout.findViewById(R.id.history_nologin).setVisibility(View.GONE);
        };

        runnableMore = () -> {
            isLoading = false;
            listVideoAdapter.notifyDataSetChanged();
        };

        runnableMoreNoWeb = () -> {
            ((TextView) uiLoadingView.findViewById(R.id.wid_load_text)).setText("好像没有网络...\n检查下网络？");
            uiLoadingView.findViewById(R.id.wid_load_button).setVisibility(View.VISIBLE);
        };

        runnableMoreNoData = () -> ((TextView) uiLoadingView.findViewById(R.id.wid_load_text)).setText("  没有更多了...");

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
                    getMoreHistory();
                }
            }
        });

        uiListView.setOnTouchListener(new ListViewTouchListener(uiListView, titleViewListener));

        isLogin = SharedPreferencesUtil.contains(SharedPreferencesUtil.cookies);
        if(isLogin)
        {
            waveSwipeRefreshLayout.setRefreshing(true);
            getHistory();
        }
        else
        {
            rootLayout.findViewById(R.id.history_noweb).setVisibility(View.GONE);
            rootLayout.findViewById(R.id.history_nologin).setVisibility(View.VISIBLE);
        }

        return rootLayout;
    }

    private void getHistory()
    {
        new Thread(() -> {
            try
            {
                pn = 1;
                isLoading = true;
                historyApi = new HistoryApi();
                ArrayList<ListVideoModel> v = historyApi.getHistory(pn);
                if(v != null && v.size() != 0)
                {
                    historyArrayList.addAll(v);
                    handler.post(runnableUi);
                }
                else
                    handler.post(runnableNoData);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                handler.post(runnableNoWeb);
            }
        }).start();
    }

    private void getMoreHistory()
    {
        new Thread(() -> {
            try
            {
                pn++;
                isLoading = true;
                ArrayList<ListVideoModel> v = historyApi.getHistory(pn);
                if(v != null && v.size() != 0)
                {
                    historyArrayList.addAll(v);
                    handler.post(runnableMore);
                }
                else
                    handler.post(runnableMoreNoData);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                handler.post(runnableMoreNoWeb);
            }
        }).start();
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        if(context instanceof TitleView.TitleViewListener)
            titleViewListener = (TitleView.TitleViewListener) context;
    }
}
