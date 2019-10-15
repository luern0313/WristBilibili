package cn.luern0313.wristbilibili.api;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static cn.luern0313.wristbilibili.util.FileUtil.fileReader;

/**
 * 被 luern0313 创建于 2019/5/27.
 */

public class DownloadApi
{
    private Context ctx;
    private ArrayList<DownloadItem> downloadingItems = new ArrayList<>();
    private ArrayList<DownloadItem> downloadedItems = new ArrayList<>();

    public DownloadApi(Context ctx)
    {
        this.ctx = ctx;
    }

    public void initDownloadItems()
    {
        File folder = new File(ctx.getExternalFilesDir(null) + "/download/");
        String[] folderList = folder.list();
        if(!folder.exists() || (folderList == null || folderList.length == 0)) return;
        for (String i : folder.list())
        {
            File folderSen = new File(ctx.getExternalFilesDir(null) + "/download/" + i + "/");
            for (String j : folderSen.list())
            {
                try
                {
                    DownloadItem downloadItem = new DownloadItem(new JSONObject(fileReader(
                            ctx.getExternalFilesDir(null) + "/download/" + i + "/" + j + "/info.json")));
                    if(downloadItem.mode == 1) downloadingItems.add(downloadItem);
                    else if(downloadItem.mode == 0) downloadedItems.add(downloadItem);
                }
                catch (IOException | JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public ArrayList<DownloadItem> getDownloadingItems()
    {
        return new ArrayList<DownloadItem>()
        {{
            add(new DownloadItem("", "", 2, "正在下载", "", "", "", 0));
            addAll(downloadingItems);
        }};
    }

    public ArrayList<DownloadItem> getDownloadedItems()
    {
        return  new ArrayList<DownloadItem>()
        {{
            add(new DownloadItem("", "", 2, "下载完成", "", "", "", 0));
            addAll(downloadedItems);
        }};
    }

    public static int findPositionInList(String url, ArrayList<DownloadItem> list)
    {
        for (int i = 0; i < list.size(); i++)
            if(list.get(i).url_video.equals(url)) return i;
        return -1;
    }

    public static class DownloadItem
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

        public DownloadItem(String uv, String ud, int m, String t, String c, String aid, String cid, int s)
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

        DownloadItem(JSONObject json)
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
}
