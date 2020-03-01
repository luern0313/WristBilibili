package cn.luern0313.wristbilibili.adapter;

import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.util.ArticleHtmlImageHandlerUtil;

/**
 * 被 luern0313 创建于 2020/2/25.
 */
public class ArticleAdapter extends BaseAdapter
{
    private LayoutInflater mInflater;

    private LruCache<String, BitmapDrawable> mImageCache;

    private Elements elements;
    private ListView listView;

    private int img_width;

    public ArticleAdapter(LayoutInflater inflater, int img_width, Elements elements, ListView listView)
    {
        mInflater = inflater;
        this.img_width = img_width;
        this.elements = elements;
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
        return elements.size();
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
        Element element = elements.get(position);
        ViewHolder viewHolder;
        if(convertView == null)
        {
            convertView = mInflater.inflate(R.layout.item_article_article, null);
            viewHolder = new ViewHolder();
            convertView.setTag(viewHolder);
            viewHolder.textView = convertView.findViewById(R.id.item_article_article_textview);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        String text = element.outerHtml();
        if(element.text().equals("") && !element.attr("class").equals("img-box"))
            viewHolder.textView.setVisibility(View.GONE);
        else
        {
            Log.i("bilibili", text);
            viewHolder.textView.setHtml(text, new ArticleHtmlImageHandlerUtil(listView.getContext(), mImageCache, viewHolder.textView, img_width));
            viewHolder.textView.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    class ViewHolder
    {
        HtmlTextView textView;
    }


    public interface ArticleListener
    {
        void onClick(int viewId, int position);
    }
}
