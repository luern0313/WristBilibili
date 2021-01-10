package cn.luern0313.wristbilibili.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.models.DynamicModel;
import cn.luern0313.wristbilibili.ui.ImgActivity;
import cn.luern0313.wristbilibili.util.ColorUtil;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.DynamicAlbumDecoration;
import cn.luern0313.wristbilibili.util.ImageTaskUtil;
import cn.luern0313.wristbilibili.util.LruCacheUtil;
import cn.luern0313.wristbilibili.util.MyApplication;
import cn.luern0313.wristbilibili.util.ReplyHtmlImageHandlerUtil;
import cn.luern0313.wristbilibili.widget.ExpandableTextView;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 被 luern0313 创建于 2019/8/31.
 */

public class DynamicAdapter extends BaseAdapter
{
    private final Context ctx;
    private final LayoutInflater mInflater;

    private final DynamicAdapterListener dynamicAdapterListener;

    private final ArrayList<DynamicModel.DynamicBaseModel> dynamicModelArrayList;
    private final View view;
    private final int dynamicWidth;

    public DynamicAdapter(LayoutInflater inflater, ArrayList<DynamicModel.DynamicBaseModel> dynamicModelArrayList, View view, int dynamicWidth, DynamicAdapterListener dynamicAdapterListener)
    {
        ctx = MyApplication.getContext();
        mInflater = inflater;
        this.dynamicModelArrayList = dynamicModelArrayList;
        this.view = view;
        this.dynamicWidth = dynamicWidth;
        this.dynamicAdapterListener = dynamicAdapterListener;
    }

    @Override
    public int getCount()
    {
        return dynamicModelArrayList.size();
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
        return 32;
    }

    @Override
    public int getItemViewType(int position)
    {
        int type = getType(dynamicModelArrayList.get(position).getCardType());
        if(type == 0)
            return getType(((DynamicModel.DynamicShareModel) dynamicModelArrayList.get(position)).getShareOriginType());
        else
            return 16 + type;
    }

    private int getType(int type)
    {
        if(type < 4200)
        {
            int t = (int) (Math.log(type) / Math.log(2));
            if(t == 12) return 9;
            else return t;
        }
        else if(type < 4300)
            return 13;
        else if(type < 4400)
            return 14;
        return 15;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup)
    {
        DynamicModel.DynamicBaseModel dynamicModel = dynamicModelArrayList.get(position);
        ViewHolder viewHolder;
        if(convertView == null || ((ViewHolder) convertView.getTag()).type != dynamicModel.getCardType())
        {
            convertView = mInflater.inflate(R.layout.item_dynamic, null);
            viewHolder = getViewHolder(dynamicModel, convertView, R.id.item_dynamic_dynamic);
        }
        else
            viewHolder = (ViewHolder) convertView.getTag();

        handlerView(viewHolder, dynamicModel, position, false, false);

        return convertView;
    }

