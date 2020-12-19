package cn.luern0313.wristbilibili.models.article;

import org.jsoup.nodes.Element;

import java.io.Serializable;

import cn.luern0313.lson.annotation.field.LsonAddPrefix;
import cn.luern0313.lson.annotation.field.LsonDateFormat;
import cn.luern0313.lson.annotation.field.LsonNumberOperations;
import cn.luern0313.lson.annotation.field.LsonPath;
import cn.luern0313.lson.annotation.method.LsonCallMethod;
import cn.luern0313.wristbilibili.util.json.ImageUrlFormat;
import cn.luern0313.wristbilibili.util.json.TimeFormat;
import cn.luern0313.wristbilibili.util.json.ViewFormat;
import lombok.Getter;
import lombok.Setter;

/**
 * 被 luern0313 创建于 2020/2/28.
 */
public class ArticleCardModel
{
    @Getter
    @Setter
    public abstract static class ArticleCardBaseModel
    {
        private String cardIdentity;
        private String cardUrl;
    }

    @Getter
    @Setter
    public static class ArticleCardTextModel extends ArticleCardBaseModel implements Serializable
    {
        private String textElement;
        private ArticleImageModel textArticleImageModel;
        ArticleCardTextModel(Element element)
        {
            setCardIdentity("te");
            setCardUrl("");

            textElement = element.outerHtml();
            Element imgElement = element.select("figure[class=img-box] > img").first();
            if(imgElement != null)
                textArticleImageModel = new ArticleImageModel(imgElement.attributes());
        }
    }

    @Getter
    @Setter
    public static class ArticleCardVideoCardModel extends ArticleCardBaseModel implements Serializable
    {
        @LsonPath("bvid")
        private String videoCardId;

        @LsonPath("title")
        private String videoCardTitle;

        @ImageUrlFormat
        @LsonPath("pic")
        private String article_video_card_cover;

        @TimeFormat
        @LsonPath("duration")
        private String article_video_card_time;

        @ViewFormat
        @LsonPath("stat.view")
        private String article_video_card_play;

        @ViewFormat
        @LsonPath("stat.danmaku")
        private String article_video_card_danmaku;

        @LsonPath("owner.name")
        private String article_video_card_up_name;

        @LsonCallMethod(timing = LsonCallMethod.CallMethodTiming.AFTER_DESERIALIZATION)
        void initCardUrl()
        {
            setCardUrl("bilibili://video/" + videoCardId);
        }

        public ArticleCardVideoCardModel(String identity)
        {
            setCardIdentity(identity);
        }
    }

    @Getter
    @Setter
    public static class ArticleCardBangumiCardModel extends ArticleCardBaseModel implements Serializable
    {
        @LsonPath("season_id")
        private String bangumiCardId;

        @LsonPath("title")
        private String bangumiCardTitle;

        @ImageUrlFormat
        @LsonPath("cover")
        private String bangumiCardCover;

        @ViewFormat
        @LsonPath("play_count")
        private String bangumiCardPlay;

        @ViewFormat
        @LsonPath("follow_count")
        private String bangumiCardFollow;

        @LsonPath("season_type_name")
        private String bangumiCardTypeName;

        @LsonPath("rating.score")
        private String bangumiCardScore;

        @LsonCallMethod(timing = LsonCallMethod.CallMethodTiming.AFTER_DESERIALIZATION)
        private void initCardUrl()
        {
            setCardUrl("bilibili://bangumi/season/" + bangumiCardId);
        }

        public ArticleCardBangumiCardModel(String identity)
        {
            setCardIdentity(identity);
        }
    }

    @Getter
    @Setter
    public static class ArticleArticleCardCardModel extends ArticleCardBaseModel implements Serializable
    {
        @LsonPath("id")
        private String articleCardId;

        @LsonPath("title")
        private String articleCardTitle;

        @ImageUrlFormat
        @LsonPath("image_urls[0]")
        private String articleCardCover;

        @ViewFormat
        @LsonPath("stats.view")
        private String articleCardView;

        @ViewFormat
        @LsonPath("stats.reply")
        private String articleCardReply;

        @LsonPath("author.name")
        private String articleCardUpName;

        @LsonCallMethod(timing = LsonCallMethod.CallMethodTiming.AFTER_DESERIALIZATION)
        private void initCardUrl()
        {
            setCardUrl("bilibili://article/" + articleCardId);
        }

        public ArticleArticleCardCardModel(String identity)
        {
            setCardIdentity(identity);
        }
    }

    @Getter
    @Setter
    public static class ArticleCardMusicCardModel extends ArticleCardBaseModel implements Serializable
    {
        @LsonPath("song_id")
        private String musicCardId;

