package cn.luern0313.wristbilibili.models;

import cn.luern0313.lson.annotation.field.LsonAddPrefix;
import cn.luern0313.lson.annotation.field.LsonAddSuffix;
import cn.luern0313.lson.annotation.field.LsonDateFormat;
import cn.luern0313.lson.annotation.field.LsonPath;
import cn.luern0313.lson.annotation.field.LsonReplaceAll;
import cn.luern0313.wristbilibili.util.json.ImageUrlHandle;
import cn.luern0313.wristbilibili.util.json.ViewFormat;

/**
 * 被 luern0313 创建于 2020/2/3.
 */

public class SearchModel
{
    public interface SearchBaseModel
    {
        int getSearchMode();
    }

    public static class SearchBangumiModel implements SearchBaseModel
    {
        @LsonReplaceAll(regex = {"<em class=\"keyword\">", "</em>"}, replacement = {"<keyword>", "</keyword>"})
        @LsonAddPrefix("<body>")
        @LsonAddSuffix("</body>")
        @LsonPath("title")
        public String search_bangumi_title;

        @LsonAddPrefix("http:")
        @ImageUrlHandle
        @LsonPath("cover")
        public String search_bangumi_cover;

        @LsonPath("season_id")
        public String search_bangumi_season_id;

        @LsonPath("ep_size")
        public int search_bangumi_episode_count;

        @LsonPath("media_score.score")
        public String search_bangumi_score;

        @LsonDateFormat("yyyy")
        @LsonPath("pubtime")
        public String search_bangumi_time;

        @LsonPath("areas")
        public String search_bangumi_area;

        @Override
        public int getSearchMode()
        {
            return 0;
        }
    }

    public static class SearchUserModel implements SearchBaseModel
    {
        @LsonPath("uname")
        public String search_user_name;

        @LsonAddPrefix("http:")
        @ImageUrlHandle
        @LsonPath("upic")
        public String search_user_face;

        @LsonPath("mid")
        public String search_user_mid;

        @LsonPath("usign")
        public String search_user_sign;

        @LsonPath("official_verify.type")
        public int search_user_official_type;

        @LsonPath("official_verify.desc")
        public String search_user_official_desc;

        @ViewFormat
        @LsonPath("fans")
        public String search_user_fans;

        @LsonPath("videos")
        public String search_user_videos;

        @Override
        public int getSearchMode()
        {
            return 1;
        }
    }

    public static class SearchVideoModel implements SearchBaseModel
    {
        @LsonReplaceAll(regex = {"<em class=\"keyword\">", "</em>"}, replacement = {"<keyword>", "</keyword>"})
        @LsonAddPrefix("<body>")
        @LsonAddSuffix("</body>")
        @LsonPath("title")
        public String search_video_title;

        @LsonPath("aid")
        public String search_video_aid;

        @LsonAddPrefix("http:")
        @ImageUrlHandle
        @LsonPath("pic")
        public String search_video_cover;

        @ViewFormat
        @LsonPath("play")
        public String search_video_play;

        @ViewFormat
        @LsonPath("video_review")
        public String search_video_danmaku;

        @LsonPath("author")
        public String search_video_up_name;

        @LsonPath("duration")
        public String search_video_duration;

        @Override
        public int getSearchMode()
        {
            return 2;
        }
    }
}
