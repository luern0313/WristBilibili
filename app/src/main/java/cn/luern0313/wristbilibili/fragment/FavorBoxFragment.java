package cn.luern0313.wristbilibili.fragment;

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
import java.util.ArrayList;

import androidx.fragment.app.Fragment;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.FavorBoxAdapter;
import cn.luern0313.wristbilibili.api.FavorBoxApi;
import cn.luern0313.wristbilibili.models.FavorBoxModel;
import cn.luern0313.wristbilibili.ui.FavorvideoActivity;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

/**
 * 被 luern0313 创建于 2018/11/16.
 * 收藏的fragment
 * 畜生！你收藏了甚么！
 */

public class FavorBoxFragment extends Fragment
{
    private static final String ARG_FAVOR_BOX_MID = "argFavorBoxMid";

    private Context ctx;
    private String mid;
    private FavorBoxApi favorBoxApi;
    private ArrayList<FavorBoxModel> favourboxArrayList;
    private FavorBoxAdapter.FavorBoxAdapterListener favorBoxAdapterListener;

    private View rootLayout;
    private ListView favListView;
    private WaveSwipeRefreshLayout waveSwipeRefreshLayout;

    public static boolean isLogin;

    private Handler handler = new Handler();
    private Runnable runnableUi, runnableNoWeb, runnableNodata;

    public FavorBoxFragment() {}

    public static FavorBoxFragment newInstance(String mid)
    {
        FavorBoxFragment fragment = new FavorBoxFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FAVOR_BOX_MID, mid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
        {
            mid = getArguments().getString(ARG_FAVOR_BOX_MID);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_favor, container, false);

        favListView = rootLayout.findViewById(R.id.fav_listview);
        waveSwipeRefreshLayout = rootLayout.findViewById(R.id.fav_swipe);
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
                        if(isLogin)
                        {
                            favListView.setVisibility(View.GONE);
                            getFavorbox();
                        }
                        else waveSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });

        favorBoxAdapterListener = new FavorBoxAdapter.FavorBoxAdapterListener()
        {
            @Override
            public void onClick(int viewId, int position)
            {
                if(viewId == R.id.favor_lay)
                {
                    Intent intent = new Intent(ctx, FavorvideoActivity.class);
                    intent.putExtra("fid", favourboxArrayList.get(position).fid);
                    intent.putExtra("mid", mid);
                    startActivity(intent);
                }
            }
        };

        runnableUi = new Runnable()
        {
            @Override
            public void run()
            {
                rootLayout.findViewById(R.id.fav_nologin).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.fav_noweb).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.fav_nonthing).setVisibility(View.GONE);
                favListView.setAdapter(new FavorBoxAdapter(inflater, favourboxArrayList, favListView, favorBoxAdapterListener));
                favListView.setVisibility(View.VISIBLE);
                waveSwipeRefreshLayout.setRefreshing(false);
            }
        };

        runnableNoWeb = new Runnable()
        {
            @Override
            public void run()
            {
                rootLayout.findViewById(R.id.fav_nologin).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.fav_noweb).setVisibility(View.VISIBLE);
                rootLayout.findViewById(R.id.fav_nonthing).setVisibility(View.GONE);
                favListView.setVisibility(View.GONE);
                waveSwipeRefreshLayout.setRefreshing(false);
            }
        };

        runnableNodata = new Runnable()
        {
            @Override
            public void run()
            {
                rootLayout.findViewById(R.id.fav_nologin).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.fav_noweb).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.fav_nonthing).setVisibility(View.VISIBLE);
                favListView.setVisibility(View.GONE);
                waveSwipeRefreshLayout.setRefreshing(false);
            }
        };

        isLogin = SharedPreferencesUtil.contains(SharedPreferencesUtil.cookies);
        if(isLogin)
        {
            waveSwipeRefreshLayout.setRefreshing(true);
            getFavorbox();
        }
        else
        {
            rootLayout.findViewById(R.id.fav_noweb).setVisibility(View.GONE);
            rootLayout.findViewById(R.id.fav_nologin).setVisibility(View.VISIBLE);
        }

        return rootLayout;
    }

    private void getFavorbox()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    favorBoxApi = new FavorBoxApi(mid);
                    favourboxArrayList = favorBoxApi.getFavorbox();
                    if(favourboxArrayList != null && favourboxArrayList.size() != 0)
                        handler.post(runnableUi);
                    else
                        handler.post(runnableNodata);
                }
                catch (NullPointerException e)
                {
                    handler.post(runnableNodata);
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    handler.post(runnableNoWeb);
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