    public ViewHolder getViewHolder(DynamicModel.DynamicBaseModel dynamicModel, View convertView, int viewId)
    {
        ViewHolder viewHolder;
        ViewHolder vh = new ViewHolder();
        if(dynamicModel instanceof DynamicModel.DynamicUnknownModel)
        {
            View view = mInflater.inflate(R.layout.widget_dynamic_unknown, null);
            ((LinearLayout) convertView.findViewById(viewId)).removeAllViewsInLayout();
            ((LinearLayout) convertView.findViewById(viewId)).addView(view);
            viewHolder = new ViewHolder.ViewHolderUnknown(convertView, dynamicModel.getCardType());
            convertView.setTag(viewHolder);
            return viewHolder;
        }
        switch (dynamicModel.getCardType())
        {
            case 1:
            {
                View view = mInflater.inflate(R.layout.widget_dynamic_share, null);
                ((LinearLayout) convertView.findViewById(viewId)).removeAllViewsInLayout();
                ((LinearLayout) convertView.findViewById(viewId)).addView(view);
                viewHolder = new ViewHolder.ViewHolderShare(convertView, dynamicModel.getCardType());
                convertView.setTag(viewHolder);
                break;
            }
            case 2:
            {
                View view = mInflater.inflate(R.layout.widget_dynamic_album, null);
                ((LinearLayout) convertView.findViewById(viewId)).removeAllViewsInLayout();
                ((LinearLayout) convertView.findViewById(viewId)).addView(view);
                viewHolder = new ViewHolder.ViewHolderAlbum(convertView, dynamicModel.getCardType());
                convertView.setTag(viewHolder);
                break;
            }
            case 4:
            {
                View view = mInflater.inflate(R.layout.widget_dynamic_text, null);
                ((LinearLayout) convertView.findViewById(viewId)).removeAllViewsInLayout();
                ((LinearLayout) convertView.findViewById(viewId)).addView(view);
                viewHolder = new ViewHolder.ViewHolderText(convertView, dynamicModel.getCardType());
                convertView.setTag(viewHolder);
                break;
            }
            case 8:
            {
                View view = mInflater.inflate(R.layout.widget_dynamic_video, null);
                ((LinearLayout) convertView.findViewById(viewId)).removeAllViewsInLayout();
                ((LinearLayout) convertView.findViewById(viewId)).addView(view);
                viewHolder = new ViewHolder.ViewHolderVideo(convertView, dynamicModel.getCardType());
                convertView.setTag(viewHolder);
                break;
            }
            case 64:
            {
                View view = mInflater.inflate(R.layout.widget_dynamic_article, null);
                ((LinearLayout) convertView.findViewById(viewId)).removeAllViewsInLayout();
                ((LinearLayout) convertView.findViewById(viewId)).addView(view);
                viewHolder = new ViewHolder.ViewHolderArticle(convertView, dynamicModel.getCardType());
                convertView.setTag(viewHolder);
                break;
            }
            case 512:
            case 4098:
            case 4099:
            case 4101:
            {
                View view = mInflater.inflate(R.layout.widget_dynamic_bangumi, null);
                ((LinearLayout) convertView.findViewById(viewId)).removeAllViewsInLayout();
                ((LinearLayout) convertView.findViewById(viewId)).addView(view);
                viewHolder = new ViewHolder.ViewHolderBangumi(convertView, dynamicModel.getCardType());
                convertView.setTag(viewHolder);
                break;
            }
            case 2048:
            {
                View view = mInflater.inflate(R.layout.widget_dynamic_url, null);
                ((LinearLayout) convertView.findViewById(viewId)).removeAllViewsInLayout();
                ((LinearLayout) convertView.findViewById(viewId)).addView(view);
                viewHolder = new ViewHolder.ViewHolderUrl(convertView, dynamicModel.getCardType());
                convertView.setTag(viewHolder);
                break;
            }
            case 4200:
            {
                View view = mInflater.inflate(R.layout.widget_dynamic_live, null);
                ((LinearLayout) convertView.findViewById(viewId)).removeAllViewsInLayout();
                ((LinearLayout) convertView.findViewById(viewId)).addView(view);
                viewHolder = new ViewHolder.ViewHolderLive(convertView, dynamicModel.getCardType());
                convertView.setTag(viewHolder);
                break;
            }
            case 4300:
            {
                View view = mInflater.inflate(R.layout.widget_dynamic_favor, null);
                ((LinearLayout) convertView.findViewById(viewId)).removeAllViewsInLayout();
                ((LinearLayout) convertView.findViewById(viewId)).addView(view);
                viewHolder = new ViewHolder.ViewHolderFavor(convertView, dynamicModel.getCardType());
                convertView.setTag(viewHolder);
                break;
            }
            default:
            {
                View view = mInflater.inflate(R.layout.widget_dynamic_unknown, null);
                ((LinearLayout) convertView.findViewById(viewId)).removeAllViewsInLayout();
                ((LinearLayout) convertView.findViewById(viewId)).addView(view);
                viewHolder = new ViewHolder.ViewHolderUnknown(convertView, dynamicModel.getCardType());
                convertView.setTag(viewHolder);
                break;
            }
        }
        return viewHolder;
    }

