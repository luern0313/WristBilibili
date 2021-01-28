package cn.luern0313.wristbilibili.fragment.user;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.ListVideoAdapter;
import cn.luern0313.wristbilibili.api.UserApi;
import cn.luern0313.wristbilibili.models.ListVideoModel;
import cn.luern0313.wristbilibili.ui.VideoActivity;
import cn.luern0313.wristbilibili.util.ListViewTouchListener;
import cn.luern0313.wristbilibili.widget.TitleView;

public class UserVideoFragment extends Fragment
{
    private static final String ARG_USER_VIDEO_MID = "argUserVideoMid";

    private Context ctx;
    private String mid;
    private int page = 1;
    private UserApi userApi;
    private final ArrayList<ListVideoModel> listVideoModelArrayList = new ArrayList<>();
    private ListVideoAdapter listVideoAdapter;
    private ListVideoAdapter.ListVideoAdapterListener listVideoAdapterListener;
    private TitleView.TitleViewListener titleViewListener;

    private View rootLayout;
    private View layoutLoading;
    private ListView uiListView;
    private LinearLayout uiLoading;
    private LinearLayout uiNoWeb;
    private LinearLayout uiNothing;

    private final Handler handler = new Handler();
    private Runnable runnableUi, runnableNoWeb, runnableNothing, runnableMore, runnableMoreNoWeb, runnableMoreNothing;

    private boolean isLoading = true;

    public UserVideoFragment() {}

    public static UserVideoFragment newInstance(String mid)
    {
        UserVideoFragment fragment = new UserVideoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_VIDEO_MID, mid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
        {
            mid = getArguments().getString(ARG_USER_VIDEO_MID);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_user_video, container, false);
        userApi = new UserApi(mid);

        listVideoAdapterListener = new ListVideoAdapter.ListVideoAdapterListener()
        {
            @Override
            public void onListVideoAdapterClick(int viewId, int position)
            {
                Intent intent = VideoActivity.getActivityIntent(ctx, "", listVideoModelArrayList.get(position).getBvid());
                startActivity(intent);
            }

            @Override
            public void onListVideoAdapterLongClick(int viewId, int position)
            {

            }
        };

        layoutLoading = inflater.inflate(R.layout.widget_loading, null, false);
        uiListView = rootLayout.findViewById(R.id.user_video_listview);
        uiLoading = rootLayout.findViewById(R.id.user_video_loading);
        uiNoWeb = rootLayout.findViewById(R.id.user_video_noweb);
        uiNothing = rootLayout.findViewById(R.id.user_video_nonthing);

        runnableUi = () -> {
            uiLoading.setVisibility(View.GONE);
            uiNoWeb.setVisibility(View.GONE);
            uiNothing.setVisibility(View.GONE);
            listVideoAdapter = new ListVideoAdapter(inflater, listVideoModelArrayList, false, uiListView, listVideoAdapterListener);
            uiListView.setAdapter(listVideoAdapter);
        };

        runnableNoWeb = () -> {
            uiLoading.setVisibility(View.GONE);
            uiNoWeb.setVisibility(View.VISIBLE);
            uiNothing.setVisibility(View.GONE);
        };

        runnableNothing = () -> {
            uiLoading.setVisibility(View.GONE);
            uiNoWeb.setVisibility(View.GONE);
            uiNothing.setVisibility(View.VISIBLE);
        };

        runnableMore = () -> listVideoAdapter.notifyDataSetChanged();

        runnableMoreNoWeb = () -> {
            ((TextView) layoutLoading.findViewById(R.id.wid_load_button)).setText("好像没有网络...\n检查下网络？");
            layoutLoading.findViewById(R.id.wid_load_button).setVisibility(View.VISIBLE);
        };

        runnableMoreNothing = () -> ((TextView) layoutLoading.findViewById(R.id.wid_load_text)).setText("  没有更多了...");

        layoutLoading.findViewById(R.id.wid_load_button).setOnClickListener(v -> {
            ((TextView) layoutLoading.findViewById(R.id.wid_load_button)).setText("  加载中...");
            layoutLoading.findViewById(R.id.wid_load_button).setVisibility(View.GONE);
            getMoreVideo();
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
                if(visibleItemCount + firstVisibleItem == totalItemCount && !isLoading)
                {
                    isLoading = true;
                    getMoreVideo();
                }
            }
        });

        uiListView.setOnTouchListener(new ListViewTouchListener(uiListView, titleViewListener));

        new Thread(() -> {
            try
            {
                ArrayList<ListVideoModel> v = userApi.getUserVideo(page);
                isLoading = false;
                if(v != null && v.size() != 0)
                {
                    listVideoModelArrayList.addAll(v);
                    handler.post(runnableUi);
                }
                else
                    handler.post(runnableNothing);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                handler.post(runnableNoWeb);
            }
            catch (NullPointerException e)
            {
                e.printStackTrace();
                handler.post(runnableNothing);
            }
        }).start();

        return rootLayout;
    }

    private void getMoreVideo()
    {
        new Thread(() -> {
            try
            {
                page++;
                ArrayList<ListVideoModel> v = userApi.getUserVideo(page);
                isLoading = false;
                if(v != null && v.size() != 0)
                {
                    listVideoModelArrayList.addAll(v);
                    handler.post(runnableMore);
                }
                else
                    handler.post(runnableMoreNothing);
            }
            catch (IOException | NullPointerException e)
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
