package cn.luern0313.wristbilibili.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.models.ReplyModel;
import cn.luern0313.wristbilibili.util.ColorUtil;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.ImageTaskUtil;
import cn.luern0313.wristbilibili.util.LruCacheUtil;
import cn.luern0313.wristbilibili.util.MyApplication;
import cn.luern0313.wristbilibili.util.ReplyHtmlImageHandlerUtil;
import cn.luern0313.wristbilibili.widget.ExpandableTextView;

/**
 * 被 luern0313 创建于 2020/1/31.
 */

public class ReplyAdapter extends BaseAdapter
{
    private final Context ctx;
    private final LayoutInflater mInflater;

    private final ReplyAdapterListener replyAdapterListener;

    private final ArrayList<ReplyModel> replyList;
    private final ListView listView;
    private final int replyWidth;

    private final boolean isShowFloor;
    private final boolean isHasRoot;
    private final int replyCount;

    public ReplyAdapter(LayoutInflater inflater, ListView listView, ArrayList<ReplyModel> replyList, boolean isShowFloor, boolean isHasRoot, int replyCount, int replyWidth, ReplyAdapterListener replyAdapterListener)
    {
        this.ctx = MyApplication.getContext();
        this.mInflater = inflater;
        this.replyList = replyList;
        this.listView = listView;
        this.isShowFloor = isShowFloor;
        this.isHasRoot = isHasRoot;
        this.replyCount = replyCount;
        this.replyWidth = replyWidth;
        this.replyAdapterListener = replyAdapterListener;
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
        return 4;
    }

