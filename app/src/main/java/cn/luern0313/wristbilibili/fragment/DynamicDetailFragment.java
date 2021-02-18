package cn.luern0313.wristbilibili.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.DynamicAdapter;
import cn.luern0313.wristbilibili.models.DynamicModel;
import cn.luern0313.wristbilibili.ui.SendDynamicActivity;
import cn.luern0313.wristbilibili.ui.UnsupportedLinkActivity;
import cn.luern0313.wristbilibili.ui.UserActivity;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.ViewTouchListener;
import cn.luern0313.wristbilibili.util.MyApplication;
import cn.luern0313.wristbilibili.widget.TitleView;

public class DynamicDetailFragment extends Fragment
{
    private static final String ARG_DYNAMIC_MODEL = "argDynamicModel";

    private Context ctx;
    private View rootLayout;
    private DynamicModel.DynamicBaseModel dynamicModel;
    private DynamicAdapter dynamicAdapter;
    private DynamicAdapter.DynamicAdapterListener dynamicAdapterListener;
    private DynamicAdapter.ViewHolder viewHolder;
    private TitleView.TitleViewListener titleViewListener;

    private DynamicDetailFragmentListener dynamicDetailFragmentListener;

    final private int RESULT_DYNAMIC_DETAIL_SEND_DYNAMIC = 101;
    final private int RESULT_DYNAMIC_DETAIL_SHARE = 102;
    final private int RESULT_DYNAMIC_DETAIL_REPLY = 103;

    public DynamicDetailFragment() { }

