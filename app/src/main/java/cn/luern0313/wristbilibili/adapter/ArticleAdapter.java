package cn.luern0313.wristbilibili.adapter;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.LruCache;
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
import org.sufficientlysecure.htmltextview.OnClickATagListener;

import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.Nullable;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.models.article.ArticleCardModel;
import cn.luern0313.wristbilibili.util.ArticleHtmlImageHandlerUtil;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.ImageDownloaderUtil;

/**
 * 被 luern0313 创建于 2020/2/25.
 */
public class ArticleAdapter extends BaseAdapter
{
    private LayoutInflater mInflater;

    private LruCache<String, BitmapDrawable> mImageCache;
    private ArticleListener articleListener;

    private ArrayList<ArticleCardModel> articleCardModelArrayList;
    private ListView listView;

    private int img_width;

    public ArticleAdapter(LayoutInflater inflater, int img_width, ArrayList<ArticleCardModel> articleCardModelArrayList, ListView listView, ArticleListener articleListener)
    {
        mInflater = inflater;
        this.img_width = img_width;
        this.articleCardModelArrayList = articleCardModelArrayList;
        this.listView = listView;
        this.articleListener = articleListener;

        int maxCache = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxCache / 8;
        mImageCache = new LruCache<String, BitmapDrawable>(cacheSize)
        {
            @Override
            protected int sizeOf(String key, BitmapDrawable value)
            {
                try
                {
                    return value.getBitmap().getByteCount();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                return 0;
            }
        };
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
        ArticleCardModel articleCardModel = articleCardModelArrayList.get(position);
        switch (articleCardModel.article_card_identity.substring(0, 2))
        {
            case "te":
                return 0;
            case "av":
                return 1;
            case "ss":
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
            ArticleCardModel.ArticleTextModel articleTextModel = (ArticleCardModel.ArticleTextModel) articleCardModelArrayList.get(position);
            Document e = Jsoup.parse(articleTextModel.article_text_element);
            if(e.text().equals("") && e.getElementsByTag("img").isEmpty())
                articleTextViewHolder.text_text.setVisibility(View.GONE);
            else
            {
                articleTextViewHolder.text_text.setHtml(articleTextModel.article_text_element, new ArticleHtmlImageHandlerUtil(listView.getContext(), mImageCache, articleTextViewHolder.text_text, img_width, articleTextModel.article_text_articleImageModel));
                articleTextViewHolder.text_text.setOnClickATagListener(new OnClickATagListener()
                {
                    @Override
                    public void onClick(View widget, @Nullable String href)
                    {
                        articleListener.onLinkClick(href);
                    }
                });
                articleTextViewHolder.text_text.setVisibility(View.VISIBLE);
                articleTextViewHolder.text_text.setOnClickListener(onViewClick(position));
            }
        }
        else if(type == 1)
        {
            ArticleCardModel.ArticleVideoCardModel articleVideoCardModel = (ArticleCardModel.ArticleVideoCardModel) articleCardModelArrayList.get(position);

            Drawable upDrawable = convertView.getResources().getDrawable(R.drawable.icon_video_up);
            Drawable playNumDrawable = convertView.getResources().getDrawable(R.drawable.icon_video_play_num_white);
            Drawable danmakuNumDrawable = convertView.getResources().getDrawable(R.drawable.icon_video_danmu_num_white);
            upDrawable.setBounds(0, 0, DataProcessUtil.dip2px(listView.getContext(), 12), DataProcessUtil.dip2px(listView.getContext(), 12));
            playNumDrawable.setBounds(0,0, DataProcessUtil.dip2px(listView.getContext(), 12), DataProcessUtil.dip2px(listView.getContext(), 12));
            danmakuNumDrawable.setBounds(0,0, DataProcessUtil.dip2px(listView.getContext(), 12), DataProcessUtil.dip2px(listView.getContext(), 12));
            articleVideoCardViewHolder.video_up_name.setCompoundDrawables(upDrawable,null, null,null);
            articleVideoCardViewHolder.video_play.setCompoundDrawables(playNumDrawable,null, null,null);
            articleVideoCardViewHolder.video_danmaku.setCompoundDrawables(danmakuNumDrawable,null, null,null);

            articleVideoCardViewHolder.video_title.setText(articleVideoCardModel.article_video_card_title);
            articleVideoCardViewHolder.video_cover.setImageResource(R.drawable.img_default_vid);
            articleVideoCardViewHolder.video_play.setText(articleVideoCardModel.article_video_card_play);
            articleVideoCardViewHolder.video_danmaku.setText(articleVideoCardModel.article_video_card_danmaku);
            articleVideoCardViewHolder.video_time.setText(articleVideoCardModel.article_video_card_time);
            articleVideoCardViewHolder.video_up_name.setText(articleVideoCardModel.article_video_card_up_name);

            articleVideoCardViewHolder.video_lay.setOnClickListener(onViewClick(position));

            articleVideoCardViewHolder.video_cover.setTag(articleVideoCardModel.article_video_card_cover);
            BitmapDrawable c = setImageFormWeb(articleVideoCardModel.article_video_card_cover);
            if(c != null) articleVideoCardViewHolder.video_cover.setImageDrawable(c);
        }
        else if(type == 2)
        {
            ArticleCardModel.ArticleBangumiCardModel articleBangumiCardModel = (ArticleCardModel.ArticleBangumiCardModel) articleCardModelArrayList.get(position);

            Drawable playNumDrawable = convertView.getResources().getDrawable(R.drawable.icon_video_play_num);
            Drawable followNumDrawable = convertView.getResources().getDrawable(R.drawable.icon_video_like_num);
            playNumDrawable.setBounds(0,0, DataProcessUtil.dip2px(listView.getContext(), 12), DataProcessUtil.dip2px(listView.getContext(), 12));
            followNumDrawable.setBounds(0,0, DataProcessUtil.dip2px(listView.getContext(), 12), DataProcessUtil.dip2px(listView.getContext(), 12));
            articleBangumiCardViewHolder.bangumi_play.setCompoundDrawables(playNumDrawable,null, null,null);
            articleBangumiCardViewHolder.bangumi_follow.setCompoundDrawables(followNumDrawable,null, null,null);

            articleBangumiCardViewHolder.bangumi_title.setText(articleBangumiCardModel.article_bangumi_card_title);
            articleBangumiCardViewHolder.bangumi_cover.setImageResource(R.drawable.img_default_animation);
            articleBangumiCardViewHolder.bangumi_play.setText(articleBangumiCardModel.article_bangumi_card_play);
            articleBangumiCardViewHolder.bangumi_follow.setText(articleBangumiCardModel.article_bangumi_card_follow);
            articleBangumiCardViewHolder.bangumi_type.setText(articleBangumiCardModel.article_bangumi_card_type_name);
            articleBangumiCardViewHolder.bangumi_score.setText(articleBangumiCardModel.article_bangumi_card_score);

            articleBangumiCardViewHolder.bangumi_lay.setOnClickListener(onViewClick(position));

            articleBangumiCardViewHolder.bangumi_cover.setTag(articleBangumiCardModel.article_bangumi_card_cover);
            BitmapDrawable c = setImageFormWeb(articleBangumiCardModel.article_bangumi_card_cover);
            if(c != null) articleBangumiCardViewHolder.bangumi_cover.setImageDrawable(c);
        }
        else if(type == 3)
        {
            ArticleCardModel.ArticleArticleCardModel articleArticleCardModel = (ArticleCardModel.ArticleArticleCardModel) articleCardModelArrayList.get(position);

            Drawable upDrawable = convertView.getResources().getDrawable(R.drawable.icon_video_up);
            Drawable playNumDrawable = convertView.getResources().getDrawable(R.drawable.icon_video_play_num_white);
            Drawable replyNumDrawable = convertView.getResources().getDrawable(R.drawable.icon_video_reply_num_white);
            upDrawable.setBounds(0,0, DataProcessUtil.dip2px(listView.getContext(), 12), DataProcessUtil.dip2px(listView.getContext(), 12));
            playNumDrawable.setBounds(0,0, DataProcessUtil.dip2px(listView.getContext(), 12), DataProcessUtil.dip2px(listView.getContext(), 12));
            replyNumDrawable.setBounds(0,0, DataProcessUtil.dip2px(listView.getContext(), 12), DataProcessUtil.dip2px(listView.getContext(), 12));
            articleArticleCardViewHolder.article_up_name.setCompoundDrawables(upDrawable,null, null,null);
            articleArticleCardViewHolder.article_view.setCompoundDrawables(playNumDrawable,null, null,null);
            articleArticleCardViewHolder.article_reply.setCompoundDrawables(replyNumDrawable,null, null,null);

            articleArticleCardViewHolder.article_title.setText(articleArticleCardModel.article_article_card_title);
            articleArticleCardViewHolder.article_cover.setImageResource(R.drawable.img_default_vid);
            articleArticleCardViewHolder.article_view.setText(articleArticleCardModel.article_article_card_view);
            articleArticleCardViewHolder.article_reply.setText(articleArticleCardModel.article_article_card_reply);
            articleArticleCardViewHolder.article_up_name.setText(articleArticleCardModel.article_article_card_up_name);

            articleArticleCardViewHolder.article_lay.setOnClickListener(onViewClick(position));

            articleArticleCardViewHolder.article_cover.setTag(articleArticleCardModel.article_article_card_cover);
            BitmapDrawable c = setImageFormWeb(articleArticleCardModel.article_article_card_cover);
            if(c != null) articleArticleCardViewHolder.article_cover.setImageDrawable(c);
        }
        else if(type == 4)
        {
            ArticleCardModel.ArticleMusicCardModel articleMusicCardModel = (ArticleCardModel.ArticleMusicCardModel) articleCardModelArrayList.get(position);

            Drawable upDrawable = convertView.getResources().getDrawable(R.drawable.icon_video_up);
            Drawable playNumDrawable = convertView.getResources().getDrawable(R.drawable.icon_video_play_num);
            Drawable danmakuNumDrawable = convertView.getResources().getDrawable(R.drawable.icon_video_danmu_num);
            upDrawable.setBounds(0,0, DataProcessUtil.dip2px(listView.getContext(), 12), DataProcessUtil.dip2px(listView.getContext(), 12));
            playNumDrawable.setBounds(0,0, DataProcessUtil.dip2px(listView.getContext(), 12), DataProcessUtil.dip2px(listView.getContext(), 12));
            danmakuNumDrawable.setBounds(0,0, DataProcessUtil.dip2px(listView.getContext(), 12), DataProcessUtil.dip2px(listView.getContext(), 12));
            articleMusicCardViewHolder.music_up_name.setCompoundDrawables(upDrawable,null, null,null);
            articleMusicCardViewHolder.music_play.setCompoundDrawables(playNumDrawable,null, null,null);
            articleMusicCardViewHolder.music_reply.setCompoundDrawables(danmakuNumDrawable,null, null,null);

            articleMusicCardViewHolder.music_title.setText(articleMusicCardModel.article_music_card_title);
            articleMusicCardViewHolder.music_cover.setImageResource(R.drawable.img_default_animation);
            articleMusicCardViewHolder.music_play.setText(articleMusicCardModel.article_music_card_play);
            articleMusicCardViewHolder.music_reply.setText(articleMusicCardModel.article_music_card_reply);
            articleMusicCardViewHolder.music_up_name.setText(articleMusicCardModel.article_music_card_up_name);

            articleMusicCardViewHolder.music_lay.setOnClickListener(onViewClick(position));

            articleMusicCardViewHolder.music_cover.setTag(articleMusicCardModel.article_music_card_cover);
            BitmapDrawable c = setImageFormWeb(articleMusicCardModel.article_music_card_cover);
            if(c != null) articleMusicCardViewHolder.music_cover.setImageDrawable(c);
        }
        else if(type == 5)
        {
            ArticleCardModel.ArticleTicketCardModel articleTicketCardModel = (ArticleCardModel.ArticleTicketCardModel) articleCardModelArrayList.get(position);

            articleTicketCardViewHolder.ticket_title.setText(articleTicketCardModel.article_ticket_card_title);
            articleTicketCardViewHolder.ticket_cover.setImageResource(R.drawable.img_default_animation);
            articleTicketCardViewHolder.ticket_time.setText(articleTicketCardModel.article_ticket_card_time);
            articleTicketCardViewHolder.ticket_location.setText(articleTicketCardModel.article_ticket_card_location);
            articleTicketCardViewHolder.ticket_price.setText(articleTicketCardModel.article_ticket_card_price);

            articleTicketCardViewHolder.ticket_lay.setOnClickListener(onViewClick(position));

            articleTicketCardViewHolder.ticket_cover.setTag(articleTicketCardModel.article_ticket_card_cover);
            BitmapDrawable c = setImageFormWeb(articleTicketCardModel.article_ticket_card_cover);
            if(c != null) articleTicketCardViewHolder.ticket_cover.setImageDrawable(c);
        }
        else if(type == 6)
        {
            ArticleCardModel.ArticleShopCardModel articleShopCardModel = (ArticleCardModel.ArticleShopCardModel) articleCardModelArrayList.get(position);

            articleShopCardViewHolder.shop_title.setText(articleShopCardModel.article_shop_card_title);
            articleShopCardViewHolder.shop_cover.setImageResource(R.drawable.img_default_animation);
            articleShopCardViewHolder.shop_detail.setText(articleShopCardModel.article_shop_card_detail);
            articleShopCardViewHolder.shop_price.setText(articleShopCardModel.article_shop_card_price);

            articleShopCardViewHolder.shop_lay.setOnClickListener(onViewClick(position));

            articleShopCardViewHolder.shop_cover.setTag(articleShopCardModel.article_shop_card_cover);
            BitmapDrawable c = setImageFormWeb(articleShopCardModel.article_shop_card_cover);
            if(c != null) articleShopCardViewHolder.shop_cover.setImageDrawable(c);
        }
        else if(type == 7)
        {
            ArticleCardModel.ArticleContainerCardModel articleContainerCardModel = (ArticleCardModel.ArticleContainerCardModel) articleCardModelArrayList.get(position);

            articleContainerCardViewHolder.container_title.setText(articleContainerCardModel.article_container_card_title);
            articleContainerCardViewHolder.container_cover.setImageResource(R.drawable.img_default_animation);
            articleContainerCardViewHolder.container_detail.setText(articleContainerCardModel.article_container_card_detail);
            articleContainerCardViewHolder.container_author.setText(articleContainerCardModel.article_container_card_author);

            articleContainerCardViewHolder.container_lay.setOnClickListener(onViewClick(position));

            articleContainerCardViewHolder.container_cover.setTag(articleContainerCardModel.article_container_card_cover);
            BitmapDrawable c = setImageFormWeb(articleContainerCardModel.article_container_card_cover);
            if(c != null) articleContainerCardViewHolder.container_cover.setImageDrawable(c);
        }
        else if(type == 8)
        {
            ArticleCardModel.ArticleLiveCardModel articleLiveCardModel = (ArticleCardModel.ArticleLiveCardModel) articleCardModelArrayList.get(position);

            Drawable upDrawable = convertView.getResources().getDrawable(R.drawable.icon_video_up);
            Drawable viewerDrawable = convertView.getResources().getDrawable(R.drawable.icon_video_viewer_num_white);
            upDrawable.setBounds(0, 0, DataProcessUtil.dip2px(listView.getContext(), 12), DataProcessUtil.dip2px(listView.getContext(), 12));
            viewerDrawable.setBounds(0, 0, DataProcessUtil.dip2px(listView.getContext(), 12), DataProcessUtil.dip2px(listView.getContext(), 12));
            articleLiveCardViewHolder.live_up_name.setCompoundDrawables(upDrawable,null, null,null);
            articleLiveCardViewHolder.live_online.setCompoundDrawables(viewerDrawable,null, null,null);

            if(articleLiveCardModel.article_live_card_status == 0)
            {
                articleLiveCardViewHolder.live_status.setText("未开播");
                articleLiveCardViewHolder.live_status.setBackgroundResource(R.drawable.shape_bg_article_card_live_notlive);
                articleLiveCardViewHolder.live_online.setText("--");
            }
            else
            {
                articleLiveCardViewHolder.live_status.setText("直播中");
                articleLiveCardViewHolder.live_status.setBackgroundResource(R.drawable.shape_bg_article_card_live_living);
                articleLiveCardViewHolder.live_online.setText(articleLiveCardModel.article_live_card_online);
            }

            articleLiveCardViewHolder.live_title.setText(articleLiveCardModel.article_live_card_title);
            articleLiveCardViewHolder.live_cover.setImageResource(R.drawable.img_default_vid);
            articleLiveCardViewHolder.live_area.setText(articleLiveCardModel.article_live_card_area);
            articleLiveCardViewHolder.live_up_name.setText(articleLiveCardModel.article_live_card_up_name);

            articleLiveCardViewHolder.live_lay.setOnClickListener(onViewClick(position));

            articleLiveCardViewHolder.live_cover.setTag(articleLiveCardModel.article_live_card_cover);
            BitmapDrawable c = setImageFormWeb(articleLiveCardModel.article_live_card_cover);
            if(c != null) articleLiveCardViewHolder.live_cover.setImageDrawable(c);
        }
        
        return convertView;
    }

    class ArticleTextViewHolder
    {
        HtmlTextView text_text;
    }

    class ArticleVideoCardViewHolder
    {
        RelativeLayout video_lay;
        TextView video_title;
        ImageView video_cover;
        TextView video_play;
        TextView video_danmaku;
        TextView video_time;
        TextView video_up_name;
    }

    class ArticleBangumiCardViewHolder
    {
        RelativeLayout bangumi_lay;
        TextView bangumi_title;
        ImageView bangumi_cover;
        TextView bangumi_play;
        TextView bangumi_follow;
        TextView bangumi_type;
        TextView bangumi_score;
    }

    class ArticleArticleCardViewHolder
    {
        RelativeLayout article_lay;
        TextView article_title;
        ImageView article_cover;
        TextView article_view;
        TextView article_reply;
        TextView article_up_name;
    }

    class ArticleMusicCardViewHolder
    {
        RelativeLayout music_lay;
        TextView music_title;
        ImageView music_cover;
        TextView music_play;
        TextView music_reply;
        TextView music_up_name;
    }

    class ArticleTicketCardViewHolder
    {
        RelativeLayout ticket_lay;
        TextView ticket_title;
        ImageView ticket_cover;
        TextView ticket_time;
        TextView ticket_location;
        TextView ticket_price;
    }

    class ArticleShopCardViewHolder
    {
        RelativeLayout shop_lay;
        TextView shop_title;
        ImageView shop_cover;
        TextView shop_detail;
        TextView shop_price;
    }

    class ArticleContainerCardViewHolder
    {
        RelativeLayout container_lay;
        TextView container_title;
        ImageView container_cover;
        TextView container_detail;
        TextView container_author;
    }

    class ArticleLiveCardViewHolder
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
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                articleListener.onCardClick(v.getId(), position);
            }
        };
    }

    private BitmapDrawable setImageFormWeb(String url)
    {
        if(mImageCache.get(url) != null)
        {
            return mImageCache.get(url);
        }
        else
        {
            ImageTask it = new ImageTask(listView);
            it.execute(url);
            return null;
        }
    }

    class ImageTask extends AsyncTask<String, Void, BitmapDrawable>
    {
        private String imageUrl;
        private Resources listViewResources;

        ImageTask(ListView listView)
        {
            this.listViewResources = listView.getResources();
        }

        @Override
        protected BitmapDrawable doInBackground(String... params)
        {
            try
            {
                imageUrl = params[0];
                Bitmap bitmap = null;
                bitmap = ImageDownloaderUtil.downloadImage(imageUrl);
                BitmapDrawable db = new BitmapDrawable(listViewResources, bitmap);
                // 如果本地还没缓存该图片，就缓存
                if(mImageCache.get(imageUrl) == null && bitmap != null)
                {
                    mImageCache.put(imageUrl, db);
                }
                return db;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(BitmapDrawable result)
        {
            // 通过Tag找到我们需要的ImageView，如果该ImageView所在的item已被移出页面，就会直接返回null
            ImageView iv = listView.findViewWithTag(imageUrl);
            if(iv != null && result != null)
            {
                iv.setImageDrawable(result);
            }
        }
    }

    public interface ArticleListener
    {
        void onCardClick(int viewId, int position);
        void onLinkClick(String url);
    }
}
