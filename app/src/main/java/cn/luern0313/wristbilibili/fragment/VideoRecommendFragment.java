package cn.luern0313.wristbilibili.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.fragment.app.Fragment;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.VideoRecommendAdapter;
import cn.luern0313.wristbilibili.models.VideoModel;
import cn.luern0313.wristbilibili.ui.VideoActivity;

public class VideoRecommendFragment extends Fragment
{
    private static final String ARG_VIDEO_MODEL = "videoModelArg";

    Context ctx;
    View rootLayout;
    private VideoModel videoModel;

    private ListView uiListView;

    public VideoRecommendFragment() {}

    public static VideoRecommendFragment newInstance(VideoModel videoModel)
    {
        VideoRecommendFragment fragment = new VideoRecommendFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_VIDEO_MODEL, videoModel);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
        {
            videoModel = (VideoModel) getArguments().getSerializable(ARG_VIDEO_MODEL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_video_recommend, container, false);

        uiListView = rootLayout.findViewById(R.id.vd_recommend_listview);
        uiListView.setEmptyView(rootLayout.findViewById(R.id.vd_recommend_nothing));
        uiListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                startActivity(VideoActivity.getActivityIntent(ctx, videoModel.video_recommend_array_list.get(position).video_recommend_video_aid, ""));
            }
        });
        VideoRecommendAdapter recommendAdapter = new VideoRecommendAdapter(inflater, videoModel.video_recommend_array_list, uiListView);
        uiListView.setAdapter(recommendAdapter);

        return rootLayout;
    }

}
