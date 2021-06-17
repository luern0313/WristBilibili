package cn.luern0313.wristbilibili.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.core.content.res.ResourcesCompat;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.models.ReplyModel;
import cn.luern0313.wristbilibili.util.ColorUtil;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.ReplyHtmlImageHandlerUtil;
import cn.luern0313.wristbilibili.widget.ExpandableTextView;

/**
 * 被 luern0313 创建于 2020/1/31.
 */

public class TailReplyAdapter extends ReplyAdapter
{
    private static boolean r;

    public TailReplyAdapter(LayoutInflater inflater, ListView listView, ArrayList<ReplyModel> replyList, boolean isShowFloor, boolean isHasRoot, int replyCount, int replyWidth, ReplyAdapterListener replyAdapterListener)
    {
        super(inflater, listView, replyList, isShowFloor, isHasRoot, replyCount, replyWidth, replyAdapterListener);
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

    @SuppressLint("ClickableViewAccessibility")
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
                    convertView = mInflater.inflate(R.layout.item_reply_tail_reply, null);
                    viewHolder = new ViewHolder();
                    convertView.setTag(viewHolder);
                    viewHolder.reply = convertView.findViewById(R.id.item_reply);
                    viewHolder.reply_img = convertView.findViewById(R.id.item_reply_head);
                    viewHolder.reply_off_1 = convertView.findViewById(R.id.item_reply_off_1);
                    viewHolder.reply_off_2 = convertView.findViewById(R.id.item_reply_off_2);
                    viewHolder.reply_name = convertView.findViewById(R.id.item_reply_name);
                    viewHolder.reply_time = convertView.findViewById(R.id.item_reply_time);
                    viewHolder.reply_floor = convertView.findViewById(R.id.item_reply_floor);
                    viewHolder.reply_level = convertView.findViewById(R.id.item_reply_level);
                    viewHolder.reply_text = convertView.findViewById(R.id.item_reply_text);

                    viewHolder.reply_apply = convertView.findViewById(R.id.item_reply_tail_apply);
                    viewHolder.reply_dislike = convertView.findViewById(R.id.item_reply_dislike);
                    viewHolder.reply_like = convertView.findViewById(R.id.item_reply_like);
                    viewHolder.reply_like_img = convertView.findViewById(R.id.item_reply_like_i);
                    viewHolder.reply_like_num = convertView.findViewById(R.id.item_reply_like_n);
                    break;

                case 1:
                    convertView = mInflater.inflate(R.layout.widget_reply_changemode, null);
                    ((TextView) convertView.findViewById(R.id.item_reply_sort_sign)).setText(ctx.getString(R.string.tail_reply_sort_sign_hot));
                    Drawable changeNewDrawable = ResourcesCompat.getDrawable(convertView.getResources(), R.drawable.icon_reply_sort, null);
                    changeNewDrawable.setBounds(0, 0, DataProcessUtil.dip2px(12), DataProcessUtil.dip2px(12));
                    ((TextView) convertView.findViewById(R.id.item_reply_sort_change)).setCompoundDrawables(changeNewDrawable,null, null,null);
                    if(isHasRoot)
                        convertView.findViewById(R.id.item_reply_sort_change).setVisibility(View.GONE);
                    else
                        convertView.findViewById(R.id.item_reply_sort_change).setVisibility(View.VISIBLE);
                    break;

                case 2:
                    convertView = mInflater.inflate(R.layout.widget_reply_changemode, null);
                    ((TextView) convertView.findViewById(R.id.item_reply_sort_sign)).setText(ctx.getString(R.string.tail_reply_sort_sign_time));
                    Drawable changeHotDrawable = ResourcesCompat.getDrawable(convertView.getResources(), R.drawable.icon_reply_sort, null);
                    changeHotDrawable.setBounds(0, 0,DataProcessUtil.dip2px(12), DataProcessUtil.dip2px(12));
                    ((TextView) convertView.findViewById(R.id.item_reply_sort_change)).setCompoundDrawables(changeHotDrawable,null, null,null);
                    if(isHasRoot)
                        convertView.findViewById(R.id.item_reply_sort_change).setVisibility(View.GONE);
                    else
                        convertView.findViewById(R.id.item_reply_sort_change).setVisibility(View.VISIBLE);
                    break;

                case 3:
                    convertView = mInflater.inflate(R.layout.widget_reply_tail_sendreply, null);
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

            viewHolder.reply_text.setOnTouchListener(viewHolder.reply_text.new LinkTouchMovementMethod());
            viewHolder.reply_text.setFocusable(false);
            viewHolder.reply_text.setClickable(false);
            viewHolder.reply_text.setLongClickable(false);

            viewHolder.reply_like_num.setText(DataProcessUtil.getView(replyModel.getLikeNum()));

            if(replyModel.isUserLike()) viewHolder.reply_like_img.setImageResource(R.drawable.icon_like_yes);
            else viewHolder.reply_like_img.setImageResource(R.drawable.icon_like_no);
            if(replyModel.isUserDislike()) viewHolder.reply_dislike.setImageResource(R.drawable.icon_dislike_yes);
            else viewHolder.reply_dislike.setImageResource(R.drawable.icon_dislike_no);
            if(replyModel.getOwnerVip() == 2)
            {
                viewHolder.reply_name.setTextColor(ColorUtil.getColor(R.attr.colorVip, listView.getContext()));
                viewHolder.reply_name.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            }
            else

            {
                viewHolder.reply_name.setTextColor(ColorUtil.getColor(R.attr.colorTitle, listView.getContext()));
                viewHolder.reply_name.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            }

            viewHolder.reply_apply.setOnClickListener(v -> {
                if((int) v.getTag() == 1)
                {
                    replyAdapterListener.onClick(v.getId(), position, 0);
                    ((TextView) v).setText(ctx.getString(R.string.tail_reply_apply_success));
                }
            });

            viewHolder.reply_apply.setOnFocusChangeListener((v, hasFocus) -> {
                if(!r)
                {
                    r = true;
                    if(hasFocus)
                    {
                        ValueAnimator valueAnimator = new ValueAnimator();
                        valueAnimator.setValues(
                                PropertyValuesHolder.ofInt("width", DataProcessUtil.dip2px(16) + DataProcessUtil.sp2px(12) * 6, DataProcessUtil.dip2px(16) + DataProcessUtil.sp2px(12) * 4),
                                PropertyValuesHolder.ofInt("alpha", 255, 0));
                        valueAnimator.setDuration(250);
                        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                        valueAnimator.addUpdateListener(animation -> {
                            ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
                            layoutParams.width = (int) animation.getAnimatedValue("width");
                            v.setLayoutParams(layoutParams);
                            ((TextView) v).setTextColor(Color.argb(((Integer) animation.getAnimatedValue("alpha")), 255, 255, 255));
                        });
                        valueAnimator.addListener(new AnimatorListenerAdapter()
                        {
                            @Override
                            public void onAnimationEnd(Animator animation)
                            {
                                ((TextView) v).setText(ctx.getString(R.string.tail_reply_apply_confirm));
                                ((TextView) v).setTextColor(Color.WHITE);
                                v.setTag(1);
                                new Handler().postDelayed(() -> r = false, 1);
                            }
                        });
                        valueAnimator.start();
                    }
                    else
                    {
                        ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
                        layoutParams.width = DataProcessUtil.dip2px(16) + DataProcessUtil.sp2px(12) * 6;
                        v.setLayoutParams(layoutParams);
                        ((TextView) v).setText(ctx.getString(R.string.tail_reply_apply));
                        v.setTag(0);
                        r = false;
                    }
                }
            });

            viewHolder.reply.setOnTouchListener((v, event) -> {
                v.findViewById(R.id.item_reply_tail_apply).clearFocus();
                return false;
            });

            viewHolder.reply_img.setTag(replyModel.getOwnerFace());
            BitmapDrawable h = setImageFormWeb(replyModel.getOwnerFace());
            if(h != null) viewHolder.reply_img.setImageDrawable(h);

            viewHolder.reply_img.setOnClickListener(onViewClick(position));
            viewHolder.reply_like.setOnClickListener(onViewClick(position));
            viewHolder.reply_dislike.setOnClickListener(onViewClick(position));
        }
        else if(type == 1 || type == 2)
        {
            convertView.findViewById(R.id.item_reply_sort_change).setOnClickListener(v -> replyAdapterListener.onSortModeChange());
        }
        else if(type == 3)
        {
            ((TextView) convertView.findViewById(R.id.reply_toolbar_total)).setText(String.format(ctx.getString(R.string.tail_reply_toolbar_total_comment), DataProcessUtil.getView(replyCount)));
            convertView.findViewById(R.id.reply_tail_toolbar_sendreply).setOnClickListener(v -> replyAdapterListener.onClick(v.getId(), -1, 1));
        }
        return convertView;
    }

    private View.OnClickListener onViewClick(final int position)
    {
        return v -> replyAdapterListener.onClick(v.getId(), position, 0);
    }

    private static class ViewHolder
    {
        RelativeLayout reply;
        ImageView reply_img;
        ImageView reply_off_1;
        ImageView reply_off_2;
        TextView reply_name;
        TextView reply_time;
        TextView reply_floor;
        TextView reply_level;
        ExpandableTextView reply_text;

        TextView reply_apply;
        ImageView reply_dislike;
        LinearLayout reply_like;
        ImageView reply_like_img;
        TextView reply_like_num;
    }
}
