package cn.luern0313.wristbilibili.fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import androidx.fragment.app.Fragment;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.BangumiRecommendAdapter;
import cn.luern0313.wristbilibili.models.bangumi.BangumiRecommendModel;
import cn.luern0313.wristbilibili.ui.BangumiActivity;

/**
 * 被 luern0313 创建于 2020/3/20.
 */
public class BangumiRecommendFragment extends Fragment
{
    private static final String ARG_BANGUMI_RECOMMEND = "bangumiRecommendArg";

    Context ctx;
    View rootLayout;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private ListView uiRecommendListView;
    private BangumiRecommendAdapter bangumiRecommendAdapter;
    private ArrayList<BangumiRecommendModel> bangumiRecommendModelArrayList;

    public BangumiRecommendFragment() { }

    public static BangumiRecommendFragment newInstance(ArrayList<BangumiRecommendModel> bangumiRecommendModelArrayList)
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
            bangumiRecommendModelArrayList = (ArrayList<BangumiRecommendModel>) getArguments().getSerializable(ARG_BANGUMI_RECOMMEND);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_bangumi_recommend, container, false);
        sharedPreferences = ctx.getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        uiRecommendListView = rootLayout.findViewById(R.id.bgm_recommend_listview);
        uiRecommendListView.setEmptyView(rootLayout.findViewById(R.id.bgm_recommend_nothing));
        uiRecommendListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Intent intent = new Intent(ctx, BangumiActivity.class);
                intent.putExtra("season_id", bangumiRecommendModelArrayList.get(position).bangumi_season_id);
                startActivity(intent);
            }
        });

        bangumiRecommendAdapter = new BangumiRecommendAdapter(inflater, uiRecommendListView, bangumiRecommendModelArrayList);
        uiRecommendListView.setAdapter(bangumiRecommendAdapter);

        return rootLayout;
    }
}
