package cn.luern0313.wristbilibili.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.LruCacheUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * 被 luern0313 创建于 2020/5/17.
 */
@Getter
@Setter
public class DynamicModel implements Serializable
{
    private int cardType;
    private boolean cardIsShared;
    private String cardId;
    private String cardReplyId;
    private String cardUrl;
    private String cardTime;

    private String cardAuthorName;
    private String cardAuthorUid;
    private String cardAuthorImg;
    private int cardAuthorOfficial = -1;
    private int cardAuthorVipStatus;
    private int cardAuthorVipType;

    private String cardShareNum;
    private String cardReplyNum;
    private int cardLikeNum;

    private boolean cardUserLike;

    private HashMap<String, Integer> cardEmoteSize = new HashMap<>();

    public DynamicModel(){}

    private DynamicModel(JSONObject card, JSONObject desc, JSONObject display, JSONObject extend, boolean isShared)
    {
        cardType = desc.optInt("type");
        this.cardIsShared = isShared;
        cardId = desc.optString("dynamic_id_str");
        cardReplyId = cardId;
        cardUrl = "bilibili://following/detail/" + cardId;
        cardTime = DataProcessUtil.getTime(desc.optInt("timestamp"), "MM-dd HH:mm");

        JSONObject author = desc.has("user_profile") ? desc.optJSONObject("user_profile") : new JSONObject();
        JSONObject author_info = author.has("info") ? author.optJSONObject("info") : new JSONObject();
        cardAuthorName = author_info.optString("uname");
        cardAuthorUid = String.valueOf(author_info.optInt("uid"));
        cardAuthorImg = LruCacheUtil.getImageUrl(author_info.optString("face"));
        JSONObject author_card = author.has("card") ? author.optJSONObject("card") : new JSONObject();
        JSONObject author_card_official = author_card.has("official_verify") ? author_card.optJSONObject("official_verify") : new JSONObject();
        cardAuthorOfficial = author_card_official.optInt("type", -1);
        JSONObject author_vip = author.has("vip") ? author.optJSONObject("vip") : new JSONObject();
        cardAuthorVipStatus = author_vip.optInt("vipStatus");
        cardAuthorVipType = author_vip.optInt("vipType");

        cardShareNum = DataProcessUtil.getView(desc.optInt("repost"));
        cardReplyNum = DataProcessUtil.getView(desc.optInt("comment"));
        cardLikeNum = desc.optInt("like");

        cardUserLike = desc.optInt("is_liked") == 1;
    }

    @Getter
    @Setter
    public class DynamicShareModel extends DynamicModel implements Serializable //1
    {
        private String shareTextOrg;
        private String shareText;
        private boolean shareTextExpand;

        private String shareOriginTips;
        private int shareOriginType;
        private DynamicModel shareOriginCard;

