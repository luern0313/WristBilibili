package cn.luern0313.wristbilibili.models.article;

import org.json.JSONObject;
import org.jsoup.nodes.Element;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cn.luern0313.wristbilibili.util.DataProcessUtil;

/**
 * 被 luern0313 创建于 2020/2/28.
 */
public class ArticleCardModel implements Serializable
{
    public String article_card_identity;
    public String article_card_url;

    public class ArticleTextModel extends ArticleCardModel implements Serializable
    {
        public String article_text_element;
        public ArticleImageModel article_text_articleImageModel;
        ArticleTextModel(Element element)
        {
            article_card_identity = "te";
            article_card_url = "";

            article_text_element = element.outerHtml();
            Element imgElement = element.select("figure[class=img-box] > img").first();
            if(imgElement != null)
                article_text_articleImageModel = new ArticleImageModel(imgElement.attributes());
        }
    }

    public class ArticleVideoCardModel extends ArticleCardModel implements Serializable
    {
        public String article_video_card_id;
        public String article_video_card_title;
        public String article_video_card_cover;
        public String article_video_card_time;
        public String article_video_card_play;
        public String article_video_card_danmaku;
        public String article_video_card_up_name;

        ArticleVideoCardModel(String identity, JSONObject videoCard)
        {
            article_card_identity = identity;
            article_card_url = "bilibili://video/" + videoCard.optString("bvid");

            article_video_card_id = String.valueOf(videoCard.optString("bvid"));
            article_video_card_title = videoCard.optString("title");
            article_video_card_cover = videoCard.optString("pic");
            article_video_card_time = DataProcessUtil.getMinFromSec(videoCard.optInt("duration"));

            JSONObject stat = videoCard.has("stat") ? videoCard.optJSONObject("stat") : new JSONObject();
            article_video_card_play = DataProcessUtil.getView(stat.optInt("view"));
            article_video_card_danmaku = DataProcessUtil.getView(stat.optInt("danmaku"));

            JSONObject up = videoCard.has("owner") ? videoCard.optJSONObject("owner") : new JSONObject();
            article_video_card_up_name = up.optString("name");
        }
    }

    public class ArticleBangumiCardModel extends ArticleCardModel implements Serializable
    {
        public String article_bangumi_card_id;
        public String article_bangumi_card_title;
        public String article_bangumi_card_cover;
        public String article_bangumi_card_play;
        public String article_bangumi_card_follow;
        public String article_bangumi_card_type_name;
        public String article_bangumi_card_score;

        ArticleBangumiCardModel(String identity, JSONObject bangumiCard)
        {
            article_card_identity = identity;
            article_card_url = "bilibili://bangumi/season/" + bangumiCard.optInt("season_id");

            article_bangumi_card_id = String.valueOf(bangumiCard.optInt("season_id"));
            article_bangumi_card_title = bangumiCard.optString("title");
            article_bangumi_card_cover = bangumiCard.optString("cover");
            article_bangumi_card_play = DataProcessUtil.getView(bangumiCard.optInt("play_count"));
            article_bangumi_card_follow = DataProcessUtil.getView(bangumiCard.optInt("follow_count"));
            article_bangumi_card_type_name = bangumiCard.optString("season_type_name");
            JSONObject score = bangumiCard.has("rating") ? bangumiCard.optJSONObject("rating") : new JSONObject();
            article_bangumi_card_score = String.valueOf(score.optDouble("score"));
        }
    }

    public class ArticleArticleCardModel extends ArticleCardModel implements Serializable
    {
        public String article_article_card_id;
        public String article_article_card_title;
        public String article_article_card_cover;
        public String article_article_card_view;
        public String article_article_card_reply;
        public String article_article_card_up_name;

        ArticleArticleCardModel(String identity, JSONObject articleCard)
        {
            article_card_identity = identity;
            article_card_url = "bilibili://article/" + articleCard.optInt("id");

            article_article_card_id = String.valueOf(articleCard.optInt("id"));
            article_article_card_title = articleCard.optString("title");
            article_article_card_cover = articleCard.has("image_urls") ?
                    articleCard.optJSONArray("image_urls").optString(0) : "";

            JSONObject stat = articleCard.has("stats") ? articleCard.optJSONObject("stats") : new JSONObject();
            article_article_card_view = DataProcessUtil.getView(stat.optInt("view"));
            article_article_card_reply = DataProcessUtil.getView(stat.optInt("reply"));

            article_article_card_up_name = articleCard.has("author") ?
                    articleCard.optJSONObject("author").optString("name") : "";
        }
    }

