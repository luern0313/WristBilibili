package cn.luern0313.wristbilibili.adapter;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.RankingApi;
import cn.luern0313.wristbilibili.widget.ImageDownloader;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 被 luern0313 创建于 2020/1/9.
 */

public class RankingAdapter extends BaseAdapter
{
    private LayoutInflater mInflater;

    private LruCache<String, BitmapDrawable> mImageCache;

    private ArrayList<RankingApi.RankingVideo> rkList;
    public ListView listView;

    public RankingAdapter(LayoutInflater inflater, ArrayList<RankingApi.RankingVideo> rkList, ListView listView)
    {
        mInflater = inflater;
        this.rkList = rkList;
        this.listView = listView;

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
        return rkList.size();
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
    public View getView(int position, View convertView, ViewGroup viewGroup)
    {
        RankingApi.RankingVideo rankingVideo = rkList.get(position);
        ViewHolder viewHolder;
        if(convertView == null)
        {
            convertView = mInflater.inflate(R.layout.item_ranking_video, null);
            viewHolder = new ViewHolder();
            convertView.setTag(viewHolder);
            viewHolder.layout = convertView.findViewById(R.id.rk_video_lay);
            viewHolder.up_head = convertView.findViewById(R.id.rk_video_video_up_head);
            viewHolder.up_name = convertView.findViewById(R.id.rk_video_video_up_name);
            viewHolder.video_rank = convertView.findViewById(R.id.rk_video_rank);
            viewHolder.video_img = convertView.findViewById(R.id.rk_video_video_img);
            viewHolder.video_title = convertView.findViewById(R.id.rk_video_video_title);
            viewHolder.video_play = convertView.findViewById(R.id.rk_video_video_play);
            viewHolder.video_danmaku = convertView.findViewById(R.id.rk_video_video_danmaku);
            viewHolder.video_score = convertView.findViewById(R.id.rk_video_video_score);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Drawable playNumDrawable = convertView.getResources().getDrawable(R.drawable.icon_video_play_num);
        Drawable danmakuNumDrawable = convertView.getResources().getDrawable(R.drawable.icon_video_danmu_num);
        playNumDrawable.setBounds(0,0,24,24);
        danmakuNumDrawable.setBounds(0,0,24,24);
        viewHolder.video_play.setCompoundDrawables(playNumDrawable,null, null,null);
        viewHolder.video_danmaku.setCompoundDrawables(danmakuNumDrawable,null, null,null);

        viewHolder.up_name.setText(rankingVideo.up_name);
        viewHolder.video_rank.setText(String.valueOf(position + 1));
        viewHolder.video_title.setText(rankingVideo.video_title);
        viewHolder.video_play.setText(getView(rankingVideo.video_play));
        viewHolder.video_danmaku.setText(getView(rankingVideo.video_danmaku));
        viewHolder.video_score.setText("综合得分：" + rankingVideo.video_score);
        viewHolder.up_head.setImageResource(R.drawable.img_default_head);
        viewHolder.video_img.setImageResource(R.drawable.img_default_vid);

        switch (position + 1)
        {
            case 1:
                viewHolder.layout.setBackgroundResource(R.drawable.shape_bg_ranking_rank_1);
                viewHolder.video_rank.setTextColor(convertView.getResources().getColor(R.color.ranking_rank_1));
                break;
            case 2:
                viewHolder.layout.setBackgroundResource(R.drawable.shape_bg_ranking_rank_2);
                viewHolder.video_rank.setTextColor(convertView.getResources().getColor(R.color.ranking_rank_2));
                break;
            case 3:
                viewHolder.layout.setBackgroundResource(R.drawable.shape_bg_ranking_rank_3);
                viewHolder.video_rank.setTextColor(convertView.getResources().getColor(R.color.ranking_rank_3));
                break;
            default:
                viewHolder.layout.setBackgroundResource(0);
                viewHolder.video_rank.setTextColor(convertView.getResources().getColor(R.color.ranking_rank_other));
                break;
        }

        viewHolder.up_head.setTag(rankingVideo.up_face);
        BitmapDrawable c = setImageFormWeb(rankingVideo.up_face);
        if(c != null) viewHolder.up_head.setImageDrawable(c);

        viewHolder.video_img.setTag(rankingVideo.video_pic);
        BitmapDrawable i = setImageFormWeb(rankingVideo.video_pic);
        if(i != null) viewHolder.video_img.setImageDrawable(i);
        return convertView;
    }

    class ViewHolder
    {
        RelativeLayout layout;
        CircleImageView up_head;
        TextView up_name;
        TextView video_rank;
        ImageView video_img;
        TextView video_title;
        TextView video_play;
        TextView video_danmaku;
        TextView video_score;
    }

    private String getView(int view)
    {
        if(view > 10000) return view / 1000 / 10.0 + "万";
        else return String.valueOf(view);
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
        private ListView listView;

        ImageTask(ListView listView)
        {
            super();
            this.listView = listView;
        }

        @Override
        protected BitmapDrawable doInBackground(String... params)
        {
            try
            {
                imageUrl = params[0];
                Bitmap bitmap = null;
                bitmap = ImageDownloader.downloadImage(imageUrl);
                BitmapDrawable db = new BitmapDrawable(listView.getResources(), bitmap);
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
}