        public DynamicShareModel(JSONObject card, JSONObject desc, JSONObject display, JSONObject extend, boolean isShared)
        {
            super(card, desc, display, extend, isShared);
            try
            {
                JSONObject card_item = card.has("item") ? card.optJSONObject("item") : new JSONObject();
                shareTextOrg = card_item.optString("content");
                shareText = super.handlerText(shareTextOrg, display, extend);

                JSONObject card_origin = new JSONObject(card.optString("origin", "{}"));
                JSONObject desc_origin = desc.has("origin") ? desc.optJSONObject("origin") : new JSONObject();
                JSONObject display_origin = display.has("origin") ? display.optJSONObject("origin") : new JSONObject();
                JSONObject extend_origin = new JSONObject(card.optString("origin_extend_json", "{}"));

                shareOriginType = desc_origin.optInt("type");
                if(card_item.optInt("miss") == 1)
                {
                    shareOriginType = 9999;
                    shareOriginTips = card_item.optString("tips");
                }

                switch(shareOriginType)
                {
                    case 1:
                        shareOriginCard = new DynamicShareModel(card_origin, desc_origin, display_origin, extend_origin, true);
                        break;
                    case 2:
                        shareOriginCard = new DynamicAlbumModel(card_origin, desc_origin, display_origin, extend_origin, true);
                        break;
                    case 4:
                        shareOriginCard = new DynamicTextModel(card_origin, desc_origin, display_origin, extend_origin, true);
                        break;
                    case 8:
                        shareOriginCard = new DynamicVideoModel(card_origin, desc_origin, display_origin, extend_origin, true);
                        break;
                    case 64:
                        shareOriginCard = new DynamicArticleModel(card_origin, desc_origin, display_origin, extend_origin, true);
                        break;
                    case 512:
                    case 4098:
                    case 4099:
                    case 4101:
                        shareOriginCard = new DynamicBangumiModel(card_origin, desc_origin, display_origin, extend_origin, true);
                        break;
                    case 2048:
                        shareOriginCard = new DynamicUrlModel(card_origin, desc_origin, display_origin, extend_origin, true);
                        break;
                    case 4200:
                        shareOriginCard = new DynamicLiveModel(card_origin, desc_origin, display_origin, extend_origin, true);
                        break;
                    case 4300:
                        shareOriginCard = new DynamicFavorModel(card_origin, desc_origin, display_origin, extend_origin, true);
                        break;
                    default:
                        shareOriginCard = new DynamicUnknownModel(card_origin, desc_origin, display_origin, extend_origin, true, shareOriginTips);
                        break;
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }

    @Getter
    @Setter
    public class DynamicAlbumModel extends DynamicModel implements Serializable //2
    {
        private String albumId;
        private String albumAuthorName;
        private String albumAuthorUid;
        private String albumTextOrg;
        private String albumText;
        private boolean albumTextExpand;
        private ArrayList<String> albumImg;

        public DynamicAlbumModel(JSONObject card, JSONObject desc, JSONObject display, JSONObject extend, boolean isShared)
        {
            super(card, desc, display, extend, isShared);
            JSONObject card_user = card.has("user") ? card.optJSONObject("user") : new JSONObject();
            albumAuthorName = card_user.optString("name");
            albumAuthorUid = String.valueOf(card_user.optInt("uid"));

            JSONObject card_item = card.has("item") ? card.optJSONObject("item") : new JSONObject();
            albumTextOrg = card_item.optString("description");
            albumText = super.handlerText(albumTextOrg, display, extend);

            JSONArray card_img = card_item.optJSONArray("pictures");
            albumImg = new ArrayList<>();
            for(int i = 0; i < card_img.length(); i++)
                albumImg.add(LruCacheUtil.getImageUrl(card_img.optJSONObject(i).optString("img_src")));

            super.cardReplyId = String.valueOf(card_item.optInt("id"));
            super.cardReplyNum = DataProcessUtil.getView(card_item.optInt("reply"));
        }
    }

    @Getter
    @Setter
    public class DynamicTextModel extends DynamicModel implements Serializable //4
    {
        private String textAuthorName;
        private String textAuthorUid;
        private String textTextOrg;
        private String textText;
        private boolean textTextExpand;

        public DynamicTextModel(JSONObject card, JSONObject desc, JSONObject display, JSONObject extend, boolean isShared)
        {
            super(card, desc, display, extend, isShared);
            JSONObject card_user = card.has("user") ? card.optJSONObject("user") : new JSONObject();
            textAuthorName = card_user.optString("uname");
            textAuthorUid = String.valueOf(card_user.optInt("uid"));
            JSONObject card_item = card.has("item") ? card.optJSONObject("item") : new JSONObject();
            textTextOrg = card_item.optString("content");
            textText = super.handlerText(textTextOrg, display, extend);
        }
    }

    @Getter
    @Setter
    public class DynamicVideoModel extends DynamicModel implements Serializable //8
    {
        private String videoAuthorName;
        private String videoAuthorImg;
        private String videoAuthorUid;
        private String videoAid;
        private String videoBvid;
        private String videoDynamicOrg;
        private String videoDynamic;
        private boolean videoDynamicExpand;
        private String videoTitle;
        private String videoImg;
        private String videoDesc;
        private String videoDuration;
        private String videoPlay;
        private String videoDanmaku;
        public DynamicVideoModel(JSONObject card, JSONObject desc, JSONObject display, JSONObject extend, boolean isShared)
        {
            super(card, desc, display, extend, isShared);
            JSONObject card_owner = card.has("owner") ? card.optJSONObject("owner") : new JSONObject();
            videoAuthorName = card_owner.optString("name");
            videoAuthorImg = LruCacheUtil.getImageUrl(card_owner.optString("face"));
            videoAuthorUid = String.valueOf(card_owner.optInt("mid"));
            videoAid = String.valueOf(card.optInt("aid"));
            videoBvid = card.optString("bvid");
            if(!card.optString("dynamic").equals(""))
            {
                videoDynamicOrg = card.optString("dynamic");
                videoDynamic = super.handlerText(videoDynamicOrg, display, extend);
            }
            videoTitle = card.optString("title");
            videoImg = LruCacheUtil.getImageUrl(card.optString("pic"));
            videoDesc = card.optString("desc");
            videoDuration = DataProcessUtil.getMinFromSec(card.optInt("duration"));
            JSONObject card_stat = card.has("stat") ? card.optJSONObject("stat") : new JSONObject();
            videoPlay = DataProcessUtil.getView(card_stat.optInt("view"));
            videoDanmaku = DataProcessUtil.getView(card_stat.optInt("danmaku"));

            super.cardReplyId = videoAid;
            super.cardUrl = "bilibili://video/" + videoAid;
            super.cardReplyNum = DataProcessUtil.getView(card_stat.optInt("reply"));
        }
    }

    @Getter
    @Setter
    public class DynamicArticleModel extends DynamicModel implements Serializable //64
    {
        private String articleId;
        private String articleAuthorName;
        private String articleAuthorImg;
        private String articleAuthorUid;
        private String articleDynamicOrg;
        private String articleDynamic;
        private boolean articleDynamicExpand;
        private String articleTitle;
        private String articleImg;
        private String articleDesc;
        private String articleView;
        public DynamicArticleModel(JSONObject card, JSONObject desc, JSONObject display, JSONObject extend, boolean isShared)
        {
            super(card, desc, display, extend, isShared);
            articleId = String.valueOf(card.optInt("id"));
            JSONObject card_author = card.has("author") ? card.optJSONObject("author") : new JSONObject();
            articleAuthorName = card_author.optString("name");
            articleAuthorImg = LruCacheUtil.getImageUrl(card_author.optString("face"));
            articleAuthorUid = String.valueOf(card_author.optInt("mid"));
            if(!card.optString("dynamic").equals(""))
            {
                articleDynamicOrg = card.optString("dynamic");
                articleDynamic = super.handlerText(articleDynamicOrg, display, extend);
            }
            articleTitle = card.optString("title");
            articleDesc = card.optString("summary");
            JSONArray card_img = card.has("image_urls") ? card.optJSONArray("image_urls") : new JSONArray();
            articleImg = LruCacheUtil.getImageUrl(card_img.optString(0));
            JSONObject card_stat = card.has("stats") ? card.optJSONObject("stats") : new JSONObject();
            articleView = DataProcessUtil.getView(card_stat.optInt("view"));

            super.cardReplyId = articleId;
            super.cardUrl = "bilibili://article/" + articleId;
            super.cardReplyNum = DataProcessUtil.getView(card_stat.optInt("reply"));
        }
    }

    @Getter
    @Setter
    public class DynamicBangumiModel extends DynamicModel implements Serializable //512番剧 4096??? 4097??? 4098电影 4099综艺、电视剧 4100??? 4101纪录片
    {
        private String bangumiSeasonId;
        private String bangumiAuthorName;
        private String bangumiTitle;
        private String bangumiImg;
        private String bangumiView;
        private String bangumiDanmaku;

        public DynamicBangumiModel(JSONObject card, JSONObject desc, JSONObject display, JSONObject extend, boolean isShared)
        {
            super(card, desc, display, extend, isShared);
            bangumiTitle = card.optString("new_desc");
            bangumiImg = LruCacheUtil.getImageUrl(card.optString("cover"));
            bangumiView = DataProcessUtil.getView(card.optInt("play_count"));
            bangumiDanmaku = DataProcessUtil.getView(card.optInt("bullet_count"));

            JSONObject card_info = card.has("apiSeasonInfo") ? card.optJSONObject("apiSeasonInfo") : new JSONObject();
            bangumiSeasonId = String.valueOf(card_info.optInt("season_id"));
            bangumiAuthorName = card_info.optString("title");
            if(bangumiTitle == null || bangumiTitle.equals(""))
                bangumiTitle = card_info.optString("title");
            if(!isShared)
            {
                super.cardAuthorName = card_info.optString("title");
                super.cardAuthorImg = LruCacheUtil.getImageUrl(card_info.optString("cover"));
                super.cardAuthorOfficial = -1;
            }

            super.cardUrl = "bilibili://bangumi/season/" + bangumiSeasonId;
            super.cardReplyNum = DataProcessUtil.getView(card.optInt("reply_count"));
        }
    }

    @Getter
    @Setter
    public class DynamicUrlModel extends DynamicModel implements Serializable //2048
    {
        private String urlAuthorName;
        private String urlAuthorUid;
        private String urlDynamicOrg;
        private String urlDynamic;
        private boolean urlDynamicExpand;
        private String urlTitle;
        private String urlDesc;
        private String urlImg;
        private String urlUrl;
        public DynamicUrlModel(JSONObject card, JSONObject desc, JSONObject display, JSONObject extend, boolean isShared)
        {
            super(card, desc, display, extend, isShared);
            JSONObject card_user = card.has("user") ? card.optJSONObject("user") : new JSONObject();
            urlAuthorName = card_user.optString("uname");
            urlAuthorUid = card_user.optString("uid");
            JSONObject card_vest = card.has("vest") ? card.optJSONObject("vest") : new JSONObject();
            if(!card_vest.optString("content").equals(""))
            {
                urlDynamicOrg = card_vest.optString("content");
                urlDynamic = super.handlerText(urlDynamicOrg, display, extend);
            }
            JSONObject card_sketch = card.has("sketch") ? card.optJSONObject("sketch") : new JSONObject();
            urlTitle = card_sketch.optString("title");
            urlDesc = card_sketch.optString("desc_text");
            urlImg = LruCacheUtil.getImageUrl(card_sketch.optString("cover_url"));
            urlUrl = card_sketch.optString("target_url");
        }
    }

    @Getter
    @Setter
    public class DynamicLiveModel extends DynamicModel implements Serializable //4200
    {
        private String liveId;
        private String liveTitle;
        private String liveAuthorName;
        private String liveAuthorUid;
        private String liveAuthorImg;
        private String liveImg;
        private String liveArea;
        private String liveOnline;
        private boolean liveStatus;

        public DynamicLiveModel(JSONObject card, JSONObject desc, JSONObject display, JSONObject extend, boolean isShared)
        {
            super(card, desc, display, extend, isShared);
            liveId = card.optString("roomid");
            liveTitle = card.optString("title");
            liveAuthorName = card.optString("uname");
            liveAuthorUid = String.valueOf(card.optInt("uid"));
            liveAuthorImg = LruCacheUtil.getImageUrl(card.optString("face"));
            liveImg = LruCacheUtil.getImageUrl(card.optString("cover"));
            liveArea = card.optString("area_v2_name");
            liveOnline = DataProcessUtil.getView(card.optInt("online"));
            liveStatus = card.optInt("live_status") == 1;

            super.cardUrl = "bilibili://live/" + liveId;
        }
    }

    @Getter
    @Setter
    public class DynamicFavorModel extends DynamicModel implements Serializable //4300
    {
        private String favorTitle;
        private String favorId;
        private String favorImg;
        private int favorCount;
        private String favorAuthorName;
        private String favorAuthorUid;
        private String favorAuthorImg;

        public DynamicFavorModel(JSONObject card, JSONObject desc, JSONObject display, JSONObject extend, boolean isShared)
        {
            super(card, desc, display, extend, isShared);
            favorTitle = card.optString("title");
            favorId = String.valueOf(card.optInt("fid"));
            favorImg = LruCacheUtil.getImageUrl(card.optString("cover"));
            favorCount = card.optInt("media_count");
            JSONObject card_upper = card.has("upper") ? card.optJSONObject("upper") : new JSONObject();
            favorAuthorName = card_upper.optString("name");
            favorAuthorUid = String.valueOf(card_upper.optInt("mid"));
            favorAuthorImg = LruCacheUtil.getImageUrl(card_upper.optString("face"));

            super.cardUrl = "bilibili://collect/" + favorId + "?uid=" + favorAuthorUid;
        }
    }

    //256音频 4302付费课程 4308开播提醒？ 2049漫画
    //懒得做了 看到这句话的好心人可以做一波2333
    /*
    {"badge":{"bg_color":"#FB7199","bg_dark_color":"#bb5b76","text":"付费课程","text_color":"#ffffff","text_dark_color":"#e5e5e5"},"cover":"https:\/\/i0.hdslb.com\/bfs\/archive\/400020088403c23af6fa854701d4d7bf3934b680.jpg","ep_count":19,"id":117,"subtitle":"绘画萌新入门必修，靠谱导师带你冲冲冲！这套课程一共设置了绘画基础、进阶演练和高阶应用三大版块，是适用于板绘领域所有绘画学习的基础课程，请大家放心大胆食用哦~","title":"十分绘画：绘画萌新入门创造营","up_id":348630592,"up_info":{"avatar":"https:\/\/i1.hdslb.com\/bfs\/face\/6ccb743564fc186b631fd60ea389390e9c291d0f.jpg","name":"十分绘画"},"update_count":0,"update_info":"更新中，更新至第4期 | 共19期","url":"https:\/\/m.bilibili.com\/cheese\/play\/ss117"}
    { "id": 1596550, "upId": 4408538, "title": "怪异电台Vol.14 看完《异度侵入》，除了好看我们还想说……", "upper": "我是怪异君", "cover": "https:\/\/i0.hdslb.com\/bfs\/music\/4b4fa92396a28e6f3e068c07b78e0c78af3c410c.jpg", "author": "我是怪异君", "ctime": 1590054493000, "replyCnt": 53, "playCnt": 3561, "intro": "哈 哈 哈 哈！\n今天还想听电台？\nYes！\n被误删的十四期回来了，\n原汁原味，一模一样，\n我还能再听亿遍！", "schema": "bilibili:\/\/music\/detail\/1596550?name=%E6%80%AA%E5%BC%82%E7%94%B5%E5%8F%B0Vol.14+%E7%9C%8B%E5%AE%8C%E3%80%8A%E5%BC%82%E5%BA%A6%E4%BE%B5%E5%85%A5%E3%80%8B%EF%BC%8C%E9%99%A4%E4%BA%86%E5%A5%BD%E7%9C%8B%E6%88%91%E4%BB%AC%E8%BF%98%E6%83%B3%E8%AF%B4%E2%80%A6%E2%80%A6&uperName=&cover_url=http%3A%2F%2Fi0.hdslb.com%2Fbfs%2Fmusic%2F4b4fa92396a28e6f3e068c07b78e0c78af3c410c.jpg&upperId=&author=%E6%88%91%E6%98%AF%E6%80%AA%E5%BC%82%E5%90%9B", "typeInfo": "有声节目 · 其他", "upperAvatar": "https:\/\/i0.hdslb.com\/bfs\/face\/4b3bdd3188d7b8b9200e16c70cba01c25b818a26.jpg" }
    {"live_play_info":{"area_id":236,"area_name":"主机游戏","cover":"https:\/\/i0.hdslb.com\/bfs\/live\/room_cover\/ab35dec973e4b088ccbc94e137b5ccdfd41b8a59.jpg","link":"https:\/\/live.bilibili.com\/1029","live_id":77037003027252229,"live_screen_type":0,"live_start_time":1590654697,"live_status":1,"online":6080,"parent_area_id":6,"parent_area_name":"单机","play_type":2,"room_id":1029,"room_type":0,"title":"测试","uid":43536},"live_record_info":null,"style":1,"type":1}
    {"rid": 437448900259136970, "user": {"uid": 444815994, "uname": "木棉花动漫有限公司", "face": "https:\/\/i1.hdslb.com\/bfs\/face\/e54ccbb7eeadc4210bace6cdf2a808c4e1dc8e77.jpg"}, "vest": {"uid": 444815994, "content": "看动画之前可以看下漫画[鼓掌]", "ctrl": "[]"}, "sketch": {"title": "无能的奈奈", "desc_text": "校园", "cover_url": "https:\/\/i0.hdslb.com\/bfs\/manga-static\/c4147e125da6773314f46e81cdba0a18e75d0fa5.jpg", "target_url": "https:\/\/manga.bilibili.com\/m\/detail\/mc28565?from_spmid=main.space.0.0&module=follow-comic&from=dynamic_card", "sketch_id": 437448900226579013, "biz_type": 201, "tags": [{"type": 201, "name": "漫画", "color": "FB7299"}], "text": "更新至 37 话"}}
    */

    @Getter
    @Setter
    public class DynamicUnknownModel extends DynamicModel implements Serializable
    {
        private String unknownTips;

        public DynamicUnknownModel(JSONObject card, JSONObject desc, JSONObject display, JSONObject extend, boolean isShared)
        {
            this(card, desc, display, extend, isShared, "");
        }

        public DynamicUnknownModel(JSONObject card, JSONObject desc, JSONObject display, JSONObject extend, boolean isShared, String tips)
        {
            super(card, desc, display, extend, isShared);
            unknownTips = tips;
        }
    }

    private String handlerText(String text, JSONObject display, JSONObject extend)
    {
        int ctrlLength = 0;
        JSONArray ctrlJSONArray = extend.has("ctrl") ? extend.optJSONArray("ctrl") : new JSONArray();
        for(int i = 0; i < ctrlJSONArray.length(); i++)
        {
            JSONObject ctrlJSON = ctrlJSONArray.optJSONObject(i);
            String data = ctrlJSON.optString("data");
            int location = ctrlJSON.optInt("location") + ctrlLength;
            int length = ctrlJSON.optInt("length");
            if(ctrlJSON.optInt("type") == 1)
            {
                String tag = "<a href=\"bilibili://space/" + data +"\">" + text.substring(location, Math.min(location + length, text.length() -1)) + "</a>";
                StringBuilder stringBuilder = new StringBuilder(text);
                stringBuilder.replace(location, location + length, tag);
                text = stringBuilder.toString();
                ctrlLength += tag.length() - length;
            }
        }

        text = text.replace("\n", "<br>");
        Element document = Jsoup.parseBodyFragment(text).body();
        List<TextNode> textNodes = document.textNodes();

        Pattern bvPattern = Pattern.compile("([Bb][Vv][a-zA-Z0-9]{10})");
        for(int i = 0; i < textNodes.size(); i++)
        {
            TextNode textNode = textNodes.get(i);
            Matcher bvMatcher = bvPattern.matcher(textNode.getWholeText());
            if(bvMatcher.find())
            {
                MatchResult bvMatcherResult = bvMatcher.toMatchResult();
                String tag = "<a href=\"bilibili://video/BV" + bvMatcherResult.group(0).substring(2) +
                        "\">BV" + bvMatcherResult.group().substring(2) + "</a>";
                textNode.before(textNode.getWholeText().substring(0, bvMatcherResult.start(0)));
                textNode.before(tag);
                textNode.text(textNode.getWholeText().substring(bvMatcherResult.end(0)));
                textNodes = document.textNodes();
                i--;
            }
        }

        Pattern avPattern = Pattern.compile("([Aa][Vv][0-9]+(?=[^1-9]|$))");
        for(int i = 0; i < textNodes.size(); i++)
        {
            TextNode textNode = textNodes.get(i);
            Matcher avMatcher = avPattern.matcher(textNode.getWholeText());
            if(avMatcher.find())
            {
                MatchResult avMatcherResult = avMatcher.toMatchResult();
                String tag = "<a href=\"bilibili://video/" + avMatcherResult.group(0).substring(2) +
                        "\">av" + avMatcherResult.group(0).substring(2) + "</a>";
                textNode.before(textNode.getWholeText().substring(0, avMatcherResult.start(0)));
                textNode.before(tag);
                textNode.text(textNode.getWholeText().substring(avMatcherResult.end(0)));
                textNodes = document.textNodes();
                i--;
            }
        }

        Pattern cvPattern = Pattern.compile("([Cc][Vv][0-9]+(?=[^1-9]|$))");
        for(int i = 0; i < textNodes.size(); i++)
        {
            TextNode textNode = textNodes.get(i);
            Matcher cvMatcher = cvPattern.matcher(textNode.getWholeText());
            if(cvMatcher.find())
            {
                MatchResult cvMatcherResult = cvMatcher.toMatchResult();
                String tag = "<a href=\"bilibili://article/" + cvMatcherResult.group(0).substring(2) +
                        "\">cv" + cvMatcherResult.group(0).substring(2) + "</a>";
                textNode.before(textNode.getWholeText().substring(0, cvMatcherResult.start(0)));
                textNode.before(tag);
                textNode.text(textNode.getWholeText().substring(cvMatcherResult.end(0)));
                textNodes = document.textNodes();
                i--;
            }
        }

        if(display != null)
        {
            JSONObject emote = display.has("emoji_info") ? display.optJSONObject("emoji_info") : new JSONObject();
            JSONArray emoteDetail = emote.has("emoji_details") ? emote.optJSONArray("emoji_details") : new JSONArray();
            for (int i = 0; i < emoteDetail.length(); i++)
            {
                JSONObject emoteJson = emoteDetail.optJSONObject(i);
                String key = emoteJson.optString("text");
                String tag = "<img src=\"" + emoteJson.optString("url") + "\"/>";
                for (int j = 0; j < textNodes.size(); j++)
                {
                    TextNode textNode = textNodes.get(j);
                    if(textNode.getWholeText().contains(key))
                    {
                        cardEmoteSize.put(emoteJson.optString("url"), emoteJson.has("meta") ? emoteJson.optJSONObject("meta").optInt("size") : 1);
                        textNode.before(textNode.getWholeText().substring(0, textNode.getWholeText().indexOf(key)));
                        textNode.before(tag);
                        textNode.text(textNode.getWholeText().substring(textNode.getWholeText().indexOf(key) + key.length()));
                        textNodes = document.textNodes();
                        j--;
                    }
                }
            }

            if((extend.has("topic") ? extend.optJSONObject("topic").optInt("is_attach_topic") : 1) == 1)
            {
                JSONObject topics = display.has("topic_info") ? display.optJSONObject("topic_info") : new JSONObject();
                JSONArray topicsDetail = topics.has("topic_details") ? topics.optJSONArray("topic_details") : new JSONArray();
                for (int i = 0; i < topicsDetail.length(); i++)
                {
                    JSONObject topicsJSON = topicsDetail.optJSONObject(i);
                    String key = "#" + topicsJSON.optString("topic_name") + "#";
                    String tag = "<a href=\"bilibili://pegasus/channel/" + topicsJSON.optString("topic_id") + "\">" + key + "</a>";
                    for (int j = 0; j < textNodes.size(); j++)
                    {
                        TextNode textNode = textNodes.get(j);
                        if(textNode.getWholeText().contains(key))
                        {
                            textNode.before(textNode.getWholeText().substring(0, textNode.getWholeText().indexOf(key)));
                            textNode.before(tag);
                            textNode.text(textNode.getWholeText().substring(textNode.getWholeText().indexOf(key) + key.length()));
                            textNodes = document.textNodes();
                            j--;
                        }
                    }
                }
            }
        }

        Pattern urlPattern = Pattern.compile("((?:https?://)?[a-zA-Z0-9.]+?\\.(?:com|cn|top|org|gov|edu|net)(?:/[a-zA-Z0-9\\-_.~!*'();:@&=+$,/?#\\[\\]]*)*)");
        for(int i = 0; i < textNodes.size(); i++)
        {
            TextNode textNode = textNodes.get(i);
            Matcher urlMatcher = urlPattern.matcher(textNode.getWholeText());
            if(urlMatcher.find())
            {
                MatchResult urlMatcherResult = urlMatcher.toMatchResult();
                String tag = "<a href=\"" + urlMatcherResult.group(0) + "\">" + urlMatcherResult.group() + "</a>";
                textNode.before(textNode.getWholeText().substring(0, urlMatcherResult.start(0)));
                textNode.before(tag);
                textNode.text(textNode.getWholeText().substring(urlMatcherResult.end(0)));
                textNodes = document.textNodes();
                i--;
            }
        }

        return document.outerHtml();
    }
}
