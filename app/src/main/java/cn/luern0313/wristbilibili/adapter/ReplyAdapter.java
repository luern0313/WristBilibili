package cn.luern0313.wristbilibili.adapter;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.models.ReplyModel;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.ImageDownloaderUtil;

/**
 * 被 luern0313 创建于 2020/1/31.
 */

public class ReplyAdapter extends BaseAdapter
{
    private LayoutInflater mInflater;

    private LruCache<String, BitmapDrawable> mImageCache;
    private ReplyAdapterListener replyAdapterListener;

    private ArrayList<ReplyModel> replyList;
    private ListView listView;

    private boolean isShowFloor;

    public ReplyAdapter(LayoutInflater inflater, ListView listView, ArrayList<ReplyModel> replyList, boolean isShowFloor, ReplyAdapterListener replyAdapterListener)
    {
        mInflater = inflater;
        this.replyList = replyList;
        this.listView = listView;
        this.isShowFloor = isShowFloor;
        this.replyAdapterListener = replyAdapterListener;

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
        return replyList.size();
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
        return 3;
    }

    @Override
    public int getItemViewType(int position)
    {
        return replyList.get(position).getMode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup)
    {
        final ReplyModel v = replyList.get(position);
        ViewHolder viewHolder = null;
        if(convertView == null)
        {
            switch (getItemViewType(position))
            {
                case 0:
                    convertView = mInflater.inflate(R.layout.item_vd_reply, null);
                    viewHolder = new ViewHolder();
                    convertView.setTag(viewHolder);
                    viewHolder.img = convertView.findViewById(R.id.item_reply_head);
                    viewHolder.name = convertView.findViewById(R.id.item_reply_name);
                    viewHolder.time = convertView.findViewById(R.id.item_reply_time);
                    viewHolder.text = convertView.findViewById(R.id.item_reply_text);
                    viewHolder.like = convertView.findViewById(R.id.item_reply_like);
                    viewHolder.likei = convertView.findViewById(R.id.item_reply_like_i);
                    viewHolder.liken = convertView.findViewById(R.id.item_reply_like_n);
                    viewHolder.dislike = convertView.findViewById(R.id.item_reply_dislike);
                    viewHolder.dislikei = convertView.findViewById(R.id.item_reply_dislike_i);
                    viewHolder.reply = convertView.findViewById(R.id.item_reply_reply);
                    viewHolder.replyn = convertView.findViewById(R.id.item_reply_reply_n);
                    break;

                case 1:
                    convertView = mInflater.inflate(R.layout.widget_reply_changemode, null);
                    ((TextView) convertView.findViewById(R.id.item_reply_sort_sign)).setText("热门评论");
                    Drawable changeNewDrawable = convertView.getResources().getDrawable(R.drawable.icon_reply_sort);
                    changeNewDrawable.setBounds(0, 0, DataProcessUtil.dip2px(listView.getContext(), 12), DataProcessUtil.dip2px(listView.getContext(), 12));
                    ((TextView) convertView.findViewById(R.id.item_reply_sort_change)).setCompoundDrawables(changeNewDrawable,null, null,null);
                    if(isHasRoot)
                        convertView.findViewById(R.id.item_reply_sort_change).setVisibility(View.GONE);
                    else
                        convertView.findViewById(R.id.item_reply_sort_change).setVisibility(View.VISIBLE);
                    break;

                case 2:
                    convertView = mInflater.inflate(R.layout.widget_reply_changemode, null);
                    ((TextView) convertView.findViewById(R.id.item_reply_sort_sign)).setText("最新评论");
                    Drawable changeHotDrawable = convertView.getResources().getDrawable(R.drawable.icon_reply_sort);
                    changeHotDrawable.setBounds(0, 0,DataProcessUtil.dip2px(listView.getContext(), 12), DataProcessUtil.dip2px(listView.getContext(), 12));
                    ((TextView) convertView.findViewById(R.id.item_reply_sort_change))
                            .setCompoundDrawables(changeHotDrawable,null, null,null);
                    if(isHasRoot)
                        convertView.findViewById(R.id.item_reply_sort_change).setVisibility(View.GONE);
                    else
                        convertView.findViewById(R.id.item_reply_sort_change).setVisibility(View.VISIBLE);
                    break;
            }
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if(getItemViewType(position) == 0)
        {
            viewHolder.img.setImageResource(R.drawable.img_default_avatar);
            viewHolder.name.setText(v.getUserName());
            viewHolder.time.setText(v.getReplyTime() + "   " + v.getReplyFloor(isShowFloor) + "   LV" + v.getUserLv());
            viewHolder.text.setText(v.getReplyText());
            viewHolder.liken.setText(v.getReplyBeLiked());
            viewHolder.replyn.setText(v.getReplyBeReply());

            if(v.isReplyLike()) viewHolder.likei.setImageResource(R.drawable.icon_liked);
            else viewHolder.likei.setImageResource(R.drawable.icon_like);
            if(v.isReplyDislike()) viewHolder.dislikei.setImageResource(R.drawable.icon_disliked);
            else viewHolder.dislikei.setImageResource(R.drawable.icon_dislike);
            if(v.getUserVip() == 2) viewHolder.name.setTextColor(listView.getResources().getColor(R.color.mainColor));
            else viewHolder.name.setTextColor(listView.getResources().getColor(R.color.black));

            viewHolder.img.setTag(v.getUserHead());
            BitmapDrawable h = setImageFormWeb(v.getUserHead());
            if(h != null) viewHolder.img.setImageDrawable(h);

            viewHolder.img.setOnClickListener(onViewClick(position));
            viewHolder.like.setOnClickListener(onViewClick(position));
            viewHolder.dislike.setOnClickListener(onViewClick(position));
            viewHolder.reply.setOnClickListener(onViewClick(position));
        }
        return convertView;
    }

    private View.OnClickListener onViewClick(final int position)
    {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                replyAdapterListener.onClick(v.getId(), position);
            }
        };
    }

    class ViewHolder
    {
        ImageView img;
        TextView name;
        TextView time;
        TextView text;
        LinearLayout like;
        ImageView likei;
        TextView liken;
        LinearLayout dislike;
        ImageView dislikei;
        LinearLayout reply;
        TextView replyn;
    }

    private BitmapDrawable setImageFormWeb(String url)
    {
        if(mImageCache.get(url) != null)
        {
            return mImageCache.get(url);
        }
        else
        {
            ImageTask it = new ImageTask(listView);
            it.execute(url);
            return null;
        }
    }

    class ImageTask extends AsyncTask<String, Void, BitmapDrawable>
    {
        private String imageUrl;
        private Resources listViewResources;

        ImageTask(ListView listView)
        {
            this.listViewResources = listView.getResources();
        }

        @Override
        protected BitmapDrawable doInBackground(String... params)
        {
            try
            {
                imageUrl = params[0];
                Bitmap bitmap = null;
                bitmap = ImageDownloaderUtil.downloadImage(imageUrl);
                BitmapDrawable db = new BitmapDrawable(listViewResources, bitmap);
                // 如果本地还没缓存该图片，就缓存
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
            ImageView iv = listView.findViewWithTag(imageUrl);
            if(iv != null && result != null)
            {
                iv.setImageDrawable(result);
            }
        }
    }

    public interface ReplyAdapterListener
    {
        void onClick(int viewId, int position);
    }
}
