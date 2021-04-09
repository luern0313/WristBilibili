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
import cn.luern0313.wristbilibili.util.ViewTouchListener;
import cn.luern0313.wristbilibili.widget.ExceptionHandlerView;
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
    private ExceptionHandlerView exceptionHandlerView;
    private ListView listView;

    private final Handler handler = new Handler();
    private Runnable runnableUi, runnableMore, runnableMoreNoWeb, runnableMoreNothing;

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

                Intent intent = new Intent(ctx, VideoActivity.class);
                intent.putExtra(VideoActivity.ARG_BVID, listVideoModelArrayList.get(position).getBvid());
                startActivity(intent);
            }

            @Override
            public void onListVideoAdapterLongClick(int viewId, int position)
            {

            }
        };

        layoutLoading = inflater.inflate(R.layout.widget_loading, null, false);
        exceptionHandlerView = rootLayout.findViewById(R.id.user_video_exception);
        listView = rootLayout.findViewById(R.id.user_video_listview);

        runnableUi = () -> {
            exceptionHandlerView.hideAllView();
            listVideoAdapter = new ListVideoAdapter(inflater, listVideoModelArrayList, false, listView, listVideoAdapterListener);
            listView.setAdapter(listVideoAdapter);
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

        listView.setOnScrollListener(new AbsListView.OnScrollListener()
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

        listView.setOnTouchListener(new ViewTouchListener(listView, titleViewListener));

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
                    exceptionHandlerView.noData();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                exceptionHandlerView.noWeb();
            }
            catch (NullPointerException e)
            {
                e.printStackTrace();
                exceptionHandlerView.noData();
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
