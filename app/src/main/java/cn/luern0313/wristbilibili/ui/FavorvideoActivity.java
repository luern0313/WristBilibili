package cn.luern0313.wristbilibili.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.FavorVideoAdapter;
import cn.luern0313.wristbilibili.api.FavorVideoApi;
import cn.luern0313.wristbilibili.models.FavorVideoModel;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

public class FavorvideoActivity extends AppCompatActivity
{
    Context ctx;
    Intent intent;
    LayoutInflater inflater;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    FavorVideoApi favorVideoApi;

    ArrayList<FavorVideoModel> favorvideoList;
    String fid;

    FavorVideoAdapter adapter;
    ListView favvListView;
    WaveSwipeRefreshLayout waveSwipeRefreshLayout;
    View loadingView;

    Handler handler = new Handler();
    Runnable runnableUi;
    Runnable runnableNoWeb;
    Runnable runnableNoWebH;
    Runnable runnableNodata;
    Runnable runnableAddlist;
    Runnable runnableNomore;

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
        sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        fid = intent.getStringExtra("fid");

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
                adapter = new FavorVideoAdapter(inflater, favorvideoList, favvListView);
                favvListView.setAdapter(adapter);
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
            }
        };

        runnableNoWebH = new Runnable()
        {
            @Override
            public void run()
            {
                ((TextView) loadingView.findViewById(R.id.wid_load_text)).setText("好像没有网络...\n检查下网络？");
                loadingView.findViewById(R.id.wid_load_button).setVisibility(View.VISIBLE);
            }
        };

        runnableNomore = new Runnable()
        {
            @Override
            public void run()
            {
                ((TextView) loadingView.findViewById(R.id.wid_load_text)).setText("  没有更多了...");
            }
        };

        runnableAddlist = new Runnable()
        {
            @Override
            public void run()
            {
                isLoading = false;
                adapter.notifyDataSetChanged();
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

        favvListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if(position < favorvideoList.size())
                {
                    Intent intent = new Intent(ctx, VideodetailsActivity.class);
                    intent.putExtra("aid", String.valueOf(favorvideoList.get(position).video_aid));
                    startActivity(intent);
                }
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
                                                handler.post(runnableAddlist);
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
        favorVideoApi = new FavorVideoApi(sharedPreferences.getString("cookies", ""),
                                          sharedPreferences.getString("mid", ""),
                                          sharedPreferences.getString("csrf", ""), fid);
        page = 1;
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    favorvideoList = favorVideoApi.getFavorvideo(page);
                    if(favorvideoList != null && favorvideoList.size() != 0)
                        handler.post(runnableUi);
                    else handler.post(runnableNodata);
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
                    ArrayList<FavorVideoModel> arrayList = favorVideoApi.getFavorvideo(page);
                    if(arrayList != null && arrayList.size() != 0)
                    {
                        favorvideoList.addAll(arrayList);
                        handler.post(runnableAddlist);
                    }
                    else handler.post(runnableNomore);
                }
                catch (IOException e)
                {
                    page--;
                    handler.post(runnableNoWebH);
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
