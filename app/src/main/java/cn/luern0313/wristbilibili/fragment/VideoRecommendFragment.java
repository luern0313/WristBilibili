package cn.luern0313.wristbilibili.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.VideoRecommendAdapter;
import cn.luern0313.wristbilibili.models.VideoModel;
import cn.luern0313.wristbilibili.ui.VideoActivity;
import cn.luern0313.wristbilibili.util.ListViewTouchListener;
import cn.luern0313.wristbilibili.widget.TitleView;

public class VideoRecommendFragment extends Fragment
{
    private static final String ARG_VIDEO_MODEL = "videoModelArg";

    Context ctx;
    View rootLayout;
    private VideoModel videoModel;

    private ListView uiListView;
    private TitleView.TitleViewListener titleViewListener;

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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_video_recommend, container, false);

        uiListView = rootLayout.findViewById(R.id.vd_recommend_listview);
        uiListView.setEmptyView(rootLayout.findViewById(R.id.vd_recommend_nothing));
        uiListView.setOnItemClickListener((parent, view, position, id) -> startActivity(VideoActivity.getActivityIntent(ctx, videoModel.getRecommendList().get(position).recommendVideoAid, "")));
        uiListView.setOnTouchListener(new ListViewTouchListener(uiListView, titleViewListener));

        if(videoModel.getRecommendList() != null)
        {
            VideoRecommendAdapter recommendAdapter = new VideoRecommendAdapter(inflater, videoModel.getRecommendList(), uiListView);
            uiListView.setAdapter(recommendAdapter);
        }
        else
            rootLayout.findViewById(R.id.vd_recommend_nothing).setVisibility(View.VISIBLE);

        return rootLayout;
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        if(context instanceof TitleView.TitleViewListener)
            titleViewListener = (TitleView.TitleViewListener) context;
    }
}
