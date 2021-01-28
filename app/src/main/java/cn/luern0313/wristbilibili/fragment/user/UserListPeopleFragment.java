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
import cn.luern0313.wristbilibili.adapter.UserListPeopleAdapter;
import cn.luern0313.wristbilibili.api.UserApi;
import cn.luern0313.wristbilibili.models.UserListPeopleModel;
import cn.luern0313.wristbilibili.ui.UserActivity;
import cn.luern0313.wristbilibili.util.ListViewTouchListener;
import cn.luern0313.wristbilibili.widget.TitleView;

public class UserListPeopleFragment extends Fragment
{
    private static final String ARG_LIST_PEOPLE_MID = "argListPeopleMid";
    private static final String ARG_LIST_PEOPLE_MODE = "argListPeopleMode";

    private Context ctx;
    private View rootLayout;
    private String mid;
    private int mode;
    private UserApi userApi;
    private final ArrayList<UserListPeopleModel> userListPeopleModelArrayList = new ArrayList<>();
    private int page = 1;

    private UserListPeopleAdapter userListPeopleAdapter;
    private UserListPeopleAdapter.UserListPeopleAdapterListener userListPeopleAdapterListener;
    private TitleView.TitleViewListener titleViewListener;

    private final Handler handler = new Handler();
    private Runnable runnableUi, runnableNoWeb, runnableNothing, runnableMore, runnableMoreNoWeb, runnableMoreNothing;

    private View layoutLoadingMore;
    private ListView uiListView;

    private boolean isLoading = true;

    public UserListPeopleFragment() {}

    public static UserListPeopleFragment newInstance(String mid, int mode)
    {
        UserListPeopleFragment fragment = new UserListPeopleFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LIST_PEOPLE_MID, mid);
        args.putInt(ARG_LIST_PEOPLE_MODE, mode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
        {
            mid = getArguments().getString(ARG_LIST_PEOPLE_MID);
            mode = getArguments().getInt(ARG_LIST_PEOPLE_MODE);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_user_list_people, container, false);

        userApi = new UserApi(mid);
        userListPeopleAdapterListener = (viewId, position) -> {
            Intent intent = new Intent(ctx, UserActivity.class);
            intent.putExtra("mid", userListPeopleModelArrayList.get(position).getUid());
            startActivity(intent);
        };

        layoutLoadingMore = inflater.inflate(R.layout.widget_loading, null);
        uiListView = rootLayout.findViewById(R.id.user_list_people_listview);
        uiListView.addFooterView(layoutLoadingMore);

        runnableUi = () -> {
            isLoading = false;
            rootLayout.findViewById(R.id.user_list_people_no_web).setVisibility(View.GONE);
            rootLayout.findViewById(R.id.user_list_people_nothing).setVisibility(View.GONE);
            userListPeopleAdapter = new UserListPeopleAdapter(inflater, userListPeopleModelArrayList, mode, uiListView, userListPeopleAdapterListener);
            uiListView.setAdapter(userListPeopleAdapter);
        };

        runnableNoWeb = () -> {
            rootLayout.findViewById(R.id.user_list_people_no_web).setVisibility(View.VISIBLE);
            rootLayout.findViewById(R.id.user_list_people_nothing).setVisibility(View.GONE);
        };

        runnableNothing = () -> {
            rootLayout.findViewById(R.id.user_list_people_no_web).setVisibility(View.GONE);
            rootLayout.findViewById(R.id.user_list_people_nothing).setVisibility(View.VISIBLE);
        };

        runnableMore = () -> userListPeopleAdapter.notifyDataSetChanged();

        runnableMoreNoWeb = () -> {
            ((TextView) layoutLoadingMore.findViewById(R.id.wid_load_text)).setText("好像没有网络...\n检查下网络？");
            layoutLoadingMore.findViewById(R.id.wid_load_button).setVisibility(View.VISIBLE);
        };

        runnableMoreNothing = () -> ((TextView) layoutLoadingMore.findViewById(R.id.wid_load_text)).setText("  没有更多了...");

        layoutLoadingMore.findViewById(R.id.wid_load_button).setOnClickListener(v -> {
            ((TextView) layoutLoadingMore.findViewById(R.id.wid_load_button)).setText("  加载中...");
            layoutLoadingMore.findViewById(R.id.wid_load_button).setVisibility(View.GONE);
            getMoreListPeople();
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
                    getMoreListPeople();
                }
            }
        });

        new Thread(() -> {
            try
            {
                ArrayList<UserListPeopleModel> p;
                if(mode == 0)
                    p = userApi.getUserFollow(page);
                else
                    p = userApi.getUserFans(page);
                isLoading = false;

                if(p != null && p.size() != 0)
                {
                    userListPeopleModelArrayList.addAll(p);
                    handler.post(runnableUi);
                }
                else
                    handler.post(runnableNothing);
            }
            catch (RuntimeException | IOException e)
            {
                e.printStackTrace();
                handler.post(runnableNoWeb);
            }
        }).start();

        return rootLayout;
    }

    private void getMoreListPeople()
    {
        new Thread(() -> {
            try
            {
                page++;
                ArrayList<UserListPeopleModel> p;
                if(mode == 0)
                    p = userApi.getUserFollow(page);
                else
                    p = userApi.getUserFans(page);
                isLoading = false;

                if(p != null && p.size() != 0)
                {
                    userListPeopleModelArrayList.addAll(p);
                    handler.post(runnableMore);
                }
                else
                    handler.post(runnableMoreNothing);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                handler.post(runnableMoreNoWeb);
            }
        }).start();
    }
}
