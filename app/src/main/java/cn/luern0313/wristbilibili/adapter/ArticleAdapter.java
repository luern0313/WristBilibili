package cn.luern0313.wristbilibili.adapter;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.models.article.ArticleCardModel;
import cn.luern0313.wristbilibili.util.ArticleHtmlImageHandlerUtil;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.ImageTaskUtil;
import cn.luern0313.wristbilibili.util.LruCacheUtil;
import cn.luern0313.wristbilibili.util.MyApplication;

/**
 * 被 luern0313 创建于 2020/2/25.
 */
public class ArticleAdapter extends BaseAdapter
{
    private final Context ctx;
    private final LayoutInflater mInflater;

    private final ArticleAdapterListener articleListener;

    private final ArrayList<ArticleCardModel.ArticleCardBaseModel> articleCardModelArrayList;
    private final ListView listView;

    private final int img_width;

    public ArticleAdapter(LayoutInflater inflater, int img_width, ArrayList<ArticleCardModel.ArticleCardBaseModel> articleCardModelArrayList, ListView listView, ArticleAdapterListener articleListener)
    {
        this.ctx = MyApplication.getContext();
        this.mInflater = inflater;
        this.img_width = img_width;
        this.articleCardModelArrayList = articleCardModelArrayList;
        this.listView = listView;
        this.articleListener = articleListener;
    }

    @Override
    public int getCount()
    {
        return articleCardModelArrayList.size();
    }

