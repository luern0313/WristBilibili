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
    private int downloadId;
    private String downloadUrlVideo = "";
    private String downloadUrlDanmaku = "";
    private int downloadMode; //0下载完成 1正在下载 2标题栏
    private int downloadState = 3; //0初始化 1下载中 2暂停中 3暂停 4错误 5完成
    private String downloadTip = "";

    private String downloadTitle = "";
    private String downloadCover = "";
    private String downloadAid = "";
    private String downloadCid = "";
    private long downloadNowdl;
    private long downloadSize;
    private int downloadSpeed;

    public DownloadModel(String uv, String ud, int m, String t, String c, String aid, String cid, int s)
    {
        downloadUrlVideo = uv;
        downloadUrlDanmaku = ud;
        downloadMode = m;
        downloadState = s;
        downloadTitle = t;
        downloadCover = c;
        this.downloadAid = aid;
        this.downloadCid = cid;
    }

    public DownloadModel(JSONObject json)
    {
        downloadId = json.optInt("task_id");
        downloadUrlVideo = json.optString("url_video");
        downloadUrlDanmaku = json.optString("url_danmaku");
        downloadMode = json.optInt("is_video_downloading");
        downloadAid = json.optString("video_aid");
        downloadCid = json.optString("video_cid");
        downloadTitle = json.optString("video_title");
        downloadCover = LruCacheUtil.getImageUrl(json.optString("video_cover"));
        downloadSize = json.optLong("video_total_size");
        downloadNowdl = json.optLong("video_downloaded_size");
    }
}
