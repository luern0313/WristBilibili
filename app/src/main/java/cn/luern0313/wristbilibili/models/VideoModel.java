package cn.luern0313.wristbilibili.models;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import cn.luern0313.wristbilibili.util.DataProcessUtil;

/**
 * 被 luern0313 创建于 2020/2/2.
 */

public class VideoModel implements Serializable
{
    public String video_aid;
    public String video_bvid;
    public String video_cid;
    public String video_title;
    public String video_cover;
    public String video_desc;
    public String video_time;
    public String video_play;
    public String video_danmaku;
    public String video_warning;
    public ArrayList<VideoPartModel> video_part_array_list = new ArrayList<>();

    public int video_detail_copyright;
    public int video_detail_like;
    public int video_detail_coin;
    public int video_detail_fav;

    public String video_up_name;
    public String video_up_face;
    public String video_up_mid;
    public int video_up_official; // -1 0 1
    public int video_up_vip; // 2
    public int video_up_fan_num;

    public String video_season_id;
    public String video_season_title;
    public String video_season_cover;
    public boolean video_season_is_finish;
    public String video_season_new_ep;
    public String video_season_total;

    public boolean video_user_follow_up;
    public boolean video_user_like;
    public boolean video_user_dislike;
    public int video_user_coin;
    public boolean video_user_fav;
    public String video_user_progress_cid;
    public int video_user_progress_position;
    public int video_user_progress_time;

    public ArrayList<VideoRecommendModel> video_recommend_array_list = new ArrayList<>();
    public VideoModel(JSONObject video)
    {
        video_aid = video.has("aid") ? String.valueOf(video.optInt("aid")) : "";
        video_bvid = video.has("bvid") ? String.valueOf(video.optString("bvid")) : "";
        video_cid = String.valueOf(video.optInt("cid"));
        video_title = video.optString("title");
        video_cover = video.optString("pic");
        video_desc = video.optString("desc");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        video_time = format.format(new Date(video.optInt("pubdate") * 1000L));

        JSONObject video_stat = video.has("stat") ? video.optJSONObject("stat") : new JSONObject();
        video_play = DataProcessUtil.getView(video_stat.optInt("view"));
        video_danmaku = DataProcessUtil.getView(video_stat.optInt("danmaku"));

        video_warning = video.optString("argue_msg");

        JSONArray video_parts = video.has("pages") ? video.optJSONArray("pages") : new JSONArray();
        for(int i = 0; i < video_parts.length(); i++)
            video_part_array_list.add(new VideoPartModel(video_parts.optJSONObject(i)));

        video_detail_copyright = video.optInt("copyright");
        video_detail_like = video_stat.optInt("like");
        video_detail_coin = video_stat.optInt("coin");
        video_detail_fav = video_stat.optInt("favorite");

        JSONObject video_owner = video.has("owner") ? video.optJSONObject("owner") : new JSONObject();
        JSONObject video_owner_other = video.has("owner_ext") ? video.optJSONObject("owner_ext") : new JSONObject();
        JSONObject video_owner_other_official = video_owner_other.has("official_verify") ? video_owner_other.optJSONObject("official_verify") : new JSONObject();
        JSONObject video_owner_other_vip = video_owner_other.has("vip") ? video_owner_other.optJSONObject("vip") : new JSONObject();
        video_up_name = video_owner.optString("name");
        video_up_face = video_owner.optString("face");
        video_up_mid = String.valueOf(video_owner.optInt("mid"));
        video_up_official = video_owner_other_official.optInt("type");
        video_up_fan_num = video_owner_other.optInt("fans");
        video_up_vip = video_owner_other_vip.optInt("vipType");

        JSONObject video_season = video.has("season") ? video.optJSONObject("season") : new JSONObject();
        video_season_id = String.valueOf(video_season.optInt("season_id"));
        video_season_title = video_season.optString("title");
        video_season_cover = video_season.optString("cover");
        video_season_is_finish = video_season.optInt("is_finish") == 1;
        video_season_new_ep = video_season.optString("newest_ep_index");
        video_season_total = video_season.optString("total_count");

        JSONObject video_user = video.has("req_user") ? video.optJSONObject("req_user") : new JSONObject();
        video_user_follow_up = video_user.optInt("attention") == 1;
        video_user_like = video_user.optInt("like") == 1;
        video_user_dislike = video_user.optInt("dislike") == 1;
        video_user_coin = video_user.optInt("coin");
        video_user_fav = video_user.optInt("favorite") == 1;
        JSONObject video_history = video.has("history") ? video.optJSONObject("history") : new JSONObject();
        video_user_progress_cid = String.valueOf(video_history.optInt("cid"));
        for(int i = 0; i < video_part_array_list.size(); i++)
            if(video_user_progress_cid.equals(video_part_array_list.get(i).video_part_cid))
                video_user_progress_position = i;
        video_user_progress_time = video_history.optInt("progress");

        JSONArray video_recommend = video.has("relates") ? video.optJSONArray("relates") : new JSONArray();
        for(int i = 0; i < video_recommend.length(); i++)
        {
            JSONObject video_recommend_video = video_recommend.optJSONObject(i);
            if(!video_recommend_video.optBoolean("is_ad"))
                video_recommend_array_list.add(new VideoRecommendModel(video_recommend_video));
        }
    }

    public class VideoPartModel implements Serializable
    {
        public String video_part_cid;
        public int video_part_num;
        public String video_part_name;
        VideoPartModel(JSONObject video_part)
        {
            video_part_cid = String.valueOf(video_part.optInt("cid", 0));
            video_part_num = video_part.optInt("page", 0);
            video_part_name = video_part.optString("part", "P" + video_part_num);
        }
    }

    public class VideoRecommendModel implements Serializable
    {
        public String video_recommend_video_aid;
        public String video_recommend_video_title;
        public String video_recommend_video_cover;
        public String video_recommend_video_play;
        public String video_recommend_video_danmaku;
        public String video_recommend_video_owner_name;

        VideoRecommendModel(JSONObject video_recommend_video)
        {
            video_recommend_video_aid = String.valueOf(video_recommend_video.optInt("aid"));
            video_recommend_video_title = video_recommend_video.optString("title");
            video_recommend_video_cover = video_recommend_video.optString("pic");

            JSONObject video_recommend_video_stat = video_recommend_video.has("stat") ? video_recommend_video.optJSONObject("stat") : new JSONObject();
            video_recommend_video_play = DataProcessUtil.getView(video_recommend_video_stat.optInt("view"));
            video_recommend_video_danmaku = DataProcessUtil.getView(video_recommend_video_stat.optInt("danmaku"));

            JSONObject video_recommend_video_owner = video_recommend_video.has("owner") ? video_recommend_video.optJSONObject("owner") : new JSONObject();
            video_recommend_video_owner_name = video_recommend_video_owner.optString("name");
        }
    }
}
