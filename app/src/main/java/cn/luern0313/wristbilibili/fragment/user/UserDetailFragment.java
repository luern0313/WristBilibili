package cn.luern0313.wristbilibili.fragment.user;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import cn.luern0313.wristbilibili.util.ColorUtil;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;

public class UserDetailFragment extends Fragment implements View.OnClickListener
{
    private static final String ARG_DETAIL_MODEL = "argDetailModel";

    private Context ctx;
    private View rootLayout;

    private UserModel userModel;

    private UserDetailFragmentListener userDetailFragmentListener;

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

        Glide.with(ctx).load(userModel.user_card_face).skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE).into((ImageView) rootLayout.findViewById(R.id.user_detail_head));
        if(userModel.user_card_nameplate_img != null && !userModel.user_card_nameplate_img.equals(""))
            Glide.with(ctx).load(userModel.user_card_nameplate_img).skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE).into((ImageView) rootLayout.findViewById(R.id.user_detail_nameplate));
        else
            rootLayout.findViewById(R.id.user_detail_nameplate).setVisibility(View.GONE);

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

        rootLayout.findViewById(R.id.user_detail_video).setOnClickListener(this);
        rootLayout.findViewById(R.id.user_detail_favor).setOnClickListener(this);
        rootLayout.findViewById(R.id.user_detail_bangumi).setOnClickListener(this);
        rootLayout.findViewById(R.id.user_detail_howfollow).setOnClickListener(this);
        rootLayout.findViewById(R.id.user_detail_howfans).setOnClickListener(this);

        initView();
        return rootLayout;
    }

    private void initView()
    {
        ((TextView) rootLayout.findViewById(R.id.user_detail_name)).setText(userModel.user_card_name);
        ((TextView) rootLayout.findViewById(R.id.user_detail_lv)).setText("LV" + userModel.user_card_lv);
        ((TextView) rootLayout.findViewById(R.id.user_detail_video)).setText("投稿：" + DataProcessUtil.getView(userModel.user_video_num));

        if(userModel.user_favor_num != 0)
        {
            rootLayout.findViewById(R.id.user_detail_favor).setVisibility(View.VISIBLE);
            ((TextView) rootLayout.findViewById(R.id.user_detail_favor)).setText("收藏：" + DataProcessUtil.getView(userModel.user_favor_num));
        }
        if(userModel.user_bangumi_num != 0)
        {
            rootLayout.findViewById(R.id.user_detail_bangumi).setVisibility(View.VISIBLE);
            ((TextView) rootLayout.findViewById(R.id.user_detail_bangumi)).setText("追番：" + DataProcessUtil.getView(userModel.user_bangumi_num));
        }
        ((TextView) rootLayout.findViewById(R.id.user_detail_howfollow)).setText("关注：" + DataProcessUtil.getView(userModel.user_card_follow_num));
        ((TextView) rootLayout.findViewById(R.id.user_detail_howfans)).setText("粉丝：" + DataProcessUtil.getView(userModel.user_card_fans_num));

        if(userModel.user_card_vip == 2)
        {
            rootLayout.findViewById(R.id.user_detail_vip).setVisibility(View.VISIBLE);
            ((TextView) rootLayout.findViewById(R.id.user_detail_vip)).setText("年度大会员");
            //noinspection ConstantConditions
            ((TextView) rootLayout.findViewById(R.id.user_detail_name)).setTextColor(ColorUtil.getColor(R.attr.colorBigMember, getContext()));
        }
        else if(userModel.user_card_vip == 1)
        {
            rootLayout.findViewById(R.id.user_detail_vip).setVisibility(View.VISIBLE);
            ((TextView) rootLayout.findViewById(R.id.user_detail_vip)).setText("大会员");
        }

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

        if(userModel.user_card_sign != null && !userModel.user_card_sign.equals(""))
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
        if(userModel.user_card_mid.equals(SharedPreferencesUtil.getString(SharedPreferencesUtil.mid, "")))
            rootLayout.findViewById(R.id.user_detail_follow).setVisibility(View.GONE);
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

    @Override
    public void onDetach()
    {
        super.onDetach();
        userDetailFragmentListener = null;
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
