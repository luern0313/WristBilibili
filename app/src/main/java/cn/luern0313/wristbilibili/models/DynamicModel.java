package cn.luern0313.wristbilibili.models;

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

import cn.luern0313.lson.LsonUtil;
import cn.luern0313.lson.annotation.field.LsonAddPrefix;
import cn.luern0313.lson.annotation.field.LsonBooleanFormatAsNumber;
import cn.luern0313.lson.annotation.field.LsonDateFormat;
import cn.luern0313.lson.annotation.field.LsonPath;
import cn.luern0313.lson.annotation.method.LsonCallMethod;
import cn.luern0313.lson.element.LsonArray;
import cn.luern0313.lson.element.LsonObject;
import cn.luern0313.wristbilibili.util.LruCacheUtil;
import cn.luern0313.wristbilibili.util.json.ImageUrlFormat;
import cn.luern0313.wristbilibili.util.json.TimeFormat;
import cn.luern0313.wristbilibili.util.json.ViewFormat;
import lombok.Getter;
import lombok.Setter;

/**
 * 被 luern0313 创建于 2020/5/17.
 */
public class DynamicModel
{
    @Getter
    @Setter
    public static class DynamicShareModel extends DynamicBaseModel implements Serializable //1
    {
        private String shareTextOrg;
        private String shareText;
        private boolean shareTextExpand;

        private String shareOriginTips;
        private int shareOriginType;
        private DynamicBaseModel shareOriginCard;

        public DynamicShareModel(LsonObject dynamic, boolean isShared)
        {
            LsonObject card_item = dynamic.getJsonObject("card").getJsonObject("item");
            shareTextOrg = card_item.getString("content");
            shareText = super.handlerText(shareTextOrg, dynamic.getJsonObject("display"), dynamic.getJsonObject("extend"));

            LsonObject origin = new LsonObject();
            origin.put("card", LsonUtil.parseAsObject(dynamic.getJsonObject("card").getString("origin", "{}")));
            origin.put("desc", dynamic.getJsonObject("desc").getJsonObject("origin"));
            origin.put("display", dynamic.getJsonObject("display").getJsonObject("origin"));
            origin.put("extend", LsonUtil.parseAsObject(dynamic.getJsonObject("card").getString("origin_extend_json", "{}")));

            shareOriginType = origin.getJsonObject("desc").getInt("type");
            if(card_item.getInt("miss") == 1)
            {
                shareOriginType = 9999;
                shareOriginTips = card_item.getString("tips");
            }

            switch(shareOriginType)
            {
                case 1:
                    shareOriginCard = LsonUtil.fromJson(origin, DynamicShareModel.class, origin, true);
                    break;
                case 2:
                    shareOriginCard = LsonUtil.fromJson(origin, DynamicAlbumModel.class, origin, true);
                    break;
                case 4:
                    shareOriginCard = LsonUtil.fromJson(origin, DynamicTextModel.class, origin, true);
                    break;
                case 8:
                    shareOriginCard = LsonUtil.fromJson(origin, DynamicVideoModel.class, origin, true);
                    break;
                case 64:
                    shareOriginCard = LsonUtil.fromJson(origin, DynamicArticleModel.class, origin, true);
                    break;
                case 512:
                case 4098:
                case 4099:
                case 4101:
                    shareOriginCard = LsonUtil.fromJson(origin, DynamicBangumiModel.class, origin, true);
                    break;
                case 2048:
                    shareOriginCard = LsonUtil.fromJson(origin, DynamicUrlModel.class, origin, true);
                    break;
                case 4200:
                    shareOriginCard = LsonUtil.fromJson(origin, DynamicLiveModel.class, origin, true);
                    break;
                case 4300:
                    shareOriginCard = LsonUtil.fromJson(origin, DynamicFavorModel.class, origin, true);
                    break;
                default:
                    shareOriginCard = LsonUtil.fromJson(origin, DynamicUnknownModel.class, origin, true, shareOriginTips);
                    break;
            }
        }
    }

    @Getter
    @Setter
    public static class DynamicAlbumModel extends DynamicBaseModel implements Serializable //2
    {
        @LsonPath("card.id")
        private String albumId;

        @LsonPath("card.user.name")
        private String albumAuthorName;

        @LsonPath("card.user.uid")
        private String albumAuthorUid;

        private String albumTextOrg;
        private String albumText;
        private boolean albumTextExpand;

        @ImageUrlFormat
        @LsonPath("card.item.pictures[*].img_src")
        private ArrayList<String> albumImg;

        @ViewFormat
        @LsonPath("card.reply")
        private String albumReply;

