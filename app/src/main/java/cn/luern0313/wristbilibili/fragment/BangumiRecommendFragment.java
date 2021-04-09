package cn.luern0313.wristbilibili.fragment;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.ListBangumiAdapter;
import cn.luern0313.wristbilibili.models.ListBangumiModel;
import cn.luern0313.wristbilibili.ui.BangumiActivity;
import cn.luern0313.wristbilibili.util.ViewTouchListener;
import cn.luern0313.wristbilibili.widget.ExceptionHandlerView;
import cn.luern0313.wristbilibili.widget.TitleView;

/**
 * 被 luern0313 创建于 2020/3/20.
 */
public class BangumiRecommendFragment extends Fragment
{
    private static final String ARG_BANGUMI_RECOMMEND = "bangumiRecommendArg";

    private Context ctx;
    private View rootLayout;

    private ExceptionHandlerView exceptionHandlerView;
    private ListView listView;
    private ListBangumiAdapter bangumiRecommendAdapter;
    private ArrayList<ListBangumiModel> bangumiRecommendModelArrayList;
    private ListBangumiAdapter.ListBangumiAdapterListener listBangumiAdapterListener;
    private TitleView.TitleViewListener titleViewListener;

    public BangumiRecommendFragment() { }

    public static BangumiRecommendFragment newInstance(ArrayList<ListBangumiModel> bangumiRecommendModelArrayList)
    {
        BangumiRecommendFragment fragment = new BangumiRecommendFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_BANGUMI_RECOMMEND, bangumiRecommendModelArrayList);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
        {
            bangumiRecommendModelArrayList = (ArrayList<ListBangumiModel>) getArguments().getSerializable(ARG_BANGUMI_RECOMMEND);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_bangumi_recommend, container, false);

        listBangumiAdapterListener = this::onViewClick;

        exceptionHandlerView = rootLayout.findViewById(R.id.bgm_recommend_exception);
        listView = rootLayout.findViewById(R.id.bgm_recommend_listview);
        listView.setOnTouchListener(new ViewTouchListener(listView, titleViewListener));

        if(bangumiRecommendModelArrayList != null && bangumiRecommendModelArrayList.size() > 0)
        {
            bangumiRecommendAdapter = new ListBangumiAdapter(inflater, listView, bangumiRecommendModelArrayList, listBangumiAdapterListener);
            listView.setAdapter(bangumiRecommendAdapter);
        }
        else
            exceptionHandlerView.noData();

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

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        if(context instanceof TitleView.TitleViewListener)
            titleViewListener = (TitleView.TitleViewListener) context;
    }
}