    public void handlerView(ViewHolder vh, DynamicModel.DynamicBaseModel dm, int position, boolean isShared, boolean isExpand)
    {
        if(dm.getCardType() == 1 && !(dm instanceof DynamicModel.DynamicUnknownModel))
        {
            final ViewHolder.ViewHolderShare viewHolder = (ViewHolder.ViewHolderShare) vh;
            final DynamicModel.DynamicShareModel dynamicModel = (DynamicModel.DynamicShareModel) dm;

            if(isExpand)
                viewHolder.share_dynamic.setMaxLinesOnShrink(Integer.MAX_VALUE);
            viewHolder.share_dynamic.setExpandListener(new ExpandableTextView.OnExpandListener()
            {
                @Override
                public void onExpand(ExpandableTextView view)
                {
                    dynamicModel.setShareTextExpand(true);
                }

                @Override
                public void onShrink(ExpandableTextView view)
                {
                    dynamicModel.setShareTextExpand(false);
                }
            });
            CharSequence text = DataProcessUtil.getClickableHtml(dynamicModel.getShareText(), new ReplyHtmlImageHandlerUtil(
                    viewHolder.share_dynamic, dynamicModel.getCardEmoteSize()));
            viewHolder.share_dynamic.setOrigText(text);
            viewHolder.share_dynamic.updateForRecyclerView(text, dynamicWidth, dynamicModel.isShareTextExpand() ?
                    ExpandableTextView.STATE_EXPAND : ExpandableTextView.STATE_SHRINK);

            viewHolder.share_dynamic.setMovementMethod(viewHolder.share_dynamic.new LinkTouchMovementMethod());
            viewHolder.share_dynamic.setFocusable(false);
            viewHolder.share_dynamic.setClickable(false);
            viewHolder.share_dynamic.setLongClickable(false);
            ViewHolder shareViewHolder = getViewHolder(dynamicModel.getShareOriginCard(), vh.dynamic_lay, R.id.dynamic_share_share);
            handlerView(shareViewHolder, dynamicModel.getShareOriginCard(), position, true, isExpand);

            viewHolder.share_share.setOnClickListener(onViewClick(position, isShared));
        }
        else if(dm.getCardType() == 2 && !(dm instanceof DynamicModel.DynamicUnknownModel))
        {
            final ViewHolder.ViewHolderAlbum viewHolder = (ViewHolder.ViewHolderAlbum) vh;
            final DynamicModel.DynamicAlbumModel dynamicModel = (DynamicModel.DynamicAlbumModel) dm;

            if(dynamicModel.isCardIsShared())
            {
                viewHolder.album_author.setText(String.format(ctx.getString(R.string.dynamic_album_author), dynamicModel.getAlbumAuthorName()));
                viewHolder.album_author.setVisibility(View.VISIBLE);
            }
            else
                viewHolder.album_author.setVisibility(View.GONE);

            if(isExpand)
                viewHolder.album_text.setMaxLinesOnShrink(Integer.MAX_VALUE);
            viewHolder.album_text.setExpandListener(new ExpandableTextView.OnExpandListener()
            {
                @Override
                public void onExpand(ExpandableTextView view)
                {
                    dynamicModel.setAlbumTextExpand(true);
                }

                @Override
                public void onShrink(ExpandableTextView view)
                {
                    dynamicModel.setAlbumTextExpand(false);
                }
            });
            CharSequence text = DataProcessUtil.getClickableHtml(dynamicModel.getAlbumText(), new ReplyHtmlImageHandlerUtil(
                    viewHolder.album_text, dynamicModel.getCardEmoteSize()));
            viewHolder.album_text.setOrigText(text);
            viewHolder.album_text.updateForRecyclerView(text, dynamicWidth, dynamicModel.isAlbumTextExpand() ?
                    ExpandableTextView.STATE_EXPAND : ExpandableTextView.STATE_SHRINK);

            viewHolder.album_text.setMovementMethod(viewHolder.album_text.new LinkTouchMovementMethod());
            viewHolder.album_text.setFocusable(false);
            viewHolder.album_text.setClickable(false);
            viewHolder.album_text.setLongClickable(false);

            DynamicAlbumImgAdapter.DynamicAlbumImgAdapterListener dynamicAlbumImgAdapterListener = position1 -> {
                Intent intent = new Intent(ctx, ImgActivity.class);
                intent.putExtra("imgUrl", dynamicModel.getAlbumImg().toArray(new String[]{}));
                intent.putExtra("position", position1);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(intent);
            };
            LinearLayoutManager layoutManager = new LinearLayoutManager(ctx);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            viewHolder.album_img.setLayoutManager(layoutManager);
            if(viewHolder.album_img.getItemDecorationCount() == 0)
                viewHolder.album_img.addItemDecoration(DynamicAlbumDecoration.createHorizontal(ctx, Color.argb(0, 0, 0, 0), DataProcessUtil.dip2px(2)));
            viewHolder.album_img.setAdapter(new DynamicAlbumImgAdapter(dynamicModel.getAlbumImg(), viewHolder.album_img, dynamicAlbumImgAdapterListener));

            viewHolder.album_author.setOnClickListener(onViewClick(position, isShared));
        }
        else if(dm.getCardType() == 4 && !(dm instanceof DynamicModel.DynamicUnknownModel))
        {
            ViewHolder.ViewHolderText viewHolder = (ViewHolder.ViewHolderText) vh;
            final DynamicModel.DynamicTextModel dynamicModel = (DynamicModel.DynamicTextModel) dm;

            if(dynamicModel.isCardIsShared())
            {
                viewHolder.text_author.setText(String.format(ctx.getString(R.string.dynamic_text_author), dynamicModel.getTextAuthorName()));
                viewHolder.text_author.setVisibility(View.VISIBLE);
            }
            else
                viewHolder.text_author.setVisibility(View.GONE);

            if(isExpand)
                viewHolder.text_text.setMaxLinesOnShrink(Integer.MAX_VALUE);
            viewHolder.text_text.setExpandListener(new ExpandableTextView.OnExpandListener()
            {
                @Override
                public void onExpand(ExpandableTextView view)
                {
                    dynamicModel.setTextTextExpand(true);
                }

                @Override
                public void onShrink(ExpandableTextView view)
                {
                    dynamicModel.setTextTextExpand(false);
                }
            });
            CharSequence text = DataProcessUtil.getClickableHtml(dynamicModel.getTextText(), new ReplyHtmlImageHandlerUtil(
                    viewHolder.text_text, dynamicModel.getCardEmoteSize()));
            viewHolder.text_text.setOrigText(text);
            viewHolder.text_text.updateForRecyclerView(text, dynamicWidth, dynamicModel.isTextTextExpand() ?
                    ExpandableTextView.STATE_EXPAND : ExpandableTextView.STATE_SHRINK);

            viewHolder.text_text.setMovementMethod(viewHolder.text_text.new LinkTouchMovementMethod());
            viewHolder.text_text.setFocusable(false);
            viewHolder.text_text.setClickable(false);
            viewHolder.text_text.setLongClickable(false);

            viewHolder.text_author.setOnClickListener(onViewClick(position, isShared));
        }
        else if(dm.getCardType() == 8 && !(dm instanceof DynamicModel.DynamicUnknownModel))
        {
            ViewHolder.ViewHolderVideo viewHolder = (ViewHolder.ViewHolderVideo) vh;
            final DynamicModel.DynamicVideoModel dynamicModel = (DynamicModel.DynamicVideoModel) dm;

            if(dynamicModel.isCardIsShared())
            {
                viewHolder.video_author_name.setText(Html.fromHtml(String.format(ctx.getResources().
                        getString(R.string.dynamic_video_author), dynamicModel.getVideoAuthorName())));
                viewHolder.video_author_img.setImageResource(R.drawable.img_default_head);
                viewHolder.video_author_img.setTag(dynamicModel.getVideoAuthorImg());
                BitmapDrawable a = setImageFormWeb(dynamicModel.getVideoAuthorImg());
                if(a != null) viewHolder.video_author_img.setImageDrawable(a);
                viewHolder.video_author_lay.setVisibility(View.VISIBLE);
            }
            else
                viewHolder.video_author_lay.setVisibility(View.GONE);

            if(dynamicModel.getVideoDynamic() != null && !dynamicModel.getVideoDynamic().equals(""))
            {
                if(isExpand)
                    viewHolder.video_dynamic.setMaxLinesOnShrink(Integer.MAX_VALUE);
                viewHolder.video_dynamic.setExpandListener(new ExpandableTextView.OnExpandListener()
                {
                    @Override
                    public void onExpand(ExpandableTextView view)
                    {
                        dynamicModel.setVideoDynamicExpand(true);
                    }

                    @Override
                    public void onShrink(ExpandableTextView view)
                    {
                        dynamicModel.setVideoDynamicExpand(false);
                    }
                });
                CharSequence text = DataProcessUtil.getClickableHtml(dynamicModel.getVideoDynamic(), new ReplyHtmlImageHandlerUtil(
                        viewHolder.video_dynamic, dynamicModel.getCardEmoteSize()));
                viewHolder.video_dynamic.setOrigText(text);
                viewHolder.video_dynamic.updateForRecyclerView(text, dynamicWidth, dynamicModel.isVideoDynamicExpand() ?
                        ExpandableTextView.STATE_EXPAND : ExpandableTextView.STATE_SHRINK);

                viewHolder.video_dynamic.setMovementMethod(viewHolder.video_dynamic.new LinkTouchMovementMethod());
                viewHolder.video_dynamic.setFocusable(false);
                viewHolder.video_dynamic.setClickable(false);
                viewHolder.video_dynamic.setLongClickable(false);
                viewHolder.video_dynamic.setVisibility(View.VISIBLE);
            }
            else
                viewHolder.video_dynamic.setVisibility(View.GONE);

            viewHolder.video_desc.setText(String.format(ctx.getString(R.string.dynamic_video_desc), dynamicModel.getVideoDuration(),
                                                        dynamicModel.getVideoPlay(), dynamicModel.getVideoDanmaku()));
            viewHolder.video_title.setText(dynamicModel.getVideoTitle());
            viewHolder.video_img.setImageResource(R.drawable.img_default_vid);

            viewHolder.video_img.setTag(dynamicModel.getVideoImg());
            BitmapDrawable a = setImageFormWeb(dynamicModel.getVideoImg());
            if(a != null) viewHolder.video_img.setImageDrawable(a);

            viewHolder.video_author_lay.setOnClickListener(onViewClick(position, isShared));
        }
        else if(dm.getCardType() == 64 && !(dm instanceof DynamicModel.DynamicUnknownModel))
        {
            ViewHolder.ViewHolderArticle viewHolder = (ViewHolder.ViewHolderArticle) vh;
            final DynamicModel.DynamicArticleModel dynamicModel = (DynamicModel.DynamicArticleModel) dm;

            if(dynamicModel.isCardIsShared())
            {
                viewHolder.article_author_name.setText(Html.fromHtml(String.format(ctx.getResources().
                        getString(R.string.dynamic_article_author), dynamicModel.getArticleAuthorName())));
                viewHolder.article_author_img.setImageResource(R.drawable.img_default_head);
                viewHolder.article_author_img.setTag(dynamicModel.getArticleAuthorImg());
                BitmapDrawable a = setImageFormWeb(dynamicModel.getArticleAuthorImg());
                if(a != null) viewHolder.article_author_img.setImageDrawable(a);
                viewHolder.article_author_lay.setVisibility(View.VISIBLE);
            }
            else
                viewHolder.article_author_lay.setVisibility(View.GONE);

            if(dynamicModel.getArticleDynamic() != null && !dynamicModel.getArticleDynamic().equals(""))
            {
                if(isExpand)
                    viewHolder.article_dynamic.setMaxLinesOnShrink(Integer.MAX_VALUE);
                viewHolder.article_dynamic.setExpandListener(new ExpandableTextView.OnExpandListener()
                {
                    @Override
                    public void onExpand(ExpandableTextView view)
                    {
                        dynamicModel.setArticleDynamicExpand(true);
                    }

                    @Override
                    public void onShrink(ExpandableTextView view)
                    {
                        dynamicModel.setArticleDynamicExpand(false);
                    }
                });
                CharSequence text = DataProcessUtil.getClickableHtml(dynamicModel.getArticleDynamic(), new ReplyHtmlImageHandlerUtil(
                        viewHolder.article_dynamic, dynamicModel.getCardEmoteSize()));
                viewHolder.article_dynamic.setOrigText(text);
                viewHolder.article_dynamic.updateForRecyclerView(text, dynamicWidth, dynamicModel.isArticleDynamicExpand() ?
                        ExpandableTextView.STATE_EXPAND : ExpandableTextView.STATE_SHRINK);

                viewHolder.article_dynamic.setMovementMethod(viewHolder.article_dynamic.new LinkTouchMovementMethod());
                viewHolder.article_dynamic.setFocusable(false);
                viewHolder.article_dynamic.setClickable(false);
                viewHolder.article_dynamic.setLongClickable(false);
                viewHolder.article_dynamic.setVisibility(View.VISIBLE);
            }
            else
                viewHolder.article_dynamic.setVisibility(View.GONE);

            viewHolder.article_title.setText(dynamicModel.getArticleTitle());
            viewHolder.article_desc.setText(dynamicModel.getArticleDesc());
            viewHolder.article_view.setText(String.format(ctx.getString(R.string.dynamic_article_view), dynamicModel.getArticleView()));
            viewHolder.article_img.setImageResource(R.drawable.img_default_vid);

            viewHolder.article_img.setTag(dynamicModel.getArticleImg());
            BitmapDrawable a = setImageFormWeb(dynamicModel.getArticleImg());
            if(a != null) viewHolder.article_img.setImageDrawable(a);

            viewHolder.article_author_lay.setOnClickListener(onViewClick(position, isShared));
        }
        else if((dm.getCardType() == 512 || dm.getCardType() == 4098 || dm.getCardType() == 4099 || dm.getCardType() == 4101) && !(dm instanceof DynamicModel.DynamicUnknownModel))
        {
            ViewHolder.ViewHolderBangumi viewHolder = (ViewHolder.ViewHolderBangumi) vh;
            DynamicModel.DynamicBangumiModel dynamicModel = (DynamicModel.DynamicBangumiModel) dm;

            if(dynamicModel.isCardIsShared())
            {
                viewHolder.bangumi_author.setText(dynamicModel.getBangumiAuthorName());
                viewHolder.bangumi_author.setVisibility(View.VISIBLE);
            }
            else
                viewHolder.bangumi_author.setVisibility(View.GONE);

            viewHolder.bangumi_title.setText(dynamicModel.getBangumiTitle());
            viewHolder.bangumi_desc.setText(String.format(ctx.getString(R.string.dynamic_bangumi_desc), dynamicModel.getBangumiView(), dynamicModel.getBangumiDanmaku()));
            viewHolder.bangumi_img.setImageResource(R.drawable.img_default_vid);

            viewHolder.bangumi_img.setTag(dynamicModel.getBangumiImg());
            BitmapDrawable a = setImageFormWeb(dynamicModel.getBangumiImg());
            if(a != null) viewHolder.bangumi_img.setImageDrawable(a);
        }
        else if(dm.getCardType() == 2048 && !(dm instanceof DynamicModel.DynamicUnknownModel))
        {
            ViewHolder.ViewHolderUrl viewHolder = (ViewHolder.ViewHolderUrl) vh;
            final DynamicModel.DynamicUrlModel dynamicModel = (DynamicModel.DynamicUrlModel) dm;

            if(dynamicModel.isCardIsShared())
            {
                viewHolder.url_author.setText(String.format(ctx.getString(R.string.dynamic_url_author), dynamicModel.getUrlAuthorName()));
                viewHolder.url_author.setVisibility(View.VISIBLE);
            }
            else
                viewHolder.url_author.setVisibility(View.GONE);

            if(dynamicModel.getUrlDynamic() != null && !dynamicModel.getUrlDynamic().equals(""))
            {
                if(isExpand)
                    viewHolder.url_dynamic.setMaxLinesOnShrink(Integer.MAX_VALUE);
                viewHolder.url_dynamic.setExpandListener(new ExpandableTextView.OnExpandListener()
                {
                    @Override
                    public void onExpand(ExpandableTextView view)
                    {
                        dynamicModel.setUrlDynamicExpand(true);
                    }

                    @Override
                    public void onShrink(ExpandableTextView view)
                    {
                        dynamicModel.setUrlDynamicExpand(false);
                    }
                });
                CharSequence text = DataProcessUtil.getClickableHtml(dynamicModel.getUrlDynamic(), new ReplyHtmlImageHandlerUtil(
                        viewHolder.url_dynamic, dynamicModel.getCardEmoteSize()));
                viewHolder.url_dynamic.setOrigText(text);
                viewHolder.url_dynamic.updateForRecyclerView(text, dynamicWidth, dynamicModel.isUrlDynamicExpand() ?
                        ExpandableTextView.STATE_EXPAND : ExpandableTextView.STATE_SHRINK);

                viewHolder.url_dynamic.setMovementMethod(viewHolder.url_dynamic.new LinkTouchMovementMethod());
                viewHolder.url_dynamic.setFocusable(false);
                viewHolder.url_dynamic.setClickable(false);
                viewHolder.url_dynamic.setLongClickable(false);
                viewHolder.url_dynamic.setVisibility(View.VISIBLE);
            }
            else
                viewHolder.url_dynamic.setVisibility(View.GONE);

            viewHolder.url_title.setText(dynamicModel.getUrlTitle());
            viewHolder.url_desc.setText(dynamicModel.getUrlDesc());
            viewHolder.url_img.setImageResource(R.drawable.img_default_animation);

            viewHolder.url_img.setTag(dynamicModel.getUrlImg());
            BitmapDrawable a = setImageFormWeb(dynamicModel.getUrlImg());
            if(a != null) viewHolder.url_img.setImageDrawable(a);

            viewHolder.url_author.setOnClickListener(onViewClick(position, isShared));
            viewHolder.url_url.setOnClickListener(onViewClick(position, isShared));
        }
        else if(dm.getCardType() == 4200 && !(dm instanceof DynamicModel.DynamicUnknownModel))
        {
            ViewHolder.ViewHolderLive viewHolder = (ViewHolder.ViewHolderLive) vh;
            DynamicModel.DynamicLiveModel dynamicModel = (DynamicModel.DynamicLiveModel) dm;

            if(dynamicModel.isCardIsShared())
            {
                viewHolder.live_author_name.setText(Html.fromHtml(String.format(ctx.getResources().
                        getString(R.string.dynamic_live_author), dynamicModel.getLiveAuthorName())));
                viewHolder.live_author_img.setImageResource(R.drawable.img_default_head);
                viewHolder.live_author_img.setTag(dynamicModel.getLiveAuthorImg());
                BitmapDrawable a = setImageFormWeb(dynamicModel.getLiveAuthorImg());
                if(a != null) viewHolder.live_author_img.setImageDrawable(a);
            }
            viewHolder.live_author_lay.setVisibility(View.VISIBLE);

            viewHolder.live_title.setText(dynamicModel.getLiveTitle());
            viewHolder.live_area.setText(dynamicModel.getLiveArea());
            viewHolder.live_online.setText(dynamicModel.getLiveOnline());

            Drawable viewerNumDrawable = ctx.getResources().getDrawable(R.drawable.icon_number_viewer_white);
            viewerNumDrawable.setBounds(0,0, DataProcessUtil.dip2px(12), DataProcessUtil.dip2px(2));
            viewHolder.live_online.setCompoundDrawables(viewerNumDrawable,null, null,null);

            if(dynamicModel.isLiveStatus())
            {
                viewHolder.live_status.setText("直播中");
                viewHolder.live_status.setBackgroundResource(R.drawable.shape_bg_article_card_live_living);
                viewHolder.live_online.setText(dynamicModel.getLiveOnline());
            }
            else
            {
                viewHolder.live_status.setText("未开播");
                viewHolder.live_status.setBackgroundResource(R.drawable.shape_bg_article_card_live_notlive);
                viewHolder.live_online.setText("--");
            }
            viewHolder.live_img.setImageResource(R.drawable.img_default_vid);

            viewHolder.live_img.setTag(dynamicModel.getLiveImg());
            BitmapDrawable a = setImageFormWeb(dynamicModel.getLiveImg());
            if(a != null) viewHolder.live_img.setImageDrawable(a);

            viewHolder.live_author_lay.setOnClickListener(onViewClick(position, isShared));
        }
        else if(dm.getCardType() == 4300 && !(dm instanceof DynamicModel.DynamicUnknownModel))
        {
            ViewHolder.ViewHolderFavor viewHolder = (ViewHolder.ViewHolderFavor) vh;
            DynamicModel.DynamicFavorModel dynamicModel = (DynamicModel.DynamicFavorModel) dm;

            if(dynamicModel.isCardIsShared())
            {
                viewHolder.favor_author_name.setText(Html.fromHtml(String.format(ctx.getResources().
                        getString(R.string.dynamic_favor_author), dynamicModel.getFavorAuthorName())));
                viewHolder.favor_author_img.setImageResource(R.drawable.img_default_head);
                viewHolder.favor_author_img.setTag(dynamicModel.getFavorAuthorImg());
                BitmapDrawable a = setImageFormWeb(dynamicModel.getFavorAuthorImg());
                if(a != null) viewHolder.favor_author_img.setImageDrawable(a);
                viewHolder.dynamic_like_lay.setVisibility(View.VISIBLE);
            }
            else
                viewHolder.favor_author_lay.setVisibility(View.GONE);

            viewHolder.favor_title.setText(dynamicModel.getFavorTitle());
            viewHolder.favor_desc.setText(String.format(ctx.getString(R.string.dynamic_favor_desc), dynamicModel.getFavorCount()));
            viewHolder.favor_img.setImageResource(R.drawable.img_default_vid);

            viewHolder.favor_img.setTag(dynamicModel.getFavorImg());
            BitmapDrawable a = setImageFormWeb(dynamicModel.getFavorImg());
            if(a != null) viewHolder.favor_img.setImageDrawable(a);

            viewHolder.favor_author_lay.setOnClickListener(onViewClick(position, isShared));
        }
        else
        {
            ViewHolder.ViewHolderUnknown viewHolder = (ViewHolder.ViewHolderUnknown) vh;
            DynamicModel.DynamicUnknownModel dynamicModel = (DynamicModel.DynamicUnknownModel) dm;

            if(dynamicModel.getUnknownTips() != null && !dynamicModel.getUnknownTips().equals(""))
                viewHolder.unknown_tips.setText(dynamicModel.getUnknownTips());
            else
                viewHolder.unknown_tips.setText(ctx.getString(R.string.dynamic_unknown_tips));
        }

        if(!isShared)
        {
            vh.dynamic_author_head.setImageResource(R.drawable.img_default_head);
            vh.dynamic_author_name.setText(dm.getCardAuthorName());
            vh.dynamic_time.setText(dm.getCardTime());
            if(dm.getCardAuthorOfficial() == 0)
            {
                vh.dynamic_author_off_1.setVisibility(View.VISIBLE);
                vh.dynamic_author_off_2.setVisibility(View.GONE);
            }
            else if(dm.getCardAuthorOfficial() == 1)
            {
                vh.dynamic_author_off_1.setVisibility(View.GONE);
                vh.dynamic_author_off_2.setVisibility(View.VISIBLE);
            }
            else
            {
                vh.dynamic_author_off_1.setVisibility(View.GONE);
                vh.dynamic_author_off_2.setVisibility(View.GONE);
            }

            if(dm.getCardAuthorVipStatus() == 1 && dm.getCardAuthorVipType() == 2)
                vh.dynamic_author_name.setTextColor(ColorUtil.getColor(R.attr.colorVip, vh.dynamic_author_name.getContext()));
            else
                vh.dynamic_author_name.setTextColor(ColorUtil.getColor(android.R.attr.textColor, vh.dynamic_author_name.getContext()));

            vh.dynamic_share_text.setText(dm.getCardShareNum());
            vh.dynamic_reply_text.setText(dm.getCardReplyNum());
            vh.dynamic_like_text.setText(DataProcessUtil.getView(dm.getCardLikeNum()));

            if(dm.isCardUserLike())
                vh.dynamic_like_img.setImageResource(R.drawable.icon_like_yes);
            else
                vh.dynamic_like_img.setImageResource(R.drawable.icon_like);

            vh.dynamic_author_head.setTag(dm.getCardAuthorImg());
            BitmapDrawable h = setImageFormWeb(dm.getCardAuthorImg());
            if(h != null) vh.dynamic_author_head.setImageDrawable(h);

            vh.dynamic_author_lay.setOnClickListener(onViewClick(position, false));
            vh.dynamic_share_lay.setOnClickListener(onViewClick(position, false));
            vh.dynamic_reply_lay.setOnClickListener(onViewClick(position, false));
            vh.dynamic_like_lay.setOnClickListener(onViewClick(position, false));
            vh.dynamic_lay.setOnClickListener(onViewClick(position, false));
        }
    }

