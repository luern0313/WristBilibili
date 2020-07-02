package cn.luern0313.wristbilibili.adapter;

import android.graphics.drawable.BitmapDrawable;
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
            viewHolder.up = convertView.findViewById(R.id.item_list_article_up);
            viewHolder.view = convertView.findViewById(R.id.item_list_article_view);
            viewHolder.time = convertView.findViewById(R.id.item_list_article_time);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        /*Drawable upDrawable = convertView.getResources().getDrawable(R.drawable.icon_number_danmu);
        Drawable playNumDrawable = convertView.getResources().getDrawable(R.drawable.icon_number_view);
        Drawable danmakuNumDrawable = convertView.getResources().getDrawable(R.drawable.icon_article_danmu_num);
        upDrawable.setBounds(0, 0, DataProcessUtil.dip2px(10), DataProcessUtil.dip2px(10));
        playNumDrawable.setBounds(0,0, DataProcessUtil.dip2px(10), DataProcessUtil.dip2px(10));
        danmakuNumDrawable.setBounds(0,0, DataProcessUtil.dip2px(10), DataProcessUtil.dip2px(10));
        viewHolder.up.setCompoundDrawables(upDrawable,null, null,null);
        viewHolder.play.setCompoundDrawables(playNumDrawable,null, null,null);
        viewHolder.danmaku.setCompoundDrawables(danmakuNumDrawable,null, null,null);

        viewHolder.img.setImageResource(R.drawable.img_default_vid);
        viewHolder.title.setText(article.article_title);
        viewHolder.up.setText(article.article_owner_name);
        viewHolder.play.setText(article.article_play);
        viewHolder.danmaku.setText(article.article_danmaku);

        viewHolder.lay.setOnClickListener(onViewClick(position));
        viewHolder.lay.setOnLongClickListener(onViewLongClick(position));

        viewHolder.img.setTag(article.article_cover);
        BitmapDrawable c = setImageFormWeb(article.article_cover);
        if(c != null) viewHolder.img.setImageDrawable(c);*/
        return convertView;
    }

    class ViewHolder
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
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                listArticleAdapterListener.onListArticleAdapterClick(v.getId(), position);
            }
        };
    }

    private View.OnLongClickListener onViewLongClick(final int position)
    {
        return new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                listArticleAdapterListener.onListArticleAdapterLongClick(v.getId(), position);
                return true;
            }
        };
    }

    private BitmapDrawable setImageFormWeb(String url)
    {
        if(LruCacheUtil.getLruCache().get(url) != null)
        {
            return LruCacheUtil.getLruCache().get(url);
        }
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
