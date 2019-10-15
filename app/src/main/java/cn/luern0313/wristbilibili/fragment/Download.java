package cn.luern0313.wristbilibili.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.DownloadApi.DownloadItem;
import cn.luern0313.wristbilibili.service.DownloadService;
import cn.luern0313.wristbilibili.util.FileUtil;
import cn.luern0313.wristbilibili.widget.ImageDownloader;

public class Download extends Fragment
{
    Context ctx;
    View rootLayout;
    LayoutInflater inflater;

    ListView uiListView;
    LinearLayout uiListViewEmpty;
    ImageView uiTipBtu;
    TextView uiTip;
    mAdapter mAdapter;

    ArrayList<DownloadItem> downloadingItems;
    ArrayList<DownloadItem> downloadedItems;

    ObjectAnimator tipAnim;

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
                mAdapter = new mAdapter(inflater, downloadingItems, downloadedItems);
                uiListView.setAdapter(mAdapter);
            }
            else
                uiListViewEmpty.setVisibility(View.VISIBLE);

            myBinder.setOnProgressListener(new DownloadService.downloadListener()
            {
                @Override
                public void onConnected()
                {
                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onProgress()
                {
                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCompleted()
                {
                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onPaused()
                {
                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onError()
                {
                    mAdapter.notifyDataSetChanged();
                }
            });
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
        uiTipBtu = rootLayout.findViewById(R.id.dl_tip_btu);
        uiTip = rootLayout.findViewById(R.id.dl_tip);

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
                    if(downloadingItems.get(position).mode != 2)
                    {
                        if(downloadingItems.get(position).state == 1)
                        {
                            myBinder.pause(position);
                            mAdapter.notifyDataSetChanged();
                        }
                        else if(downloadingItems.get(position).state == 3 || downloadingItems.get(position).state == 5)
                        {
                            myBinder.start(position);
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                }
                else
                {
                    String name = getVideoName();
                    if(!Objects.equals(name, ""))
                    {
                        DownloadItem downloadItem = downloadedItems.get(position - downloadingItems.size());
                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName(name, name + ".ui.PlayerActivity"));
                        intent.putExtra("mode", 2);
                        intent.putExtra("url", ctx.getExternalFilesDir(null) + "/download/" + downloadItem.aid + "/" + downloadItem.cid + "/video.mp4");
                        intent.putExtra("title", downloadItem.title);
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
                                DownloadItem downloadItem;
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
                                                             "/download/" + downloadItem.aid + "/" + downloadItem.cid + "/"));
                                File dirFile = new File(ctx.getExternalFilesDir(null) + "/download/" + downloadItem.aid + "/");
                                if(dirFile.list().length == 0)
                                    FileUtil.deleteDir(dirFile);
                                mAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("取消", null).show();
                return true;
            }
        });

        uiTip.setText(getString(R.string.dl_tip_1) + ctx.getExternalFilesDir(null) + "/download/" + getString(R.string.dl_tip_2));

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

    String getSize(long size)
    {
        String[] unit = new String[]{"B", "KB", "MB", "GB"};
        long s = size * 10;
        int u = 0;
        while (s > 10240 && u < unit.length - 1)
        {
            s /= 1024;
            u++;
        }
        return String.valueOf(s / 10.0) + unit[u];
    }

    String getSurplusTime(long surplusByte, int speed)
    {
        if(speed <= 0) return "未知";
        long time = surplusByte / speed;

        String sec = String.valueOf(time % 60);
        if(sec.length() == 1) sec = "0" + sec;
        String min = String.valueOf(time / 60 % 60);
        if(min.length() == 1) min = "0" + min;
        String hour = String.valueOf(time / 3600 % 60);
        if(hour.length() == 1) hour = "0" + hour;

        if(hour.equals("00")) return min + ":" + sec;
        else return hour + ":" + min + ":" + sec;
    }

    String getVideoName()
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

    class mAdapter extends BaseAdapter
    {
        private LayoutInflater mInflater;

        private LruCache<String, BitmapDrawable> mImageCache;

        private ArrayList<DownloadItem> downloadingItems = new ArrayList<>();
        private ArrayList<DownloadItem> downloadedItems = new ArrayList<>();

        public mAdapter(LayoutInflater inflater, ArrayList<DownloadItem> dlingList, ArrayList<DownloadItem> dledList)
        {
            mInflater = inflater;
            downloadingItems.clear();
            downloadedItems.clear();
            /*if(dlingList.size() <= 1)
                this.downloadingItems = new ArrayList<>();
            else
                this.downloadingItems = dlingList;

            if(dledList.size() <= 1)
                this.downloadedItems = new ArrayList<>();
            else
                this.downloadedItems = dledList;*/
            downloadingItems = dlingList;
            downloadedItems = dledList;

            int maxCache = (int) Runtime.getRuntime().maxMemory();
            int cacheSize = maxCache / 8;
            mImageCache = new LruCache<String, BitmapDrawable>(cacheSize)
            {
                @Override
                protected int sizeOf(String key, BitmapDrawable value)
                {
                    try
                    {
                        return value.getBitmap().getByteCount();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    return 0;
                }
            };
        }

        @Override
        public int getCount()
        {
            return downloadingItems.size() + downloadedItems.size();
        }

        @Override
        public Object getItem(int position)
        {
            return position;
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public int getViewTypeCount()
        {
            return 2;
        }

        @Override
        public int getItemViewType(int position)
        {
            if(position < downloadingItems.size())
            {
                if(downloadingItems.get(position).mode == 2) return 1;
                else return 0;
            }
            else
            {
                if(downloadedItems.get(position - downloadingItems.size()).mode == 2) return 1;
                else return 0;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup)
        {
            DownloadItem downloadItem;
            if(position < downloadingItems.size()) downloadItem = downloadingItems.get(position);
            else downloadItem = downloadedItems.get(position - downloadingItems.size());

            int type = getItemViewType(position);
            ViewHolder viewHolder = null;
            if(convertView == null)
            {
                if(type == 1)
                {
                    convertView = mInflater.inflate(R.layout.widget_reply_changemode, null);
                }
                else
                {
                    convertView = mInflater.inflate(R.layout.item_dl_dling, null);
                    viewHolder = new ViewHolder();
                    convertView.setTag(viewHolder);
                    viewHolder.lay = convertView.findViewById(R.id.dling_lay);
                    viewHolder.img = convertView.findViewById(R.id.dling_img);
                    viewHolder.title = convertView.findViewById(R.id.dling_title);
                    viewHolder.size = convertView.findViewById(R.id.dling_size);
                    viewHolder.speed = convertView.findViewById(R.id.dling_speed);
                    viewHolder.progress = convertView.findViewById(R.id.dling_progress);
                    viewHolder.time = convertView.findViewById(R.id.dling_time);
                    viewHolder.prog = convertView.findViewById(R.id.dling_prog);
                }
            }
            else
            {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if(type == 0)
            {
                viewHolder.title.setText(downloadItem.title);
                viewHolder.size.setText(getSize(downloadItem.size));
                viewHolder.img.setImageResource(R.drawable.img_default_animation);

                if(downloadItem.mode == 0) //下载完成
                {
                    viewHolder.speed.setVisibility(View.GONE);
                    viewHolder.progress.setVisibility(View.GONE);
                    viewHolder.time.setVisibility(View.GONE);
                    viewHolder.prog.setVisibility(View.GONE);
                }
                else //下载中
                {
                    viewHolder.speed.setVisibility(View.VISIBLE);
                    viewHolder.progress.setVisibility(View.VISIBLE);
                    viewHolder.time.setVisibility(View.VISIBLE);
                    viewHolder.prog.setVisibility(View.VISIBLE);
                    viewHolder.time.setTextColor(getResources().getColor(R.color.textColor3));
                    if(downloadItem.state == 0) //初始化
                    {
                        viewHolder.size.setText(downloadItem.size == 0 ? "未知" : getSize(downloadItem.size));
                        viewHolder.speed.setText(getSize(downloadItem.speed) + "/s");
                        viewHolder.progress.setText((int) (downloadItem.nowdl * 100.0 / downloadItem.size) + "%");
                        viewHolder.time.setText("连接中..");
                        viewHolder.time.setTextColor(getResources().getColor(R.color.colorAccent));
                        viewHolder.prog.setProgress((int) (downloadItem.nowdl * 100.0 / downloadItem.size));
                    }
                    else if(downloadItem.state == 1) //下载中
                    {
                        viewHolder.size.setText(getSize(downloadItem.size));
                        viewHolder.speed.setText(getSize(downloadItem.speed) + "/s");
                        viewHolder.progress.setText((int) (downloadItem.nowdl * 100.0 / downloadItem.size) + "%");
                        viewHolder.time.setText(getSurplusTime(downloadItem.size - downloadItem.nowdl, downloadItem.speed));
                        viewHolder.prog.setProgress((int) (downloadItem.nowdl * 100.0 / downloadItem.size));
                    }
                    else if(downloadItem.state == 2) //暂停中
                    {
                        viewHolder.size.setText(getSize(downloadItem.size));
                        viewHolder.speed.setText("0.0B/s");
                        viewHolder.progress.setText((int) (downloadItem.nowdl * 100.0 / downloadItem.size) + "%");
                        viewHolder.time.setText("暂停中..");
                        viewHolder.time.setTextColor(getResources().getColor(R.color.colorAccent));
                        viewHolder.prog.setProgress((int) (downloadItem.nowdl * 100.0 / downloadItem.size));
                    }
                    else if(downloadItem.state == 3) //暂停
                    {
                        viewHolder.size.setText(getSize(downloadItem.size));
                        viewHolder.speed.setText("0.0B/s");
                        viewHolder.progress.setText((int) (downloadItem.nowdl * 100.0 / downloadItem.size) + "%");
                        viewHolder.time.setText("暂停");
                        viewHolder.time.setTextColor(getResources().getColor(R.color.colorAccent));
                        viewHolder.prog.setProgress((int) (downloadItem.nowdl * 100.0 / downloadItem.size));
                    }
                    else if(downloadItem.state == 4) //错误
                    {
                        viewHolder.size.setText(getSize(downloadItem.size));
                        viewHolder.speed.setText(getSize(downloadItem.speed) + "/s");
                        viewHolder.progress.setText((int) (downloadItem.nowdl * 100.0 / downloadItem.size) + "%");
                        viewHolder.time.setText(downloadItem.tip);
                        viewHolder.time.setTextColor(getResources().getColor(R.color.colorAccent));
                        viewHolder.prog.setProgress((int) (downloadItem.nowdl * 100.0 / downloadItem.size));
                    }
                }

                viewHolder.img.setTag(downloadItem.cover);
                BitmapDrawable c = setImageFormWeb(downloadItem.cover);
                if(c != null) viewHolder.img.setImageDrawable(c);
            }
            else if(type == 1)
            {
                ((TextView) convertView.findViewById(R.id.item_reply_sign)).setText(downloadItem.title);
                convertView.findViewById(R.id.item_reply_sign).setVisibility(View.VISIBLE);
                if(position == 0 && downloadingItems.size() == 1)
                    convertView.findViewById(R.id.item_reply_sign).setVisibility(View.GONE);
                else if(position == downloadingItems.size() && downloadedItems.size() == 1)
                    convertView.findViewById(R.id.item_reply_sign).setVisibility(View.GONE);
            }
            return convertView;
        }

        class ViewHolder
        {
            RelativeLayout lay;
            ImageView img;
            TextView title;
            TextView size;
            TextView speed;
            TextView progress;
            TextView time;
            ProgressBar prog;
        }

        BitmapDrawable setImageFormWeb(String url)
        {
            if(mImageCache.get(url) != null) return mImageCache.get(url);
            else
            {
                ImageTask it = new ImageTask();
                it.execute(url);
                return null;
            }
        }

        class ImageTask extends AsyncTask<String, Void, BitmapDrawable>
        {
            private String imageUrl;

            @Override
            protected BitmapDrawable doInBackground(String... params)
            {
                try
                {
                    imageUrl = params[0];
                    Bitmap bitmap = null;
                    bitmap = ImageDownloader.downloadImage(imageUrl);
                    BitmapDrawable db = new BitmapDrawable(uiListView.getResources(), bitmap);
                    if(mImageCache.get(imageUrl) == null && bitmap != null)
                    {
                        mImageCache.put(imageUrl, db);
                    }
                    return db;
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(BitmapDrawable result)
            {
                // 通过Tag找到我们需要的ImageView，如果该ImageView所在的item已被移出页面，就会直接返回null
                ImageView iv = uiListView.findViewWithTag(imageUrl);
                if(iv != null && result != null)
                {
                    iv.setImageDrawable(result);
                }
            }
        }
    }
}
