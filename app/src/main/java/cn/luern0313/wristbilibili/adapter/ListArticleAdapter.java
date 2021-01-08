package cn.luern0313.wristbilibili.adapter;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.models.ListArticleModel;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.ImageTaskUtil;
import cn.luern0313.wristbilibili.util.LruCacheUtil;

/**
 * 被 luern0313 创建于 2020/2/2.
 */

public class ListArticleAdapter extends BaseAdapter
{
    private LayoutInflater mInflater;

    private ListArticleAdapterListener listArticleAdapterListener;
    private ListView listView;

    private ArrayList<ListArticleModel> articleList;

    public ListArticleAdapter(LayoutInflater inflater, ArrayList<ListArticleModel> articleList, ListView listView, ListArticleAdapterListener listArticleAdapterListener)
    {
        mInflater = inflater;
        this.articleList = articleList;
        this.listView = listView;
        this.listArticleAdapterListener = listArticleAdapterListener;
    }

    @Override
    public int getCount()
    {
        return articleList.size();
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
        ListArticleModel article = articleList.get(position);
        ViewHolder viewHolder;
        if(convertView == null)
        {
            convertView = mInflater.inflate(R.layout.item_list_article, null);
            viewHolder = new ViewHolder();
            convertView.setTag(viewHolder);
            viewHolder.lay = convertView.findViewById(R.id.item_list_article_lay);
            viewHolder.img1 = convertView.findViewById(R.id.item_list_article_img_1);
            viewHolder.img2 = convertView.findViewById(R.id.item_list_article_img_2);
            viewHolder.img3 = convertView.findViewById(R.id.item_list_article_img_3);
            viewHolder.title = convertView.findViewById(R.id.item_list_article_title);
            viewHolder.desc = convertView.findViewById(R.id.item_list_article_desc);
            viewHolder.up = convertView.findViewById(R.id.item_list_article_up);
            viewHolder.view = convertView.findViewById(R.id.item_list_article_view);
            viewHolder.time = convertView.findViewById(R.id.item_list_article_time);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Drawable upDrawable = convertView.getResources().getDrawable(R.drawable.icon_video_up);
        Drawable playNumDrawable = convertView.getResources().getDrawable(R.drawable.icon_number_view);
        upDrawable.setBounds(0, 0, DataProcessUtil.dip2px(10), DataProcessUtil.dip2px(10));
        playNumDrawable.setBounds(0, 0, DataProcessUtil.dip2px(10), DataProcessUtil.dip2px(10));
        viewHolder.up.setCompoundDrawables(upDrawable, null, null, null);
        viewHolder.view.setCompoundDrawables(playNumDrawable, null, null, null);

        viewHolder.img1.setImageResource(R.drawable.img_default_vid);
        viewHolder.img2.setImageResource(R.drawable.img_default_vid);
        viewHolder.img3.setImageResource(R.drawable.img_default_vid);
        viewHolder.title.setText(article.getTitle());
        viewHolder.desc.setText(article.getDesc());
        viewHolder.up.setText(article.getUp());
        viewHolder.view.setText(article.getView());
        viewHolder.time.setText(article.getTime());

        viewHolder.lay.setOnClickListener(onViewClick(position));
        viewHolder.lay.setOnLongClickListener(onViewLongClick(position));

        viewHolder.img2.setVisibility(View.GONE);
        viewHolder.img3.setVisibility(View.GONE);
        if(article.getImg() != null && article.getImg().length >= 3)
        {
            viewHolder.img3.setVisibility(View.VISIBLE);
            viewHolder.img3.setTag(article.getImg()[2]);
            BitmapDrawable img3 = setImageFormWeb(article.getImg()[2]);
            if(img3 != null) viewHolder.img3.setImageDrawable(img3);
        }
        if(article.getImg() != null && article.getImg().length >= 2)
        {
            viewHolder.img2.setVisibility(View.VISIBLE);
            viewHolder.img2.setTag(article.getImg()[1]);
            BitmapDrawable img2 = setImageFormWeb(article.getImg()[1]);
            if(img2 != null) viewHolder.img2.setImageDrawable(img2);
        }
        if(article.getImg() != null && article.getImg().length >= 1)
        {
            viewHolder.img1.setTag(article.getImg()[0]);
            BitmapDrawable img1 = setImageFormWeb(article.getImg()[0]);
            if(img1 != null) viewHolder.img1.setImageDrawable(img1);
        }
        return convertView;
    }

    private static class ViewHolder
    {
        RelativeLayout lay;
        ImageView img1;
        ImageView img2;
        ImageView img3;
        TextView title;
        TextView desc;
        TextView up;
        TextView view;
        TextView time;
    }

    private View.OnClickListener onViewClick(final int position)
    {
        return v -> listArticleAdapterListener.onListArticleAdapterClick(v.getId(), position);
    }

    private View.OnLongClickListener onViewLongClick(final int position)
    {
        return v -> {
            listArticleAdapterListener.onListArticleAdapterLongClick(v.getId(), position);
            return true;
        };
    }

    private BitmapDrawable setImageFormWeb(String url)
    {
        if(url != null && LruCacheUtil.getLruCache().get(url) != null)
            return LruCacheUtil.getLruCache().get(url);
        else
        {
            ImageTaskUtil it = new ImageTaskUtil(listView);
            it.execute(url);
            return null;
        }
    }

    public interface ListArticleAdapterListener
    {
        void onListArticleAdapterClick(int viewId, int position);

        void onListArticleAdapterLongClick(int viewId, int position);
    }
}
