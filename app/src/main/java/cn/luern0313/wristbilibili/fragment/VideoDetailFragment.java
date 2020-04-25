package cn.luern0313.wristbilibili.fragment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

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
import cn.luern0313.wristbilibili.ui.UserActivity;
import cn.luern0313.wristbilibili.ui.VideoActivity;
import cn.luern0313.wristbilibili.util.DataProcessUtil;

public class VideoDetailFragment extends Fragment implements View.OnClickListener
{
    private static final String ARG_VIDEO_MODEL = "videoModelArg";

    Context ctx;
    View rootLayout;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private VideoDetailFragmentListener videoDetailFragmentListener;

    private VideoModel videoModel;

    private ImageView uiVideoDoLike, uiVideoDoCoin, uiVideoDoFav;
    private VideoPartAdapter videoPartAdapter;
    private VideoPartAdapter.VideoPartListener videoPartListener;

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
        EventBus.getDefault().register(this);
        if(getArguments() != null)
        {
            videoModel = (VideoModel) getArguments().getSerializable(ARG_VIDEO_MODEL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_video_detail, container, false);
        sharedPreferences = ctx.getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

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

        Drawable playDrawable = getResources().getDrawable(R.drawable.icon_video_play_num);
        Drawable danmakuDrawable = getResources().getDrawable(R.drawable.icon_video_danmu_num);
        playDrawable.setBounds(0,0, DataProcessUtil.dip2px(ctx, 11), DataProcessUtil.dip2px(ctx, 11));
        danmakuDrawable.setBounds(0,0, DataProcessUtil.dip2px(ctx, 11), DataProcessUtil.dip2px(ctx, 11));
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
        ((TextView) rootLayout.findViewById(R.id.vd_video_details)).setText(videoModel.video_desc);
        ((TextView) rootLayout.findViewById(R.id.vd_card_name)).setText(videoModel.video_up_name);
        ((TextView) rootLayout.findViewById(R.id.vd_card_sen)).setText("粉丝：" + DataProcessUtil.getView(videoModel.video_up_fan_num));
        if(videoModel.video_up_vip == 2)
            ((TextView) rootLayout.findViewById(R.id.vd_card_name)).setTextColor(getResources().getColor(R.color.mainColor));
        if(videoModel.video_up_official == 0)
            rootLayout.findViewById(R.id.vd_card_off_1).setVisibility(View.VISIBLE);
        else if(videoModel.video_up_official == 1)
            rootLayout.findViewById(R.id.vd_card_off_2).setVisibility(View.VISIBLE);


        Glide.with(ctx).load(videoModel.video_up_face).into((ImageView) rootLayout.findViewById(R.id.vd_card_head));
        if(videoModel.video_part_array_list.size() > 1)
        {
            rootLayout.findViewById(R.id.vd_video_part_layout).setVisibility(View.VISIBLE);
            rootLayout.findViewById(R.id.vd_bt_play).setVisibility(View.GONE);
            ((TextView) rootLayout.findViewById(R.id.vd_video_part_text)).setText("共" + videoModel.video_part_array_list.size() + "P");
            LinearLayoutManager layoutManager = new LinearLayoutManager(ctx);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            ((RecyclerView) rootLayout.findViewById(R.id.vd_video_part)).setLayoutManager(layoutManager);
            videoPartAdapter = new VideoPartAdapter(videoModel.video_part_array_list);
            videoPartAdapter.setOnItemClickListener(new VideoPartAdapter.VideoPartListener()
            {
                @Override
                public void onItemClick(View view, int position)
                {
                    videoDetailFragmentListener.onVideoDetailFragmentPartClick(position);
                }
            });
            ((RecyclerView) rootLayout.findViewById(R.id.vd_video_part)).setAdapter(videoPartAdapter);
        }
        else rootLayout.findViewById(R.id.vd_video_part_layout).setVisibility(View.GONE);

        rootLayout.findViewById(R.id.vd_like).setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                switch (motionEvent.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        tripleAnim();
                        break;
                    case MotionEvent.ACTION_UP:
                            //view.performClick(); //TODO ？？？？
                    case MotionEvent.ACTION_CANCEL:
                        break;
                }
                return false;
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
        setIcon();
        return rootLayout;
    }

    private void tripleAnim()
    {
        AnimatorSet animatorSet = new AnimatorSet();
        ArrayList<ObjectAnimator> objectAnimatorArrayList = new ArrayList<>();
        Random r = new Random();
        float translationX = 0f, translationY = 0f, rotation = 0f, scaleX = 1f, scaleY = 1f;
        for(int i = 0; i < 60; i++)
        {
            float tX = DataProcessUtil.getFloatRandom(r, -3, 3);
            float tY = DataProcessUtil.getFloatRandom(r, -3, 3);
            float ro = DataProcessUtil.getFloatRandom(r, -3, 3);
            float sX = DataProcessUtil.getFloatRandom(r, 0.97f, 1.03f);
            float sY = DataProcessUtil.getFloatRandom(r, 0.97f, 1.03f);
            objectAnimatorArrayList.add(0, ObjectAnimator.ofFloat(uiVideoDoCoin, "translationX", translationX, tX));
            objectAnimatorArrayList.add(0, ObjectAnimator.ofFloat(uiVideoDoCoin, "translationY", translationY, tY));
            objectAnimatorArrayList.add(0, ObjectAnimator.ofFloat(uiVideoDoCoin, "rotation", rotation, ro));
            objectAnimatorArrayList.add(0, ObjectAnimator.ofFloat(uiVideoDoCoin, "scaleX", scaleX, sX));
            objectAnimatorArrayList.add(0, ObjectAnimator.ofFloat(uiVideoDoCoin, "scaleY", scaleY, sY));
            if(i == 0)
                animatorSet.play(objectAnimatorArrayList.get(0)).with(objectAnimatorArrayList.get(1)).with(objectAnimatorArrayList.get(2))
                        .with(objectAnimatorArrayList.get(3)).with(objectAnimatorArrayList.get(4));
            else
                animatorSet.play(objectAnimatorArrayList.get(0)).with(objectAnimatorArrayList.get(1)).with(objectAnimatorArrayList.get(2))
                        .with(objectAnimatorArrayList.get(3)).with(objectAnimatorArrayList.get(4)).after(objectAnimatorArrayList.get(5));
            translationX = tX;
            translationY = tY;
            rotation = ro;
            scaleX = sX;
            scaleY = sY;
        }
        animatorSet.setDuration(22);
        animatorSet.start();
    }

    private void setIcon()
    {
        ((TextView) rootLayout.findViewById(R.id.vd_like_text)).setText(videoModel.video_detail_like == 0 ? "点赞" : DataProcessUtil.getView(videoModel.video_detail_like));
        ((TextView) rootLayout.findViewById(R.id.vd_coin_text)).setText(videoModel.video_detail_coin == 0 ? "投币" : DataProcessUtil.getView(videoModel.video_detail_coin));
        ((TextView) rootLayout.findViewById(R.id.vd_fav_text)).setText(videoModel.video_detail_fav == 0 ? "收藏" : DataProcessUtil.getView(videoModel.video_detail_fav));

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
    public void onDestroy()
    {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public interface VideoDetailFragmentListener
    {
        void onVideoDetailFragmentViewClick(int viewId);
        void onVideoDetailFragmentPartClick(int position);
    }
}