        public DynamicAlbumModel(LsonObject dynamic, boolean isShared)
        {
            albumTextOrg = dynamic.getJsonObject("card").getJsonObject("item").getString("description");
            albumText = super.handlerText(albumTextOrg, dynamic.getJsonObject("display"), dynamic.getJsonObject("extend"));

            setCardIsShared(isShared);
        }

        @LsonCallMethod(timing = LsonCallMethod.CallMethodTiming.AFTER_DESERIALIZATION)
        private void initData()
        {
            setCardReplyId(albumId);
            setCardReplyNum(albumReply);
        }
    }

    @Getter
    @Setter
    public static class DynamicTextModel extends DynamicBaseModel implements Serializable //4
    {
        @LsonPath("card.user.uname")
        private String textAuthorName;

        @LsonPath("card.user.uid")
        private String textAuthorUid;

        private String textTextOrg;
        private String textText;
        private boolean textTextExpand;

        public DynamicTextModel(LsonObject dynamic, boolean isShared)
        {
            textTextOrg = dynamic.getJsonObject("card").getJsonObject("item").getString("content");
            textText = super.handlerText(textTextOrg, dynamic.getJsonObject("display"), dynamic.getJsonObject("extend"));

            setCardIsShared(isShared);
        }
    }

    @Getter
    @Setter
    public static class DynamicVideoModel extends DynamicBaseModel implements Serializable //8
    {
        @LsonPath("card.owner.name")
        private String videoAuthorName;

        @ImageUrlFormat
        @LsonPath("card.owner.face")
        private String videoAuthorImg;

        @LsonPath("card.owner.mid")
        private String videoAuthorUid;

        @LsonPath("card.aid")
        private String videoAid;

        @LsonPath("card.bvid")
        private String videoBvid;

        private String videoDynamicOrg;
        private String videoDynamic;
        private boolean videoDynamicExpand;

        @LsonPath("card.title")
        private String videoTitle;

        @ImageUrlFormat
        @LsonPath("card.pic")
        private String videoImg;

        @LsonPath("card.desc")
        private String videoDesc;

        @TimeFormat
        @LsonPath("card.duration")
        private String videoDuration;

        @ViewFormat
        @LsonPath("card.stat.view")
        private String videoPlay;

        @ViewFormat
        @LsonPath("card.stat.danmaku")
        private String videoDanmaku;

        @ViewFormat
        @LsonPath("card.stat.reply")
        private String videoReply;
        public DynamicVideoModel(LsonObject dynamic, boolean isShared)
        {
            String content = dynamic.getJsonObject("card").getString("dynamic");
            if(content != null && !content.equals(""))
            {
                videoDynamicOrg = dynamic.getJsonObject("card").getString("dynamic");
                videoDynamic = super.handlerText(videoDynamicOrg, dynamic.getJsonObject("display"), dynamic.getJsonObject("extend"));
            }

            setCardIsShared(isShared);
        }

        @LsonCallMethod(timing = LsonCallMethod.CallMethodTiming.AFTER_DESERIALIZATION)
        private void initData()
        {
            setCardUrl("bilibili://video/" + videoAid);
            setCardReplyId(videoAid);
            setCardReplyNum(videoReply);
        }
    }

    @Getter
    @Setter
    public static class DynamicArticleModel extends DynamicBaseModel implements Serializable //64
    {
        @LsonPath("card.id")
        private String articleId;

        @LsonPath("card.author.name")
        private String articleAuthorName;

        @ImageUrlFormat
        @LsonPath("card.author.face")
        private String articleAuthorImg;

        @LsonPath("card.author.mid")
        private String articleAuthorUid;

        private String articleDynamicOrg;
        private String articleDynamic;
        private boolean articleDynamicExpand;

        @LsonPath("card.title")
        private String articleTitle;

        @LsonPath("card.image_urls[0]")
        private String articleImg;

        @LsonPath("card.summary")
        private String articleDesc;

        @ViewFormat
        @LsonPath("card.stats.view")
        private String articleView;

        @ViewFormat
        @LsonPath("card.stats.reply")
        private String articleReply;
        public DynamicArticleModel(LsonObject dynamic, boolean isShared)
        {
            String content = dynamic.getJsonObject("card").getString("dynamic");
            if(content != null && !content.equals(""))
            {
                articleDynamicOrg = dynamic.getJsonObject("card").getString("dynamic");
                articleDynamic = super.handlerText(articleDynamicOrg, dynamic.getJsonObject("display"), dynamic.getJsonObject("extend"));
            }

            setCardIsShared(isShared);
        }

        @LsonCallMethod(timing = LsonCallMethod.CallMethodTiming.AFTER_DESERIALIZATION)
        private void initData()
        {
            setCardUrl("bilibili://article/" + articleId);
            setCardReplyId(articleId);
            setCardReplyNum(articleReply);
        }
    }