    @Override
    public Object getItem(int position)
    {
        return position;
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public int getViewTypeCount()
    {
        return 9;
    }

    @Override
    public int getItemViewType(int position)
    {
        ArticleCardModel.ArticleCardBaseModel articleCardModel = articleCardModelArrayList.get(position);
        switch (articleCardModel.getCardIdentity().substring(0, 2))
        {
            case "te":
                return 0;
            case "av":
                return 1;
            case "ss":
            case "ep":
                return 2;
            case "cv":
                return 3;
            case "au":
                return 4;
            case "pw":
                return 5;
            case "sp":
                return 6;
            case "mc":
                return 7;
            case "lv":
                return 8;
        }
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup)
    {
        int type = getItemViewType(position);
        ArticleTextViewHolder articleTextViewHolder = null;
        ArticleVideoCardViewHolder articleVideoCardViewHolder = null;
        ArticleBangumiCardViewHolder articleBangumiCardViewHolder = null;
        ArticleArticleCardViewHolder articleArticleCardViewHolder = null;
        ArticleMusicCardViewHolder articleMusicCardViewHolder = null;
        ArticleTicketCardViewHolder articleTicketCardViewHolder = null;
        ArticleShopCardViewHolder articleShopCardViewHolder = null;
        ArticleContainerCardViewHolder articleContainerCardViewHolder = null;
        ArticleLiveCardViewHolder articleLiveCardViewHolder = null;
        if(convertView == null)
        {
            if(type == 0)
            {
                convertView = mInflater.inflate(R.layout.item_article_article, null);
                articleTextViewHolder = new ArticleTextViewHolder();
                convertView.setTag(articleTextViewHolder);
                articleTextViewHolder.text_text = convertView.findViewById(R.id.item_article_article_textview);
            }
            else if(type == 1)
            {
                convertView = mInflater.inflate(R.layout.item_article_card_video, null);
                articleVideoCardViewHolder = new ArticleVideoCardViewHolder();
                convertView.setTag(articleVideoCardViewHolder);
                articleVideoCardViewHolder.video_lay = convertView.findViewById(R.id.artcard_video_lay);
                articleVideoCardViewHolder.video_title = convertView.findViewById(R.id.artcard_video_title);
                articleVideoCardViewHolder.video_cover = convertView.findViewById(R.id.artcard_video_img);
                articleVideoCardViewHolder.video_play = convertView.findViewById(R.id.artcard_video_play);
                articleVideoCardViewHolder.video_danmaku = convertView.findViewById(R.id.artcard_video_danmaku);
                articleVideoCardViewHolder.video_time = convertView.findViewById(R.id.artcard_video_time);
                articleVideoCardViewHolder.video_up_name = convertView.findViewById(R.id.artcard_video_up);
            }
            else if(type == 2)
            {
                convertView = mInflater.inflate(R.layout.item_article_card_bangumi, null);
                articleBangumiCardViewHolder = new ArticleBangumiCardViewHolder();
                convertView.setTag(articleBangumiCardViewHolder);
                articleBangumiCardViewHolder.bangumi_lay = convertView.findViewById(R.id.artcard_bangumi_lay);
                articleBangumiCardViewHolder.bangumi_title = convertView.findViewById(R.id.artcard_bangumi_title);
                articleBangumiCardViewHolder.bangumi_cover = convertView.findViewById(R.id.artcard_bangumi_img);
                articleBangumiCardViewHolder.bangumi_play = convertView.findViewById(R.id.artcard_bangumi_play);
                articleBangumiCardViewHolder.bangumi_follow = convertView.findViewById(R.id.artcard_bangumi_follow);
                articleBangumiCardViewHolder.bangumi_type = convertView.findViewById(R.id.artcard_bangumi_type);
                articleBangumiCardViewHolder.bangumi_score = convertView.findViewById(R.id.artcard_bangumi_score);
            }
            else if(type == 3)
            {
                convertView = mInflater.inflate(R.layout.item_article_card_article, null);
                articleArticleCardViewHolder = new ArticleArticleCardViewHolder();
                convertView.setTag(articleArticleCardViewHolder);
                articleArticleCardViewHolder.article_lay = convertView.findViewById(R.id.artcard_article_lay);
                articleArticleCardViewHolder.article_title = convertView.findViewById(R.id.artcard_article_title);
                articleArticleCardViewHolder.article_cover = convertView.findViewById(R.id.artcard_article_img);
                articleArticleCardViewHolder.article_view = convertView.findViewById(R.id.artcard_article_view);
                articleArticleCardViewHolder.article_reply = convertView.findViewById(R.id.artcard_article_reply);
                articleArticleCardViewHolder.article_up_name = convertView.findViewById(R.id.artcard_article_up);
            }
            else if(type == 4)
            {
                convertView = mInflater.inflate(R.layout.item_article_card_music, null);
                articleMusicCardViewHolder = new ArticleMusicCardViewHolder();
                convertView.setTag(articleMusicCardViewHolder);
                articleMusicCardViewHolder.music_lay = convertView.findViewById(R.id.artcard_music_lay);
                articleMusicCardViewHolder.music_title = convertView.findViewById(R.id.artcard_music_title);
                articleMusicCardViewHolder.music_cover = convertView.findViewById(R.id.artcard_music_img);
                articleMusicCardViewHolder.music_play = convertView.findViewById(R.id.artcard_music_play);
                articleMusicCardViewHolder.music_reply = convertView.findViewById(R.id.artcard_music_reply);
                articleMusicCardViewHolder.music_up_name = convertView.findViewById(R.id.artcard_music_up);
            }
            else if(type == 5)
            {
                convertView = mInflater.inflate(R.layout.item_article_card_ticket, null);
                articleTicketCardViewHolder = new ArticleTicketCardViewHolder();
                convertView.setTag(articleTicketCardViewHolder);
                articleTicketCardViewHolder.ticket_lay = convertView.findViewById(R.id.artcard_ticket_lay);
                articleTicketCardViewHolder.ticket_title = convertView.findViewById(R.id.artcard_ticket_title);
                articleTicketCardViewHolder.ticket_cover = convertView.findViewById(R.id.artcard_ticket_img);
                articleTicketCardViewHolder.ticket_time = convertView.findViewById(R.id.artcard_ticket_time);
                articleTicketCardViewHolder.ticket_location = convertView.findViewById(R.id.artcard_ticket_location);
                articleTicketCardViewHolder.ticket_price = convertView.findViewById(R.id.artcard_ticket_price);
            }
            else if(type == 6)
            {
                convertView = mInflater.inflate(R.layout.item_article_card_shop, null);
                articleShopCardViewHolder = new ArticleShopCardViewHolder();
                convertView.setTag(articleShopCardViewHolder);
                articleShopCardViewHolder.shop_lay = convertView.findViewById(R.id.artcard_shop_lay);
                articleShopCardViewHolder.shop_title = convertView.findViewById(R.id.artcard_shop_title);
                articleShopCardViewHolder.shop_cover = convertView.findViewById(R.id.artcard_shop_img);
                articleShopCardViewHolder.shop_detail = convertView.findViewById(R.id.artcard_shop_detail);
                articleShopCardViewHolder.shop_price = convertView.findViewById(R.id.artcard_shop_price);
            }
            else if(type == 7)
            {
                convertView = mInflater.inflate(R.layout.item_article_card_container, null);
                articleContainerCardViewHolder = new ArticleContainerCardViewHolder();
                convertView.setTag(articleContainerCardViewHolder);
                articleContainerCardViewHolder.container_lay = convertView.findViewById(R.id.artcard_container_lay);
                articleContainerCardViewHolder.container_title = convertView.findViewById(R.id.artcard_container_title);
                articleContainerCardViewHolder.container_cover = convertView.findViewById(R.id.artcard_container_img);
                articleContainerCardViewHolder.container_detail = convertView.findViewById(R.id.artcard_container_detail);
                articleContainerCardViewHolder.container_author = convertView.findViewById(R.id.artcard_container_author);
            }
            else if(type == 8)
            {
                convertView = mInflater.inflate(R.layout.item_article_card_live, null);
                articleLiveCardViewHolder = new ArticleLiveCardViewHolder();
                convertView.setTag(articleLiveCardViewHolder);
                articleLiveCardViewHolder.live_lay = convertView.findViewById(R.id.artcard_live_lay);
                articleLiveCardViewHolder.live_title = convertView.findViewById(R.id.artcard_live_title);
                articleLiveCardViewHolder.live_cover = convertView.findViewById(R.id.artcard_live_img);
                articleLiveCardViewHolder.live_status = convertView.findViewById(R.id.artcard_live_stat);
                articleLiveCardViewHolder.live_area = convertView.findViewById(R.id.artcard_live_area);
                articleLiveCardViewHolder.live_online = convertView.findViewById(R.id.artcard_live_online);
                articleLiveCardViewHolder.live_up_name = convertView.findViewById(R.id.artcard_live_up);
            }
        }
        else
        {
            if(type == 0) articleTextViewHolder = (ArticleTextViewHolder) convertView.getTag();
            else if(type == 1) articleVideoCardViewHolder = (ArticleVideoCardViewHolder) convertView.getTag();
            else if(type == 2) articleBangumiCardViewHolder = (ArticleBangumiCardViewHolder) convertView.getTag();
            else if(type == 3) articleArticleCardViewHolder = (ArticleArticleCardViewHolder) convertView.getTag();
            else if(type == 4) articleMusicCardViewHolder = (ArticleMusicCardViewHolder) convertView.getTag();
            else if(type == 5) articleTicketCardViewHolder = (ArticleTicketCardViewHolder) convertView.getTag();
            else if(type == 6) articleShopCardViewHolder = (ArticleShopCardViewHolder) convertView.getTag();
            else if(type == 7) articleContainerCardViewHolder = (ArticleContainerCardViewHolder) convertView.getTag();
            else if(type == 8) articleLiveCardViewHolder = (ArticleLiveCardViewHolder) convertView.getTag();
        }

        if(type == 0)
        {
            ArticleCardModel.ArticleCardTextModel articleTextModel = (ArticleCardModel.ArticleCardTextModel) articleCardModelArrayList.get(position);
            Document e = Jsoup.parseBodyFragment(articleTextModel.getTextElement());
            if(e.text().equals("") && articleTextModel.getTextArticleImageModel() == null)
                articleTextViewHolder.text_text.setVisibility(View.GONE);
            else
            {
                articleTextViewHolder.text_text.setHtml(articleTextModel.getTextElement(), new ArticleHtmlImageHandlerUtil(listView.getContext(), articleTextViewHolder.text_text, img_width, articleTextModel.getTextArticleImageModel()));
                articleTextViewHolder.text_text.setOnClickATagListener((widget, href) -> articleListener.onLinkClick(href));
                articleTextViewHolder.text_text.setVisibility(View.VISIBLE);
                articleTextViewHolder.text_text.setOnClickListener(onViewClick(position));
            }
        }
        else if(type == 1)
        {
            ArticleCardModel.ArticleCardVideoCardModel articleVideoCardModel = (ArticleCardModel.ArticleCardVideoCardModel) articleCardModelArrayList.get(position);

            Drawable upDrawable = convertView.getResources().getDrawable(R.drawable.icon_video_up);
            Drawable playNumDrawable = convertView.getResources().getDrawable(R.drawable.icon_number_play_white);
            Drawable danmakuNumDrawable = convertView.getResources().getDrawable(R.drawable.icon_number_danmu_white);
            upDrawable.setBounds(0, 0, DataProcessUtil.dip2px(12), DataProcessUtil.dip2px(12));
            playNumDrawable.setBounds(0,0, DataProcessUtil.dip2px(12), DataProcessUtil.dip2px(12));
            danmakuNumDrawable.setBounds(0,0, DataProcessUtil.dip2px(12), DataProcessUtil.dip2px(12));
            articleVideoCardViewHolder.video_up_name.setCompoundDrawables(upDrawable,null, null,null);
            articleVideoCardViewHolder.video_play.setCompoundDrawables(playNumDrawable,null, null,null);
            articleVideoCardViewHolder.video_danmaku.setCompoundDrawables(danmakuNumDrawable,null, null,null);

            articleVideoCardViewHolder.video_title.setText(articleVideoCardModel.getVideoCardTitle());
            articleVideoCardViewHolder.video_cover.setImageResource(R.drawable.img_default_vid);
            articleVideoCardViewHolder.video_play.setText(articleVideoCardModel.getArticle_video_card_play());
            articleVideoCardViewHolder.video_danmaku.setText(articleVideoCardModel.getArticle_video_card_danmaku());
            articleVideoCardViewHolder.video_time.setText(articleVideoCardModel.getArticle_video_card_time());
            articleVideoCardViewHolder.video_up_name.setText(articleVideoCardModel.getArticle_video_card_up_name());

            articleVideoCardViewHolder.video_lay.setOnClickListener(onViewClick(position));

            articleVideoCardViewHolder.video_cover.setTag(articleVideoCardModel.getArticle_video_card_cover());
            BitmapDrawable c = setImageFormWeb(articleVideoCardModel.getArticle_video_card_cover());
            if(c != null) articleVideoCardViewHolder.video_cover.setImageDrawable(c);
        }
        else if(type == 2)
        {
            ArticleCardModel.ArticleCardBangumiCardModel articleBangumiCardModel = (ArticleCardModel.ArticleCardBangumiCardModel) articleCardModelArrayList.get(position);

            Drawable playNumDrawable = convertView.getResources().getDrawable(R.drawable.icon_number_play);
            Drawable followNumDrawable = convertView.getResources().getDrawable(R.drawable.icon_number_like);
            playNumDrawable.setBounds(0,0, DataProcessUtil.dip2px(12), DataProcessUtil.dip2px(12));
            followNumDrawable.setBounds(0,0, DataProcessUtil.dip2px(12), DataProcessUtil.dip2px(12));
            articleBangumiCardViewHolder.bangumi_play.setCompoundDrawables(playNumDrawable,null, null,null);
            articleBangumiCardViewHolder.bangumi_follow.setCompoundDrawables(followNumDrawable,null, null,null);

            articleBangumiCardViewHolder.bangumi_title.setText(articleBangumiCardModel.getBangumiCardTitle());
            articleBangumiCardViewHolder.bangumi_cover.setImageResource(R.drawable.img_default_animation);
            articleBangumiCardViewHolder.bangumi_play.setText(articleBangumiCardModel.getBangumiCardPlay());
            articleBangumiCardViewHolder.bangumi_follow.setText(articleBangumiCardModel.getBangumiCardFollow());
            articleBangumiCardViewHolder.bangumi_type.setText(articleBangumiCardModel.getBangumiCardTypeName());
            articleBangumiCardViewHolder.bangumi_score.setText(articleBangumiCardModel.getBangumiCardScore());

            articleBangumiCardViewHolder.bangumi_lay.setOnClickListener(onViewClick(position));

            articleBangumiCardViewHolder.bangumi_cover.setTag(articleBangumiCardModel.getBangumiCardCover());
            BitmapDrawable c = setImageFormWeb(articleBangumiCardModel.getBangumiCardCover());
            if(c != null) articleBangumiCardViewHolder.bangumi_cover.setImageDrawable(c);
        }
        else if(type == 3)
        {
            ArticleCardModel.ArticleArticleCardCardModel articleArticleCardModel = (ArticleCardModel.ArticleArticleCardCardModel) articleCardModelArrayList.get(position);

            Drawable upDrawable = convertView.getResources().getDrawable(R.drawable.icon_video_up);
            Drawable playNumDrawable = convertView.getResources().getDrawable(R.drawable.icon_number_play_white);
            Drawable replyNumDrawable = convertView.getResources().getDrawable(R.drawable.icon_number_reply_white);
            upDrawable.setBounds(0,0, DataProcessUtil.dip2px(12), DataProcessUtil.dip2px(12));
            playNumDrawable.setBounds(0,0, DataProcessUtil.dip2px(12), DataProcessUtil.dip2px(12));
            replyNumDrawable.setBounds(0,0, DataProcessUtil.dip2px(12), DataProcessUtil.dip2px(12));
            articleArticleCardViewHolder.article_up_name.setCompoundDrawables(upDrawable,null, null,null);
            articleArticleCardViewHolder.article_view.setCompoundDrawables(playNumDrawable,null, null,null);
            articleArticleCardViewHolder.article_reply.setCompoundDrawables(replyNumDrawable,null, null,null);

            articleArticleCardViewHolder.article_title.setText(articleArticleCardModel.getArticleCardTitle());
            articleArticleCardViewHolder.article_cover.setImageResource(R.drawable.img_default_vid);
            articleArticleCardViewHolder.article_view.setText(articleArticleCardModel.getArticleCardView());
            articleArticleCardViewHolder.article_reply.setText(articleArticleCardModel.getArticleCardReply());
            articleArticleCardViewHolder.article_up_name.setText(articleArticleCardModel.getArticleCardUpName());

            articleArticleCardViewHolder.article_lay.setOnClickListener(onViewClick(position));

            articleArticleCardViewHolder.article_cover.setTag(articleArticleCardModel.getArticleCardCover());
            BitmapDrawable c = setImageFormWeb(articleArticleCardModel.getArticleCardCover());
            if(c != null) articleArticleCardViewHolder.article_cover.setImageDrawable(c);
        }
        else if(type == 4)
        {
            ArticleCardModel.ArticleCardMusicCardModel articleMusicCardModel = (ArticleCardModel.ArticleCardMusicCardModel) articleCardModelArrayList.get(position);

            Drawable upDrawable = convertView.getResources().getDrawable(R.drawable.icon_video_up);
            Drawable playNumDrawable = convertView.getResources().getDrawable(R.drawable.icon_number_play);
            Drawable replyNumDrawable = convertView.getResources().getDrawable(R.drawable.icon_number_reply);
            upDrawable.setBounds(0,0, DataProcessUtil.dip2px(12), DataProcessUtil.dip2px(12));
            playNumDrawable.setBounds(0,0, DataProcessUtil.dip2px(12), DataProcessUtil.dip2px(12));
            replyNumDrawable.setBounds(0,0, DataProcessUtil.dip2px(12), DataProcessUtil.dip2px(12));
            articleMusicCardViewHolder.music_up_name.setCompoundDrawables(upDrawable,null, null,null);
            articleMusicCardViewHolder.music_play.setCompoundDrawables(playNumDrawable,null, null,null);
            articleMusicCardViewHolder.music_reply.setCompoundDrawables(replyNumDrawable,null, null,null);

            articleMusicCardViewHolder.music_title.setText(articleMusicCardModel.getMusicCardTitle());
            articleMusicCardViewHolder.music_cover.setImageResource(R.drawable.img_default_animation);
            articleMusicCardViewHolder.music_play.setText(articleMusicCardModel.getMusicCardPlay());
            articleMusicCardViewHolder.music_reply.setText(articleMusicCardModel.getMusicCardReply());
            articleMusicCardViewHolder.music_up_name.setText(articleMusicCardModel.getMusicCardUpName());

            articleMusicCardViewHolder.music_lay.setOnClickListener(onViewClick(position));

            articleMusicCardViewHolder.music_cover.setTag(articleMusicCardModel.getMusicCardCover());
            BitmapDrawable c = setImageFormWeb(articleMusicCardModel.getMusicCardCover());
            if(c != null) articleMusicCardViewHolder.music_cover.setImageDrawable(c);
        }
        else if(type == 5)
        {
            ArticleCardModel.ArticleCardTicketCardModel articleTicketCardModel = (ArticleCardModel.ArticleCardTicketCardModel) articleCardModelArrayList.get(position);

            articleTicketCardViewHolder.ticket_title.setText(articleTicketCardModel.getTicketCardTitle());
            articleTicketCardViewHolder.ticket_cover.setImageResource(R.drawable.img_default_animation);
            articleTicketCardViewHolder.ticket_time.setText(articleTicketCardModel.getTicketCardTime());
            articleTicketCardViewHolder.ticket_location.setText(String.format(ctx.getString(R.string.article_card_ticket_location), articleTicketCardModel.getTicketCardLocationCity(), articleTicketCardModel.getTicketCardLocationVenue()));
            articleTicketCardViewHolder.ticket_price.setText(articleTicketCardModel.getTicketCardPrice());

            articleTicketCardViewHolder.ticket_lay.setOnClickListener(onViewClick(position));

            articleTicketCardViewHolder.ticket_cover.setTag(articleTicketCardModel.getTicketCardCover());
            BitmapDrawable c = setImageFormWeb(articleTicketCardModel.getTicketCardCover());
            if(c != null) articleTicketCardViewHolder.ticket_cover.setImageDrawable(c);
        }
        else if(type == 6)
        {
            ArticleCardModel.ArticleCardShopCardModel articleShopCardModel = (ArticleCardModel.ArticleCardShopCardModel) articleCardModelArrayList.get(position);

            articleShopCardViewHolder.shop_title.setText(articleShopCardModel.getShopCardTitle());
            articleShopCardViewHolder.shop_cover.setImageResource(R.drawable.img_default_animation);
            articleShopCardViewHolder.shop_detail.setText(articleShopCardModel.getShopCardDetail());
            articleShopCardViewHolder.shop_price.setText(articleShopCardModel.getShopCardPrice());

            articleShopCardViewHolder.shop_lay.setOnClickListener(onViewClick(position));

            articleShopCardViewHolder.shop_cover.setTag(articleShopCardModel.getShopCardCover());
            BitmapDrawable c = setImageFormWeb(articleShopCardModel.getShopCardCover());
            if(c != null) articleShopCardViewHolder.shop_cover.setImageDrawable(c);
        }
        else if(type == 7)
        {
            ArticleCardModel.ArticleCardContainerCardModel articleContainerCardModel = (ArticleCardModel.ArticleCardContainerCardModel) articleCardModelArrayList.get(position);

            articleContainerCardViewHolder.container_title.setText(articleContainerCardModel.getContainerCardTitle());
            articleContainerCardViewHolder.container_cover.setImageResource(R.drawable.img_default_animation);
            articleContainerCardViewHolder.container_detail.setText(articleContainerCardModel.getContainerCardDetail());
            articleContainerCardViewHolder.container_author.setText(articleContainerCardModel.getContainerCardAuthor());

            articleContainerCardViewHolder.container_lay.setOnClickListener(onViewClick(position));

            articleContainerCardViewHolder.container_cover.setTag(articleContainerCardModel.getContainerCardCover());
            BitmapDrawable c = setImageFormWeb(articleContainerCardModel.getContainerCardCover());
            if(c != null) articleContainerCardViewHolder.container_cover.setImageDrawable(c);
        }
        else if(type == 8)
        {
            ArticleCardModel.ArticleCardLiveCardModel articleLiveCardModel = (ArticleCardModel.ArticleCardLiveCardModel) articleCardModelArrayList.get(position);

            Drawable upDrawable = convertView.getResources().getDrawable(R.drawable.icon_video_up);
            Drawable viewerDrawable = convertView.getResources().getDrawable(R.drawable.icon_number_viewer_white);
            upDrawable.setBounds(0, 0, DataProcessUtil.dip2px(12), DataProcessUtil.dip2px(12));
            viewerDrawable.setBounds(0, 0, DataProcessUtil.dip2px(12), DataProcessUtil.dip2px(12));
            articleLiveCardViewHolder.live_up_name.setCompoundDrawables(upDrawable,null, null,null);
            articleLiveCardViewHolder.live_online.setCompoundDrawables(viewerDrawable,null, null,null);

            if(articleLiveCardModel.liveCardStatus == 0)
            {
                articleLiveCardViewHolder.live_status.setText(ctx.getString(R.string.article_card_live_status_off));
                articleLiveCardViewHolder.live_status.setBackgroundResource(R.drawable.shape_bg_article_card_live_notlive);
                articleLiveCardViewHolder.live_online.setText(ctx.getString(R.string.article_card_live_online_default));
            }
            else
            {
                articleLiveCardViewHolder.live_status.setText(ctx.getString(R.string.article_card_live_status_on));
                articleLiveCardViewHolder.live_status.setBackgroundResource(R.drawable.shape_bg_article_card_live_living);
                articleLiveCardViewHolder.live_online.setText(articleLiveCardModel.liveCardOnline);
            }

            articleLiveCardViewHolder.live_title.setText(articleLiveCardModel.liveCardTitle);
            articleLiveCardViewHolder.live_cover.setImageResource(R.drawable.img_default_vid);
            articleLiveCardViewHolder.live_area.setText(articleLiveCardModel.liveCardArea);
            articleLiveCardViewHolder.live_up_name.setText(articleLiveCardModel.liveCardUpName);

            articleLiveCardViewHolder.live_lay.setOnClickListener(onViewClick(position));

            articleLiveCardViewHolder.live_cover.setTag(articleLiveCardModel.liveCardCover);
            BitmapDrawable c = setImageFormWeb(articleLiveCardModel.liveCardCover);
            if(c != null) articleLiveCardViewHolder.live_cover.setImageDrawable(c);
        }
        
        return convertView;
    }

    static class ArticleTextViewHolder
    {
        HtmlTextView text_text;
    }

    static class ArticleVideoCardViewHolder
    {
        RelativeLayout video_lay;
        TextView video_title;
        ImageView video_cover;
        TextView video_play;
        TextView video_danmaku;
        TextView video_time;
        TextView video_up_name;
    }

    static class ArticleBangumiCardViewHolder
    {
        RelativeLayout bangumi_lay;
        TextView bangumi_title;
        ImageView bangumi_cover;
        TextView bangumi_play;
        TextView bangumi_follow;
        TextView bangumi_type;
        TextView bangumi_score;
    }

    static class ArticleArticleCardViewHolder
    {
        RelativeLayout article_lay;
        TextView article_title;
        ImageView article_cover;
        TextView article_view;
        TextView article_reply;
        TextView article_up_name;
    }

    static class ArticleMusicCardViewHolder
    {
        RelativeLayout music_lay;
        TextView music_title;
        ImageView music_cover;
        TextView music_play;
        TextView music_reply;
        TextView music_up_name;
    }

    static class ArticleTicketCardViewHolder
    {
        RelativeLayout ticket_lay;
        TextView ticket_title;
        ImageView ticket_cover;
        TextView ticket_time;
        TextView ticket_location;
        TextView ticket_price;
    }

    static class ArticleShopCardViewHolder
    {
        RelativeLayout shop_lay;
        TextView shop_title;
        ImageView shop_cover;
        TextView shop_detail;
        TextView shop_price;
    }

    static class ArticleContainerCardViewHolder
    {
        RelativeLayout container_lay;
        TextView container_title;
        ImageView container_cover;
        TextView container_detail;
        TextView container_author;
    }

    static class ArticleLiveCardViewHolder
    {
        RelativeLayout live_lay;
        TextView live_title;
        ImageView live_cover;
        TextView live_status;
        TextView live_area;
        TextView live_online;
        TextView live_up_name;
    }

    private View.OnClickListener onViewClick(final int position)
    {
        return v -> articleListener.onCardClick(v.getId(), position);
    }

    private BitmapDrawable setImageFormWeb(String url)
    {
        if(url != null && LruCacheUtil.getLruCache().get(url) != null)
            return LruCacheUtil.getLruCache().get(url);
        else
        {
            ImageTaskUtil it = new ImageTaskUtil(listView);
            it.execute(url);
            return null;
        }
    }

    public interface ArticleAdapterListener
    {
        void onCardClick(int viewId, int position);
        void onLinkClick(String url);
    }
}
