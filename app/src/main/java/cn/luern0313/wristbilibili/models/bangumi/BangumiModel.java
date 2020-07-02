package cn.luern0313.wristbilibili.models.bangumi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.LruCacheUtil;

/**
 * 被 luern0313 创建于 2020/1/21.
 */

public class BangumiModel implements Serializable
{
    public String bangumi_title;
    public String bangumi_score;
    public String bangumi_play;
    public String bangumi_like;
    public String bangumi_series;
    public String bangumi_needvip;
    public String bangumi_cover;
    public String bangumi_cover_small;
    public ArrayList<BangumiEpisodeModel> bangumi_episodes = new ArrayList<>();
    public StringBuilder bangumi_section_name = new StringBuilder();
    public ArrayList<BangumiEpisodeModel> bangumi_sections = new ArrayList<>();
    public ArrayList<BangumiSeasonModel> bangumi_seasons = new ArrayList<>();

    public boolean bangumi_user_is_follow;
    public String bangumi_user_progress_epid;
    public int bangumi_user_progress_mode; //1ep 2se
    public int bangumi_user_progress_position;
    public String bangumi_user_progress_aid;

    public String bangumi_detail_typename; //类型
    public StringBuilder bangumi_detail_areas = new StringBuilder(); //地区
    public String bangumi_detail_publish_date;
    public String bangumi_detail_publish_ep;
    public StringBuilder bangumi_detail_styles = new StringBuilder(); //风格
    public String bangumi_detail_evaluate; //简介
    public String bangumi_detail_actor_title; //声优栏标题
    public String bangumi_detail_actor_info;//声优栏信息
    public String bangumi_detail_staff_title; //staff栏标题
    public String bangumi_detail_staff_info;//staff栏信息

