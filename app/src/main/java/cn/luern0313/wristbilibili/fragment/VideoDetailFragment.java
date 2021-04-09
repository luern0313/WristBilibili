package cn.luern0313.wristbilibili.fragment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.king.view.circleprogressview.CircleProgressView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Random;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.VideoPartAdapter;
import cn.luern0313.wristbilibili.models.VideoModel;
import cn.luern0313.wristbilibili.ui.BangumiActivity;
import cn.luern0313.wristbilibili.ui.UserActivity;
import cn.luern0313.wristbilibili.ui.VideoActivity;
import cn.luern0313.wristbilibili.util.ColorUtil;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.ViewTouchListener;
import cn.luern0313.wristbilibili.widget.CircleButtonView;
import cn.luern0313.wristbilibili.widget.ExceptionHandlerView;
import cn.luern0313.wristbilibili.widget.TitleView;

public class VideoDetailFragment extends Fragment implements View.OnClickListener
{
    private static final String ARG_VIDEO_MODEL = "videoModelArg";

    private Context ctx;
    private View rootLayout;
    private ExceptionHandlerView exceptionHandlerView;
    private VideoDetailFragmentListener videoDetailFragmentListener;
    private TitleView.TitleViewListener titleViewListener;

    private VideoModel videoModel;

    private CircleButtonView uiVideoDoLike, uiVideoDoCoin, uiVideoDoFav;
    private VideoPartAdapter videoPartAdapter;
    private AnimatorSet animatorSet, animatorCancelSet, animatorEndSet;
    private CircleProgressView.OnChangeListener onChangeListener;

    public VideoDetailFragment() {}

    public static VideoDetailFragment newInstance(VideoModel videoModel)
    {
        VideoDetailFragment fragment = new VideoDetailFragment();
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
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_video_detail, container, false);

        exceptionHandlerView = rootLayout.findViewById(R.id.vd_exception);
        uiVideoDoLike = rootLayout.findViewById(R.id.vd_like);
        uiVideoDoCoin = rootLayout.findViewById(R.id.vd_coin);
        uiVideoDoFav = rootLayout.findViewById(R.id.vd_fav);