        @LsonPath("title")
        private String musicCardTitle;

        @ImageUrlFormat
        @LsonPath("cover_url")
        private String musicCardCover;

        @ViewFormat
        @LsonPath("play_num")
        private String musicCardPlay;

        @ViewFormat
        @LsonPath("reply_num")
        private String musicCardReply;

        @LsonPath("up_name")
        private String musicCardUpName;

        @LsonCallMethod(timing = LsonCallMethod.CallMethodTiming.AFTER_DESERIALIZATION)
        private void initCardUrl()
        {
            setCardUrl("bilibili://music/detail/" + musicCardId);
        }

        public ArticleCardMusicCardModel(String identity)
        {
            setCardIdentity(identity);
        }
    }

    @Getter
    @Setter
    public static class ArticleCardTicketCardModel extends ArticleCardBaseModel implements Serializable
    {
        @LsonPath("id")
        private String ticketCardId;

        @LsonPath("name")
        private String ticketCardTitle;

        @LsonAddPrefix("http:")
        @ImageUrlFormat
        @LsonPath("performance_image")
        private String ticketCardCover;

        @LsonDateFormat("yyyy/MM/dd")
        @LsonPath("start_time")
        private String ticketCardTime;

        @LsonPath("city_name")
        private String ticketCardLocationCity;

        @LsonPath("venue_name")
        private String ticketCardLocationVenue;

        @LsonAddPrefix("¥")
        @LsonPath("price_low")
        private String ticketCardPrice;

        @LsonCallMethod(timing = LsonCallMethod.CallMethodTiming.AFTER_DESERIALIZATION)
        private void initCardUrl()
        {
            setCardUrl("bilibili://show/" + ticketCardId);
        }

        public ArticleCardTicketCardModel(String identity)
        {
            setCardIdentity(identity);
        }
    }

    @Getter
    @Setter
    public static class ArticleCardShopCardModel extends ArticleCardBaseModel implements Serializable
    {
        @LsonPath("itemsId")
        private String shopCardId;

        @LsonPath("name")
        private String shopCardTitle;

        @LsonAddPrefix("http:")
        @ImageUrlFormat
        @LsonPath("img[0]")
        private String shopCardCover;

        @LsonPath("brief")
        private String shopCardDetail;

        @LsonNumberOperations(operator = LsonNumberOperations.Operator.DIVISION, number = 100)
        @LsonAddPrefix("￥")
        @LsonPath(value = "price", annotationsOrder = {LsonNumberOperations.class, LsonAddPrefix.class})
        private String shopCardPrice;

        @LsonCallMethod(timing = LsonCallMethod.CallMethodTiming.AFTER_DESERIALIZATION)
        private void initCardUrl()
        {
            setCardUrl("bilibili://mall/" + shopCardId);
        }

        public ArticleCardShopCardModel(String identity)
        {
            setCardIdentity(identity);
        }
    }

    @Getter
    @Setter
    public static class ArticleCardContainerCardModel extends ArticleCardBaseModel implements Serializable
    {
        @LsonPath("id")
        private String containerCardId;

        @LsonPath("title")
        private String containerCardTitle;

        @ImageUrlFormat
        @LsonPath("vertical_cover")
        private String containerCardCover;

        @LsonPath("evaluate")
        private String containerCardDetail;

        @LsonPath("author[0]")
        private String containerCardAuthor;

        @LsonCallMethod(timing = LsonCallMethod.CallMethodTiming.AFTER_DESERIALIZATION)
        private void initCardUrl()
        {
            setCardUrl("bilibili://manga/" + containerCardId);
        }

        public ArticleCardContainerCardModel(String identity)
        {
            setCardIdentity(identity);
        }
    }

    @Getter
    @Setter
    public static class ArticleCardLiveCardModel extends ArticleCardBaseModel implements Serializable
    {
        @LsonPath("room_id")
        public String liveCardId;

        @LsonPath("title")
        public String liveCardTitle;

        @ImageUrlFormat
        @LsonPath("cover")
        public String liveCardCover;

        @LsonPath("area_v2_name")
        public String liveCardArea;

        @LsonPath("live_status")
        public int liveCardStatus;

        @ViewFormat
        @LsonPath("online")
        public String liveCardOnline;

        @LsonPath("uname")
        public String liveCardUpName;

        @LsonCallMethod(timing = LsonCallMethod.CallMethodTiming.AFTER_DESERIALIZATION)
        private void initCardUrl()
        {
            setCardUrl("bilibili://live/" + liveCardId);
        }

        public ArticleCardLiveCardModel(String identity)
        {
            setCardIdentity(identity);
        }
    }
}
