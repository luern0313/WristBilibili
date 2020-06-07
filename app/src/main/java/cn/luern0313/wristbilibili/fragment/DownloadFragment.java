package cn.luern0313.wristbilibili.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.fragment.app.Fragment;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.DownloadAdapter;
import cn.luern0313.wristbilibili.models.DownloadModel;
import cn.luern0313.wristbilibili.service.DownloadService;
import cn.luern0313.wristbilibili.util.FileUtil;

public class DownloadFragment extends Fragment
{
    Context ctx;
    View rootLayout;
    LayoutInflater inflater;

    private ListView uiListView;
    private LinearLayout uiListViewEmpty;
    private TextView uiTip;
    private DownloadAdapter downloadAdapter;

    private ArrayList<DownloadModel> downloadingItems;
    private ArrayList<DownloadModel> downloadedItems;

    private ObjectAnimator tipAnim;
    private String BASE_DOWNLOAD_PATH;

    private DownloadService myBinder;
    private ServiceConnection connection = new ServiceConnection()
    {
        @Override
        public void onServiceDisconnected(ComponentName name)
        {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            myBinder = ((DownloadService.MyBinder) service).getService();

            downloadingItems = myBinder.downloadingItems;
            downloadedItems = myBinder.downloadedItems;

            if(downloadingItems.size() + downloadedItems.size() > 2)
            {
                downloadAdapter = new DownloadAdapter(inflater, downloadingItems, downloadedItems, uiListView, BASE_DOWNLOAD_PATH);
                uiListView.setAdapter(downloadAdapter);
            }
            else
                uiListViewEmpty.setVisibility(View.VISIBLE);

            myBinder.setOnProgressListener(new DownloadService.downloadListener()
            {
                @Override
                public void onConnected()
                {
                    downloadAdapter.notifyDataSetChanged();
                }

                @Override
                public void onProgress()
                {
                    downloadAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCompleted()
                {
                    downloadAdapter.notifyDataSetChanged();
                }

                @Override
                public void onPaused()
                {
                    downloadAdapter.notifyDataSetChanged();
                }

                @Override
                public void onError()
                {
                    downloadAdapter.notifyDataSetChanged();
                }
            });
            rootLayout.findViewById(R.id.dl_loading).setVisibility(View.GONE);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = this.getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_download, container, false);
        this.inflater = inflater;

        uiListView = rootLayout.findViewById(R.id.dl_listview);
        uiListViewEmpty = rootLayout.findViewById(R.id.dl_nothing);
        uiListView.setEmptyView(uiListViewEmpty);
        ImageView uiTipBtu = rootLayout.findViewById(R.id.dl_tip_btu);
        uiTip = rootLayout.findViewById(R.id.dl_tip);

        BASE_DOWNLOAD_PATH = ctx.getExternalFilesDir(null) + "/download/";

        Intent intent = new Intent(ctx, DownloadService.class);
        getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);

        uiListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if(position == 0 || position == downloadingItems.size())
                    return;
                if(position < downloadingItems.size())
                {
                    if(downloadingItems.get(position).getMode() != 2)
                    {
                        if(downloadingItems.get(position).getState() == 1)
                        {
                            myBinder.pause(position);
                            downloadAdapter.notifyDataSetChanged();
                        }
                        else if(downloadingItems.get(position).getState() == 3 || downloadingItems.get(position).getState() == 5)
                        {
                            myBinder.start(position);
                            downloadAdapter.notifyDataSetChanged();
                        }
                    }
                }
                else
                {
                    String name = getVideoName();
                    if(!Objects.equals(name, ""))
                    {
                        DownloadModel downloadItem = downloadedItems.get(position - downloadingItems.size());
                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName(name, name + ".ui.PlayerActivity"));
                        intent.putExtra("mode", 2);
                        intent.putExtra("url", ctx.getExternalFilesDir(null) + "/download/" + downloadItem.getAid() + "/" + downloadItem.getCid() + "/video.mp4");
                        intent.putExtra("title", downloadItem.getTitle());
                        startActivityForResult(intent, 0);
                    }
                    else
                        Toast.makeText(ctx, "你没有安装配套视频软件：腕上视频，请先前往应用商店下载！", Toast.LENGTH_LONG).show();
                }
            }
        });

        uiListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id)
            {
                if(position == 0 || position == downloadingItems.size())
                    return false;
                new AlertDialog.Builder(ctx)
                        .setMessage("你确定要删除该任务及本地文件吗？")
                        .setPositiveButton("删除", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                DownloadModel downloadItem;
                                if(position < downloadingItems.size())
                                {
                                    myBinder.pause(position);
                                    downloadItem = downloadingItems.get(position);
                                    downloadingItems.remove(position);
                                }
                                else
                                {
                                    downloadItem = downloadedItems.get(position - downloadingItems.size());
                                    downloadedItems.remove(position - downloadingItems.size());
                                }
                                FileUtil.deleteDir(new File(ctx.getExternalFilesDir(null) +
                                                             "/download/" + downloadItem.getAid() + "/" + downloadItem.getCid() + "/"));
                                File dirFile = new File(ctx.getExternalFilesDir(null) + "/download/" + downloadItem.getAid() + "/");
                                if(dirFile.list().length == 0)
                                    FileUtil.deleteDir(dirFile);
                                downloadAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("取消", null).show();
                return true;
            }
        });

        uiTip.setText(getString(R.string.dl_tip_1) + BASE_DOWNLOAD_PATH + getString(R.string.dl_tip_2));

        uiTipBtu.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                uiTip.setVisibility(View.VISIBLE);
                ObjectAnimator.ofFloat(uiTip, "alpha", 0.0f, 1.0f).setDuration(500).start();
            }
        });

        uiTip.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                tipAnim.start();
            }
        });

        tipAnim = ObjectAnimator.ofFloat(uiTip, "alpha", 1.0f, 0.0f);
        tipAnim.setDuration(500);
        tipAnim.addListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator animation)
            {
                super.onAnimationEnd(animation);
                uiTip.setVisibility(View.GONE);
            }
        });

        return rootLayout;
    }

    private String getVideoName()
    {
        String ver = "";
        List<PackageInfo> packages = ctx.getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < packages.size(); i++)
        {
            PackageInfo packageInfo = packages.get(i);
            if(packageInfo.packageName.equals("cn.luern0313.wristvideoplayer"))
            {
                ver = "cn.luern0313.wristvideoplayer";
                break;
            }
            else if(packageInfo.packageName.equals("cn.luern0313.wristvideoplayer_free"))
                if(ver.equals(""))
                    ver = "cn.luern0313.wristvideoplayer_free";
        }
        return ver;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        try
        {
            getActivity().unbindService(connection);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
