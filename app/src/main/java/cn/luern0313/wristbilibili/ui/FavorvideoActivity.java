package cn.luern0313.wristbilibili.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.ListVideoAdapter;
import cn.luern0313.wristbilibili.api.FavorVideoApi;
import cn.luern0313.wristbilibili.models.ListVideoModel;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

public class FavorvideoActivity extends AppCompatActivity
{
    Context ctx;
    Intent intent;
    LayoutInflater inflater;
    FavorVideoApi favorVideoApi;

    ArrayList<ListVideoModel> favorvideoList;
    String mid;
    String fid;

    ListVideoAdapter listVideoAdapter;
    ListVideoAdapter.ListVideoAdapterListener listVideoAdapterListener;
    ListView favvListView;
    WaveSwipeRefreshLayout waveSwipeRefreshLayout;
    View loadingView;

    Handler handler = new Handler();
    Runnable runnableUi, runnableNoWeb, runnableNothing, runnableMore, runnableMoreNoWeb, runnableMoreNothing;

    int page = 0;
    boolean isLoading = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorvideo);
        ctx = this;
        intent = getIntent();
        inflater = getLayoutInflater();
        mid = intent.getStringExtra("mid");
        fid = intent.getStringExtra("fid");

        listVideoAdapterListener = new ListVideoAdapter.ListVideoAdapterListener()
        {
            @Override
            public void onListVideoAdapterClick(int viewId, int position)
            {
                Intent intent = VideoActivity.getActivityIntent(ctx, favorvideoList.get(position).video_aid, "");
                startActivity(intent);
            }
        };

        loadingView = inflater.inflate(R.layout.widget_loading, null);
        favvListView = findViewById(R.id.favv_listview);
        waveSwipeRefreshLayout = findViewById(R.id.favv_swipe);
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
                        favvListView.setVisibility(View.GONE);
                        getFavorVideo();
                    }
                });
            }
        });

        runnableUi = new Runnable()
        {
            @Override
            public void run()
            {
                isLoading = false;
                findViewById(R.id.favv_noweb).setVisibility(View.GONE);
                findViewById(R.id.favv_nonthing).setVisibility(View.GONE);
                favvListView.setVisibility(View.VISIBLE);

                waveSwipeRefreshLayout.setRefreshing(false);
                listVideoAdapter = new ListVideoAdapter(inflater, favorvideoList, favvListView, listVideoAdapterListener);
                favvListView.setAdapter(listVideoAdapter);
            }
        };

        runnableNothing = new Runnable()
        {
            @Override
            public void run()
            {
                findViewById(R.id.favv_noweb).setVisibility(View.GONE);
                findViewById(R.id.favv_nonthing).setVisibility(View.VISIBLE);
                favvListView.setVisibility(View.GONE);
                waveSwipeRefreshLayout.setRefreshing(false);
            }
        };

        runnableNoWeb = new Runnable()
        {
            @Override
            public void run()
            {
                findViewById(R.id.favv_noweb).setVisibility(View.VISIBLE);
                findViewById(R.id.favv_nonthing).setVisibility(View.GONE);
                favvListView.setVisibility(View.GONE);
                waveSwipeRefreshLayout.setRefreshing(false);
            }
        };

        runnableMoreNoWeb = new Runnable()
        {
            @Override
            public void run()
            {
                ((TextView) loadingView.findViewById(R.id.wid_load_text)).setText("好像没有网络...\n检查下网络？");
                loadingView.findViewById(R.id.wid_load_button).setVisibility(View.VISIBLE);
            }
        };

        runnableMoreNothing = new Runnable()
        {
            @Override
            public void run()
            {
                ((TextView) loadingView.findViewById(R.id.wid_load_text)).setText("  没有更多了...");
            }
        };

        runnableMore = new Runnable()
        {
            @Override
            public void run()
            {
                isLoading = false;
                listVideoAdapter.notifyDataSetChanged();
            }
        };

        loadingView.findViewById(R.id.wid_load_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((TextView) loadingView.findViewById(R.id.wid_load_text)).setText(" 加载中. . .");
                loadingView.findViewById(R.id.wid_load_button).setVisibility(View.GONE);
                getMoreFavorVideo();
            }
        });

        favvListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id)
            {
                new AlertDialog.Builder(ctx)
                        .setMessage("你确定要取消收藏这个视频吗？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                new Thread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        try
                                        {
                                            String result = favorVideoApi.cancelFavVideo(String.valueOf(favorvideoList.get(position).video_aid));
                                            if(result.equals(""))
                                            {
                                                favorvideoList.remove(position);
                                                handler.post(runnableMore);
                                                Looper.prepare();
                                                Toast.makeText(ctx, "取消收藏成功！", Toast.LENGTH_SHORT).show();
                                                Looper.loop();
                                            }
                                            else
                                            {
                                                Looper.prepare();
                                                Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
                                                Looper.loop();
                                            }
                                        }
                                        catch(IOException e)
                                        {
                                            e.printStackTrace();
                                        }
                                    }
                                }).start();
                            }
                        })
                        .setNegativeButton("取消", null).show();
                return true;
            }
        });

        favvListView.setOnScrollListener(new AbsListView.OnScrollListener()
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
                    getMoreFavorVideo();
                }
            }
        });

        waveSwipeRefreshLayout.setRefreshing(true);
        favvListView.addFooterView(loadingView);
        getFavorVideo();
    }

    void getFavorVideo()
    {
        isLoading = true;
        favorVideoApi = new FavorVideoApi(mid, fid);
        page = 1;
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    favorvideoList = favorVideoApi.getFavorVideo(page);
                    if(favorvideoList != null && favorvideoList.size() != 0)
                        handler.post(runnableUi);
                    else handler.post(runnableNothing);
                }
                catch (IOException e)
                {
                    handler.post(runnableNoWeb);
                    e.printStackTrace();
                }
            }
        }).start();
    }

    void getMoreFavorVideo()
    {
        isLoading = true;
        page++;
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    ArrayList<ListVideoModel> arrayList = favorVideoApi.getFavorVideo(page);
                    if(arrayList != null && arrayList.size() != 0)
                    {
                        favorvideoList.addAll(arrayList);
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
            }
        }).start();
    }
}