    public String bangumi_type_name;
    public String bangumi_type_follow;
    public String bangumi_type_ep;
    public BangumiModel(JSONObject bangumi)
    {
        bangumi_title = bangumi.optString("season_title");
        bangumi_score = bangumi.has("rating") ? bangumi.optJSONObject("rating").optDouble("score") + "分" : "";
        bangumi_play = DataProcessUtil.getView(bangumi.optJSONObject("stat").optInt("views"));
        bangumi_like = DataProcessUtil.getView(bangumi.optJSONObject("stat").optInt("favorites"));
        bangumi_series = bangumi.optJSONObject("publish").optString("time_length_show");
        bangumi_needvip = bangumi.optString("badge");
        bangumi_cover = LruCacheUtil.getImageUrl(bangumi.optString("cover"));
        bangumi_cover_small = LruCacheUtil.getImageUrl(bangumi.optString("square_cover"));

        JSONObject bangumi_user_jsonobject = bangumi.has("user_status") ? bangumi.optJSONObject("user_status") : new JSONObject();
        bangumi_user_is_follow = bangumi_user_jsonobject.optInt("follow") == 1;
        bangumi_user_progress_epid = bangumi_user_jsonobject.has("progress") ?
                String.valueOf(bangumi_user_jsonobject.optJSONObject("progress").optInt("last_ep_id")) : "0";

        JSONArray episodes = bangumi.optJSONArray("episodes");
        for(int i = 0; i < episodes.length(); i++)
        {
            BangumiEpisodeModel bangumiEpisodeModel = new BangumiEpisodeModel(episodes.optJSONObject(i), i);
            if(i == 0 && bangumi_user_progress_epid.equals("0"))
                bangumi_user_progress_epid = bangumiEpisodeModel.bangumi_episode_id;
            if(bangumi_user_progress_epid.equals(bangumiEpisodeModel.bangumi_episode_id))
            {
                bangumi_user_progress_mode = 1;
                bangumi_user_progress_position = i;
                bangumi_user_progress_aid = bangumiEpisodeModel.bangumi_episode_aid;
            }
            bangumi_episodes.add(bangumiEpisodeModel);
        }

        JSONArray sections = bangumi.optJSONArray("section");
        for(int i = 0; i < sections.length(); i++)
        {
            bangumi_section_name.append(i == 0 ? sections.optJSONObject(i).optString("title") : ("&" + sections.optJSONObject(i).optString("title")));
            JSONArray sections_episodes = sections.optJSONObject(i).optJSONArray("episodes");
            for (int j = 0; j < sections_episodes.length(); j++)
            {
                BangumiEpisodeModel bangumiEpisodeModel = new BangumiEpisodeModel(sections_episodes.optJSONObject(j), bangumi_sections.size());
                if(bangumi_user_progress_epid.equals(bangumiEpisodeModel.bangumi_episode_id))
                {
                    bangumi_user_progress_mode = 2;
                    bangumi_user_progress_position = bangumi_sections.size();
                    bangumi_user_progress_aid = bangumiEpisodeModel.bangumi_episode_aid;
                }
                bangumi_sections.add(bangumiEpisodeModel);
            }
        }

        JSONArray seasons = bangumi.has("season") ? bangumi.optJSONArray("seasons") : new JSONArray();
        for(int i = 0; i < seasons.length(); i++)
            bangumi_seasons.add(new BangumiSeasonModel(seasons.optJSONObject(i)));

        bangumi_detail_typename = bangumi.optString("type_name");
        JSONArray bangumi_detail_areas_jsonarray = bangumi.optJSONArray("areas");
        for(int i = 0; i < bangumi_detail_areas_jsonarray.length(); i++)
            bangumi_detail_areas.append(i == 0 ? "" : " ").append(bangumi_detail_areas_jsonarray.optJSONObject(i).optString("name"));

        JSONObject bangumi_detail_public = bangumi.has("publish") ? bangumi.optJSONObject("publish") : new JSONObject();
        bangumi_detail_publish_date = bangumi_detail_public.optString("release_date_show");
        bangumi_detail_publish_ep = bangumi_detail_public.optString("time_length_show");

        JSONArray bangumi_detail_styles_jsonarray = bangumi.optJSONArray("styles");
        for(int i = 0; i < bangumi_detail_styles_jsonarray.length(); i++)
            bangumi_detail_styles.append(i == 0 ? "" : " ").append(bangumi_detail_styles_jsonarray.optJSONObject(i).optString("name"));

        bangumi_detail_evaluate = bangumi.optString("evaluate");

        JSONObject bangumi_detail_actor_jsonobject = bangumi.has("actor") ? bangumi.optJSONObject("actor") : new JSONObject();
        bangumi_detail_actor_title = bangumi_detail_actor_jsonobject.optString("title");
        bangumi_detail_actor_info = bangumi_detail_actor_jsonobject.optString("info");

        JSONObject bangumi_detail_staff_jsonobject = bangumi.has("staff") ? bangumi.optJSONObject("staff") : new JSONObject();
        bangumi_detail_staff_title = bangumi_detail_staff_jsonobject.optString("title");
        bangumi_detail_staff_info = bangumi_detail_staff_jsonobject.optString("info");

        bangumi_type_name = (bangumi_detail_typename.equals("番剧") || bangumi_detail_typename.equals("国创")) ? "番剧" : "影视";
        bangumi_type_follow = bangumi_type_name.equals("番剧") ? "追番" : "追剧";
        bangumi_type_ep = bangumi_type_name.equals("番剧") ? "话" : "集";
    }

    public class BangumiEpisodeModel implements Serializable
    {
        public String bangumi_episode_title;
        public String bangumi_episode_title_long;
        public String bangumi_episode_id;
        public String bangumi_episode_aid;
        public String bangumi_episode_cid;
        public String bangumi_episode_vip;
        public int position;
        BangumiEpisodeModel(JSONObject episode, int position)
        {
            bangumi_episode_title = episode.optString("title");
            bangumi_episode_title_long = episode.optString("long_title");
            bangumi_episode_id = String.valueOf(episode.optInt("id"));
            bangumi_episode_aid = String.valueOf(episode.optInt("aid"));
            bangumi_episode_cid = String.valueOf(episode.optInt("cid"));
            bangumi_episode_vip = episode.optString("badge");
            this.position = position;
        }
    }

    public class BangumiSeasonModel implements Serializable
    {
        public String bangumi_season_id;
        public String bangumi_season_title;
        BangumiSeasonModel(JSONObject season)
        {
            bangumi_season_id = String.valueOf(season.optInt("season_id"));
            bangumi_season_title = season.optString("season_title");
        }
    }
}
