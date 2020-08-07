package cn.luern0313.wristbilibili.fragment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
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

public class VideoDetailFragment extends Fragment implements View.OnClickListener
{
    private static final String ARG_VIDEO_MODEL = "videoModelArg";

    private Context ctx;
    private View rootLayout;
    private VideoDetailFragmentListener videoDetailFragmentListener;

    private VideoModel videoModel;

    private ImageView uiVideoDoLike, uiVideoDoCoin, uiVideoDoFav;
    private CircleProgressView uiVideoCoinProgress, uiVideoFavProgress;
    private VideoPartAdapter videoPartAdapter;
    private VideoPartAdapter.VideoPartListener videoPartListener;
    private AnimatorSet animatorSet, animatorCancelSet, animatorEndSet;
    private CircleProgressView.OnChangeListener onChangeListener;

    private Handler handler = new Handler();
    private Runnable runnableLoadingStart, runnableLoadingFin;

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

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_video_detail, container, false);

        runnableLoadingStart = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    rootLayout.findViewById(R.id.vd_vd_loading).setVisibility(View.VISIBLE);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableLoadingFin = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    rootLayout.findViewById(R.id.vd_vd_loading).setVisibility(View.GONE);
                    setIcon();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        uiVideoDoLike = rootLayout.findViewById(R.id.vd_like_img);
        uiVideoDoCoin = rootLayout.findViewById(R.id.vd_coin_img);
        uiVideoDoFav = rootLayout.findViewById(R.id.vd_fav_img);
        uiVideoCoinProgress = rootLayout.findViewById(R.id.vd_coin_progress);
        uiVideoFavProgress = rootLayout.findViewById(R.id.vd_fav_progress);

        Drawable playDrawable = getResources().getDrawable(R.drawable.icon_number_play);
        Drawable danmakuDrawable = getResources().getDrawable(R.drawable.icon_number_danmu);
        playDrawable.setBounds(0,0, DataProcessUtil.dip2px(11), DataProcessUtil.dip2px(11));
        danmakuDrawable.setBounds(0,0, DataProcessUtil.dip2px(11), DataProcessUtil.dip2px(11));
        ((TextView) rootLayout.findViewById(R.id.vd_video_play)).setCompoundDrawables(playDrawable,null, null,null);
        ((TextView) rootLayout.findViewById(R.id.vd_video_danamku)).setCompoundDrawables(danmakuDrawable,null, null,null);

        ((TextView) rootLayout.findViewById(R.id.vd_video_title)).setText(videoModel.video_title);
        ((TextView) rootLayout.findViewById(R.id.vd_video_play)).setText(videoModel.video_play);
        ((TextView) rootLayout.findViewById(R.id.vd_video_danamku)).setText(videoModel.video_danmaku);
        ((TextView) rootLayout.findViewById(R.id.vd_video_time)).setText(videoModel.video_time);
        if(videoModel.video_aid.equals(""))
            rootLayout.findViewById(R.id.vd_video_aid).setVisibility(View.GONE);
        else
            ((TextView) rootLayout.findViewById(R.id.vd_video_aid)).setText("AV" + videoModel.video_aid);
        if(videoModel.video_bvid.equals(""))
            rootLayout.findViewById(R.id.vd_video_bvid).setVisibility(View.GONE);
        else
            ((TextView) rootLayout.findViewById(R.id.vd_video_bvid)).setText(videoModel.video_bvid);
        if(!videoModel.video_warning.equals(""))
        {
            ((TextView) rootLayout.findViewById(R.id.vd_video_warning_text)).setText(videoModel.video_warning);
            rootLayout.findViewById(R.id.vd_video_warning).setVisibility(View.VISIBLE);
        }

        ((TextView) rootLayout.findViewById(R.id.vd_video_details)).setText(videoModel.video_desc);
        ((TextView) rootLayout.findViewById(R.id.vd_card_name)).setText(videoModel.video_up_name);
        ((TextView) rootLayout.findViewById(R.id.vd_card_sen)).setText("粉丝：" + DataProcessUtil.getView(videoModel.video_up_fan_num));
        if(videoModel.video_up_vip == 2)
            ((TextView) rootLayout.findViewById(R.id.vd_card_name)).setTextColor(ColorUtil.getColor(R.attr.colorBigMember, getContext()));
        if(videoModel.video_up_official == 0)
            rootLayout.findViewById(R.id.vd_card_off_1).setVisibility(View.VISIBLE);
        else if(videoModel.video_up_official == 1)
            rootLayout.findViewById(R.id.vd_card_off_2).setVisibility(View.VISIBLE);

        if(!videoModel.video_season_title.equals(""))
        {
            rootLayout.findViewById(R.id.vd_season).setVisibility(View.VISIBLE);
            Glide.with(ctx).load(videoModel.video_season_cover).skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE).into((ImageView) rootLayout.findViewById(R.id.vd_season_img));
            ((TextView) rootLayout.findViewById(R.id.vd_season_title)).setText(videoModel.video_season_title);
            StringBuilder title = new StringBuilder();
            if(videoModel.video_season_is_finish)
                title.append("已看完，");
            title.append("共").append(videoModel.video_season_new_ep).append("集");
            ((TextView) rootLayout.findViewById(R.id.vd_season_detail)).setText(title);
        }

        Glide.with(ctx).load(videoModel.video_up_face).skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE).into((ImageView) rootLayout.findViewById(R.id.vd_card_head));
        if(videoModel.video_part_array_list.size() > 1)
        {
            rootLayout.findViewById(R.id.vd_video_part_layout).setVisibility(View.VISIBLE);
            rootLayout.findViewById(R.id.vd_bt_play).setVisibility(View.GONE);
            ((TextView) rootLayout.findViewById(R.id.vd_video_part_text)).setText("共" + videoModel.video_part_array_list.size() + "P");
            LinearLayoutManager layoutManager = new LinearLayoutManager(ctx);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            ((RecyclerView) rootLayout.findViewById(R.id.vd_video_part)).setLayoutManager(layoutManager);
            videoPartAdapter = new VideoPartAdapter(videoModel);
            videoPartAdapter.setOnItemClickListener(new VideoPartAdapter.VideoPartListener()
            {
                @Override
                public void onItemClick(View view, int position)
                {
                    videoDetailFragmentListener.onVideoDetailFragmentPartClick(position);
                }
            });
            ((RecyclerView) rootLayout.findViewById(R.id.vd_video_part)).setAdapter(videoPartAdapter);
            if(videoModel.video_user_progress_position != -1)
                layoutManager.scrollToPositionWithOffset(videoModel.video_user_progress_position, 0);
        }
        else rootLayout.findViewById(R.id.vd_video_part_layout).setVisibility(View.GONE);

        rootLayout.findViewById(R.id.vd_like).setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
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
            }
        });

        rootLayout.findViewById(R.id.vd_like).setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                if(!videoModel.video_user_like || !videoModel.video_user_fav || videoModel.video_user_coin == 0)
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
            }
        });

        rootLayout.findViewById(R.id.vd_season).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(ctx, BangumiActivity.class);
                intent.putExtra("season_id", videoModel.video_season_id);
                startActivity(intent);
            }
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

        rootLayout.findViewById(R.id.vd_card_lay).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(ctx, UserActivity.class);
                intent.putExtra("mid", videoModel.video_up_mid);
                startActivity(intent);
            }
        });

        onChangeListener = new CircleProgressView.OnChangeListener()
        {
            @Override
            public void onProgressChanged(float progress, float max)
            {
                if(progress == max && uiVideoCoinProgress.getVisibility() == View.VISIBLE)
                {
                    animatorSet.cancel();
                    tripleAnimCancel();
                    tripleAnimEnd();
                    uiVideoCoinProgress.setVisibility(View.GONE);
                    uiVideoFavProgress.setVisibility(View.GONE);
                    videoDetailFragmentListener.onVideoDetailFragmentTriple();
                }
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
        ((TextView) rootLayout.findViewById(R.id.vd_like_text)).setText(videoModel.video_detail_like == 0 ? "点赞" : DataProcessUtil.getView(videoModel.video_detail_like));
        ((TextView) rootLayout.findViewById(R.id.vd_coin_text)).setText(videoModel.video_detail_coin == 0 ? "投币" : DataProcessUtil.getView(videoModel.video_detail_coin));
        ((TextView) rootLayout.findViewById(R.id.vd_fav_text)).setText(videoModel.video_detail_fav == 0 ? "收藏" : DataProcessUtil.getView(videoModel.video_detail_fav));

        if(videoModel.video_user_follow_up)
            rootLayout.findViewById(R.id.vd_card_follow).setVisibility(View.GONE);

        if(videoModel.video_user_like)
        {
            ((ImageView) rootLayout.findViewById(R.id.vd_like_img)).setImageResource(R.drawable.icon_vdd_do_like_yes_nobg);
            ((ImageView) rootLayout.findViewById(R.id.vd_like_img_bg)).setImageResource(R.drawable.icon_vdd_do_yes_bg);
            ((ImageView) rootLayout.findViewById(R.id.vd_dislike_img)).setImageResource(R.drawable.icon_vdd_do_dislike_no);
        }
        else if(videoModel.video_user_dislike)
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

        if(videoModel.video_user_coin > 0)
        {
            ((ImageView) rootLayout.findViewById(R.id.vd_coin_img)).setImageResource(R.drawable.icon_vdd_do_coin_yes_nobg);
            ((ImageView) rootLayout.findViewById(R.id.vd_coin_img_bg)).setImageResource(R.drawable.icon_vdd_do_yes_bg);
        }
        else
        {
            ((ImageView) rootLayout.findViewById(R.id.vd_coin_img)).setImageResource(R.drawable.icon_vdd_do_coin_no_nobg);
            ((ImageView) rootLayout.findViewById(R.id.vd_coin_img_bg)).setImageResource(R.drawable.icon_vdd_do_no_bg);
        }

        if(videoModel.video_user_fav)
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
    public void onAttach(Context context)
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