        Drawable playDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.icon_number_play, null);
        Drawable danmakuDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.icon_number_danmu, null);
        playDrawable.setBounds(0,0, DataProcessUtil.dip2px(11), DataProcessUtil.dip2px(11));
        danmakuDrawable.setBounds(0,0, DataProcessUtil.dip2px(11), DataProcessUtil.dip2px(11));
        ((TextView) rootLayout.findViewById(R.id.vd_video_play)).setCompoundDrawables(playDrawable,null, null,null);
        ((TextView) rootLayout.findViewById(R.id.vd_video_danamku)).setCompoundDrawables(danmakuDrawable,null, null,null);

        ((TextView) rootLayout.findViewById(R.id.vd_video_title)).setText(videoModel.getTitle());
        ((TextView) rootLayout.findViewById(R.id.vd_video_play)).setText(videoModel.getPlay());
        ((TextView) rootLayout.findViewById(R.id.vd_video_danamku)).setText(videoModel.getDanmaku());
        ((TextView) rootLayout.findViewById(R.id.vd_video_time)).setText(videoModel.getTime());
        if(videoModel.getAid().equals(""))
            rootLayout.findViewById(R.id.vd_video_aid).setVisibility(View.GONE);
        else
            ((TextView) rootLayout.findViewById(R.id.vd_video_aid)).setText(String.format(getString(R.string.video_detail_av), videoModel.getAid()));
        if(videoModel.getBvid().equals(""))
            rootLayout.findViewById(R.id.vd_video_bvid).setVisibility(View.GONE);
        else
            ((TextView) rootLayout.findViewById(R.id.vd_video_bvid)).setText(videoModel.getBvid());
        if(!videoModel.getWarning().equals(""))
        {
            ((TextView) rootLayout.findViewById(R.id.vd_video_warning_text)).setText(videoModel.getWarning());
            rootLayout.findViewById(R.id.vd_video_warning).setVisibility(View.VISIBLE);
        }

        ((TextView) rootLayout.findViewById(R.id.vd_video_details)).setText(videoModel.getDesc());
        ((TextView) rootLayout.findViewById(R.id.vd_card_name)).setText(videoModel.getUpName());
        ((TextView) rootLayout.findViewById(R.id.vd_card_sen)).setText(String.format(getString(R.string.video_card_fans), DataProcessUtil.getView(videoModel.getUpFanNum())));
        if(videoModel.getUpVip() == 2)
            ((TextView) rootLayout.findViewById(R.id.vd_card_name)).setTextColor(ColorUtil.getColor(R.attr.colorVip, ctx));
        if(videoModel.getUpOfficial() == 0)
            rootLayout.findViewById(R.id.vd_card_off_1).setVisibility(View.VISIBLE);
        else if(videoModel.getUpOfficial() == 1)
            rootLayout.findViewById(R.id.vd_card_off_2).setVisibility(View.VISIBLE);

        if(videoModel.getSeasonTitle() != null)
        {
            rootLayout.findViewById(R.id.vd_season).setVisibility(View.VISIBLE);
            Glide.with(ctx).load(videoModel.getSeasonCover()).skipMemoryCache(true)
                 .diskCacheStrategy(DiskCacheStrategy.NONE).into((ImageView) rootLayout.findViewById(R.id.vd_season_img));
            ((TextView) rootLayout.findViewById(R.id.vd_season_title)).setText(videoModel.getSeasonTitle());
            StringBuilder title = new StringBuilder();
            if(videoModel.isSeasonIsFinish())
                title.append("已看完，");
            title.append("共").append(videoModel.getSeasonNewEp()).append("集");
            ((TextView) rootLayout.findViewById(R.id.vd_season_detail)).setText(title);
        }

        Glide.with(ctx).load(videoModel.getUpFace()).skipMemoryCache(true)
             .diskCacheStrategy(DiskCacheStrategy.NONE).into((ImageView) rootLayout.findViewById(R.id.vd_card_head));
        if(videoModel.getPartList().size() > 1)
        {
            rootLayout.findViewById(R.id.vd_video_part_layout).setVisibility(View.VISIBLE);
            rootLayout.findViewById(R.id.vd_bt_play).setVisibility(View.GONE);
            ((TextView) rootLayout.findViewById(R.id.vd_video_part_text)).setText(String.format(getString(R.string.video_part_label), videoModel.getPartList().size()));
            LinearLayoutManager layoutManager = new LinearLayoutManager(ctx);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            ((RecyclerView) rootLayout.findViewById(R.id.vd_video_part)).setLayoutManager(layoutManager);
            videoPartAdapter = new VideoPartAdapter(videoModel);
            videoPartAdapter.setOnItemClickListener((view, position) -> videoDetailFragmentListener.onVideoDetailFragmentPartClick(position));
            ((RecyclerView) rootLayout.findViewById(R.id.vd_video_part)).setAdapter(videoPartAdapter);
            if(videoModel.getUserProgressPosition() != -1)
                layoutManager.scrollToPositionWithOffset(videoModel.getUserProgressPosition(), 0);
        }
        else rootLayout.findViewById(R.id.vd_video_part_layout).setVisibility(View.GONE);

        rootLayout.findViewById(R.id.vd_lay).setOnTouchListener(new ViewTouchListener(rootLayout.findViewById(R.id.vd_lay), titleViewListener));

        rootLayout.findViewById(R.id.vd_like).setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction())
            {
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if(animatorSet != null)
                        animatorSet.cancel();
                    tripleAnimCancel();
                    uiVideoCoinProgress.setVisibility(View.GONE);
                    uiVideoFavProgress.setVisibility(View.GONE);
                    break;
            }
            return false;
        });

        rootLayout.findViewById(R.id.vd_like).setOnLongClickListener(v -> {
            if(!videoModel.isUserLike() || !videoModel.isUserFav() || videoModel.getUserCoin() == 0)
            {
                tripleAnim();
                uiVideoCoinProgress.setVisibility(View.VISIBLE);
                uiVideoFavProgress.setVisibility(View.VISIBLE);
                uiVideoCoinProgress.setOnChangeListener(onChangeListener);
                uiVideoCoinProgress.showAnimation(0, 100, 3000);
                uiVideoFavProgress.showAnimation(0, 100, 3000);
            }
            else Toast.makeText(ctx, "已完成三连", Toast.LENGTH_SHORT).show();
            return true;
        });

        rootLayout.findViewById(R.id.vd_season).setOnClickListener(v -> {
            Intent intent = new Intent(ctx, BangumiActivity.class);
            intent.putExtra("season_id", videoModel.getSeasonId());
            startActivity(intent);
        });

        rootLayout.findViewById(R.id.vd_video_part_layout).setOnClickListener(this);
        rootLayout.findViewById(R.id.vd_bt_cover).setOnClickListener(this);
        rootLayout.findViewById(R.id.vd_bt_play).setOnClickListener(this);
        rootLayout.findViewById(R.id.vd_bt_watchlater).setOnClickListener(this);
        rootLayout.findViewById(R.id.vd_bt_download).setOnClickListener(this);
        rootLayout.findViewById(R.id.vd_bt_share).setOnClickListener(this);
        rootLayout.findViewById(R.id.vd_like).setOnClickListener(this);
        rootLayout.findViewById(R.id.vd_coin).setOnClickListener(this);
        rootLayout.findViewById(R.id.vd_fav).setOnClickListener(this);
        rootLayout.findViewById(R.id.vd_dislike).setOnClickListener(this);

        rootLayout.findViewById(R.id.vd_card_lay).setOnClickListener(v -> {
            Intent intent = new Intent(ctx, UserActivity.class);
            intent.putExtra("mid", videoModel.getUpMid());
            startActivity(intent);
        });

        onChangeListener = (progress, max) -> {
            if(progress == max && uiVideoCoinProgress.getVisibility() == View.VISIBLE)
            {
                animatorSet.cancel();
                tripleAnimCancel();
                tripleAnimEnd();
                uiVideoCoinProgress.setVisibility(View.GONE);
                uiVideoFavProgress.setVisibility(View.GONE);
                videoDetailFragmentListener.onVideoDetailFragmentTriple();
            }
        };
        setIcon();
        return rootLayout;
    }

    private void tripleAnim()
    {
        if(animatorSet != null)
            animatorSet.cancel();
        animatorSet = new AnimatorSet();
        ArrayList<ObjectAnimator> objectAnimatorArrayList = new ArrayList<>();
        Random r = new Random();
        float translationX = 0f, translationY = 0f;
        for(int i = 0; i < 200; i++)
        {
            float tX = DataProcessUtil.getFloatRandom(r, -3, 3);
            float tY = DataProcessUtil.getFloatRandom(r, -3, 3);
            objectAnimatorArrayList.add(0, ObjectAnimator.ofFloat(uiVideoDoCoin, "translationX", translationX, tX));
            objectAnimatorArrayList.add(0, ObjectAnimator.ofFloat(uiVideoDoCoin, "translationY", translationY, tY));
            objectAnimatorArrayList.add(0, ObjectAnimator.ofFloat(uiVideoDoFav, "translationX", translationX, tX));
            objectAnimatorArrayList.add(0, ObjectAnimator.ofFloat(uiVideoDoFav, "translationY", translationY, tY));
            if(i == 0)
                animatorSet.play(objectAnimatorArrayList.get(0)).with(objectAnimatorArrayList.get(1)).with(objectAnimatorArrayList.get(2))
                        .with(objectAnimatorArrayList.get(3));
            else
                animatorSet.play(objectAnimatorArrayList.get(0)).with(objectAnimatorArrayList.get(1)).with(objectAnimatorArrayList.get(2))
                        .with(objectAnimatorArrayList.get(3)).after(objectAnimatorArrayList.get(4));
            translationX = tX;
            translationY = tY;
        }
        animatorSet.setDuration(30);
        animatorSet.start();
    }

    private void tripleAnimCancel()
    {
        animatorCancelSet = new AnimatorSet();
        ArrayList<ObjectAnimator> objectAnimatorArrayList = new ArrayList<>();
        objectAnimatorArrayList.add(0, ObjectAnimator.ofFloat(uiVideoDoCoin, "translationX", 0));
        objectAnimatorArrayList.add(0, ObjectAnimator.ofFloat(uiVideoDoCoin, "translationY", 0));
        objectAnimatorArrayList.add(0, ObjectAnimator.ofFloat(uiVideoDoFav, "translationX", 0));
        objectAnimatorArrayList.add(0, ObjectAnimator.ofFloat(uiVideoDoFav, "translationY", 0));
        animatorCancelSet.play(objectAnimatorArrayList.get(0)).with(objectAnimatorArrayList.get(1)).with(objectAnimatorArrayList.get(2))
                    .with(objectAnimatorArrayList.get(3));
        animatorCancelSet.setDuration(30);
        animatorCancelSet.start();
    }

    private void tripleAnimEnd()
    {
        animatorEndSet = new AnimatorSet();
        ArrayList<ObjectAnimator> objectAnimatorArrayList = new ArrayList<>();
        objectAnimatorArrayList.add(0, ObjectAnimator.ofFloat(uiVideoDoCoin, "scaleX", 1.0f, 1.42f, 1.0f));
        objectAnimatorArrayList.add(0, ObjectAnimator.ofFloat(uiVideoDoCoin, "scaleY", 1.0f, 1.42f, 1.0f));
        objectAnimatorArrayList.add(0, ObjectAnimator.ofFloat(uiVideoDoFav, "scaleX", 1.0f, 1.42f, 1.0f));
        objectAnimatorArrayList.add(0, ObjectAnimator.ofFloat(uiVideoDoFav, "scaleY", 1.0f, 1.42f, 1.0f));
        animatorEndSet.play(objectAnimatorArrayList.get(0)).with(objectAnimatorArrayList.get(1)).with(objectAnimatorArrayList.get(2))
                .with(objectAnimatorArrayList.get(3));
        animatorEndSet.setDuration(500);
        animatorEndSet.start();
    }

    private void setIcon()
    {
        ((TextView) rootLayout.findViewById(R.id.vd_like_text)).setText(videoModel.getDetailLike() == 0 ? getString(R.string.video_control_like) : DataProcessUtil.getView(videoModel.getDetailLike()));
        ((TextView) rootLayout.findViewById(R.id.vd_coin_text)).setText(videoModel.getDetailCoin() == 0 ? getString(R.string.video_control_coin) : DataProcessUtil.getView(videoModel.getDetailCoin()));
        ((TextView) rootLayout.findViewById(R.id.vd_fav_text)).setText(videoModel.getDetailFav() == 0 ? getString(R.string.video_control_fav) : DataProcessUtil.getView(videoModel.getDetailFav()));

        if(videoModel.isUserFollowUp())
            rootLayout.findViewById(R.id.vd_card_follow).setVisibility(View.GONE);

        if(videoModel.isUserLike())
        {
            ((ImageView) rootLayout.findViewById(R.id.vd_like_img)).setImageResource(R.drawable.icon_vdd_do_like_yes_nobg);
            ((ImageView) rootLayout.findViewById(R.id.vd_like_img_bg)).setImageResource(R.drawable.icon_vdd_do_yes_bg);
            ((ImageView) rootLayout.findViewById(R.id.vd_dislike_img)).setImageResource(R.drawable.icon_vdd_do_dislike_no);
        }
        else if(videoModel.isUserDislike())
        {
            ((ImageView) rootLayout.findViewById(R.id.vd_like_img)).setImageResource(R.drawable.icon_vdd_do_like_no_nobg);
            ((ImageView) rootLayout.findViewById(R.id.vd_like_img_bg)).setImageResource(R.drawable.icon_vdd_do_no_bg);
            ((ImageView) rootLayout.findViewById(R.id.vd_dislike_img)).setImageResource(R.drawable.icon_vdd_do_dislike_yes);
        }
        else
        {
            ((ImageView) rootLayout.findViewById(R.id.vd_like_img)).setImageResource(R.drawable.icon_vdd_do_like_no_nobg);
            ((ImageView) rootLayout.findViewById(R.id.vd_like_img_bg)).setImageResource(R.drawable.icon_vdd_do_no_bg);
            ((ImageView) rootLayout.findViewById(R.id.vd_dislike_img)).setImageResource(R.drawable.icon_vdd_do_dislike_no);
        }

        if(videoModel.getUserCoin() > 0)
        {
            ((ImageView) rootLayout.findViewById(R.id.vd_coin_img)).setImageResource(R.drawable.icon_vdd_do_coin_yes_nobg);
            ((ImageView) rootLayout.findViewById(R.id.vd_coin_img_bg)).setImageResource(R.drawable.icon_vdd_do_yes_bg);
        }
        else
        {
            ((ImageView) rootLayout.findViewById(R.id.vd_coin_img)).setImageResource(R.drawable.icon_vdd_do_coin_no_nobg);
            ((ImageView) rootLayout.findViewById(R.id.vd_coin_img_bg)).setImageResource(R.drawable.icon_vdd_do_no_bg);
        }

        if(videoModel.isUserFav())
        {
            ((ImageView) rootLayout.findViewById(R.id.vd_fav_img)).setImageResource(R.drawable.icon_vdd_do_fav_yes_nobg);
            ((ImageView) rootLayout.findViewById(R.id.vd_fav_img_bg)).setImageResource(R.drawable.icon_vdd_do_yes_bg);
        }
        else
        {
            ((ImageView) rootLayout.findViewById(R.id.vd_fav_img)).setImageResource(R.drawable.icon_vdd_do_fav_no_nobg);
            ((ImageView) rootLayout.findViewById(R.id.vd_fav_img_bg)).setImageResource(R.drawable.icon_vdd_do_no_bg);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(VideoModel videoModel)
    {
        this.videoModel = videoModel;
        rootLayout.findViewById(R.id.vd_vd_loading).setVisibility(View.GONE);
        setIcon();
        videoPartAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(@Nullable Context context)
    {
        super.onAttach(context);
        if(context instanceof VideoDetailFragmentListener)
        {
            videoDetailFragmentListener = (VideoDetailFragmentListener) context;
            ((VideoActivity) getActivity()).setVideoDetailActivityListener(new VideoActivity.VideoDetailActivityListener() {
                @Override
                public void onVideoDetailActivityLoadingStart()
                {
                    handler.post(runnableLoadingStart);
                }

                @Override
                public void onVideoDetailActivityLoadingFin()
                {
                    handler.post(runnableLoadingFin);
                }
            });
        }
        else
        {
            throw new RuntimeException(context.toString() + " must implement VideoDetailFragmentListener");
        }

        if(context instanceof TitleView.TitleViewListener)
            titleViewListener = (TitleView.TitleViewListener) context;
    }

    @Override
    public void onClick(View v)
    {
        videoDetailFragmentListener.onVideoDetailFragmentViewClick(v.getId());
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        videoDetailFragmentListener = null;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    public interface VideoDetailFragmentListener
    {
        void onVideoDetailFragmentViewClick(int viewId);
        void onVideoDetailFragmentPartClick(int position);
        void onVideoDetailFragmentTriple();
    }
}