    @Getter
    @Setter
    public static class DynamicBangumiModel extends DynamicBaseModel implements Serializable //512番剧 4096??? 4097??? 4098电影 4099综艺、电视剧 4100??? 4101纪录片
    {
        @LsonPath("card.apiSeasonInfo.season_id")
        private String bangumiSeasonId;

        @LsonPath("card.apiSeasonInfo.title")
        private String bangumiAuthorName;

        @LsonPath("card.new_desc")
        private String bangumiTitle;

        @ImageUrlFormat
        @LsonPath("card.cover")
        private String bangumiImg;

        @ViewFormat
        @LsonPath("card.play_count")
        private String bangumiView;

        @ViewFormat
        @LsonPath("card.bullet_count")
        private String bangumiDanmaku;

        @ViewFormat
        @LsonPath("card.reply_count")
        private String bangumiReply;

        public DynamicBangumiModel(LsonObject dynamic, boolean isShared)
        {
            LsonObject card_info = dynamic.getJsonObject("card").getJsonObject("apiSeasonInfo");
            if(!isShared)
            {
                setCardAuthorName(card_info.getString("title"));
                setCardAuthorImg(LruCacheUtil.getImageUrl(card_info.getString("cover")));
                setCardAuthorOfficial(-1);
            }

            setCardIsShared(isShared);
        }

        @LsonCallMethod(timing = LsonCallMethod.CallMethodTiming.AFTER_DESERIALIZATION)
        private void initData()
        {
            if(bangumiTitle == null || bangumiTitle.equals(""))
                bangumiTitle = bangumiAuthorName;
            setCardUrl("bilibili://bangumi/season/" + bangumiSeasonId);
            setCardReplyNum(bangumiReply);
        }
    }

    @Getter
    @Setter
    public static class DynamicUrlModel extends DynamicBaseModel implements Serializable //2048
    {
        @LsonPath("card.user.uname")
        private String urlAuthorName;

        @LsonPath("card.user.uid")
        private String urlAuthorUid;

        private String urlDynamicOrg;
        private String urlDynamic;
        private boolean urlDynamicExpand;

        @LsonPath("card.sketch.title")
        private String urlTitle;

        @LsonPath("card.sketch.desc_text")
        private String urlDesc;

        @ImageUrlFormat
        @LsonPath("card.sketch.cover_url")
        private String urlImg;

        @LsonPath("card.sketch.target_url")
        private String urlUrl;
        public DynamicUrlModel(LsonObject dynamic, boolean isShared)
        {
            String content = dynamic.getJsonObject("card").getJsonObject("vest").getString("content");
            if(content != null && !content.equals(""))
            {
                urlDynamicOrg = content;
                urlDynamic = super.handlerText(urlDynamicOrg, dynamic.getJsonObject("display"), dynamic.getJsonObject("extend"));
            }
            setCardIsShared(isShared);
        }
    }

    @Getter
    @Setter
    public static class DynamicLiveModel extends DynamicBaseModel implements Serializable //4200
    {
        @LsonPath("card.roomid")
        private String liveId;

        @LsonPath("card.title")
        private String liveTitle;

        @LsonPath("card.uname")
        private String liveAuthorName;

        @LsonPath("card.uid")
        private String liveAuthorUid;

        @ImageUrlFormat
        @LsonPath("card.face")
        private String liveAuthorImg;

        @ImageUrlFormat
        @LsonPath("card.cover")
        private String liveImg;

        @LsonPath("card.area_v2_name")
        private String liveArea;

        @ViewFormat
        @LsonPath("card.online")
        private String liveOnline;

        @LsonBooleanFormatAsNumber(equal = 1)
        @LsonPath("card.live_status")
        private boolean liveStatus;

        public DynamicLiveModel(LsonObject dynamic, boolean isShared)
        {
            setCardIsShared(isShared);
        }

        @LsonCallMethod(timing = LsonCallMethod.CallMethodTiming.AFTER_DESERIALIZATION)
        private void initData()
        {
            setCardUrl("bilibili://live/" + liveId);
        }
    }

    @Getter
    @Setter
    public static class DynamicFavorModel extends DynamicBaseModel implements Serializable //4300
    {
        @LsonPath("card.title")
        private String favorTitle;

        @LsonPath("card.fid")
        private String favorId;

        @ImageUrlFormat
        @LsonPath("card.cover")
        private String favorImg;

        @LsonPath("card.media_count")
        private int favorCount;

        @LsonPath("card.upper.name")
        private String favorAuthorName;

        @LsonPath("card.upper.mid")
        private String favorAuthorUid;

