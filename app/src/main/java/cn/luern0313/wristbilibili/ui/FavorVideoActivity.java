package cn.luern0313.wristbilibili.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.ListVideoAdapter;
import cn.luern0313.wristbilibili.api.FavorVideoApi;
import cn.luern0313.wristbilibili.models.ListVideoModel;
import cn.luern0313.wristbilibili.util.ColorUtil;
import cn.luern0313.wristbilibili.util.ViewScrollListener;
import cn.luern0313.wristbilibili.util.ViewTouchListener;
import cn.luern0313.wristbilibili.widget.ExceptionHandlerView;
import cn.luern0313.wristbilibili.widget.TitleView;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

public class FavorVideoActivity extends BaseActivity implements TitleView.TitleViewListener, ViewScrollListener.CustomScrollResult
{
    private Context ctx;
    private Intent intent;
    private LayoutInflater inflater;
    private FavorVideoApi favorVideoApi;

    private ArrayList<ListVideoModel> favorVideoList;
    private String mid;
    private String fid;

    private TitleView titleView;
    private ExceptionHandlerView exceptionHandlerView;
    private ListVideoAdapter listVideoAdapter;
    private ListVideoAdapter.ListVideoAdapterListener listVideoAdapterListener;
    private ListView listView;
    private WaveSwipeRefreshLayout waveSwipeRefreshLayout;
    private View loadingView;

    private final Handler handler = new Handler();
    private Runnable runnableUi, runnableMore, runnableMoreNoWeb, runnableMoreNothing;

    private int page = 0;
    private boolean isLoading = true;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favor_video);
        ctx = this;
        intent = getIntent();
        inflater = getLayoutInflater();
        mid = intent.getStringExtra("mid");
        fid = intent.getStringExtra("fid");

        titleView = findViewById(R.id.favor_video_title);
        exceptionHandlerView = findViewById(R.id.favor_video_exception);

        listVideoAdapterListener = new ListVideoAdapter.ListVideoAdapterListener()
        {
            @Override
            public void onListVideoAdapterClick(int viewId, int position)
            {
                Intent intent = new Intent(ctx, VideoActivity.class);
                intent.putExtra(VideoActivity.ARG_AID, favorVideoList.get(position).getAid());
                startActivity(intent);
            }

            @Override
            public void onListVideoAdapterLongClick(int viewId, final int position)
            {
                new AlertDialog.Builder(ctx)
                        .setMessage(ctx.getString(R.string.favor_video_delete_message))
                        .setPositiveButton(ctx.getString(R.string.favor_video_delete_ok),
                                           (dialog, which) -> new Thread(() -> {
                                               try
                                               {
                                                   String result = favorVideoApi.cancelFavVideo(String.valueOf(
                                                           favorVideoList.get(position).getAid()));
                                                   if(result.equals(""))
                                                   {
                                                       favorVideoList.remove(position);
                                                       handler.post(runnableMore);
                                                       Looper.prepare();
                                                       Toast.makeText(ctx, ctx.getString(R.string.favor_video_delete_message), Toast.LENGTH_SHORT).show();
                                                   }
                                                   else
                                                   {
                                                       Looper.prepare();
                                                       Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
                                                   }
                                                   Looper.loop();
                                               }
                                               catch(IOException e)
                                               {
                                                   e.printStackTrace();
                                               }
                                           }).start()).setNegativeButton(ctx.getString(R.string.favor_video_delete_cancel), null).show();
            }
        };

        loadingView = inflater.inflate(R.layout.widget_loading, null);
        listView = findViewById(R.id.favor_video_listview);
        waveSwipeRefreshLayout = findViewById(R.id.favor_video_swipe);
        waveSwipeRefreshLayout.setColorSchemeColors(Color.WHITE, Color.WHITE);
        waveSwipeRefreshLayout.setWaveColor(ColorUtil.getColor(R.attr.colorPrimary, ctx));
        waveSwipeRefreshLayout.setTopOffsetOfWave(getResources().getDimensionPixelSize(R.dimen.titleHeight));
        waveSwipeRefreshLayout.setOnRefreshListener(() -> handler.post(() -> {
            listView.setVisibility(View.GONE);
            getFavorVideo();
        }));

        runnableUi = () -> {
            isLoading = false;
            exceptionHandlerView.hideAllView();
            listView.setVisibility(View.VISIBLE);

            waveSwipeRefreshLayout.setRefreshing(false);
            listVideoAdapter = new ListVideoAdapter(inflater, favorVideoList, false, listView, listVideoAdapterListener);
            listView.setAdapter(listVideoAdapter);
        };

        runnableMoreNoWeb = () -> {
            ((TextView) loadingView.findViewById(R.id.wid_load_text)).setText(ctx.getString(R.string.main_tip_no_more_web));
            loadingView.findViewById(R.id.wid_load_button).setVisibility(View.VISIBLE);
        };

        runnableMoreNothing = () -> ((TextView) loadingView.findViewById(R.id.wid_load_text)).setText(ctx.getString(R.string.main_tip_no_more_data));

        runnableMore = () -> {
            isLoading = false;
            listVideoAdapter.notifyDataSetChanged();
        };

        loadingView.findViewById(R.id.wid_load_button).setOnClickListener(v -> {
            ((TextView) loadingView.findViewById(R.id.wid_load_text)).setText(ctx.getString(R.string.main_tip_no_more_data_loading));
            loadingView.findViewById(R.id.wid_load_button).setVisibility(View.GONE);
            getMoreFavorVideo();
        });

        listView.setOnScrollListener(new ViewScrollListener(this));
        listView.setOnTouchListener(new ViewTouchListener(listView, this));

        waveSwipeRefreshLayout.setRefreshing(true);
        listView.addFooterView(loadingView);
        getFavorVideo();
    }

    private void getFavorVideo()
    {
        isLoading = true;
        favorVideoApi = new FavorVideoApi(mid, fid);
        page = 1;
        new Thread(() -> {
            try
            {
                favorVideoList = favorVideoApi.getFavorVideo(page);
                if(favorVideoList != null && favorVideoList.size() != 0)
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

    private void getMoreFavorVideo()
    {
        isLoading = true;
        page++;
        new Thread(() -> {
            try
            {
                ArrayList<ListVideoModel> arrayList = favorVideoApi.getFavorVideo(page);
                if(arrayList != null && arrayList.size() != 0)
                {
                    favorVideoList.addAll(arrayList);
                    handler.post(runnableMore);
                }
                else handler.post(runnableMoreNothing);
            }
            catch (IOException e)
            {
                page--;
                handler.post(runnableMoreNoWeb);
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public boolean hideTitle()
    {
        return titleView.hide();
    }

    @Override
    public boolean showTitle()
    {
        return titleView.show();
    }

    @Override
    public boolean rule()
    {
        return !isLoading;
    }

    @Override
    public void result()
    {
        getMoreFavorVideo();
    }
}
