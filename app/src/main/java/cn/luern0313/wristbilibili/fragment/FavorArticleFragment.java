package cn.luern0313.wristbilibili.fragment;

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
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import androidx.fragment.app.Fragment;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.ListArticleAdapter;
import cn.luern0313.wristbilibili.api.FavorArticleApi;
import cn.luern0313.wristbilibili.models.ListArticleModel;
import cn.luern0313.wristbilibili.ui.ArticleActivity;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

/**
 * 被 luern0313 创建于 2020/7/29.
 */

public class FavorArticleFragment extends Fragment
{
    private Context ctx;
    private View rootLayout;
    private ListView uiListView;
    private View layoutLoading;
    private WaveSwipeRefreshLayout waveSwipeRefreshLayout;

    private FavorArticleApi favorArticleApi;
    private ArrayList<ListArticleModel> listArticleModelArrayList;
    private ListArticleAdapter listArticleAdapter;
    private ListArticleAdapter.ListArticleAdapterListener listArticleAdapterListener;

    private Handler handler = new Handler();
    private Runnable runnableUi, runnableNoWeb, runnableNoData, runnableMore, runnableMoreNoWeb, runnableMoreNoData;

    private int favorPage = 1;
    private boolean isFavorLoading = true;

    public static Fragment newInstance()
    {
        return new FavorArticleFragment();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_favor_article, container, false);

        uiListView = rootLayout.findViewById(R.id.favor_article_listview);
        layoutLoading = inflater.inflate(R.layout.widget_loading, null);
        waveSwipeRefreshLayout = rootLayout.findViewById(R.id.favor_article_swipe);
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
                        getFavorArticle();
                        waveSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
        uiListView.addFooterView(layoutLoading, null, true);
        uiListView.setHeaderDividersEnabled(false);

        listArticleAdapterListener = new ListArticleAdapter.ListArticleAdapterListener()
        {
            @Override
            public void onListArticleAdapterClick(int viewId, int position)
            {
                onViewClick(viewId, position);
            }

            @Override
            public void onListArticleAdapterLongClick(int viewId, int position)
            {
                onViewLongClick(viewId, position);
            }
        };

        runnableUi = new Runnable()
        {
            @Override
            public void run()
            {
                isFavorLoading = false;
                rootLayout.findViewById(R.id.favor_article_no_web).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.favor_article_no_data).setVisibility(View.GONE);
                listArticleAdapter = new ListArticleAdapter(inflater, listArticleModelArrayList, uiListView, listArticleAdapterListener);
                uiListView.setAdapter(listArticleAdapter);
                uiListView.setVisibility(View.VISIBLE);
                waveSwipeRefreshLayout.setRefreshing(false);
            }
        };

        runnableNoWeb = new Runnable()
        {
            @Override
            public void run()
            {
                rootLayout.findViewById(R.id.favor_article_no_web).setVisibility(View.VISIBLE);
                rootLayout.findViewById(R.id.favor_article_no_data).setVisibility(View.GONE);
                waveSwipeRefreshLayout.setRefreshing(false);
            }
        };

        runnableNoData = new Runnable()
        {
            @Override
            public void run()
            {
                rootLayout.findViewById(R.id.favor_article_no_web).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.favor_article_no_data).setVisibility(View.VISIBLE);
                waveSwipeRefreshLayout.setRefreshing(false);
            }
        };

        runnableMore = new Runnable()
        {
            @Override
            public void run()
            {
                isFavorLoading = false;
                listArticleAdapter.notifyDataSetChanged();
            }
        };

        runnableMoreNoWeb = new Runnable()
        {
            @Override
            public void run()
            {
                ((TextView) layoutLoading.findViewById(R.id.wid_load_text)).setText(ctx.getString(R.string.main_tip_no_web));
                layoutLoading.findViewById(R.id.wid_load_button).setVisibility(View.VISIBLE);
                isFavorLoading = false;
            }
        };

        runnableMoreNoData = new Runnable()
        {
            @Override
            public void run()
            {
                ((TextView) layoutLoading.findViewById(R.id.wid_load_text)).setText(ctx.getString(R.string.main_tip_no_more_data));
            }
        };

        layoutLoading.findViewById(R.id.wid_load_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((TextView) layoutLoading.findViewById(R.id.wid_load_text)).setText(ctx.getString(R.string.main_tip_no_more_data_loading));
                layoutLoading.findViewById(R.id.wid_load_button).setVisibility(View.GONE);
                getMoreFavorArticle();
            }
        });

        uiListView.setOnScrollListener(new AbsListView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState)
            {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
            {
                if(visibleItemCount + firstVisibleItem == totalItemCount && !isFavorLoading)
                {
                    getMoreFavorArticle();
                }
            }
        });

        waveSwipeRefreshLayout.setRefreshing(true);
        getFavorArticle();

        return rootLayout;
    }

    private void getFavorArticle()
    {
        isFavorLoading = true;
        favorPage = 1;
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    favorArticleApi = new FavorArticleApi();
                    listArticleModelArrayList = favorArticleApi.getFavorArticle(favorPage);
                    if(listArticleModelArrayList != null && listArticleModelArrayList.size() > 0)
                        handler.post(runnableUi);
                    else
                        handler.post(runnableNoData);
                }
                catch (IOException e)
                {
                    handler.post(runnableNoWeb);
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void getMoreFavorArticle()
    {
        isFavorLoading = true;
        favorPage++;
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    favorArticleApi = new FavorArticleApi();
                    ArrayList<ListArticleModel> articleModelArrayList = favorArticleApi.getFavorArticle(favorPage);
                    if(articleModelArrayList.size() > 0)
                    {
                        listArticleModelArrayList.addAll(articleModelArrayList);
                        handler.post(runnableMore);
                    }
                    else
                        handler.post(runnableMoreNoData);
                }
                catch (IOException e)
                {
                    handler.post(runnableMoreNoWeb);
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void onViewClick(int id, int position)
    {
        if(id == R.id.item_list_article_lay)
        {
            Intent intent = new Intent(ctx, ArticleActivity.class);
            intent.putExtra("article_id", listArticleModelArrayList.get(position).getId());
            startActivity(intent);
        }
    }

    private void onViewLongClick(int id, final int position)
    {
        if(id == R.id.item_list_article_lay)
        {
            new AlertDialog.Builder(ctx)
                    .setMessage(ctx.getString(R.string.favor_article_delete_message))
                    .setPositiveButton(ctx.getString(R.string.favor_article_delete_ok), new DialogInterface.OnClickListener()
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
                                        String result = favorArticleApi.cancelFavorArticle(String.valueOf(listArticleModelArrayList.get(position).getId()));
                                        if(result.equals(""))
                                        {
                                            listArticleModelArrayList.remove(position);
                                            handler.post(runnableMore);
                                            Looper.prepare();
                                            Toast.makeText(ctx, ctx.getString(R.string.favor_article_delete_done), Toast.LENGTH_SHORT).show();
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
                                }
                            }).start();
                        }
                    }).setNegativeButton(ctx.getString(R.string.favor_article_delete_cancel), null).show();
        }
    }
}