        @ImageUrlFormat
        @LsonPath("card.upper.face")
        private String favorAuthorImg;

        public DynamicFavorModel(LsonObject dynamic, boolean isShared)
        {
            setCardIsShared(isShared);
        }

        @LsonCallMethod(timing = LsonCallMethod.CallMethodTiming.AFTER_DESERIALIZATION)
        private void initData()
        {
            setCardUrl("bilibili://collect/" + favorId + "?uid=" + favorAuthorUid);
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
    public static class DynamicUnknownModel extends DynamicBaseModel implements Serializable
    {
        private String unknownTips;

        public DynamicUnknownModel(LsonObject dynamic, boolean isShared)
        {
            this(dynamic, isShared, "");
        }

        public DynamicUnknownModel(LsonObject dynamic, boolean isShared, String tips)
        {
            unknownTips = tips;
            setCardIsShared(isShared);
        }
    }

    @Getter
    @Setter
    public abstract static class DynamicBaseModel implements Serializable
    {
        @LsonPath("desc.type")
        private int cardType;

        private boolean cardIsShared;

        @LsonPath("desc.dynamic_id_str")
        private String cardId;

        @LsonPath("desc.dynamic_id_str")
        private String cardReplyId;

        @LsonAddPrefix("bilibili://following/detail/")
        @LsonPath("desc.dynamic_id_str")
        private String cardUrl;

        @LsonDateFormat("MM-dd HH:mm")
        @LsonPath("desc.timestamp")
        private String cardTime;


        @LsonPath("desc.user_profile.info.uname")
        private String cardAuthorName;

        @LsonPath("desc.user_profile.info.uid")
        private String cardAuthorUid;

        @LsonPath("desc.user_profile.info.face")
        private String cardAuthorImg;

        @LsonPath("desc.user_profile.card.official_verify.type")
        private int cardAuthorOfficial = -1;

        @LsonPath("desc.user_profile.vip.vipStatus")
        private int cardAuthorVipStatus;

        @LsonPath("desc.user_profile.vip.vipType")
        private int cardAuthorVipType;


        @ViewFormat
        @LsonPath("desc.repost")
        private String cardShareNum;

        @ViewFormat
        @LsonPath("desc.comment")
        private String cardReplyNum;

        @LsonPath("desc.like")
        private int cardLikeNum;

        @LsonBooleanFormatAsNumber(equal = 1)
        @LsonPath("desc.is_liked")
        private boolean cardUserLike;

        private HashMap<String, Integer> cardEmoteSize = new HashMap<>();

        private String handlerText(String text, LsonObject display, LsonObject extend)
        {
            int ctrlLength = 0;
            LsonArray ctrlJSONArray = extend.getJsonArray("ctrl");
            for(int i = 0; i < ctrlJSONArray.size(); i++)
            {
                LsonObject ctrlJSON = ctrlJSONArray.getJsonObject(i);
                String data = ctrlJSON.getString("data");
                int location = ctrlJSON.getInt("location") + ctrlLength;
                int length = ctrlJSON.getInt("length");
                if(ctrlJSON.getInt("type") == 1)
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
                LsonArray emoteDetail = display.getJsonObject("emoji_info").getJsonArray("emoji_details");
                for (int i = 0; i < emoteDetail.size(); i++)
                {
                    LsonObject emoteJson = emoteDetail.getJsonObject(i);
                    String key = emoteJson.getString("text");
                    String tag = "<img src=\"" + emoteJson.getString("url") + "\"/>";
                    for (int j = 0; j < textNodes.size(); j++)
                    {
                        TextNode textNode = textNodes.get(j);
                        if(textNode.getWholeText().contains(key))
                        {
                            cardEmoteSize.put(emoteJson.getString("url"), emoteJson.getJsonObject("meta").getInt("size", 1));
                            textNode.before(textNode.getWholeText().substring(0, textNode.getWholeText().indexOf(key)));
                            textNode.before(tag);
                            textNode.text(textNode.getWholeText().substring(textNode.getWholeText().indexOf(key) + key.length()));
                            textNodes = document.textNodes();
                            j--;
                        }
                    }
                }

                if(extend.getJsonObject("topic").getInt("is_attach_topic", 1) == 1)
                {
                    LsonArray topicsDetail = display.getJsonObject("topic_info").getJsonArray("topic_details");
                    for (int i = 0; i < topicsDetail.size(); i++)
                    {
                        LsonObject topicsJSON = topicsDetail.getJsonObject(i);
                        String key = "#" + topicsJSON.getString("topic_name") + "#";
                        String tag = "<a href=\"bilibili://pegasus/channel/" + topicsJSON.getString("topic_id") + "\">" + key + "</a>";
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
}