    @Override
    public int getItemViewType(int position)
    {
        return replyList.get(position).getMode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup)
    {
        final ReplyModel replyModel = replyList.get(position);
        int type = getItemViewType(position);
        ViewHolder viewHolder = null;
        if(convertView == null)
        {
            switch(type)
            {
                case 0:
                    convertView = mInflater.inflate(R.layout.item_reply_reply, null);
                    viewHolder = new ViewHolder();
                    convertView.setTag(viewHolder);
                    viewHolder.reply_img = convertView.findViewById(R.id.item_reply_head);
                    viewHolder.reply_off_1 = convertView.findViewById(R.id.item_reply_off_1);
                    viewHolder.reply_off_2 = convertView.findViewById(R.id.item_reply_off_2);
                    viewHolder.reply_name = convertView.findViewById(R.id.item_reply_name);
                    viewHolder.reply_is_up = convertView.findViewById(R.id.item_reply_up);
                    viewHolder.reply_time = convertView.findViewById(R.id.item_reply_time);
                    viewHolder.reply_floor = convertView.findViewById(R.id.item_reply_floor);
                    viewHolder.reply_level = convertView.findViewById(R.id.item_reply_level);
                    viewHolder.reply_text = convertView.findViewById(R.id.item_reply_text);
                    viewHolder.reply_reply_show = convertView.findViewById(R.id.item_reply_reply_show);
                    viewHolder.reply_reply_show_1 = convertView.findViewById(R.id.item_reply_reply_show_1);
                    viewHolder.reply_reply_show_2 = convertView.findViewById(R.id.item_reply_reply_show_2);
                    viewHolder.reply_reply_show_3 = convertView.findViewById(R.id.item_reply_reply_show_3);
                    viewHolder.reply_reply_show_show = convertView.findViewById(R.id.item_reply_reply_show_show);
                    viewHolder.reply_is_up_like = convertView.findViewById(R.id.item_reply_up_like);

                    viewHolder.reply_report = convertView.findViewById(R.id.item_reply_report);
                    viewHolder.reply_reply = convertView.findViewById(R.id.item_reply_reply);
                    viewHolder.reply_dislike = convertView.findViewById(R.id.item_reply_dislike);
                    viewHolder.reply_like = convertView.findViewById(R.id.item_reply_like);
                    viewHolder.reply_like_img = convertView.findViewById(R.id.item_reply_like_i);
                    viewHolder.reply_like_num = convertView.findViewById(R.id.item_reply_like_n);
                    break;

                case 1:
                    convertView = mInflater.inflate(R.layout.widget_reply_changemode, null);
                    ((TextView) convertView.findViewById(R.id.item_reply_sort_sign)).setText(ctx.getString(R.string.reply_sore_sign_hot));
                    Drawable changeNewDrawable = convertView.getResources().getDrawable(R.drawable.icon_reply_sort);
                    changeNewDrawable.setBounds(0, 0, DataProcessUtil.dip2px(12), DataProcessUtil.dip2px(12));
                    ((TextView) convertView.findViewById(R.id.item_reply_sort_change)).setCompoundDrawables(changeNewDrawable,null, null,null);
                    if(isHasRoot)
                        convertView.findViewById(R.id.item_reply_sort_change).setVisibility(View.GONE);
                    else
                        convertView.findViewById(R.id.item_reply_sort_change).setVisibility(View.VISIBLE);
                    break;

                case 2:
                    convertView = mInflater.inflate(R.layout.widget_reply_changemode, null);
                    ((TextView) convertView.findViewById(R.id.item_reply_sort_sign)).setText(ctx.getString(R.string.reply_sore_sign_time));
                    Drawable changeHotDrawable = convertView.getResources().getDrawable(R.drawable.icon_reply_sort);
                    changeHotDrawable.setBounds(0, 0,DataProcessUtil.dip2px(12), DataProcessUtil.dip2px(12));
                    ((TextView) convertView.findViewById(R.id.item_reply_sort_change))
                            .setCompoundDrawables(changeHotDrawable,null, null,null);
                    if(isHasRoot)
                        convertView.findViewById(R.id.item_reply_sort_change).setVisibility(View.GONE);
                    else
                        convertView.findViewById(R.id.item_reply_sort_change).setVisibility(View.VISIBLE);
                    break;

                case 3:
                    convertView = mInflater.inflate(R.layout.widget_reply_sendreply, null);
            }
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if(type == 0)
        {
            viewHolder.reply_img.setImageResource(R.drawable.img_default_avatar);
            if(replyModel.getOwnerOfficial() == 0)
            {
                viewHolder.reply_off_1.setVisibility(View.VISIBLE);
                viewHolder.reply_off_2.setVisibility(View.GONE);
            }
            else if(replyModel.getOwnerOfficial() == 1)
            {
                viewHolder.reply_off_1.setVisibility(View.GONE);
                viewHolder.reply_off_2.setVisibility(View.VISIBLE);
            }
            else
            {
                viewHolder.reply_off_1.setVisibility(View.GONE);
                viewHolder.reply_off_2.setVisibility(View.GONE);
            }

            viewHolder.reply_name.setText(replyModel.getOwnerName());
            viewHolder.reply_time.setText(replyModel.getTime());

            viewHolder.reply_is_up.setVisibility(replyModel.isUp() ? View.VISIBLE : View.GONE);
            viewHolder.reply_floor.setVisibility(isShowFloor ? View.VISIBLE : View.GONE);
            if(isShowFloor)
                viewHolder.reply_floor.setText(replyModel.getFloor());
            viewHolder.reply_level.setText(String.format(ctx.getString(R.string.reply_lv), replyModel.getOwnerLv()));

            viewHolder.reply_text.setExpandListener(new ExpandableTextView.OnExpandListener()
            {
                @Override
                public void onExpand(ExpandableTextView view)
                {
                    replyModel.setTextExpend(true);
                }

                @Override
                public void onShrink(ExpandableTextView view)
                {
                    replyModel.setTextExpend(false);
                }
            });
            CharSequence text = DataProcessUtil.getClickableHtml(replyModel.getText(), new ReplyHtmlImageHandlerUtil(
                    viewHolder.reply_text, replyModel.getEmoteSize()));
            viewHolder.reply_text.setOrigText(text);
            viewHolder.reply_text.updateForRecyclerView(text, replyWidth, replyModel.isTextExpend() ?
                    ExpandableTextView.STATE_EXPAND : ExpandableTextView.STATE_SHRINK);

            viewHolder.reply_text.setMovementMethod(viewHolder.reply_text.new LinkTouchMovementMethod());
            viewHolder.reply_text.setFocusable(false);
            viewHolder.reply_text.setClickable(false);
            viewHolder.reply_text.setLongClickable(false);

            if(replyModel.getReplyShow().size() > 0 && !isHasRoot)
            {
                viewHolder.reply_reply_show.setVisibility(View.VISIBLE);
                viewHolder.reply_reply_show_1.setVisibility(View.GONE);
                viewHolder.reply_reply_show_2.setVisibility(View.GONE);
                viewHolder.reply_reply_show_3.setVisibility(View.GONE);
                viewHolder.reply_reply_show.setOnClickListener(onViewClick(position));
                switch(replyModel.getReplyShow().size())
                {
                    case 3:
                        viewHolder.reply_reply_show_3.setVisibility(View.VISIBLE);
                        viewHolder.reply_reply_show_3.setText(
                                DataProcessUtil.getClickableHtml(replyModel.getReplyShow().get(2),
                                              new ReplyHtmlImageHandlerUtil(viewHolder.reply_reply_show_3, replyModel.getEmoteSize())));
                    case 2:
                        viewHolder.reply_reply_show_2.setVisibility(View.VISIBLE);
                        viewHolder.reply_reply_show_2.setText(
                                DataProcessUtil.getClickableHtml(replyModel.getReplyShow().get(1),
                                              new ReplyHtmlImageHandlerUtil(viewHolder.reply_reply_show_2, replyModel.getEmoteSize())));
                    case 1:
                        viewHolder.reply_reply_show_1.setVisibility(View.VISIBLE);
                        viewHolder.reply_reply_show_1.setText(
                                DataProcessUtil.getClickableHtml(replyModel.getReplyShow().get(0),
                                              new ReplyHtmlImageHandlerUtil(viewHolder.reply_reply_show_1, replyModel.getEmoteSize())));
                }
                if(replyModel.getReplyNum() > Math.min(replyModel.getReplyShow().size(), 3))
                {
                    if(!replyModel.isUpReply()) viewHolder.reply_reply_show_show.setText(
                            Html.fromHtml("<font color=\"#188ad0\">共" + DataProcessUtil.getView(replyModel.getReplyNum()) + "条回复 ></font>"));
                    else viewHolder.reply_reply_show_show.setText(Html.fromHtml(
                            "UP主等人<font color=\"#188ad0\">共" + DataProcessUtil.getView(replyModel.getReplyNum()) + "条回复 ></font>"));
                    viewHolder.reply_reply_show_show.setVisibility(View.VISIBLE);
                }
                else
                    viewHolder.reply_reply_show_show.setVisibility(View.GONE);

                viewHolder.reply_reply_show_1.setMovementMethod(viewHolder.reply_text.new LinkTouchMovementMethod());
                viewHolder.reply_reply_show_2.setMovementMethod(viewHolder.reply_text.new LinkTouchMovementMethod());
                viewHolder.reply_reply_show_3.setMovementMethod(viewHolder.reply_text.new LinkTouchMovementMethod());
                viewHolder.reply_reply_show_1.setFocusable(false);
                viewHolder.reply_reply_show_1.setClickable(false);
                viewHolder.reply_reply_show_1.setLongClickable(false);
                viewHolder.reply_reply_show_2.setFocusable(false);
                viewHolder.reply_reply_show_2.setClickable(false);
                viewHolder.reply_reply_show_2.setLongClickable(false);
                viewHolder.reply_reply_show_3.setFocusable(false);
                viewHolder.reply_reply_show_3.setClickable(false);
                viewHolder.reply_reply_show_3.setLongClickable(false);
            }
            else
                viewHolder.reply_reply_show.setVisibility(View.GONE);

            viewHolder.reply_is_up_like.setVisibility(replyModel.isUpLike() ? View.VISIBLE : View.GONE);
            viewHolder.reply_like_num.setText(DataProcessUtil.getView(replyModel.getLikeNum()));

            if(replyModel.isUpLike()) viewHolder.reply_like_img.setImageResource(R.drawable.icon_liked);
            else viewHolder.reply_like_img.setImageResource(R.drawable.icon_like);
            if(replyModel.isUserDislike()) viewHolder.reply_dislike.setImageResource(R.drawable.icon_disliked);
            else viewHolder.reply_dislike.setImageResource(R.drawable.icon_dislike);
            if(replyModel.getOwnerVip() == 2)
            {
                viewHolder.reply_name.setTextColor(ColorUtil.getColor(R.attr.colorVip, listView.getContext()));
                viewHolder.reply_name.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            }
            else
            {
                viewHolder.reply_name.setTextColor(ColorUtil.getColor(android.R.attr.textColor, listView.getContext()));
                viewHolder.reply_name.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            }

            viewHolder.reply_img.setTag(replyModel.getOwnerFace());
            BitmapDrawable h = setImageFormWeb(replyModel.getOwnerFace());
            if(h != null) viewHolder.reply_img.setImageDrawable(h);

            viewHolder.reply_img.setOnClickListener(onViewClick(position));
            viewHolder.reply_report.setOnClickListener(onViewClick(position));
            viewHolder.reply_like.setOnClickListener(onViewClick(position));
            viewHolder.reply_dislike.setOnClickListener(onViewClick(position));
            viewHolder.reply_reply.setOnClickListener(onViewClick(position));
        }
        else if(type == 1 || type == 2)
        {
            convertView.findViewById(R.id.item_reply_sort_change).setOnClickListener(v -> replyAdapterListener.onSortModeChange());
        }
        else if(type == 3)
        {
            if(isHasRoot)
                ((TextView) convertView.findViewById(R.id.reply_toolbar_sendreply)).setText(ctx.getString(R.string.reply_toolbar_send));
            ((TextView) convertView.findViewById(R.id.reply_toolbar_total)).setText(String.format(ctx.getString(isHasRoot ? R.string.reply_toolbar_total_reply : R.string.reply_toolbar_total_comment), DataProcessUtil.getView(replyCount)));
            convertView.findViewById(R.id.reply_toolbar_sendreply).setOnClickListener(v -> replyAdapterListener.onClick(v.getId(), -1, 1));
        }
        return convertView;
    }

    private View.OnClickListener onViewClick(final int position)
    {
        return v -> replyAdapterListener.onClick(v.getId(), position, 0);
    }

    private static class ViewHolder
    {
        ImageView reply_img;
        ImageView reply_off_1;
        ImageView reply_off_2;
        TextView reply_name;
        TextView reply_is_up;
        TextView reply_time;
        TextView reply_floor;
        TextView reply_level;
        ExpandableTextView reply_text;
        LinearLayout reply_reply_show;
        TextView reply_reply_show_1;
        TextView reply_reply_show_2;
        TextView reply_reply_show_3;
        TextView reply_reply_show_show;
        TextView reply_is_up_like;

        ImageView reply_report;
        ImageView reply_reply;
        ImageView reply_dislike;
        LinearLayout reply_like;
        ImageView reply_like_img;
        TextView reply_like_num;
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

    public interface ReplyAdapterListener
    {
        void onClick(int viewId, int position, int mode);
        void onSortModeChange();
    }
}
