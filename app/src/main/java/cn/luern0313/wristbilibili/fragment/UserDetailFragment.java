package cn.luern0313.wristbilibili.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.fragment.app.Fragment;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.models.UserModel;
import cn.luern0313.wristbilibili.ui.ImgActivity;
import cn.luern0313.wristbilibili.util.DataProcessUtil;

public class UserDetailFragment extends Fragment implements View.OnClickListener
{
    private static final String ARG_DETAIL_MODEL = "argDetailModel";

    Context ctx;
    View rootLayout;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private UserModel userModel;

    private UserDetailFragmentListener userDetailFragmentListener;

    private Handler handler = new Handler();
    private Runnable runnableLoadingStart;

    public UserDetailFragment() { }

    public static UserDetailFragment newInstance(UserModel userModel)
    {
        UserDetailFragment fragment = new UserDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DETAIL_MODEL, userModel);
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
            userModel = (UserModel) getArguments().getSerializable(ARG_DETAIL_MODEL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_user_detail, container, false);
        sharedPreferences = ctx.getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        Glide.with(ctx).load(userModel.user_card_face).skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE).into((ImageView) rootLayout.findViewById(R.id.user_detail_head));
        if(userModel.user_card_nameplate_img != null)
            Glide.with(ctx).load(userModel.user_card_nameplate_img).skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE).into((ImageView) rootLayout.findViewById(R.id.user_detail_nameplate));

        rootLayout.findViewById(R.id.user_detail_follow).setOnClickListener(this);
        rootLayout.findViewById(R.id.user_detail_head).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(ctx, ImgActivity.class);
                intent.putExtra("imgUrl", new String[]{userModel.user_card_face});
                startActivity(intent);
            }
        });

        initView();
        return rootLayout;
    }

    private void initView()
    {
        ((TextView) rootLayout.findViewById(R.id.user_detail_name)).setText(userModel.user_card_name);
        ((TextView) rootLayout.findViewById(R.id.user_detail_lv)).setText("LV" + userModel.user_card_lv);
        ((TextView) rootLayout.findViewById(R.id.user_detail_howfollow)).setText("关注：" + DataProcessUtil.getView(userModel.user_card_follow_num));
        ((TextView) rootLayout.findViewById(R.id.user_detail_howfans)).setText("粉丝：" + DataProcessUtil.getView(userModel.user_card_fans_num));
        ((TextView) rootLayout.findViewById(R.id.user_detail_other)).setText("投稿：" + DataProcessUtil.getView(userModel.user_video_num));

        if(userModel.user_card_vip == 2)
            ((TextView) rootLayout.findViewById(R.id.user_detail_name)).setTextColor(getResources().getColor(R.color.mainColor));
        if(userModel.user_card_official_type == 0)
        {
            rootLayout.findViewById(R.id.user_detail_official_1).setVisibility(View.VISIBLE);
            rootLayout.findViewById(R.id.user_detail_official).setVisibility(View.VISIBLE);
            ((TextView) rootLayout.findViewById(R.id.user_detail_official)).setText(userModel.user_card_official_verify);
        }
        else if(userModel.user_card_official_type == 1)
        {
            rootLayout.findViewById(R.id.user_detail_official_2).setVisibility(View.VISIBLE);
            rootLayout.findViewById(R.id.user_detail_official).setVisibility(View.VISIBLE);
            ((TextView) rootLayout.findViewById(R.id.user_detail_official)).setText(userModel.user_card_official_verify);
        }

        if(userModel.user_card_sign != null)
        {
            rootLayout.findViewById(R.id.user_detail_sign).setVisibility(View.VISIBLE);
            ((TextView) rootLayout.findViewById(R.id.user_detail_sign)).setText(userModel.user_card_sign);
        }

        if(userModel.user_user_follow)
        {
            ((TextView) rootLayout.findViewById(R.id.user_detail_follow)).setText("已关注");
            rootLayout.findViewById(R.id.user_detail_follow).setBackgroundResource(R.drawable.shape_anre_followbgyes);
        }
        else
        {
            ((TextView) rootLayout.findViewById(R.id.user_detail_follow)).setText("+关注");
            rootLayout.findViewById(R.id.user_detail_follow).setBackgroundResource(R.drawable.shape_anre_followbg);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(UserModel userModel)
    {
        this.userModel = userModel;
        initView();
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if(context instanceof UserDetailFragmentListener)
        {
            userDetailFragmentListener = (UserDetailFragmentListener) context;
        }
        else
        {
            throw new RuntimeException(context.toString() + " must implement UserDetailFragmentListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        userDetailFragmentListener = null;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onClick(View v)
    {
        userDetailFragmentListener.onUserDetailFragmentViewClick(v.getId());
    }

    public interface UserDetailFragmentListener
    {
        void onUserDetailFragmentViewClick(int viewId);
    }
}
