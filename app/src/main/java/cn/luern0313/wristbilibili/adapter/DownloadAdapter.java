package cn.luern0313.wristbilibili.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.models.DownloadModel;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.ImageTaskUtil;
import cn.luern0313.wristbilibili.util.MyApplication;

/**
 * 被 luern0313 创建于 2020/4/29.
 */
public class DownloadAdapter extends BaseAdapter
{
    private Context ctx;
    private LayoutInflater mInflater;
    private ListView listView;
    private String path;

    private LruCache<String, BitmapDrawable> mImageCache;

    private ArrayList<DownloadModel> downloadingItems = new ArrayList<>();
    private ArrayList<DownloadModel> downloadedItems = new ArrayList<>();

    public DownloadAdapter(LayoutInflater inflater, ArrayList<DownloadModel> dlingList, ArrayList<DownloadModel> dledList, ListView listView, String path)
    {
        this.ctx = MyApplication.getContext();
        this.mInflater = inflater;
        this.downloadingItems = dlingList;
        this.downloadedItems = dledList;
        this.listView = listView;
        this.path = path;

        int maxCache = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxCache / 8;
        mImageCache = new LruCache<String, BitmapDrawable>(cacheSize)
        {
            @Override
            protected int sizeOf(@NonNull String key, @NonNull BitmapDrawable value)
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
            if(downloadingItems.get(position).getDownloadMode() == 2) return 1;
            else return 0;
        }
        else
        {
            if(downloadedItems.get(position - downloadingItems.size()).getDownloadMode() == 2) return 1;
            else return 0;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup)
    {
        DownloadModel downloadItem;
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
            viewHolder.title.setText(downloadItem.getDownloadTitle());
            viewHolder.size.setText(DataProcessUtil.getSize(downloadItem.getDownloadSize()));
            viewHolder.img.setImageResource(R.drawable.img_default_animation);

            if(downloadItem.getDownloadMode() == 0) //下载完成
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
                viewHolder.time.setTextColor(listView.getResources().getColor(R.color.gray_44));
                if(downloadItem.getDownloadState() == 0) //初始化
                {
                    viewHolder.size.setText(downloadItem.getDownloadSize() == 0 ? ctx.getString(R.string.download_size_unknown) : DataProcessUtil.getSize(downloadItem.getDownloadSize()));
                    viewHolder.speed.setText(String.format(ctx.getString(R.string.download_size), DataProcessUtil.getSize(downloadItem.getDownloadSpeed())));
                    viewHolder.progress.setText(String.format(ctx.getString(R.string.download_progress), (int) (downloadItem.getDownloadNowdl() * 100.0 / downloadItem.getDownloadSize())));
                    viewHolder.time.setText(ctx.getString(R.string.download_state_0));
                    viewHolder.time.setTextColor(listView.getResources().getColor(R.color.colorAccent));
                    viewHolder.prog.setProgress((int) (downloadItem.getDownloadNowdl() * 100.0 / downloadItem.getDownloadSize()));
                }
                else if(downloadItem.getDownloadState() == 1) //下载中
                {
                    viewHolder.size.setText(DataProcessUtil.getSize(downloadItem.getDownloadSize()));
                    viewHolder.speed.setText(String.format(ctx.getString(R.string.download_size), DataProcessUtil.getSize(downloadItem.getDownloadSpeed())));
                    viewHolder.progress.setText(String.format(ctx.getString(R.string.download_progress), (int) (downloadItem.getDownloadNowdl() * 100.0 / downloadItem.getDownloadSize())));
                    viewHolder.time.setText(DataProcessUtil.getSurplusTime(downloadItem.getDownloadSize() - downloadItem.getDownloadNowdl(), downloadItem.getDownloadSpeed()));
                    viewHolder.prog.setProgress((int) (downloadItem.getDownloadNowdl() * 100.0 / downloadItem.getDownloadSize()));
                }
                else if(downloadItem.getDownloadState() == 2) //暂停中
                {
                    viewHolder.size.setText(DataProcessUtil.getSize(downloadItem.getDownloadSize()));
                    viewHolder.speed.setText(ctx.getString(R.string.download_speed_zero));
                    viewHolder.progress.setText(String.format(ctx.getString(R.string.download_progress), (int) (downloadItem.getDownloadNowdl() * 100.0 / downloadItem.getDownloadSize())));
                    viewHolder.time.setText(ctx.getString(R.string.download_state_2));
                    viewHolder.time.setTextColor(listView.getResources().getColor(R.color.colorAccent));
                    viewHolder.prog.setProgress((int) (downloadItem.getDownloadNowdl() * 100.0 / downloadItem.getDownloadSize()));
                }
                else if(downloadItem.getDownloadState() == 3) //暂停
                {
                    viewHolder.size.setText(DataProcessUtil.getSize(downloadItem.getDownloadSize()));
                    viewHolder.speed.setText(ctx.getString(R.string.download_speed_zero));
                    viewHolder.progress.setText(String.format(ctx.getString(R.string.download_progress), (int) (downloadItem.getDownloadNowdl() * 100.0 / downloadItem.getDownloadSize())));
                    viewHolder.time.setText(ctx.getString(R.string.download_state_3));
                    viewHolder.time.setTextColor(listView.getResources().getColor(R.color.colorAccent));
                    viewHolder.prog.setProgress((int) (downloadItem.getDownloadNowdl() * 100.0 / downloadItem.getDownloadSize()));
                }
                else if(downloadItem.getDownloadState() == 4) //错误
                {
                    viewHolder.size.setText(DataProcessUtil.getSize(downloadItem.getDownloadSize()));
                    viewHolder.speed.setText(String.format(ctx.getString(R.string.download_size), DataProcessUtil.getSize(downloadItem.getDownloadSpeed())));
                    viewHolder.progress.setText(String.format(ctx.getString(R.string.download_progress), (int) (downloadItem.getDownloadNowdl() * 100.0 / downloadItem.getDownloadSize())));
                    viewHolder.time.setText(downloadItem.getDownloadTip());
                    viewHolder.time.setTextColor(listView.getResources().getColor(R.color.colorAccent));
                    viewHolder.prog.setProgress((int) (downloadItem.getDownloadNowdl() * 100.0 / downloadItem.getDownloadSize()));
                }
            }

            viewHolder.img.setTag(downloadItem.getDownloadCover());
            BitmapDrawable c = setImageFormWeb(downloadItem.getDownloadCover(), downloadItem);
            if(c != null) viewHolder.img.setImageDrawable(c);
        }
        else if(type == 1)
        {
            ((TextView) convertView.findViewById(R.id.item_reply_sort_sign)).setText(downloadItem.getDownloadTitle());
            convertView.findViewById(R.id.item_reply_sort_change).setVisibility(View.GONE);
            convertView.findViewById(R.id.item_reply_sort_sign).setVisibility(View.VISIBLE);
            if((position == 0 && downloadingItems.size() == 1) || (position == downloadingItems.size() && downloadedItems.size() == 1))
            {
                convertView.findViewById(R.id.item_reply_sort).setVisibility(View.GONE);
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,1);
                convertView.setLayoutParams(params);
            }

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

    BitmapDrawable setImageFormWeb(String url, DownloadModel downloadItem)
    {
        if(mImageCache.get(url) != null)
            return mImageCache.get(url);
        else
        {
            Bitmap bitmap = BitmapFactory.decodeFile(path + downloadItem.getDownloadAid() + "/" + downloadItem.getDownloadCid() + "/cover.png");
            if(bitmap != null)
            {
                BitmapDrawable db = new BitmapDrawable(listView.getResources(), bitmap);
                mImageCache.put(downloadItem.getDownloadCover(), db);
                return db;
            }
            else
            {
                ImageTaskUtil it = new ImageTaskUtil(listView);
                it.execute(url);
            }
            return null;
        }
    }
}
