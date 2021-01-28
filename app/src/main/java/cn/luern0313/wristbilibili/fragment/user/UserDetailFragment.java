package cn.luern0313.wristbilibili.fragment.user;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
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
import cn.luern0313.wristbilibili.util.ListViewTouchListener;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;
import cn.luern0313.wristbilibili.widget.TitleView;

public class UserDetailFragment extends Fragment implements View.OnClickListener
{
    private static final String ARG_DETAIL_MODEL = "argDetailModel";

    private Context ctx;
    private View rootLayout;

    private UserModel userModel;

    private UserDetailFragmentListener userDetailFragmentListener;
    private TitleView.TitleViewListener titleViewListener;

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

        Glide.with(ctx).load(userModel.getCardFace()).skipMemoryCache(true)
             .diskCacheStrategy(DiskCacheStrategy.NONE).into((ImageView) rootLayout.findViewById(R.id.user_detail_head));
        if(userModel.getCardNameplateImg() != null && !userModel.getCardNameplateImg().equals(""))
            Glide.with(ctx).load(userModel.getCardNameplateImg()).skipMemoryCache(true)
                 .diskCacheStrategy(DiskCacheStrategy.NONE).into((ImageView) rootLayout.findViewById(R.id.user_detail_nameplate));
        else
            rootLayout.findViewById(R.id.user_detail_nameplate).setVisibility(View.GONE);

        rootLayout.findViewById(R.id.user_detail_follow).setOnClickListener(this);
        rootLayout.findViewById(R.id.user_detail_head).setOnClickListener(v -> {
            Intent intent = new Intent(ctx, ImgActivity.class);
            intent.putExtra("imgUrl", new String[]{userModel.getCardFace()});
            startActivity(intent);
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
        ((TextView) rootLayout.findViewById(R.id.user_detail_name)).setText(userModel.getCardName());
        ((TextView) rootLayout.findViewById(R.id.user_detail_lv)).setText(String.format(getString(R.string.user_card_lv), userModel.getCardLv()));
        ((TextView) rootLayout.findViewById(R.id.user_detail_video)).setText(String.format(getString(R.string.user_card_video_num), DataProcessUtil.getView(userModel.getVideoNum())));

        if(userModel.getFavorNum() != 0)
        {
            rootLayout.findViewById(R.id.user_detail_favor).setVisibility(View.VISIBLE);
            ((TextView) rootLayout.findViewById(R.id.user_detail_favor)).setText(String.format(getString(R.string.user_card_favor_num), DataProcessUtil.getView(userModel.getFavorNum())));
        }
        if(userModel.getBangumiNum() != 0)
        {
            rootLayout.findViewById(R.id.user_detail_bangumi).setVisibility(View.VISIBLE);
            ((TextView) rootLayout.findViewById(R.id.user_detail_bangumi)).setText(String.format(getString(R.string.user_card_bangumi_num), DataProcessUtil.getView(userModel.getBangumiNum())));
        }
        ((TextView) rootLayout.findViewById(R.id.user_detail_howfollow)).setText(String.format(getString(R.string.user_card_follow_num), DataProcessUtil.getView(userModel.getCardFollowNum())));
        ((TextView) rootLayout.findViewById(R.id.user_detail_howfans)).setText(String.format(getString(R.string.user_card_fans_num), DataProcessUtil.getView(userModel.getCardFansNum())));

        if(userModel.getCardVip() == 2)
        {
            rootLayout.findViewById(R.id.user_detail_vip).setVisibility(View.VISIBLE);
            ((TextView) rootLayout.findViewById(R.id.user_detail_vip)).setText(getString(R.string.vip_annual));
            ((TextView) rootLayout.findViewById(R.id.user_detail_name)).setTextColor(ColorUtil.getColor(R.attr.colorVip, getContext()));
        }
        else if(userModel.getCardVip() == 1)
        {
            rootLayout.findViewById(R.id.user_detail_vip).setVisibility(View.VISIBLE);
            ((TextView) rootLayout.findViewById(R.id.user_detail_vip)).setText(getString(R.string.vip_normal));
        }

        if(userModel.getCardOfficialType() == 0)
        {
            rootLayout.findViewById(R.id.user_detail_official_1).setVisibility(View.VISIBLE);
            rootLayout.findViewById(R.id.user_detail_official).setVisibility(View.VISIBLE);
            ((TextView) rootLayout.findViewById(R.id.user_detail_official)).setText(userModel.getCardOfficialVerify());
        }
        else if(userModel.getCardOfficialType() == 1)
        {
            rootLayout.findViewById(R.id.user_detail_official_2).setVisibility(View.VISIBLE);
            rootLayout.findViewById(R.id.user_detail_official).setVisibility(View.VISIBLE);
            ((TextView) rootLayout.findViewById(R.id.user_detail_official)).setText(userModel.getCardOfficialVerify());
        }

        if(userModel.getCardSign() != null && !userModel.getCardSign().equals(""))
        {
            rootLayout.findViewById(R.id.user_detail_sign).setVisibility(View.VISIBLE);
            ((TextView) rootLayout.findViewById(R.id.user_detail_sign)).setText(userModel.getCardSign());
        }

        if(userModel.isUserFollow())
        {
            ((TextView) rootLayout.findViewById(R.id.user_detail_follow)).setText(getString(R.string.user_follow_cancel));
            rootLayout.findViewById(R.id.user_detail_follow).setBackgroundResource(R.drawable.shape_bg_anre_followbgyes);
        }
        else
        {
            ((TextView) rootLayout.findViewById(R.id.user_detail_follow)).setText(getString(R.string.user_follow_follow));
            rootLayout.findViewById(R.id.user_detail_follow).setBackgroundResource(R.drawable.shape_bg_anre_followbg);
        }
        if(userModel.getCardMid().equals(SharedPreferencesUtil.getString(SharedPreferencesUtil.mid, "")))
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
