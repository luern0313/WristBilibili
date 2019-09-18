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

import org.json.JSONArray;
import org.json.JSONException;

/**
 * 被 luern0313 创建于 2019/8/4.
 * as
 */

public class DownloadService extends Service
{
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    FileDownloadListener fileDownloadListener;

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        getApplicationContext();
        super.onCreate();

        sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        try
        {
            JSONArray dlJson = new JSONArray(sharedPreferences.getString("downloadJson", "{}"));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        fileDownloadListener = new FileDownloadListener()
        {
            @Override
            protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes)
            {
                //等待，已经进入下载队列
            }

            @Override
            protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes)
            {

            }

            @Override
            protected void completed(BaseDownloadTask task)
            {

            }

            @Override
            protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes)
            {
                //暂停下载
            }

            @Override
            protected void error(BaseDownloadTask task, Throwable e)
            {

            }

            @Override
            protected void warn(BaseDownloadTask task)
            {
                //在下载队列中(正在等待/正在下载)已经存在相同下载连接与相同存储路径的任务
            }
        };
    }

    public class MyBinder extends Binder
    {
        public DownloadService getService()
        {
            return DownloadService.this;
        }

        public void startDownload(String aid, String cid, String url_video, String url_danmaku)
        {
            FileDownloader.getImpl().create(url_danmaku)
                    .setPath(getExternalFilesDir(null) + "/" + aid + "/" + cid + "/danmuku.xml")
                    .setTag(0, 0).setListener(fileDownloadListener).asInQueueTask().enqueue();
            FileDownloader.getImpl().create(url_video)
                    .setPath(getExternalFilesDir(null) + "/" + aid + "/" + cid + "/video.mp4")
                    .setTag(0, 1).setListener(fileDownloadListener).asInQueueTask().enqueue();
            FileDownloader.getImpl().start(fileDownloadListener, true);
        }
    }
}
