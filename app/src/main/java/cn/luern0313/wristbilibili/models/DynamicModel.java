package cn.luern0313.wristbilibili.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.luern0313.wristbilibili.util.DataProcessUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * 被 luern0313 创建于 2020/5/17.
 */
@Getter
@Setter
public class DynamicModel
{
    private int cardType;
    private boolean cardIsShared;
    private String cardId;
    private String cardTime;

    private String cardAuthorName;
    private String cardAuthorUid;
    private String cardAuthorImg;
    private int cardAuthorOfficial;
    private int cardAuthorVipStatus;
    private int cardAuthorVipType;

    private String cardShareNum;
    private String cardReplyNum;
    private int cardLikeNum;

    private boolean cardUserLike;

    public DynamicModel(){}

    private DynamicModel(JSONObject card, JSONObject desc, boolean isShared)
    {
        cardType = desc.optInt("type");
        this.cardIsShared = isShared;
        cardId = desc.optString("dynamic_id_str");
        cardTime = DataProcessUtil.getTime(desc.optInt("timestamp"), "MM-dd HH:mm");

        JSONObject author = desc.has("user_profile") ? desc.optJSONObject("user_profile") : new JSONObject();
        JSONObject author_info = author.has("info") ? author.optJSONObject("info") : new JSONObject();
        cardAuthorName = author_info.optString("uname");
        cardAuthorUid = String.valueOf(author_info.optInt("uid"));
        cardAuthorImg = author_info.optString("face");
        JSONObject author_card = author.has("card") ? author.optJSONObject("card") : new JSONObject();
        JSONObject author_card_official = author_card.has("official_verify") ? author_card.optJSONObject("official_verify") : new JSONObject();
        cardAuthorOfficial = author_card_official.optInt("type");
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
    public class DynamicShareModel extends DynamicModel //1
    {
        private String shareText;

        private int shareOriginType;
        private DynamicModel shareOriginCard;

        public DynamicShareModel(JSONObject card, JSONObject desc, boolean isShared)
        {
            super(card, desc, isShared);
            try
            {
                JSONObject card_item = card.has("item") ? card.optJSONObject("item") : new JSONObject();
                shareText = card_item.optString("content");

                JSONObject card_origin = new JSONObject(card.optString("origin"));
                JSONObject desc_origin = desc.optJSONObject("origin");
                shareOriginType = desc_origin.optInt("type");
                switch(shareOriginType)
                {
                    case 1:
                        shareOriginCard = new DynamicShareModel(card_origin, desc_origin, true);
                        break;
                    case 2:
                        shareOriginCard = new DynamicAlbumModel(card_origin, desc_origin, true);
                        break;
                    case 4:
                        shareOriginCard = new DynamicTextModel(card_origin, desc_origin, true);
                        break;
                    case 8:
                        shareOriginCard = new DynamicVideoModel(card_origin, desc_origin, true);
                        break;
                    case 64:
                        shareOriginCard = new DynamicArticleModel(card_origin, desc_origin, true);
                        break;
                    case 512:
                    case 4098:
                    case 4099:
                    case 4101:
                        shareOriginCard = new DynamicBangumiModel(card_origin, desc_origin, true);
                        break;
                    case 2048:
                        shareOriginCard = new DynamicUrlModel(card_origin, desc_origin, true);
                        break;
                    case 4200:
                        shareOriginCard = new DynamicLiveModel(card_origin, desc_origin, true);
                        break;
                    case 4300:
                        shareOriginCard = new DynamicFavorModel(card_origin, desc_origin, true);
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
    public class DynamicAlbumModel extends DynamicModel //2
    {
        private String albumAuthorName;
        private String albumAuthorUid;
        private String albumText;
        private ArrayList<String> albumImg;

        public DynamicAlbumModel(JSONObject card, JSONObject desc, boolean isShared)
        {
            super(card, desc, isShared);
            JSONObject card_user = card.has("user") ? card.optJSONObject("user") : new JSONObject();
            albumAuthorName = card_user.optString("name");
            albumAuthorUid = String.valueOf(card_user.optInt("uid"));
            JSONObject card_item = card.has("item") ? card.optJSONObject("item") : new JSONObject();
            albumText = card_item.optString("description");

            JSONArray card_img = card_item.optJSONArray("pictures");
            albumImg = new ArrayList<>();
            for(int i = 0; i < card_img.length(); i++)
                albumImg.add(card_img.optJSONObject(i).optString("img_src"));
        }
    }

    @Getter
    @Setter
    public class DynamicTextModel extends DynamicModel //4
    {
        private String textAuthorName;
        private String textAuthorUid;
        private String textText;

        public DynamicTextModel(JSONObject card, JSONObject desc, boolean isShared)
        {
            super(card, desc, isShared);
            JSONObject card_user = card.has("user") ? card.optJSONObject("user") : new JSONObject();
            textAuthorName = card_user.optString("uname");
            textAuthorUid = String.valueOf(card_user.optInt("uid"));
            JSONObject card_item = card.has("item") ? card.optJSONObject("item") : new JSONObject();
            textText = card_item.optString("content");
        }
    }

    @Getter
    @Setter
    public class DynamicVideoModel extends DynamicModel //8
    {
        private String videoAuthorName;
        private String videoAuthorImg;
        private String videoAuthorUid;
        private String videoAid;
        private String videoBvid;
        private String videoDynamic;
        private String videoTitle;
        private String videoImg;
        private String videoDesc;
        private String videoDuration;
        private String videoPlay;
        private String videoDanmaku;
        public DynamicVideoModel(JSONObject card, JSONObject desc, boolean isShared)
        {
            super(card, desc, isShared);
            JSONObject card_owner = card.has("owner") ? card.optJSONObject("owner") : new JSONObject();
            videoAuthorName = card_owner.optString("name");
            videoAuthorImg = card_owner.optString("face");
            videoAuthorUid = String.valueOf(card_owner.optInt("mid"));
            videoAid = String.valueOf(card.optInt("aid"));
            videoBvid = card.optString("bvid");
            videoDynamic = card.optString("dynamic");
            videoTitle = card.optString("title");
            videoImg = card.optString("pic");
            videoDesc = card.optString("desc");
            videoDuration = DataProcessUtil.getMinFromSec(card.optInt("duration"));
            JSONObject card_stat = card.has("stat") ? card.optJSONObject("stat") : new JSONObject();
            videoPlay = DataProcessUtil.getView(card_stat.optInt("view"));
            videoDanmaku = DataProcessUtil.getView(card_stat.optInt("danmaku"));
        }
    }

    @Getter
    @Setter
    public class DynamicArticleModel extends DynamicModel //64
    {
        private String articleAuthorName;
        private String articleAuthorImg;
        private String articleAuthorUid;
        private String articleDynamic;
        private String articleTitle;
        private String articleImg;
        private String articleDesc;
        private String articleView;
        public DynamicArticleModel(JSONObject card, JSONObject desc, boolean isShared)
        {
            super(card, desc, isShared);
            JSONObject card_author = card.has("author") ? card.optJSONObject("author") : new JSONObject();
            articleAuthorName = card_author.optString("name");
            articleAuthorImg = card_author.optString("face");
            articleAuthorUid = String.valueOf(card_author.optInt("mid"));
            articleDynamic = card.optString("dynamic");
            articleTitle = card.optString("title");
            articleDesc = card.optString("summary");
            JSONArray card_img = card.has("image_urls") ? card.optJSONArray("image_urls") : new JSONArray();
            articleImg = card_img.optString(0);
            JSONObject card_stat = card.has("stats") ? card.optJSONObject("stats") : new JSONObject();
            articleView = DataProcessUtil.getView(card_stat.optInt("view"));
        }
    }

    @Getter
    @Setter
    public class DynamicBangumiModel extends DynamicModel //512番剧 4096??? 4097??? 4098电影 4099综艺、电视剧 4100??? 4101纪录片
    {
        private String bangumiAuthorName;
        private String bangumiTitle;
        private String bangumiImg;
        private String bangumiView;
        private String bangumiDanmaku;

        public DynamicBangumiModel(JSONObject card, JSONObject desc, boolean isShared)
        {
            super(card, desc, isShared);
            bangumiTitle = card.optString("new_desc");
            bangumiImg = card.optString("cover");
            bangumiView = DataProcessUtil.getView(card.optInt("play_count"));
            bangumiDanmaku = DataProcessUtil.getView(card.optInt("bullet_count"));

            JSONObject card_info = card.has("apiSeasonInfo") ? card.optJSONObject("apiSeasonInfo") : new JSONObject();
            bangumiAuthorName = card_info.optString("title");
            if(!isShared)
            {
                cardAuthorName = card_info.optString("title");
                cardAuthorImg = card_info.optString("cover");
            }
        }
    }

    @Getter
    @Setter
    public class DynamicUrlModel extends DynamicModel //2048
    {
        private String urlAuthorName;
        private String urlAuthorUid;
        private String urlDynamic;
        private String urlTitle;
        private String urlDesc;
        private String urlImg;
        private String urlUrl;
        public DynamicUrlModel(JSONObject card, JSONObject desc, boolean isShared)
        {
            super(card, desc, isShared);
            JSONObject card_user = card.has("user") ? card.optJSONObject("user") : new JSONObject();
            urlAuthorName = card_user.optString("uname");
            urlAuthorUid = card_user.optString("uid");
            JSONObject card_vest = card.has("vest") ? card.optJSONObject("vest") : new JSONObject();
            urlDynamic = card_vest.optString("content");
            JSONObject card_sketch = card.has("sketch") ? card.optJSONObject("sketch") : new JSONObject();
            urlTitle = card_sketch.optString("title");
            urlDesc = card_sketch.optString("desc_text");
            urlImg = card_sketch.optString("cover_url");
            urlUrl = card_sketch.optString("target_url");
        }
    }

    @Getter
    @Setter
    public class DynamicLiveModel extends DynamicModel //4200
    {
        private String liveTitle;
        private String liveAuthorName;
        private String liveAuthorUid;
        private String liveAuthorImg;
        private String liveImg;
        private String liveArea;
        private String liveOnline;
        private boolean liveStatus;

        public DynamicLiveModel(JSONObject card, JSONObject desc, boolean isShared)
        {
            super(card, desc, isShared);
            liveTitle = card.optString("title");
            liveAuthorName = card.optString("uname");
            liveAuthorUid = String.valueOf(card.optInt("uid"));
            liveAuthorImg = card.optString("face");
            liveImg = card.optString("cover");
            liveArea = card.optString("area_v2_name");
            liveOnline = DataProcessUtil.getView(card.optInt("online"));
            liveStatus = card.optInt("live_status") == 1;
        }
    }

    @Getter
    @Setter
    public class DynamicFavorModel extends DynamicModel //4300
    {
        private String favorTitle;
        private String favorId;
        private String favorImg;
        private int favorCount;
        private String favorAuthorName;
        private String favorAuthorUid;
        private String favorAuthorImg;

        public DynamicFavorModel(JSONObject card, JSONObject desc, boolean isShared)
        {
            super(card, desc, isShared);
            favorTitle = card.optString("title");
            favorId = String.valueOf(card.optInt("fid"));
            favorImg = card.optString("cover");
            favorCount = card.optInt("media_count");
            JSONObject card_upper = card.has("upper") ? card.optJSONObject("upper") : new JSONObject();
            favorAuthorName = card_upper.optString("name");
            favorAuthorUid = String.valueOf(card_upper.optInt("mid"));
            favorAuthorImg = card_upper.optString("face");
        }
    }

    public class DynamicUnknownModel extends DynamicModel
    {
        public DynamicUnknownModel(JSONObject card, JSONObject desc, boolean isShared)
        {
            super(card, desc, isShared);
        }
    }
}
