package cn.luern0313.wristbilibili.fragment;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import cn.luern0313.wristbilibili.R;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

/**
 * Created by liupe on 2018/11/16.
 * 搜索。。
 */

public class Search extends Fragment
{
    Context ctx;

    View rootLayout;
    ListView seaListView;
    WaveSwipeRefreshLayout waveSwipeRefreshLayout;

    Handler handler = new Handler();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_search, container, false);
        seaListView = rootLayout.findViewById(R.id.sea_listview);
        waveSwipeRefreshLayout = rootLayout.findViewById(R.id.sea_swipe);

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

                    }
                });
            }
        });
        return rootLayout;
    }
}
