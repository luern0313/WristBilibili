package cn.luern0313.wristbilibili.adapter;

import android.graphics.drawable.BitmapDrawable;
import android.support.v4.util.LruCache;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import cn.carbs.android.expandabletextview.library.ExpandableTextView;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.UserDynamicApi;
import cn.luern0313.wristbilibili.util.ImageTaskUtil;

/**
 * 被 luern0313 创建于 2019/8/31.
 */

public class DynamicAdapter extends BaseAdapter
{
    private LayoutInflater mInflater;

    private LruCache<String, BitmapDrawable> mImageCache;
    private DynamicAdapterListener dynamicAdapterListener;

    private ArrayList<Object> dyList;
    private ListView listView;

    public DynamicAdapter(LayoutInflater inflater, ArrayList<Object> dyList, ListView listView, DynamicAdapterListener dynamicAdapterListener)
    {
        mInflater = inflater;
        this.dyList = dyList;
        this.listView = listView;
        this.dynamicAdapterListener = dynamicAdapterListener;

        int maxCache = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxCache / 6;
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
        return dyList.size();
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
        return 5;
    }

    @Override
    public int getItemViewType(int position)
    {
        if(dyList.get(position) instanceof UserDynamicApi.cardOriginalVideo) return 4;
        else if(dyList.get(position) instanceof UserDynamicApi.cardOriginalText) return 3;
        else if(dyList.get(position) instanceof UserDynamicApi.cardUnknow) return 2;
        else if(dyList.get(position) instanceof UserDynamicApi.cardShareVideo) return 1;
        else if(dyList.get(position) instanceof UserDynamicApi.cardShareText) return 0;
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup)
    {
        ViewHolderOriText viewHolderOriText = null;
        ViewHolderOriVid viewHolderOriVid = null;
        ViewHolderShaText viewHolderShaText = null;
        ViewHolderShaVid viewHolderShaVid = null;
        ViewHolderUnktyp viewHolderUnktyp = null;
        int type = getItemViewType(position);

        // 若无可重用的 view 则进行加载
        if(convertView == null)
        {
            switch (type)
            {
                case 4:
                    //原创视频
                    convertView = mInflater.inflate(R.layout.item_news_original_video, null);
                    viewHolderOriVid = new ViewHolderOriVid();
                    convertView.setTag(viewHolderOriVid);

                    viewHolderOriVid.lay = convertView.findViewById(R.id.liov_lay);
                    viewHolderOriVid.head = convertView.findViewById(R.id.liov_head);
                    viewHolderOriVid.name = convertView.findViewById(R.id.liov_name);
                    viewHolderOriVid.time = convertView.findViewById(R.id.liov_time);
                    viewHolderOriVid.text = convertView.findViewById(R.id.liov_text);
                    viewHolderOriVid.img = convertView.findViewById(R.id.liov_share_img);
                    viewHolderOriVid.imgtext = convertView.findViewById(R.id.liov_textimg);
                    viewHolderOriVid.title = convertView.findViewById(R.id.liov_title);
                    viewHolderOriVid.likebu = convertView.findViewById(R.id.liov_likebu);
                    viewHolderOriVid.likei = convertView.findViewById(R.id.liov_likei);
                    viewHolderOriVid.like = convertView.findViewById(R.id.liov_like);
                    break;

                case 3:
                    //原创文字
                    convertView = mInflater.inflate(R.layout.item_news_original_text, null);
                    viewHolderOriText = new ViewHolderOriText();
                    convertView.setTag(viewHolderOriText);

                    viewHolderOriText.head = convertView.findViewById(R.id.liot_head);
                    viewHolderOriText.name = convertView.findViewById(R.id.liot_name);
                    viewHolderOriText.time = convertView.findViewById(R.id.liot_time);
                    viewHolderOriText.text = convertView.findViewById(R.id.liot_text);
                    viewHolderOriText.sharei = convertView.findViewById(R.id.liot_sharei);
                    viewHolderOriText.textimg = convertView.findViewById(R.id.liot_textimg);
                    viewHolderOriText.replybu = convertView.findViewById(R.id.liot_replybu);
                    viewHolderOriText.reply = convertView.findViewById(R.id.liot_reply);
                    viewHolderOriText.likebu = convertView.findViewById(R.id.liot_likebu);
                    viewHolderOriText.likei = convertView.findViewById(R.id.liot_likei);
                    viewHolderOriText.like = convertView.findViewById(R.id.liot_like);
                    break;

                case 2:
                    //未知类型
                    convertView = mInflater.inflate(R.layout.item_news_unknowtype, null);
                    viewHolderUnktyp = new ViewHolderUnktyp();
                    convertView.setTag(viewHolderUnktyp);

                    viewHolderUnktyp.head = convertView.findViewById(R.id.liuk_head);
                    viewHolderUnktyp.name = convertView.findViewById(R.id.liuk_name);
                    viewHolderUnktyp.time = convertView.findViewById(R.id.liuk_time);
                    break;

                case 1:
                    //转发视频
                    convertView = mInflater.inflate(R.layout.item_news_share_video, null);
                    viewHolderShaVid = new ViewHolderShaVid();
                    convertView.setTag(viewHolderShaVid);

                    viewHolderShaVid.head = convertView.findViewById(R.id.lisv_head);
                    viewHolderShaVid.name = convertView.findViewById(R.id.lisv_name);
                    viewHolderShaVid.time = convertView.findViewById(R.id.lisv_time);
                    viewHolderShaVid.text = convertView.findViewById(R.id.lisv_text);
                    viewHolderShaVid.slay = convertView.findViewById(R.id.lisv_share_lay);
                    viewHolderShaVid.shead = convertView.findViewById(R.id.lisv_share_head);
                    viewHolderShaVid.sname = convertView.findViewById(R.id.lisv_share_name);
                    viewHolderShaVid.simg = convertView.findViewById(R.id.lisv_share_img);
                    viewHolderShaVid.simgtext = convertView.findViewById(R.id.lisv_share_imgtext);
                    viewHolderShaVid.stitle = convertView.findViewById(R.id.lisv_share_text);
                    viewHolderShaVid.sharei = convertView.findViewById(R.id.lisv_sharei);
                    viewHolderShaVid.replybu = convertView.findViewById(R.id.lisv_replybu);
                    viewHolderShaVid.reply = convertView.findViewById(R.id.lisv_reply);
                    viewHolderShaVid.likebu = convertView.findViewById(R.id.lisv_likebu);
                    viewHolderShaVid.likei = convertView.findViewById(R.id.lisv_likei);
                    viewHolderShaVid.like = convertView.findViewById(R.id.lisv_like);
                    break;

                case 0:
                    //转发文字
                    convertView = mInflater.inflate(R.layout.item_news_share_text, null);
                    viewHolderShaText = new ViewHolderShaText();
                    convertView.setTag(viewHolderShaText);

                    viewHolderShaText.head = convertView.findViewById(R.id.list_head);
                    viewHolderShaText.name = convertView.findViewById(R.id.list_name);
                    viewHolderShaText.time = convertView.findViewById(R.id.list_time);
                    viewHolderShaText.text = convertView.findViewById(R.id.list_text);
                    viewHolderShaText.shead = convertView.findViewById(R.id.list_share_head);
                    viewHolderShaText.sname = convertView.findViewById(R.id.list_share_name);
                    viewHolderShaText.stext = convertView.findViewById(R.id.list_share_text);
                    viewHolderShaText.stextimg = convertView.findViewById(R.id.list_share_textimg);
                    viewHolderShaText.sharei = convertView.findViewById(R.id.list_sharei);
                    viewHolderShaText.replybu = convertView.findViewById(R.id.list_replybu);
                    viewHolderShaText.reply = convertView.findViewById(R.id.list_reply);
                    viewHolderShaText.likebu = convertView.findViewById(R.id.list_likebu);
                    viewHolderShaText.likei = convertView.findViewById(R.id.list_likei);
                    viewHolderShaText.like = convertView.findViewById(R.id.list_like);
                    break;
            }
        }
        else
        {
            switch (type)
            {
                case 4:
                    viewHolderOriVid = (ViewHolderOriVid) convertView.getTag();
                    break;
                case 3:
                    viewHolderOriText = (ViewHolderOriText) convertView.getTag();
                    break;
                case 2:
                    viewHolderUnktyp = (ViewHolderUnktyp) convertView.getTag();
                    break;
                case 1:
                    viewHolderShaVid = (ViewHolderShaVid) convertView.getTag();
                    break;
                case 0:
                    viewHolderShaText = (ViewHolderShaText) convertView.getTag();
                    break;
            }
        }

        if(type == 4) //原创视频
        {
            final UserDynamicApi.cardOriginalVideo dy = (UserDynamicApi.cardOriginalVideo) dyList.get(position);
            viewHolderOriVid.name.setText(Html.fromHtml("<b>" + dy.getOwnerName() + "</b>投稿了视频"));
            viewHolderOriVid.time.setText(dy.getDynamicTime());
            if(!dy.getDynamic().equals(""))
            {
                viewHolderOriVid.text.setVisibility(View.VISIBLE);
                viewHolderOriVid.text.setText(dy.getDynamic());
            }
            else viewHolderOriVid.text.setVisibility(View.GONE);
            viewHolderOriVid.imgtext.setText(
                    dy.getVideoDuration() + "  " + dy.getVideoView() + "观看");
            viewHolderOriVid.title.setText(dy.getVideoTitle());
            if(dy.isLike) viewHolderOriVid.likei.setImageResource(R.drawable.icon_liked);
            else viewHolderOriVid.likei.setImageResource(R.drawable.icon_like);
            viewHolderOriVid.like.setText(String.valueOf(dy.getBeLiked()));
            viewHolderOriVid.head.setImageResource(R.drawable.img_default_head);
            viewHolderOriVid.img.setImageResource(R.drawable.img_default_vid);

            viewHolderOriVid.head.setTag(dy.getOwnerHead());
            viewHolderOriVid.img.setTag(dy.getVideoImg());
            final BitmapDrawable h = setImageFormWeb(dy.getOwnerHead());
            BitmapDrawable i = setImageFormWeb(dy.getVideoImg());
            if(h != null) viewHolderOriVid.head.setImageDrawable(h);
            if(i != null) viewHolderOriVid.img.setImageDrawable(i);

            viewHolderOriVid.lay.setOnClickListener(onViewClick(position, 4));
            viewHolderOriVid.head.setOnClickListener(onViewClick(position, 4));
            viewHolderOriVid.likebu.setOnClickListener(onViewClick(position, 4));

        }
        else if(type == 3)// 原创文字
        {
            final UserDynamicApi.cardOriginalText dy = (UserDynamicApi.cardOriginalText) dyList.get(position);
            viewHolderOriText.name.setText(dy.getUserName());
            viewHolderOriText.time.setText(dy.getDynamicTime());
            viewHolderOriText.text.setText(dy.getDynamicText());
            if(!dy.getTextImgCount().equals("0"))
            {
                viewHolderOriText.textimg.setVisibility(View.VISIBLE);
                viewHolderOriText.textimg.setText("查看共" + dy.getTextImgCount() + "张图片");
            }
            else viewHolderOriText.textimg.setVisibility(View.GONE);
            viewHolderOriText.reply.setText(String.valueOf(dy.getBeReply()));
            if(dy.isLike) viewHolderOriText.likei.setImageResource(R.drawable.icon_liked);
            else viewHolderOriText.likei.setImageResource(R.drawable.icon_like);
            viewHolderOriText.like.setText(String.valueOf(dy.getBeLiked()));
            viewHolderOriText.head.setImageResource(R.drawable.img_default_head);

            viewHolderOriText.head.setTag(dy.getUserHead());
            BitmapDrawable h = setImageFormWeb(dy.getUserHead());
            if(h != null) viewHolderOriText.head.setImageDrawable(h);

            viewHolderOriText.textimg.setOnClickListener(onViewClick(position, 3));
            viewHolderOriText.head.setOnClickListener(onViewClick(position, 3));
            viewHolderOriText.sharei.setOnClickListener(onViewClick(position, 3));
            viewHolderOriText.replybu.setOnClickListener(onViewClick(position, 3));
            viewHolderOriText.likebu.setOnClickListener(onViewClick(position, 3));
        }
        else if(type == 2) //未知类型
        {
            final UserDynamicApi.cardUnknow dy = (UserDynamicApi.cardUnknow) dyList.get(position);
            viewHolderUnktyp.name.setText(dy.getOwnerName());
            viewHolderUnktyp.time.setText(dy.getDynamicTime());
            viewHolderUnktyp.head.setImageResource(R.drawable.img_default_head);

            if(dy.getOwnerHead() != null)
            {
                viewHolderUnktyp.head.setTag(dy.getOwnerHead());
                BitmapDrawable h = setImageFormWeb(dy.getOwnerHead());
                if(h != null) viewHolderUnktyp.head.setImageDrawable(h);
            }

            viewHolderUnktyp.head.setOnClickListener(onViewClick(position, 2));
        }
        else if(type == 1) //转发视频
        {
            final UserDynamicApi.cardShareVideo dy = (UserDynamicApi.cardShareVideo) dyList.get(position);
            final UserDynamicApi.cardOriginalVideo sdy = dy.getOriginalVideo();
            viewHolderShaVid.name.setText(dy.getUserName());
            viewHolderShaVid.time.setText(dy.getDynamicTime());
            viewHolderShaVid.text.setText(dy.getDynamicText());
            viewHolderShaVid.sname.setText(sdy.getOwnerName());
            viewHolderShaVid.simgtext.setText(sdy.getVideoDuration() + "  " + sdy.getVideoView() + "观看");
            viewHolderShaVid.stitle.setText(sdy.getVideoTitle());
            viewHolderShaVid.reply.setText(String.valueOf(dy.getBeReply()));
            if(dy.isLike) viewHolderShaVid.likei.setImageResource(R.drawable.icon_liked);
            else viewHolderShaVid.likei.setImageResource(R.drawable.icon_like);
            viewHolderShaVid.like.setText(String.valueOf(dy.getBeLiked()));
            viewHolderShaVid.head.setImageResource(R.drawable.img_default_head);
            viewHolderShaVid.shead.setImageResource(R.drawable.img_default_head);
            viewHolderShaVid.simg.setImageResource(R.drawable.img_default_vid);

            viewHolderShaVid.head.setTag(dy.getUserHead());
            viewHolderShaVid.shead.setTag(sdy.getOwnerHead());
            viewHolderShaVid.simg.setTag(sdy.getVideoImg());
            BitmapDrawable h = setImageFormWeb(dy.getUserHead());
            BitmapDrawable o = setImageFormWeb(sdy.getOwnerHead());
            BitmapDrawable i = setImageFormWeb(sdy.getVideoImg());
            if(h != null) viewHolderShaVid.head.setImageDrawable(h);
            if(o != null) viewHolderShaVid.shead.setImageDrawable(o);
            if(i != null) viewHolderShaVid.simg.setImageDrawable(i);

            viewHolderShaVid.slay.setOnClickListener(onViewClick(position, 1));
            viewHolderShaVid.head.setOnClickListener(onViewClick(position, 1));
            convertView.findViewById(R.id.lisv_share_user).setOnClickListener(onViewClick(position, 1));
            viewHolderShaVid.sharei.setOnClickListener(onViewClick(position, 1));
            viewHolderShaVid.replybu.setOnClickListener(onViewClick(position, 1));
            viewHolderShaVid.likebu.setOnClickListener(onViewClick(position, 1));
        }
        else if(type == 0) //转发文字
        {
            final UserDynamicApi.cardShareText dy = (UserDynamicApi.cardShareText) dyList.get(position);
            final UserDynamicApi.cardOriginalText sdy = dy.getOriginalText();
            viewHolderShaText.name.setText(dy.getUserName());
            viewHolderShaText.time.setText(dy.getDynamicTime());
            viewHolderShaText.text.setText(dy.getDynamicText());
            viewHolderShaText.sname.setText(sdy.getUserName());
            viewHolderShaText.stext.setText(sdy.getDynamicText());
            if(!sdy.getTextImgCount().equals("0"))
            {
                viewHolderShaText.stextimg.setVisibility(View.VISIBLE);
                viewHolderShaText.stextimg.setText("查看共" + sdy.getTextImgCount() + "张图片");
            }
            else viewHolderShaText.stextimg.setVisibility(View.GONE);
            viewHolderShaText.reply.setText(String.valueOf(dy.getBeReply()));
            if(dy.isLike) viewHolderShaText.likei.setImageResource(R.drawable.icon_liked);
            else viewHolderShaText.likei.setImageResource(R.drawable.icon_like);
            viewHolderShaText.like.setText(String.valueOf(dy.getBeLiked()));
            viewHolderShaText.head.setImageResource(R.drawable.img_default_head);
            viewHolderShaText.shead.setImageResource(R.drawable.img_default_head);

            viewHolderShaText.head.setTag(dy.getUserHead());
            viewHolderShaText.shead.setTag(sdy.getUserHead());
            BitmapDrawable h = setImageFormWeb(dy.getUserHead());
            BitmapDrawable o = setImageFormWeb(sdy.getUserHead());
            if(h != null) viewHolderShaText.head.setImageDrawable(h);
            if(o != null) viewHolderShaText.shead.setImageDrawable(o);

            viewHolderShaText.stextimg.setOnClickListener(onViewClick(position, 0));
            viewHolderShaText.head.setOnClickListener(onViewClick(position, 0));
            convertView.findViewById(R.id.list_share_user).setOnClickListener(onViewClick(position, 0));
            viewHolderShaText.sharei.setOnClickListener(onViewClick(position, 0));
            viewHolderShaText.replybu.setOnClickListener(onViewClick(position, 0));
            viewHolderShaText.likebu.setOnClickListener(onViewClick(position, 0));
        }
        return convertView;
    }