    private BitmapDrawable setImageFormWeb(String url)
    {
        if(url != null && LruCacheUtil.getLruCache().get(url) != null)
            return LruCacheUtil.getLruCache().get(url);
        else
        {
            ImageTaskUtil it = new ImageTaskUtil(view);
            it.execute(url);
            return null;
        }
    }

    private View.OnClickListener onViewClick(final int position, final boolean isShared)
    {
        return v -> dynamicAdapterListener.onClick(v.getId(), position, isShared);
    }

    public static class ViewHolder
    {
        int type;
        RelativeLayout dynamic_lay;
        RelativeLayout dynamic_author_lay;
        CircleImageView dynamic_author_head;
        ImageView dynamic_author_off_1;
        ImageView dynamic_author_off_2;
        TextView dynamic_author_name;
        TextView dynamic_time;
        LinearLayout dynamic_share_lay;
        ImageView dynamic_share_img;
        TextView dynamic_share_text;
        LinearLayout dynamic_reply_lay;
        TextView dynamic_reply_text;
        LinearLayout dynamic_like_lay;
        ImageView dynamic_like_img;
        TextView dynamic_like_text;

        ViewHolder() {}
        ViewHolder(View view, int type)
        {
            this.type = type;
            dynamic_lay = view.findViewById(R.id.item_dynamic_lay);
            dynamic_author_lay = view.findViewById(R.id.item_dynamic_author_lay);
            dynamic_author_head = view.findViewById(R.id.item_dynamic_head);
            dynamic_author_off_1 = view.findViewById(R.id.item_dynamic_off_1);
            dynamic_author_off_2 = view.findViewById(R.id.item_dynamic_off_2);
            dynamic_author_name = view.findViewById(R.id.item_dynamic_name);
            dynamic_time = view.findViewById(R.id.item_dynamic_time);
            dynamic_share_lay = view.findViewById(R.id.item_dynamic_share_lay);
            dynamic_share_img = view.findViewById(R.id.item_dynamic_share_img);
            dynamic_share_text = view.findViewById(R.id.item_dynamic_share);
            dynamic_reply_lay = view.findViewById(R.id.item_dynamic_reply_lay);
            dynamic_reply_text = view.findViewById(R.id.item_dynamic_reply);
            dynamic_like_lay = view.findViewById(R.id.item_dynamic_like_lay);
            dynamic_like_img = view.findViewById(R.id.item_dynamic_like_img);
            dynamic_like_text = view.findViewById(R.id.item_dynamic_like);
        }

