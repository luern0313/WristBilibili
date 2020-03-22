package cn.luern0313.wristbilibili.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.fragment.app.Fragment;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.ArticleAdapter;
import cn.luern0313.wristbilibili.models.article.ArticleCardModel;
import cn.luern0313.wristbilibili.models.article.ArticleModel;
import cn.luern0313.wristbilibili.ui.ArticleActivity;
import cn.luern0313.wristbilibili.ui.BangumiActivity;
import cn.luern0313.wristbilibili.ui.ImgActivity;
import cn.luern0313.wristbilibili.ui.UnsupportedLinkActivity;
import cn.luern0313.wristbilibili.ui.VideodetailsActivity;
import cn.luern0313.wristbilibili.util.DataProcessUtil;

public class ArticleDetailFragment extends Fragment implements View.OnClickListener
{
    private static final String ARG_ARTICLE_MODEL = "articleModelArg";

    Context ctx;
    View rootLayout;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ArticleModel articleModel;
    private ArticleAdapter articleAdapter;
    private ArticleDetailFragmentListener articleDetailFragmentListener;
    private ArticleAdapter.ArticleListener articleListener;

    private int img_width;

    private ListView uiArticleListView;
    private View layoutArticleHeader;


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
        EventBus.getDefault().register(this);
        if(getArguments() != null)
        {
            articleModel = (ArticleModel) getArguments().getSerializable(ARG_ARTICLE_MODEL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_article_detail, container, false);
        sharedPreferences = ctx.getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        WindowManager manager = getActivity().getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        img_width = outMetrics.widthPixels - DataProcessUtil.dip2px(ctx, 28) * 2;

        articleListener = new ArticleAdapter.ArticleListener()
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

        ((TextView) layoutArticleHeader.findViewById(R.id.article_article_title)).setText(articleModel.article_title);
        ((TextView) layoutArticleHeader.findViewById(R.id.article_article_channel)).setText(articleModel.article_channel);
        ((TextView) layoutArticleHeader.findViewById(R.id.article_article_view)).setText(DataProcessUtil.getView(articleModel.article_view) + "观看");
        ((TextView) layoutArticleHeader.findViewById(R.id.article_article_time)).setText(articleModel.article_time);
        ((TextView) layoutArticleHeader.findViewById(R.id.article_article_id)).setText("CV" + articleModel.article_id);

        setArticleIcon();

        layoutArticleHeader.findViewById(R.id.article_article_bt_cover).setOnClickListener(this);
        layoutArticleHeader.findViewById(R.id.article_article_bt_like).setOnClickListener(this);
        layoutArticleHeader.findViewById(R.id.article_article_bt_coin).setOnClickListener(this);
        layoutArticleHeader.findViewById(R.id.article_article_bt_fav).setOnClickListener(this);
        layoutArticleHeader.findViewById(R.id.article_article_bt_share).setOnClickListener(this);

        articleAdapter = new ArticleAdapter(inflater, img_width, articleModel.article_article_card_model_list, uiArticleListView, articleListener);
        uiArticleListView.addHeaderView(layoutArticleHeader);
        uiArticleListView.setAdapter(articleAdapter);

        return rootLayout;
    }

    private void setArticleIcon()
    {
        rootLayout.findViewById(R.id.article_article_loading).setVisibility(View.GONE);
        if(articleModel.article_user_like)
            ((ImageView) layoutArticleHeader.findViewById(R.id.article_article_bt_like_img)).setImageResource(R.drawable.icon_vdd_do_like_yes);
        else
            ((ImageView) layoutArticleHeader.findViewById(R.id.article_article_bt_like_img)).setImageResource(R.drawable.icon_vdd_do_like_no);

        if(articleModel.article_user_coin == 1)
            ((ImageView) layoutArticleHeader.findViewById(R.id.article_article_bt_coin_img)).setImageResource(R.drawable.icon_vdd_do_coin_yes);
        else
            ((ImageView) layoutArticleHeader.findViewById(R.id.article_article_bt_coin_img)).setImageResource(R.drawable.icon_vdd_do_coin_no);

        if(articleModel.article_user_fav)
            ((ImageView) layoutArticleHeader.findViewById(R.id.article_article_bt_fav_img)).setImageResource(R.drawable.icon_vdd_do_fav_yes);
        else
            ((ImageView) layoutArticleHeader.findViewById(R.id.article_article_bt_fav_img)).setImageResource(R.drawable.icon_vdd_do_fav_no);

        if(articleModel.article_like == 0)
            ((TextView) layoutArticleHeader.findViewById(R.id.article_article_bt_like_text)).setText("点赞");
        else
            ((TextView) layoutArticleHeader.findViewById(R.id.article_article_bt_like_text)).setText(DataProcessUtil.getView(articleModel.article_like));

        if(articleModel.article_coin == 0)
            ((TextView) layoutArticleHeader.findViewById(R.id.article_article_bt_coin_text)).setText("投币");
        else
            ((TextView) layoutArticleHeader.findViewById(R.id.article_article_bt_coin_text)).setText(DataProcessUtil.getView(articleModel.article_coin));

        if(articleModel.article_fav == 0)
            ((TextView) layoutArticleHeader.findViewById(R.id.article_article_bt_fav_text)).setText("收藏");
        else
            ((TextView) layoutArticleHeader.findViewById(R.id.article_article_bt_fav_text)).setText(DataProcessUtil.getView(articleModel.article_fav));
    }

    private void onArticleViewClick(int viewId, int position)
    {
        ArticleCardModel articleCardModel = articleModel.article_article_card_model_list.get(position);
        if(articleCardModel.article_card_identity.equals("te"))
        {
            if(((ArticleCardModel.ArticleTextModel) articleCardModel).article_text_articleImageModel != null)
            {
                Intent intent = new Intent(ctx, ImgActivity.class);
                intent.putExtra("imgUrl", articleModel.article_article_img_url.toArray(new String[0]));
                //intent.putExtra("position", );
                startActivity(intent);
            }
        }
        else
        {
            if(articleCardModel.article_card_support)
            {
                if(articleCardModel.article_card_identity.substring(0, 2).equals("av"))
                {
                    Intent intent = new Intent(ctx, VideodetailsActivity.class);
                    intent.putExtra("aid", ((ArticleCardModel.ArticleVideoCardModel) articleCardModel).article_video_card_id);
                    startActivity(intent);
                }
                else if(articleCardModel.article_card_identity.substring(0, 2).equals("ss"))
                {
                    Intent intent = new Intent(ctx, BangumiActivity.class);
                    intent.putExtra("season_id", ((ArticleCardModel.ArticleBangumiCardModel) articleCardModel).article_bangumi_card_id);
                    startActivity(intent);
                }
                else if(articleCardModel.article_card_identity.substring(0, 2).equals("cv"))
                {
                    Intent intent = new Intent(ctx, ArticleActivity.class);
                    intent.putExtra("article_id", ((ArticleCardModel.ArticleArticleCardModel) articleCardModel).article_article_card_id);
                    startActivity(intent);
                }
            }
            else
            {
                Intent intent = new Intent(ctx, UnsupportedLinkActivity.class);
                intent.putExtra("url", articleCardModel.article_card_url);
                startActivity(intent);
            }
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
    public void onDestroy()
    {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public interface ArticleDetailFragmentListener
    {
        void onArticleDetailFragmentViewClick(int viewId);
    }
}
