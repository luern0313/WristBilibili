package cn.luern0313.wristbilibili.api;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.models.DownloadModel;

import static cn.luern0313.wristbilibili.util.FileUtil.fileReader;

/**
 * 被 luern0313 创建于 2019/5/27.
 */

public class DownloadApi
{
    private Context ctx;
    private ArrayList<DownloadModel> downloadingItems = new ArrayList<>();
    private ArrayList<DownloadModel> downloadedItems = new ArrayList<>();

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
                    DownloadModel downloadItem = new DownloadModel(new JSONObject(fileReader(
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

    public ArrayList<DownloadModel> getDownloadingItems()
    {
        return new ArrayList<DownloadModel>()
        {{
            add(new DownloadModel("", "", 2, "正在下载", "", "", "", 0));
            addAll(downloadingItems);
        }};
    }

    public ArrayList<DownloadModel> getDownloadedItems()
    {
        return new ArrayList<DownloadModel>()
        {{
            add(new DownloadModel("", "", 2, "下载完成", "", "", "", 0));
            addAll(downloadedItems);
        }};
    }

    public static int findPositionInList(String url, ArrayList<DownloadModel> list)
    {
        for (int i = 0; i < list.size(); i++)
            if(list.get(i).url_video.equals(url)) return i;
        return -1;
    }
}