        private static class ViewHolderShare extends ViewHolder
        {
            ExpandableTextView share_dynamic;
            LinearLayout share_share;
            ViewHolderShare(View view, int type)
            {
                super(view, type);
                share_dynamic = view.findViewById(R.id.dynamic_share_dynamic);
                share_share = view.findViewById(R.id.dynamic_share_share);
            }
        }

        private static class ViewHolderText extends ViewHolder
        {
            TextView text_author;
            ExpandableTextView text_text;
            ViewHolderText(View view, int type)
            {
                super(view, type);
                text_author = view.findViewById(R.id.dynamic_text_author);
                text_text = view.findViewById(R.id.dynamic_text_text);
            }
        }

        private static class ViewHolderAlbum extends ViewHolder
        {
            TextView album_author;
            ExpandableTextView album_text;
            RecyclerView album_img;
            ViewHolderAlbum(View view, int type)
            {
                super(view, type);
                album_author = view.findViewById(R.id.dynamic_album_author);
                album_text = view.findViewById(R.id.dynamic_album_text);
                album_img = view.findViewById(R.id.dynamic_album_img_lay);
            }
        }

        private static class ViewHolderVideo extends ViewHolder
        {
            LinearLayout video_author_lay;
            CircleImageView video_author_img;
            TextView video_author_name;
            ExpandableTextView video_dynamic;
            ImageView video_img;
            TextView video_desc;
            TextView video_title;
            ViewHolderVideo(View view, int type)
            {
                super(view, type);
                video_author_lay = view.findViewById(R.id.dynamic_video_author);
                video_author_img = view.findViewById(R.id.dynamic_video_author_img);
                video_author_name = view.findViewById(R.id.dynamic_video_author_name);
                video_dynamic = view.findViewById(R.id.dynamic_video_dynamic);
                video_img = view.findViewById(R.id.dynamic_video_img);
                video_desc = view.findViewById(R.id.dynamic_video_desc);
                video_title = view.findViewById(R.id.dynamic_video_title);
            }
        }

