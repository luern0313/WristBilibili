package cn.luern0313.wristbilibili.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import androidx.fragment.app.Fragment;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.ListBangumiAdapter;
import cn.luern0313.wristbilibili.models.ListBangumiModel;
import cn.luern0313.wristbilibili.ui.BangumiActivity;

/**
 * 被 luern0313 创建于 2020/3/20.
 */
public class BangumiRecommendFragment extends Fragment
{
    private static final String ARG_BANGUMI_RECOMMEND = "bangumiRecommendArg";

    Context ctx;
    View rootLayout;

    private ListView uiRecommendListView;
    private ListBangumiAdapter bangumiRecommendAdapter;
    private ArrayList<ListBangumiModel> bangumiRecommendModelArrayList;
    private ListBangumiAdapter.ListBangumiAdapterListener listBangumiAdapterListener;

    public BangumiRecommendFragment() { }

    public static BangumiRecommendFragment newInstance(ArrayList<ListBangumiModel> bangumiRecommendModelArrayList)
    {
        BangumiRecommendFragment fragment = new BangumiRecommendFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_BANGUMI_RECOMMEND, bangumiRecommendModelArrayList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
        {
            bangumiRecommendModelArrayList = (ArrayList<ListBangumiModel>) getArguments().getSerializable(ARG_BANGUMI_RECOMMEND);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_bangumi_recommend, container, false);

        listBangumiAdapterListener = new ListBangumiAdapter.ListBangumiAdapterListener()
        {
            @Override
            public void onListBangumiAdapterClick(int viewId, int position)
            {
                onViewClick(viewId, position);
            }
        };

        uiRecommendListView = rootLayout.findViewById(R.id.bgm_recommend_listview);
        uiRecommendListView.setEmptyView(rootLayout.findViewById(R.id.bgm_recommend_nothing));

        bangumiRecommendAdapter = new ListBangumiAdapter(inflater, uiRecommendListView, bangumiRecommendModelArrayList, listBangumiAdapterListener);
        uiRecommendListView.setAdapter(bangumiRecommendAdapter);

        return rootLayout;
    }

    private void onViewClick(int viewId, int position)
    {
        if(viewId == R.id.item_list_bangumi_lay)
        {
            Intent intent = new Intent(ctx, BangumiActivity.class);
            intent.putExtra("season_id", bangumiRecommendModelArrayList.get(position).getSeasonId());
            startActivity(intent);
        }
    }
}
