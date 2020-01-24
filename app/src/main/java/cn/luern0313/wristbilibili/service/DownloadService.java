package cn.luern0313.wristbilibili.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.exception.FileDownloadGiveUpRetryException;
import com.liulishuo.filedownloader.exception.FileDownloadHttpException;
import com.liulishuo.filedownloader.exception.FileDownloadOutOfSpaceException;
import com.liulishuo.filedownloader.exception.PathConflictException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.api.DownloadApi;
import cn.luern0313.wristbilibili.models.DownloadModel;
import cn.luern0313.wristbilibili.util.NetWorkUtil;

import static cn.luern0313.wristbilibili.util.FileUtil.fileReader;

/**
 * 被 luern0313 创建于 2019/8/4.
 * tag
 * 0 -> 下载类型 0 弹幕 1 视频
 * 1 -> aid
 * 2 -> cid
 * 3 -> url
 */

public class DownloadService extends Service
{
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    DownloadApi downloadApi;
    public ArrayList<DownloadModel> downloadingItems;
    public ArrayList<DownloadModel> downloadedItems;
    FileDownloadListener fileDownloadListener;

    MyBinder myBinder = new MyBinder();
    private downloadListener downloadListener;

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return myBinder;
    }

    @Override
    public void onCreate()
    {
        getApplicationContext();
        super.onCreate();

        sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        downloadApi = new DownloadApi(getApplication());
        downloadApi.initDownloadItems();
        downloadingItems = downloadApi.getDownloadingItems();
        downloadedItems = downloadApi.getDownloadedItems();

        fileDownloadListener = new FileDownloadListener()
        {
            @Override
            protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes)
            {
            }

            @Override
            protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes)
            {
                super.connected(task, etag, isContinue, soFarBytes, totalBytes);
                int po = DownloadApi.findPositionInList((String) task.getTag(3), downloadingItems);
                DownloadModel item = downloadingItems.get(po);
                item.state = 1;
                item.id = task.getId();
                item.size = task.getSmallFileTotalBytes();
                downloadingItems.set(po, item);

                try
                {
                    File infoFile = new File(getExternalFilesDir(null) + "/download/" + task.getTag(1) + "/" + task.getTag(2) + "/info.json");
                    JSONObject json = new JSONObject(fileReader(infoFile));
                    FileOutputStream out = new FileOutputStream(infoFile);
                    out.write(json.put("video_total_size", task.getSmallFileTotalBytes()).put("task_id", task.getId()).toString().getBytes("UTF-8"));
                    out.close();
                }
                catch (IOException | JSONException e)
                {
                    e.printStackTrace();
                }
                if(downloadListener != null) downloadListener.onConnected();
            }

            @Override
            protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes)
            {
                int po = DownloadApi.findPositionInList(task.getUrl(), downloadingItems);
                DownloadModel item = downloadingItems.get(po);
                item.nowdl = soFarBytes;
                item.speed = task.getSpeed() * 1024;
                downloadingItems.set(po, item);

                try
                {
                    File infoFile = new File(getExternalFilesDir(null) + "/download/" + task.getTag(1) + "/" + task.getTag(2) + "/info.json");
                    JSONObject json = new JSONObject(fileReader(infoFile));
                    FileOutputStream out = new FileOutputStream(infoFile);
                    out.write(json.put("video_downloaded_size", soFarBytes).toString().getBytes("UTF-8"));
                    out.close();
                }
                catch (IOException | JSONException e)
                {
                    e.printStackTrace();
                }

                if(downloadListener != null) downloadListener.onProgress();
            }

            @Override
            protected void completed(BaseDownloadTask task)
            {
                int po = DownloadApi.findPositionInList(task.getUrl(), downloadingItems);
                DownloadModel item = downloadingItems.get(po);
                item.mode = 0;
                item.state = 5;
                item.nowdl = task.getSmallFileSoFarBytes();
                item.speed = task.getSpeed() * 1024;
                downloadingItems.remove(po);
                downloadedItems.add(item);
                try
                {
                    File infoFile = new File(
                            getExternalFilesDir(null) + "/download/" + task.getTag(1) + "/" + task
                                    .getTag(2) + "/info.json");
                    JSONObject json = new JSONObject(fileReader(infoFile));
                    FileOutputStream out = new FileOutputStream(infoFile);
                    out.write(json.put("is_video_downloading", 0).toString().getBytes("UTF-8"));
                    out.close();
                }
                catch (IOException | JSONException e)
                {
                    e.printStackTrace();
                }
                if(downloadListener != null) downloadListener.onCompleted();
            }

            @Override
            protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes)
            {
                try
                {
                    int po = DownloadApi.findPositionInList(task.getUrl(), downloadingItems);
                    DownloadModel item = downloadingItems.get(po);
                    item.state = 3;
                    item.nowdl = task.getSmallFileSoFarBytes();
                    item.speed = task.getSpeed() * 1024;
                    downloadingItems.set(po, item);
                    if(downloadListener != null) downloadListener.onPaused();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            protected void error(BaseDownloadTask task, Throwable e)
            {
                int po = DownloadApi.findPositionInList(task.getUrl(), downloadingItems);
                DownloadModel item = downloadingItems.get(po);
                item.state = 4;
                if(e instanceof FileDownloadHttpException) item.tip = "错误的下载链接";
                else if(e instanceof FileDownloadGiveUpRetryException) item.tip = "无法获取文件信息";
                else if(e instanceof FileDownloadOutOfSpaceException) item.tip = "储存空间不足";
                else if(e instanceof PathConflictException) item.tip = "下载文件已存在";
                else item.tip = "未知错误";
                item.nowdl = task.getSmallFileSoFarBytes();
                item.speed = task.getSpeed() * 1024;
                downloadingItems.set(po, item);
                if(downloadListener != null) downloadListener.onError();
            }

            @Override
            protected void warn(BaseDownloadTask task)
            {
            }
        };
    }

    public void setOnProgressListener(downloadListener onProgressListener)
    {
        this.downloadListener = onProgressListener;
    }

    public interface downloadListener
    {
        void onConnected();
        void onProgress();
        void onCompleted();
        void onPaused();
        void onError();
    }

    public void pause(int position)
    {
        FileDownloader.getImpl().pause(downloadingItems.get(position).id);
        DownloadModel downloadItem = downloadingItems.get(position);
        downloadItem.state = 2;
        downloadItem.speed = 0;
        downloadingItems.set(position, downloadItem);
    }

    public void start(int position)
    {
        DownloadModel downloadItem = downloadingItems.get(position);
        FileDownloader.getImpl().create(downloadItem.url_video).setPath(getExternalFilesDir(
                null) + "/download/" + downloadItem.aid + "/" + downloadItem.cid + "/video.mp4")
                .addHeader("User-Agent",
                           "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36")
                .addHeader("Referer", "https://www.bilibili.com/").setTag(1, downloadItem.aid)
                .setTag(2, downloadItem.cid).setTag(3, downloadItem.url_video).setListener(
                fileDownloadListener).asInQueueTask().enqueue();
        FileDownloader.getImpl().start(fileDownloadListener, false);
        downloadItem.state = 0;
        downloadingItems.set(position, downloadItem);
    }

    public class MyBinder extends Binder
    {
        public DownloadService getService()
        {
            return DownloadService.this;
        }

        public String startDownload(final String aid, final String cid, String title, final String cover, String url_video, final String url_danmaku)
        {
            try
            {
                File downFolder = new File(getExternalFilesDir(null) + "/download/" + aid + "/" + cid + "/");
                if(downFolder.exists() && downFolder.list().length != 0) return "下载数据已存在";
                File infoFile = new File(getExternalFilesDir(null) + "/download/" + aid + "/" + cid + "/info.json");
                File folInfoFile = new File(getExternalFilesDir(null) + "/download/" + aid + "/" + cid + "/");
                if(!folInfoFile.exists()) folInfoFile.mkdirs();
                if(infoFile.createNewFile())
                {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("task_id", 0);
                    jsonObject.put("url_video", url_video);
                    jsonObject.put("url_danmaku", url_danmaku);
                    jsonObject.put("is_video_downloading", 1);
                    jsonObject.put("video_aid", aid);
                    jsonObject.put("video_cid", cid);
                    jsonObject.put("video_title", title);
                    jsonObject.put("video_cover", cover);
                    jsonObject.put("video_total_size", 0);
                    jsonObject.put("video_downloaded_size", 0);
                    FileOutputStream out = new FileOutputStream(infoFile);
                    out.write(jsonObject.toString().getBytes("UTF-8"));
                    out.close();
                }
                else return "创建文件错误";
            }
            catch (IOException | JSONException e)
            {
                e.printStackTrace();
                return "文件操作错误";
            }

            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        File danmakuFile = new File(getExternalFilesDir(null) + "/download/" + aid + "/" + cid + "/danmaku.xml");
                        File coverFile = new File(getExternalFilesDir(null) + "/download/" + aid + "/" + cid + "/cover.png");
                        danmakuFile.createNewFile();
                        coverFile.createNewFile();
                        FileOutputStream danmakuOut = new FileOutputStream(danmakuFile);
                        danmakuOut.write(NetWorkUtil.uncompress(NetWorkUtil.get(url_danmaku).body().bytes()));
                        danmakuOut.close();
                        FileOutputStream coverOut = new FileOutputStream(coverFile);
                        coverOut.write(NetWorkUtil.get(cover).body().bytes());
                        coverOut.close();
                    }
                    catch (IOException | NullPointerException e)
                    {
                        e.printStackTrace();
                    }
                }
            }).start();

            FileDownloader.getImpl().create(url_video).setPath(
                    getExternalFilesDir(null) + "/download/" + aid + "/" + cid + "/video.mp4")
                    .addHeader("User-Agent",
                               "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36")
                    .addHeader("Referer", "https://www.bilibili.com/").setTag(1, aid).setTag(2, cid)
                    .setTag(3, url_video).setListener(fileDownloadListener).asInQueueTask()
                    .enqueue();
            FileDownloader.getImpl().start(fileDownloadListener, false);
            downloadingItems.add(1, new DownloadModel(url_video, url_danmaku, 1, title, cover, aid, cid, 0));
            return "";
        }
    }
}
