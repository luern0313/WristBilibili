package cn.luern0313.wristbilibili.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import cn.luern0313.wristbilibili.util.ViewTouchListener;
import cn.luern0313.wristbilibili.widget.ExceptionHandlerView;
import cn.luern0313.wristbilibili.widget.TitleView;

public class VideoRecommendFragment extends Fragment
{
    private static final String ARG_VIDEO_MODEL = "videoModelArg";

    private Context ctx;
    private View rootLayout;
    private VideoModel videoModel;

    private ExceptionHandlerView exceptionHandlerView;
    private ListView listView;
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

        exceptionHandlerView = rootLayout.findViewById(R.id.vd_recommend_exception);
        listView = rootLayout.findViewById(R.id.vd_recommend_listview);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(ctx, VideoActivity.class);
            intent.putExtra(VideoActivity.ARG_AID, videoModel.getRecommendList().get(position).getRecommendVideoAid());
            startActivity(intent);
        });
        listView.setOnTouchListener(new ViewTouchListener(listView, titleViewListener));

        if(videoModel.getRecommendList() != null && videoModel.getRecommendList().size() > 0)
        {
            VideoRecommendAdapter recommendAdapter = new VideoRecommendAdapter(inflater, videoModel.getRecommendList(), listView);
            listView.setAdapter(recommendAdapter);
        }
        else
            exceptionHandlerView.noData();

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
