package cn.luern0313.wristbilibili.models;

import org.json.JSONObject;

/**
 * 被 luern0313 创建于 2020/1/15.
 */

public class DownloadModel
{
    public int id;
    public String url_video = "";
    public String url_danmaku = "";
    public int mode; //0下载完成 1正在下载 2标题栏
    public int state = 3; //0初始化 1下载中 2暂停中 3暂停 4错误 5完成
    public String tip = "";

    public String title = "";
    public String cover = "";
    public String aid = "";
    public String cid = "";
    public long nowdl;
    public long size;
    public int speed;

    public DownloadModel(String uv, String ud, int m, String t, String c, String aid, String cid, int s)
    {
        url_video = uv;
        url_danmaku = ud;
        mode = m;
        state = s;
        title = t;
        cover = c;
        this.aid = aid;
        this.cid = cid;
    }

    public DownloadModel(JSONObject json)
    {
        id = json.optInt("task_id", 0);
        url_video = json.optString("url_video", "");
        url_danmaku = json.optString("url_danmaku", "");
        mode = json.optInt("is_video_downloading", 0);
        aid = json.optString("video_aid", "");
        cid = json.optString("video_cid", "");
        title = json.optString("video_title", "");
        cover = json.optString("video_cover", "");
        size = json.optLong("video_total_size", 0);
        nowdl = json.optLong("video_downloaded_size", 0);
    }
}
