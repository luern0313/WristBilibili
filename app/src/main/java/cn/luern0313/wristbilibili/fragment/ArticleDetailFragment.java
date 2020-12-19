package cn.luern0313.wristbilibili.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.fragment.app.Fragment;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.ArticleAdapter;
import cn.luern0313.wristbilibili.models.article.ArticleCardModel;
import cn.luern0313.wristbilibili.models.article.ArticleModel;
import cn.luern0313.wristbilibili.ui.ArticleActivity;
import cn.luern0313.wristbilibili.ui.ImgActivity;
import cn.luern0313.wristbilibili.ui.UserActivity;
import cn.luern0313.wristbilibili.ui.UnsupportedLinkActivity;
import cn.luern0313.wristbilibili.util.ColorUtil;
import cn.luern0313.wristbilibili.util.DataProcessUtil;

public class ArticleDetailFragment extends Fragment implements View.OnClickListener
{
    private static final String ARG_ARTICLE_MODEL = "articleModelArg";

    Context ctx;
    View rootLayout;
    private ArticleModel articleModel;
    private ArticleAdapter articleAdapter;
    private ArticleDetailFragmentListener articleDetailFragmentListener;
    private ArticleAdapter.ArticleAdapterListener articleListener;

    private ListView uiArticleListView;
    private View layoutArticleHeader;
    private View layoutArticleFooter;

    public ArticleDetailFragment() {}

    public static ArticleDetailFragment newInstance(ArticleModel articleModel)
    {
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ARTICLE_MODEL, articleModel);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
        {
            articleModel = (ArticleModel) getArguments().getSerializable(ARG_ARTICLE_MODEL);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_article_detail, container, false);

        WindowManager manager = getActivity().getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        int img_width = outMetrics.widthPixels - DataProcessUtil.dip2px(28) * 2;

        articleListener = new ArticleAdapter.ArticleAdapterListener()
        {
            @Override
            public void onCardClick(int viewId, int position)
            {
                onArticleViewClick(viewId, position);
            }

            @Override
            public void onLinkClick(String url)
            {
                Intent intent = new Intent(ctx, UnsupportedLinkActivity.class);
                intent.putExtra("url", url);
                startActivity(intent);
            }
        };

        uiArticleListView = rootLayout.findViewById(R.id.article_article_listview);
        layoutArticleHeader = inflater.inflate(R.layout.widget_article_header, null);
        layoutArticleFooter = inflater.inflate(R.layout.widget_article_return_top, null);

        ((TextView) layoutArticleHeader.findViewById(R.id.article_article_title)).setText(articleModel.getTitle());
        ((TextView) layoutArticleHeader.findViewById(R.id.article_article_channel)).setText(articleModel.getChannel());
        ((TextView) layoutArticleHeader.findViewById(R.id.article_article_view)).setText(String.format(getString(R.string.article_view), DataProcessUtil.getView(articleModel.getView())));
        ((TextView) layoutArticleHeader.findViewById(R.id.article_article_time)).setText(articleModel.getTime());
        ((TextView) layoutArticleHeader.findViewById(R.id.article_article_id)).setText(String.format(getString(R.string.article_id), articleModel.getId()));

        Glide.with(ctx).load(articleModel.getUpFace()).into((ImageView) layoutArticleHeader.findViewById(R.id.article_card_head));
        ((TextView) layoutArticleHeader.findViewById(R.id.article_card_name)).setText(articleModel.getUpName());
        ((TextView) layoutArticleHeader.findViewById(R.id.article_card_sen)).setText(String.format(getString(R.string.article_fans), DataProcessUtil.getView(articleModel.getUpFansNum())));
        if(articleModel.getUpVip() == 2)
            ((TextView) layoutArticleHeader.findViewById(R.id.article_card_name)).setTextColor(ColorUtil.getColor(R.attr.colorVip, ctx));
        if(articleModel.getUpOfficial() == 0)
            layoutArticleHeader.findViewById(R.id.article_card_off_1).setVisibility(View.VISIBLE);
        else if(articleModel.getUpOfficial() == 1)
            layoutArticleHeader.findViewById(R.id.article_card_off_2).setVisibility(View.VISIBLE);