        private static class ViewHolderArticle extends ViewHolder
        {
            LinearLayout article_author_lay;
            CircleImageView article_author_img;
            TextView article_author_name;
            ExpandableTextView article_dynamic;
            TextView article_title;
            ImageView article_img;
            TextView article_desc;
            TextView article_view;
            ViewHolderArticle(View view, int type)
            {
                super(view, type);
                article_author_lay = view.findViewById(R.id.dynamic_article_author);
                article_author_img = view.findViewById(R.id.dynamic_article_author_img);
                article_author_name = view.findViewById(R.id.dynamic_article_author_name);
                article_dynamic = view.findViewById(R.id.dynamic_article_dynamic);
                article_title = view.findViewById(R.id.dynamic_article_title);
                article_img = view.findViewById(R.id.dynamic_article_img);
                article_desc = view.findViewById(R.id.dynamic_article_desc);
                article_view = view.findViewById(R.id.dynamic_article_view);
            }
        }

        private static class ViewHolderBangumi extends ViewHolder
        {
            TextView bangumi_author;
            ImageView bangumi_img;
            TextView bangumi_desc;
            TextView bangumi_title;
            ViewHolderBangumi(View view, int type)
            {
                super(view, type);
                bangumi_author = view.findViewById(R.id.dynamic_bangumi_author);
                bangumi_img = view.findViewById(R.id.dynamic_bangumi_img);
                bangumi_desc = view.findViewById(R.id.dynamic_bangumi_desc);
                bangumi_title = view.findViewById(R.id.dynamic_bangumi_title);
            }
        }

