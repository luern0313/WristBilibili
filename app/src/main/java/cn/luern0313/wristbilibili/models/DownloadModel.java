package cn.luern0313.wristbilibili.models;

import org.json.JSONObject;

import cn.luern0313.wristbilibili.util.LruCacheUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * 被 luern0313 创建于 2020/1/15.
 */

@Getter
@Setter
public class DownloadModel
{
    private int id;
    private String urlVideo = "";
    private String urlDanmaku = "";
    private int mode; //0下载完成 1正在下载 2标题栏
    private int state = 3; //0初始化 1下载中 2暂停中 3暂停 4错误 5完成
    private String tip = "";

    private String title = "";
    private String cover = "";
    private String aid = "";
    private String cid = "";
    private long nowdl;
    private long size;
    private int speed;

    public DownloadModel(String uv, String ud, int m, String t, String c, String aid, String cid, int s)
    {
        urlVideo = uv;
        urlDanmaku = ud;
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
        urlVideo = json.optString("url_video", "");
        urlDanmaku = json.optString("url_danmaku", "");
        mode = json.optInt("is_video_downloading", 0);
        aid = json.optString("video_aid", "");
        cid = json.optString("video_cid", "");
        title = json.optString("video_title", "");
        cover = json.optString("video_cover", "");
        size = json.optLong("video_total_size", 0);
        nowdl = json.optLong("video_downloaded_size", 0);
    }
}