    private BitmapDrawable setImageFormWeb(String url)
    {
        if(url != null && mImageCache.get(url) != null)
        {
            return mImageCache.get(url);
        }
        else
        {
            ImageTaskUtil it = new ImageTaskUtil(listView, mImageCache);
            it.execute(url);
            return null;
        }
    }

    private View.OnClickListener onViewClick(final int position, final int mode)
    {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dynamicAdapterListener.onClick(v.getId(), position, mode);
            }
        };
    }

    class ViewHolderOriVid
    {
        RelativeLayout lay;
        ImageView head;
        TextView name;
        TextView time;
        ExpandableTextView text;
        ImageView img;
        TextView imgtext;
        TextView title;
        LinearLayout likebu;
        ImageView likei;
        TextView like;
    }

    class ViewHolderOriText
    {
        ImageView head;
        TextView name;
        TextView time;
        ExpandableTextView text;
        TextView textimg;
        ImageView sharei;
        LinearLayout replybu;
        TextView reply;
        LinearLayout likebu;
        ImageView likei;
        TextView like;
    }

    class ViewHolderUnktyp
    {
        ImageView head;
        TextView name;
        TextView time;
    }

    class ViewHolderShaVid
    {
        ImageView head;
        TextView name;
        TextView time;
        ExpandableTextView text;
        RelativeLayout slay;
        ImageView shead;
        TextView sname;
        ImageView simg;
        TextView simgtext;
        TextView stitle;
        ImageView sharei;
        LinearLayout replybu;
        TextView reply;
        LinearLayout likebu;
        ImageView likei;
        TextView like;
    }

    class ViewHolderShaText
    {
        ImageView head;
        TextView name;
        TextView time;
        ExpandableTextView text;
        ImageView shead;
        TextView sname;
        ExpandableTextView stext;
        TextView stextimg;
        ImageView sharei;
        LinearLayout replybu;
        TextView reply;
        LinearLayout likebu;
        ImageView likei;
        TextView like;
    }

    public interface DynamicAdapterListener
    {
        void onClick(int viewId, int position, int mode);
    }
}
