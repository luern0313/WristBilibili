package cn.luern0313.wristbilibili.models;

import org.json.JSONObject;

/**
 * 被 luern0313 创建于 2020/1/15.
 */

public class RecommendModel
{
    public int mode;

    public String video_aid;
    public String video_title;
    public String video_img;
    public String video_time;
    public int video_data_1_icon;
    public String video_data_1_text;
    public int video_data_2_icon;
    public String video_data_2_text;
    public String video_recommend_reason;
    public String video_lable;
    public RecommendModel(JSONObject video)
    {
        video_aid = video.optString("param");
        video_title = video.optString("title");
        video_img = video.optString("cover");
        video_time = video.optString("cover_right_text");
        video_data_1_icon = video.optInt("cover_left_icon_1");
        video_data_1_text = video.optString("cover_left_text_1");
        video_data_2_icon = video.optInt("cover_left_icon_2");
        video_data_2_text = video.optString("cover_left_text_2");
        video_recommend_reason = video.optString("rcmd_reason");
        video_lable = video.has("desc_button") ? video.optJSONObject("desc_button").optString("text") : "";
    }

    public RecommendModel(int mode)
    {
        this.mode = mode;
    }
}
