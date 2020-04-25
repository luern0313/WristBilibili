package cn.luern0313.wristbilibili.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;

import androidx.fragment.app.Fragment;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.UserListPeopleAdapter;
import cn.luern0313.wristbilibili.api.UserApi;
import cn.luern0313.wristbilibili.models.UserListPeopleModel;

public class UserListPeopleFragment extends Fragment
{
    private static final String ARG_LIST_PEOPLE_MID = "argListPeopleMid";
    private static final String ARG_LIST_PEOPLE_MODE = "argListPeopleMode";

    Context ctx;
    View rootLayout;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String mid;
    private int mode;
    private UserApi userApi;
    private UserListPeopleModel userListPeopleModel;

    private UserListPeopleAdapter userListPeopleAdapter;
    private UserListPeopleAdapter.UserListPeopleAdapterListener userListPeopleAdapterListener;

    private Handler handler = new Handler();
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

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_user_list_people, container, false);
        sharedPreferences = ctx.getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        String cookie = sharedPreferences.getString("cookies", "");
        String csrf = sharedPreferences.getString("csrf", "");
        String access_key = sharedPreferences.getString("access_key", "");
        userApi = new UserApi(cookie, csrf, access_key, mid);
        userListPeopleModel = userApi.userListPeopleModel;
        userListPeopleAdapterListener = new UserListPeopleAdapter.UserListPeopleAdapterListener()
        {
            @Override
            public void onClick(int viewId, int position)
            {

            }
        };

        layoutLoadingMore = inflater.inflate(R.layout.widget_loading, null);
        uiListView = rootLayout.findViewById(R.id.user_list_people_listview);
        uiListView.addFooterView(layoutLoadingMore);

        runnableUi = new Runnable()
        {
            @Override
            public void run()
            {
                isLoading = false;
                rootLayout.findViewById(R.id.user_list_people_no_web).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.user_list_people_nothing).setVisibility(View.GONE);
                userListPeopleAdapter = new UserListPeopleAdapter(inflater, userListPeopleModel.userListPeoplePeopleModelArrayList, mode, uiListView, userListPeopleAdapterListener);
                uiListView.setAdapter(userListPeopleAdapter);
            }
        };

        runnableNoWeb = new Runnable()
        {
            @Override
            public void run()
            {
                rootLayout.findViewById(R.id.user_list_people_no_web).setVisibility(View.VISIBLE);
                rootLayout.findViewById(R.id.user_list_people_nothing).setVisibility(View.GONE);
            }
        };

        runnableNothing = new Runnable()
        {
            @Override
            public void run()
            {
                rootLayout.findViewById(R.id.user_list_people_no_web).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.user_list_people_nothing).setVisibility(View.VISIBLE);
            }
        };

        runnableMore = new Runnable()
        {
            @Override
            public void run()
            {
                userListPeopleAdapter.notifyDataSetChanged();
            }
        };

        runnableMoreNoWeb = new Runnable()
        {
            @Override
            public void run()
            {
                ((TextView) layoutLoadingMore.findViewById(R.id.wid_load_text)).setText("好像没有网络...\n检查下网络？");
                layoutLoadingMore.findViewById(R.id.wid_load_button).setVisibility(View.VISIBLE);
            }
        };

        runnableMoreNothing = new Runnable()
        {
            @Override
            public void run()
            {
                ((TextView) layoutLoadingMore.findViewById(R.id.wid_load_text)).setText("  没有更多了...");
            }
        };

        layoutLoadingMore.findViewById(R.id.wid_load_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((TextView) layoutLoadingMore.findViewById(R.id.wid_load_button)).setText("  加载中...");
                layoutLoadingMore.findViewById(R.id.wid_load_button).setVisibility(View.GONE);
                getMoreListPeople();
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
                if(visibleItemCount + firstVisibleItem == totalItemCount && !isLoading)
                {
                    isLoading = true;
                    getMoreListPeople();
                }
            }
        });

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    int result;
                    if(mode == 0)
                        result = userApi.getUserFollow();
                    else
                        result = userApi.getUserFans();
                    isLoading = false;

                    if(result == 0)
                        handler.post(runnableUi);
                    else
                        handler.post(runnableNothing);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    handler.post(runnableNoWeb);
                }
            }
        }).start();

        return rootLayout;
    }

    private void getMoreListPeople()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    int result;
                    if(mode == 0)
                        result = userApi.getUserFollow();
                    else
                        result = userApi.getUserFans();
                    isLoading = false;

                    if(result == 0)
                        handler.post(runnableMore);
                    else
                        handler.post(runnableMoreNothing);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    handler.post(runnableMoreNoWeb);
                }
            }
        }).start();
    }
}
