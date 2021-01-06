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
import cn.luern0313.wristbilibili.models.ListBangumiModel;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.ImageTaskUtil;
import cn.luern0313.wristbilibili.util.LruCacheUtil;

/**
 * 被 luern0313 创建于 2020/1/31.
 */

public class ListBangumiAdapter extends BaseAdapter
{
    private LayoutInflater mInflater;

    private ListBangumiAdapterListener listBangumiAdapterListener;

    private ArrayList<ListBangumiModel> listBangumiModelArrayList;
    private ListView listView;

    public ListBangumiAdapter(LayoutInflater inflater, ListView listView, ArrayList<ListBangumiModel> listBangumiModelArrayList, ListBangumiAdapterListener listBangumiAdapterListener)
    {
        mInflater = inflater;
        this.listBangumiModelArrayList = listBangumiModelArrayList;
        this.listView = listView;
        this.listBangumiAdapterListener = listBangumiAdapterListener;
    }

    @Override
    public int getCount()
    {
        return listBangumiModelArrayList.size();
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
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ListBangumiModel bangumiRecommendModel = listBangumiModelArrayList.get(position);
        ViewHolder viewHolder;
        if(convertView == null)
        {
            convertView = mInflater.inflate(R.layout.item_list_bangumi, null);
            viewHolder = new ViewHolder();
            convertView.setTag(viewHolder);
            viewHolder.lay = convertView.findViewById(R.id.item_list_bangumi_lay);
            viewHolder.cover = convertView.findViewById(R.id.item_list_bangumi_img);
            viewHolder.title = convertView.findViewById(R.id.item_list_bangumi_title);
            viewHolder.play = convertView.findViewById(R.id.item_list_bangumi_play);
            viewHolder.follow = convertView.findViewById(R.id.item_list_bangumi_follow);
            viewHolder.newep = convertView.findViewById(R.id.item_list_bangumi_new);
            viewHolder.score = convertView.findViewById(R.id.item_list_bangumi_score);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.cover.setImageResource(R.drawable.img_default_vid);
        viewHolder.title.setText(bangumiRecommendModel.getTitle());
        viewHolder.play.setText(bangumiRecommendModel.getPlay());
        viewHolder.follow.setText(bangumiRecommendModel.getFollow());
        viewHolder.newep.setText(bangumiRecommendModel.getNewEp());
        viewHolder.score.setText(bangumiRecommendModel.getScore());

        Drawable playNumDrawable = convertView.getResources().getDrawable(R.drawable.icon_number_play);
        Drawable likeNumDrawable = convertView.getResources().getDrawable(R.drawable.icon_number_like);
        playNumDrawable.setBounds(0, 0, DataProcessUtil.dip2px(10), DataProcessUtil.dip2px(10));
        likeNumDrawable.setBounds(0, 0, DataProcessUtil.dip2px(10), DataProcessUtil.dip2px(10));
        viewHolder.play.setCompoundDrawables(playNumDrawable,null, null,null);
        viewHolder.follow.setCompoundDrawables(likeNumDrawable,null, null,null);

        viewHolder.lay.setOnClickListener(onViewClick(position));

        viewHolder.cover.setTag(bangumiRecommendModel.getCover());
        BitmapDrawable c = setImageFormWeb(bangumiRecommendModel.getCover());
        if(c != null) viewHolder.cover.setImageDrawable(c);
        return convertView;
    }

    private static class ViewHolder
    {
        RelativeLayout lay;
        ImageView cover;
        TextView title;
        TextView play;
        TextView follow;
        TextView newep;
        TextView score;
    }

    private View.OnClickListener onViewClick(final int position)
    {
        return v -> listBangumiAdapterListener.onListBangumiAdapterClick(v.getId(), position);
    }

    private BitmapDrawable setImageFormWeb(String url)
    {
        if(url == null)
            return null;
        else
        {
            BitmapDrawable cache = LruCacheUtil.getLruCache().get(url);
            if (cache != null) {
                return LruCacheUtil.getLruCache().get(url);
            }
            else
            {
                ImageTaskUtil it = new ImageTaskUtil(listView);
                it.execute(url);
                return null;
            }
        }
    }

    public interface ListBangumiAdapterListener
    {
        void onListBangumiAdapterClick(int viewId, int position);
    }
}