    public class ArticleMusicCardModel extends ArticleCardModel implements Serializable
    {
        public String article_music_card_id;
        public String article_music_card_title;
        public String article_music_card_cover;
        public String article_music_card_play;
        public String article_music_card_reply;
        public String article_music_card_up_name;

        ArticleMusicCardModel(String identity, JSONObject musicCard)
        {
            article_card_identity = identity;
            article_card_url = "bilibili://music/detail/" + musicCard.optInt("song_id");

            article_music_card_id = String.valueOf(musicCard.optInt("song_id"));
            article_music_card_title = musicCard.optString("title");
            article_music_card_cover = musicCard.optString("cover_url");
            article_music_card_play = DataProcessUtil.getView(musicCard.optInt("play_num"));
            article_music_card_reply = DataProcessUtil.getView(musicCard.optInt("reply_num"));
            article_music_card_up_name = musicCard.optString("up_name");
        }
    }

    public class ArticleTicketCardModel extends ArticleCardModel implements Serializable
    {
        public String article_ticket_card_id;
        public String article_ticket_card_title;
        public String article_ticket_card_cover;
        public String article_ticket_card_time;
        public String article_ticket_card_location;
        public String article_ticket_card_price;

        ArticleTicketCardModel(String identity, JSONObject ticketCard)
        {
            article_card_identity = identity;
            article_card_url = "bilibili://show/" + ticketCard.optInt("id");

            article_ticket_card_id = String.valueOf(ticketCard.optInt("id"));
            article_ticket_card_title = ticketCard.optString("name");
            article_ticket_card_cover = "http:" + ticketCard.optString("performance_image");

            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
            article_ticket_card_time = format.format(new Date(ticketCard.optInt("start_time") * 1000L));

            article_ticket_card_location = ticketCard.optString("city_name") + " " + ticketCard.optString("venue_name");
            article_ticket_card_price = "¥" + ticketCard.optInt("price_low");
        }
    }

    public class ArticleShopCardModel extends ArticleCardModel implements Serializable
    {
        public String article_shop_card_id;
        public String article_shop_card_title;
        public String article_shop_card_cover;
        public String article_shop_card_detail;
        public String article_shop_card_price;
        ArticleShopCardModel(String identity, JSONObject shopCard)
        {
            article_card_identity = identity;
            article_card_url = "bilibili://mall/" + shopCard.optInt("itemsId");

            article_shop_card_id = String.valueOf(shopCard.optInt("itemsId"));
            article_shop_card_title = shopCard.optString("name");
            article_shop_card_cover = shopCard.has("img") ?
                    "http:" + shopCard.optJSONArray("img").optString(0) : "";
            article_shop_card_detail = shopCard.optString("brief");
            article_shop_card_price = "￥" + shopCard.optInt("price") / 100.0;
        }
    }

    public class ArticleContainerCardModel extends ArticleCardModel implements Serializable
    {
        public String article_container_card_id;
        public String article_container_card_title;
        public String article_container_card_cover;
        public String article_container_card_detail;
        public String article_container_card_author;

        ArticleContainerCardModel(String identity, JSONObject containerCard)
        {
            article_card_identity = identity;
            article_card_url = "bilibili://manga/" + containerCard.optInt("id");

            article_container_card_id = String.valueOf(containerCard.optInt("id"));
            article_container_card_title = containerCard.optString("title");
            article_container_card_cover = containerCard.optString("vertical_cover");
            article_container_card_detail = containerCard.optString("evaluate");
            article_container_card_author = containerCard.has("author") ?
                    containerCard.optJSONArray("author").optString(0) : "";
        }
    }

    public class ArticleLiveCardModel extends ArticleCardModel implements Serializable
    {
        public String article_live_card_id;
        public String article_live_card_title;
        public String article_live_card_cover;
        public String article_live_card_area;
        public int article_live_card_status;
        public String article_live_card_online;
        public String article_live_card_up_name;
        ArticleLiveCardModel(String identity, JSONObject liveCard)
        {
            article_card_identity = identity;
            article_card_url = "bilibili://live/" + liveCard.optInt("room_id");

            article_live_card_id = String.valueOf(liveCard.optInt("room_id"));
            article_live_card_title = liveCard.optString("title");
            article_live_card_cover = liveCard.optString("cover");
            article_live_card_area = liveCard.optString("area_v2_name");
            article_live_card_status = liveCard.optInt("live_status");
            article_live_card_online = String.valueOf(liveCard.optInt("online"));
            article_live_card_up_name = liveCard.optString("uname");
        }
    }
}
