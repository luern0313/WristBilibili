package cn.luern0313.wristbilibili.models.article;

import org.json.JSONObject;

import cn.luern0313.wristbilibili.util.DataProcessUtil;

/**
 * 被 luern0313 创建于 2020/2/28.
 */
public class ArticleCardModel
{
    public String article_card_identity;

    public class ArticleVideoCardModel extends ArticleCardModel
    {
        public String article_video_card_id;
        public String article_video_card_title;
        public String article_video_card_cover;
        public String article_video_card_time;
        public String article_video_card_play;
        public String article_video_card_danmaku;
        public String article_video_card_up_name;

        public ArticleVideoCardModel(String identity, JSONObject videoCard)
        {
            article_card_identity = identity;
            article_video_card_id = String.valueOf(videoCard.optInt("aid"));
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

    public class ArticleBangumiCardModel extends ArticleCardModel
    {
        public String article_bangumi_card_id;
        public String article_bangumi_card_title;
        public String article_bangumi_card_cover;
        public String article_bangumi_card_play;
        public String article_bangumi_card_follow;
        public String article_bangumi_card_type_name;
        public String article_bangumi_card_score;

        public ArticleBangumiCardModel(String identity, JSONObject bangumiCard)
        {
            article_card_identity = identity;
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

    public class ArticleArticleCardModel extends ArticleCardModel
    {
        public String article_article_card_id;
        public String article_article_card_title;

        public ArticleArticleCardModel(String identity, JSONObject articleCard)
        {
            article_card_identity = identity;

        }
    }
}