        layoutArticleHeader.findViewById(R.id.article_card_follow).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                articleDetailFragmentListener.onArticleDetailFragmentViewClick(v.getId());
            }
        });

        setArticleIcon();

        layoutArticleHeader.findViewById(R.id.article_article_bt_cover).setOnClickListener(this);
        layoutArticleHeader.findViewById(R.id.article_article_bt_like).setOnClickListener(this);
        layoutArticleHeader.findViewById(R.id.article_article_bt_coin).setOnClickListener(this);
        layoutArticleHeader.findViewById(R.id.article_article_bt_fav).setOnClickListener(this);
        layoutArticleHeader.findViewById(R.id.article_article_bt_share).setOnClickListener(this);

        articleAdapter = new ArticleAdapter(inflater, img_width, articleModel.getArticleCardModelList(), uiArticleListView, articleListener);
        uiArticleListView.addHeaderView(layoutArticleHeader);
        uiArticleListView.addFooterView(layoutArticleFooter);
        uiArticleListView.setAdapter(articleAdapter);

        layoutArticleFooter.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                uiArticleListView.smoothScrollToPositionFromTop(0, 0, 500);
                uiArticleListView.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        uiArticleListView.setSelection(0);
                    }
                }, 500);
            }
        });

        layoutArticleHeader.findViewById(R.id.article_card_lay).setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(ctx, UserActivity.class);
                        intent.putExtra("mid", articleModel.getUpMid());
                        ArticleDetailFragment.this.startActivity(intent);
                    }
                });

        return rootLayout;
    }

    private void setArticleIcon()
    {
        rootLayout.findViewById(R.id.article_article_loading).setVisibility(View.GONE);
        if(articleModel.isUserLike())
            ((ImageView) layoutArticleHeader.findViewById(R.id.article_article_bt_like_img)).setImageResource(R.drawable.icon_vdd_do_like_yes);
        else
            ((ImageView) layoutArticleHeader.findViewById(R.id.article_article_bt_like_img)).setImageResource(R.drawable.icon_vdd_do_like_no);

        if(articleModel.getUserCoin() == 1)
            ((ImageView) layoutArticleHeader.findViewById(R.id.article_article_bt_coin_img)).setImageResource(R.drawable.icon_vdd_do_coin_yes);
        else
            ((ImageView) layoutArticleHeader.findViewById(R.id.article_article_bt_coin_img)).setImageResource(R.drawable.icon_vdd_do_coin_no);

        if(articleModel.isUserFavor())
            ((ImageView) layoutArticleHeader.findViewById(R.id.article_article_bt_fav_img)).setImageResource(R.drawable.icon_vdd_do_fav_yes);
        else
            ((ImageView) layoutArticleHeader.findViewById(R.id.article_article_bt_fav_img)).setImageResource(R.drawable.icon_vdd_do_fav_no);

        if(articleModel.getLike() == 0)
            ((TextView) layoutArticleHeader.findViewById(R.id.article_article_bt_like_text)).setText("点赞");
        else
            ((TextView) layoutArticleHeader.findViewById(R.id.article_article_bt_like_text)).setText(DataProcessUtil.getView(articleModel.getLike()));

        if(articleModel.getCoin() == 0)
            ((TextView) layoutArticleHeader.findViewById(R.id.article_article_bt_coin_text)).setText("投币");
        else
            ((TextView) layoutArticleHeader.findViewById(R.id.article_article_bt_coin_text)).setText(DataProcessUtil.getView(articleModel.getCoin()));

        if(articleModel.getFavor() == 0)
            ((TextView) layoutArticleHeader.findViewById(R.id.article_article_bt_fav_text)).setText("收藏");
        else
            ((TextView) layoutArticleHeader.findViewById(R.id.article_article_bt_fav_text)).setText(DataProcessUtil.getView(articleModel.getFavor()));

        if(articleModel.isUserFollowUp())
            layoutArticleHeader.findViewById(R.id.article_card_follow).setVisibility(View.GONE);
    }

    private void onArticleViewClick(int viewId, int position)
    {
        ArticleCardModel.ArticleCardBaseModel articleCardModel = articleModel.getArticleCardModelList().get(position);
        if(articleCardModel.getCardIdentity().equals("te"))
        {
            if(((ArticleCardModel.ArticleCardTextModel) articleCardModel).getTextArticleImageModel() != null)
            {
                int p = DataProcessUtil.getPositionInArrayList(articleModel.getArticleImgUrl(),
                        ((ArticleCardModel.ArticleCardTextModel) articleCardModel).getTextArticleImageModel().article_image_src);
                if(p != -1)
                {
                    Intent intent = new Intent(ctx, ImgActivity.class);
                    intent.putExtra("imgUrl", articleModel.getArticleImgUrl().toArray(new String[0]));
                    intent.putExtra("position", p);
                    startActivity(intent);
                }
            }
        }
        else
        {
            Uri uri = Uri.parse(articleCardModel.getCardUrl());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setClass(getContext(), UnsupportedLinkActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View v)
    {
        articleDetailFragmentListener.onArticleDetailFragmentViewClick(v.getId());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ArticleModel articleModel)
    {
        this.articleModel = articleModel;
        rootLayout.findViewById(R.id.article_article_loading).setVisibility(View.GONE);
        setArticleIcon();
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if(context instanceof ArticleDetailFragmentListener)
        {
            articleDetailFragmentListener = (ArticleDetailFragmentListener) context;
            ((ArticleActivity) getActivity()).setArticleDetailActivityListener(new ArticleActivity.ArticleDetailActivityListener() {
                @Override
                public void onArticleDetailActivityLoadingStart()
                {

                }

                @Override
                public void onArticleDetailActivityLoadingFin()
                {

                }
            });
        }
        else
        {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        articleDetailFragmentListener = null;
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

    public interface ArticleDetailFragmentListener
    {
        void onArticleDetailFragmentViewClick(int viewId);
    }
}