    public static DynamicDetailFragment newInstance(DynamicModel.DynamicBaseModel dynamicModel)
    {
        DynamicDetailFragment fragment = new DynamicDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DYNAMIC_MODEL, dynamicModel);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
        {
            dynamicModel = (DynamicModel.DynamicBaseModel) getArguments().getSerializable(ARG_DYNAMIC_MODEL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = MyApplication.getContext();
        rootLayout = inflater.inflate(R.layout.fragment_dynamic_detail, container, false);

        WindowManager manager = getActivity().getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        int dynamicWidth = outMetrics.widthPixels - DataProcessUtil.dip2px(18) * 2;

        dynamicAdapterListener = new DynamicAdapter.DynamicAdapterListener()
        {
            @Override
            public void onClick(int viewId, int position, boolean isShared)
            {
                onDynamicClick(viewId, position, isShared);
            }

            @Override
            public boolean onLongClick(int viewId, int position, boolean isShared)
            {
                return false;
            }
        };

        rootLayout.findViewById(R.id.dynamic_detail_lay).setOnTouchListener(new ViewTouchListener(rootLayout.findViewById(R.id.dynamic_detail_lay), titleViewListener));

        rootLayout.findViewById(R.id.item_dynamic_lay).setBackground(null);
        dynamicAdapter = new DynamicAdapter(inflater, null, rootLayout, dynamicWidth, dynamicAdapterListener);
        viewHolder = dynamicAdapter.getViewHolder(dynamicModel, rootLayout, R.id.item_dynamic_dynamic);
        dynamicAdapter.handlerView(viewHolder, dynamicModel, 0, false, true);

        return rootLayout;
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        if(context instanceof DynamicDetailFragmentListener)
            dynamicDetailFragmentListener = (DynamicDetailFragmentListener) context;
        else
            throw new RuntimeException(context.toString() + " must implement DynamicDetailFragmentListener");

        if(context instanceof TitleView.TitleViewListener)
            titleViewListener = (TitleView.TitleViewListener) context;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DynamicModel.DynamicBaseModel dynamicModel)
    {
        this.dynamicModel = dynamicModel;
        dynamicAdapter.handlerView(viewHolder, dynamicModel, 0, false, true);
    }

    private void onDynamicClick(int viewId, int position, boolean isShared)
    {
        if(viewId == R.id.item_dynamic_author_lay)
        {
            Intent intent = new Intent(ctx, UserActivity.class);
            intent.putExtra("mid", dynamicModel.getCardAuthorUid());
            startActivity(intent);
        }
        else if(viewId == R.id.item_dynamic_share_lay)
        {
            Intent intent = new Intent(ctx, SendDynamicActivity.class);
            intent.putExtra("is_share", true);
            intent.putExtra("share_up", dynamicModel.getCardAuthorName());
            intent.putExtra("share_id", dynamicModel.getCardId());
            getShareIntent(intent, dynamicModel);
            startActivityForResult(intent, RESULT_DYNAMIC_DETAIL_SHARE);
        }
        else if(viewId == R.id.item_dynamic_reply_lay)
        {
            dynamicDetailFragmentListener.onDynamicDetailFragmentOnClick(viewId);
        }
        else if(viewId == R.id.item_dynamic_like_lay)
        {
            dynamicDetailFragmentListener.onDynamicDetailFragmentOnClick(viewId);
        }
        else if(viewId == R.id.dynamic_share_share)
        {
            Intent intent = new Intent(ctx, UnsupportedLinkActivity.class);
            intent.putExtra("url", ((DynamicModel.DynamicShareModel) dynamicModel).getShareOriginCard().getCardUrl());
            startActivity(intent);
        }
        else if(viewId == R.id.dynamic_album_author)
        {
            Intent intent = new Intent(ctx, UserActivity.class);
            intent.putExtra("mid", ((DynamicModel.DynamicAlbumModel) ((DynamicModel.DynamicShareModel) dynamicModel).getShareOriginCard()).getAlbumAuthorUid());
            startActivity(intent);
        }
        else if(viewId == R.id.dynamic_text_author)
        {
            Intent intent = new Intent(ctx, UserActivity.class);
            intent.putExtra("mid", ((DynamicModel.DynamicTextModel) ((DynamicModel.DynamicShareModel) dynamicModel).getShareOriginCard()).getTextAuthorUid());
            startActivity(intent);
        }
        else if(viewId == R.id.dynamic_video_author)
        {
            Intent intent = new Intent(ctx, UserActivity.class);
            intent.putExtra("mid", ((DynamicModel.DynamicVideoModel) ((DynamicModel.DynamicShareModel) dynamicModel).getShareOriginCard()).getVideoAuthorUid());
            startActivity(intent);
        }
        else if(viewId == R.id.dynamic_article_author)
        {
            Intent intent = new Intent(ctx, UserActivity.class);
            intent.putExtra("mid", ((DynamicModel.DynamicArticleModel) ((DynamicModel.DynamicShareModel) dynamicModel).getShareOriginCard()).getArticleAuthorUid());
            startActivity(intent);
        }
        else if(viewId == R.id.dynamic_url_author)
        {
            Intent intent = new Intent(ctx, UserActivity.class);
            intent.putExtra("mid", ((DynamicModel.DynamicUrlModel) ((DynamicModel.DynamicShareModel) dynamicModel).getShareOriginCard()).getUrlAuthorUid());
            startActivity(intent);
        }
        else if(viewId == R.id.dynamic_url_url)
        {
            if(!isShared)
            {
                Intent intent = new Intent(ctx, UnsupportedLinkActivity.class);
                intent.putExtra("url", ((DynamicModel.DynamicUrlModel) dynamicModel).getUrlUrl());
                startActivity(intent);
            }
            else
            {
                Intent intent = new Intent(ctx, UnsupportedLinkActivity.class);
                intent.putExtra("url", ((DynamicModel.DynamicUrlModel) ((DynamicModel.DynamicShareModel) dynamicModel).getShareOriginCard()).getUrlUrl());
                startActivity(intent);
            }
        }
        else if(viewId == R.id.dynamic_live_author)
        {
            Intent intent = new Intent(ctx, UserActivity.class);
            intent.putExtra("mid", ((DynamicModel.DynamicLiveModel) ((DynamicModel.DynamicShareModel) dynamicModel).getShareOriginCard()).getLiveAuthorUid());
            startActivity(intent);
        }
        else if(viewId == R.id.dynamic_favor_author)
        {
            Intent intent = new Intent(ctx, UserActivity.class);
            intent.putExtra("mid", ((DynamicModel.DynamicFavorModel) ((DynamicModel.DynamicShareModel) dynamicModel).getShareOriginCard()).getFavorAuthorUid());
            startActivity(intent);
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        dynamicDetailFragmentListener = null;
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

    private void getShareIntent(Intent intent, DynamicModel.DynamicBaseModel dynamicModel)
    {
        switch (dynamicModel.getCardType())
        {
            case 1:
            {
                DynamicModel.DynamicShareModel dm = (DynamicModel.DynamicShareModel) dynamicModel;
                intent.putExtra("share_text", "//@" + dm.getCardAuthorName() + ":" + dm.getShareTextOrg());
                getShareIntent(intent, dm.getShareOriginCard());
                break;
            }
            case 2:
            {
                DynamicModel.DynamicAlbumModel dm = (DynamicModel.DynamicAlbumModel) dynamicModel;
                if(dm.getAlbumImg().size() > 0) intent.putExtra("share_img", dm.getAlbumImg().get(0));
                intent.putExtra("share_title", dm.getAlbumTextOrg());
                break;
            }
            case 4:
            {
                DynamicModel.DynamicTextModel dm = (DynamicModel.DynamicTextModel) dynamicModel;
                intent.putExtra("share_title", dm.getTextTextOrg());
                break;
            }
            case 8:
            {
                DynamicModel.DynamicVideoModel dm = (DynamicModel.DynamicVideoModel) dynamicModel;
                intent.putExtra("share_img", dm.getVideoImg());
                intent.putExtra("share_title", dm.getVideoTitle());
                break;
            }
            case 64:
            {
                DynamicModel.DynamicArticleModel dm = (DynamicModel.DynamicArticleModel) dynamicModel;
                intent.putExtra("share_img", dm.getArticleImg());
                intent.putExtra("share_title", dm.getArticleTitle());
                break;
            }
            case 512:
            case 4098:
            case 4099:
            case 4101:
            {
                DynamicModel.DynamicBangumiModel dm = (DynamicModel.DynamicBangumiModel) dynamicModel;
                intent.putExtra("share_img", dm.getBangumiImg());
                intent.putExtra("share_title", dm.getBangumiTitle());
                break;
            }
            case 2048:
            {
                DynamicModel.DynamicUrlModel dm = (DynamicModel.DynamicUrlModel) dynamicModel;
                intent.putExtra("share_img", dm.getUrlImg());
                intent.putExtra("share_title", dm.getUrlDynamicOrg());
                break;
            }
            case 4200:
            {
                DynamicModel.DynamicLiveModel dm = (DynamicModel.DynamicLiveModel) dynamicModel;
                intent.putExtra("share_img", dm.getLiveImg());
                intent.putExtra("share_title", dm.getLiveTitle());
                break;
            }
            case 4300:
            {
                DynamicModel.DynamicFavorModel dm = (DynamicModel.DynamicFavorModel) dynamicModel;
                intent.putExtra("share_img", dm.getFavorImg());
                intent.putExtra("share_title", dm.getFavorTitle());
                break;
            }
        }
    }

    public interface DynamicDetailFragmentListener
    {
        void onDynamicDetailFragmentOnClick(int viewId);
    }
}
