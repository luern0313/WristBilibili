package cn.luern0313.wristbilibili.models;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cn.luern0313.wristbilibili.util.DataProcessUtil;

/**
 * 被 luern0313 创建于 2020/2/3.
 */

public class SearchModel
{
    public int search_mode;

    public class SearchBangumiModel extends SearchModel
    {
        public String search_bangumi_title;
        public String search_bangumi_cover;
        public String search_bangumi_season_id;
        public int search_bangumi_episode_count;
        public String search_bangumi_score;
        public String search_bangumi_time;
        public String search_bangumi_area;

        public SearchBangumiModel(JSONObject bangumi)
        {
            search_mode = 0;
            search_bangumi_title = bangumi.optString("title").replaceAll("<em class=\"keyword\">", "<keyword>");
            search_bangumi_title = search_bangumi_title.replaceAll("</em>", "</keyword>");
            search_bangumi_cover = "http:" + bangumi.optString("cover");
            search_bangumi_season_id = String.valueOf(bangumi.optInt("season_id"));
            search_bangumi_episode_count = bangumi.optInt("ep_size");

            JSONObject bangumi_score = bangumi.optJSONObject("media_score") != null ? bangumi.optJSONObject("media_score") : new JSONObject();
            search_bangumi_score = bangumi_score.has("score") ? String.valueOf(bangumi_score.optDouble("score", -1)) : "";

            SimpleDateFormat format = new SimpleDateFormat("yyyy", Locale.getDefault());
            search_bangumi_time = format.format(new Date(bangumi.optInt("pubtime") * 1000L));

            search_bangumi_area = bangumi.optString("areas");
        }
    }

    public class SearchUserModel extends SearchModel
    {
        public String search_user_name;
        public String search_user_face;
        public String search_user_mid;
        public String search_user_sign;
        public int search_user_official_type;
        public String search_user_official_desc;
        public String search_user_fans;
        public String search_user_videos;

        public SearchUserModel(JSONObject user)
        {
            search_mode = 1;
            search_user_name = user.optString("uname");
            search_user_face = "http:" + user.optString("upic");
            search_user_mid = String.valueOf(user.optInt("mid"));
            search_user_sign = user.optString("usign");

            JSONObject user_official = user.has("official_verify") ? user.optJSONObject("official_verify") : new JSONObject();
            search_user_official_type = user_official.optInt("type");
            search_user_official_desc = user_official.optString("desc");

            search_user_fans = DataProcessUtil.getView(user.optInt("fans"));
            search_user_videos = String.valueOf(user.optInt("videos"));
        }
    }

    public class SearchVideoModel extends SearchModel
    {
        public String search_video_title;
        public String search_video_aid;
        public String search_video_cover;
        public String search_video_play;
        public String search_video_danmaku;
        public String search_video_up_name;
        public String search_video_duration;

        public SearchVideoModel(JSONObject video)
        {
            search_mode = 2;
            search_video_title = video.optString("title").replaceAll("<em class=\"keyword\">", "<keyword>");
            search_video_title = search_video_title.replaceAll("</em>", "</keyword>");
            search_video_aid = String.valueOf(video.optInt("aid"));
            search_video_cover = "http:" + video.optString("pic");
            search_video_play = DataProcessUtil.getView(video.optInt("play"));
            search_video_danmaku = DataProcessUtil.getView(video.optInt("video_review"));
            search_video_up_name = video.optString("author");
            search_video_duration = video.optString("duration");
        }
    }
}