        private static class ViewHolderUrl extends ViewHolder
        {
            TextView url_author;
            ExpandableTextView url_dynamic;
            CardView url_url;
            ImageView url_img;
            TextView url_title;
            TextView url_desc;
            ViewHolderUrl(View view, int type)
            {
                super(view, type);
                url_author = view.findViewById(R.id.dynamic_url_author);
                url_dynamic = view.findViewById(R.id.dynamic_url_dynamic);
                url_url = view.findViewById(R.id.dynamic_url_url);
                url_img = view.findViewById(R.id.dynamic_url_img);
                url_title = view.findViewById(R.id.dynamic_url_title);
                url_desc = view.findViewById(R.id.dynamic_url_desc);
            }
        }

        private static class ViewHolderLive extends ViewHolder
        {
            LinearLayout live_author_lay;
            CircleImageView live_author_img;
            TextView live_author_name;
            ImageView live_img;
            TextView live_status;
            TextView live_area;
            TextView live_online;
            TextView live_title;
            ViewHolderLive(View view, int type)
            {
                super(view, type);
                live_author_lay = view.findViewById(R.id.dynamic_live_author);
                live_author_img = view.findViewById(R.id.dynamic_live_author_img);
                live_author_name = view.findViewById(R.id.dynamic_live_author_name);
                live_img = view.findViewById(R.id.dynamic_live_img);
                live_status = view.findViewById(R.id.dynamic_live_stat);
                live_area = view.findViewById(R.id.dynamic_live_area);
                live_online = view.findViewById(R.id.dynamic_live_online);
                live_title = view.findViewById(R.id.dynamic_live_title);
            }
        }

        private static class ViewHolderFavor extends ViewHolder
        {
            LinearLayout favor_author_lay;
            CircleImageView favor_author_img;
            TextView favor_author_name;
            ImageView favor_img;
            TextView favor_title;
            TextView favor_desc;
            ViewHolderFavor(View view, int type)
            {
                super(view, type);
                favor_author_lay = view.findViewById(R.id.dynamic_favor_author);
                favor_author_img = view.findViewById(R.id.dynamic_favor_author_img);
                favor_author_name = view.findViewById(R.id.dynamic_favor_author_name);
                favor_img = view.findViewById(R.id.dynamic_favor_img);
                favor_title = view.findViewById(R.id.dynamic_favor_title);
                favor_desc = view.findViewById(R.id.dynamic_favor_desc);
            }
        }

        private static class ViewHolderUnknown extends ViewHolder
        {
            TextView unknown_tips;
            ViewHolderUnknown(View view, int type)
            {
                super(view, type);
                unknown_tips = view.findViewById(R.id.dynamic_unknown_tips);
            }
        }
    }


    public interface DynamicAdapterListener
    {
        void onClick(int viewId, int position, boolean isShared);
    }
}
