package cn.luern0313.wristbilibili.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.Log;
import android.util.LruCache;
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

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.models.DynamicModel;
import cn.luern0313.wristbilibili.ui.ImgActivity;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.DynamicAlbumDecoration;
import cn.luern0313.wristbilibili.util.ImageTaskUtil;
import cn.luern0313.wristbilibili.util.MyApplication;
import de.hdodenhof.circleimageview.CircleImageView;
import lombok.ToString;

/**
 * 被 luern0313 创建于 2019/8/31.
 */

public class DynamicAdapter extends BaseAdapter
{
    private Context ctx;
    private LayoutInflater mInflater;

    private LruCache<String, BitmapDrawable> mImageCache;
    private DynamicAdapterListener dynamicAdapterListener;

    private ArrayList<DynamicModel> dynamicModelArrayList;
    private ListView listView;

    public DynamicAdapter(LayoutInflater inflater, ArrayList<DynamicModel> dynamicModelArrayList, ListView listView, DynamicAdapterListener dynamicAdapterListener)
    {
        ctx = MyApplication.getContext();
        mInflater = inflater;
        this.dynamicModelArrayList = dynamicModelArrayList;
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
            return getType(((DynamicModel.DynamicShareModel) dynamicModelArrayList.get(position)).getShareOriginCard().getCardType());
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
        DynamicModel dynamicModel = dynamicModelArrayList.get(position);
        ViewHolder viewHolder;
        if(convertView == null || ((ViewHolder) convertView.getTag()).type != dynamicModel.getCardType())
        {
            convertView = mInflater.inflate(R.layout.item_dynamic, null);
            viewHolder = getViewHolder(dynamicModel, convertView, R.id.item_dynamic_dynamic);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
            Log.w("bilibili", "复用：" + viewHolder.type + "→" + dynamicModel.getCardType());
            Log.w("bilibili", "复用type：" + getType(viewHolder.type) + "→" + getType(dynamicModel.getCardType()));
        }

        handlerView(viewHolder, dynamicModel, position, false);

        return convertView;
    }

    private ViewHolder getViewHolder(DynamicModel dynamicModel, View convertView, int viewId)
    {
        ViewHolder viewHolder;
        ViewHolder vh = new ViewHolder();
        switch (dynamicModel.getCardType())
        {
            case 1:
            {
                View view = mInflater.inflate(R.layout.widget_dynamic_share, null);
                ((LinearLayout) convertView.findViewById(viewId)).removeAllViewsInLayout();
                ((LinearLayout) convertView.findViewById(viewId)).addView(view);
                viewHolder = vh.new ViewHolderShare(convertView, dynamicModel.getCardType());
                convertView.setTag(viewHolder);
                break;
            }
            case 2:
            {
                View view = mInflater.inflate(R.layout.widget_dynamic_album, null);
                ((LinearLayout) convertView.findViewById(viewId)).removeAllViewsInLayout();
                ((LinearLayout) convertView.findViewById(viewId)).addView(view);
                viewHolder = vh.new ViewHolderAlbum(convertView, dynamicModel.getCardType());
                convertView.setTag(viewHolder);
                break;
            }
            case 4:
            {
                View view = mInflater.inflate(R.layout.widget_dynamic_text, null);
                ((LinearLayout) convertView.findViewById(viewId)).removeAllViewsInLayout();
                ((LinearLayout) convertView.findViewById(viewId)).addView(view);
                viewHolder = vh.new ViewHolderText(convertView, dynamicModel.getCardType());
                convertView.setTag(viewHolder);
                break;
            }
            case 8:
            {
                View view = mInflater.inflate(R.layout.widget_dynamic_video, null);
                ((LinearLayout) convertView.findViewById(viewId)).removeAllViewsInLayout();
                ((LinearLayout) convertView.findViewById(viewId)).addView(view);
                viewHolder = vh.new ViewHolderVideo(convertView, dynamicModel.getCardType());
                convertView.setTag(viewHolder);
                break;
            }
            case 64:
            {
                View view = mInflater.inflate(R.layout.widget_dynamic_article, null);
                ((LinearLayout) convertView.findViewById(viewId)).removeAllViewsInLayout();
                ((LinearLayout) convertView.findViewById(viewId)).addView(view);
                viewHolder = vh.new ViewHolderArticle(convertView, dynamicModel.getCardType());
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
                viewHolder = vh.new ViewHolderBangumi(convertView, dynamicModel.getCardType());
                convertView.setTag(viewHolder);
                break;
            }
            case 2048:
            {
                View view = mInflater.inflate(R.layout.widget_dynamic_url, null);
                ((LinearLayout) convertView.findViewById(viewId)).removeAllViewsInLayout();
                ((LinearLayout) convertView.findViewById(viewId)).addView(view);
                viewHolder = vh.new ViewHolderUrl(convertView, dynamicModel.getCardType());
                convertView.setTag(viewHolder);
                break;
            }
            case 4200:
            {
                View view = mInflater.inflate(R.layout.widget_dynamic_live, null);
                ((LinearLayout) convertView.findViewById(viewId)).removeAllViewsInLayout();
                ((LinearLayout) convertView.findViewById(viewId)).addView(view);
                viewHolder = vh.new ViewHolderLive(convertView, dynamicModel.getCardType());
                convertView.setTag(viewHolder);
                break;
            }
            case 4300:
            {
                View view = mInflater.inflate(R.layout.widget_dynamic_favor, null);
                ((LinearLayout) convertView.findViewById(viewId)).removeAllViewsInLayout();
                ((LinearLayout) convertView.findViewById(viewId)).addView(view);
                viewHolder = vh.new ViewHolderFavor(convertView, dynamicModel.getCardType());
                convertView.setTag(viewHolder);
                break;
            }
            default:
            {
                View view = mInflater.inflate(R.layout.widget_dynamic_unknown, null);
                ((LinearLayout) convertView.findViewById(viewId)).removeAllViewsInLayout();
                ((LinearLayout) convertView.findViewById(viewId)).addView(view);
                viewHolder = vh.new ViewHolderUnknown(convertView, dynamicModel.getCardType());
                convertView.setTag(viewHolder);
                break;
            }
        }
        return viewHolder;
    }

    private void handlerView(ViewHolder vh, DynamicModel dm, int position, boolean isShared)
    {
        if(dm.getCardType() == 1)
        {
            ViewHolder.ViewHolderShare viewHolder = (ViewHolder.ViewHolderShare) vh;
            DynamicModel.DynamicShareModel dynamicModel = (DynamicModel.DynamicShareModel) dm;

            viewHolder.share_dynamic.setText(dynamicModel.getShareText());
            ViewHolder shareViewHolder = getViewHolder(dynamicModel.getShareOriginCard(), vh.dynamic_lay, R.id.dynamic_share_share);
            handlerView(shareViewHolder, dynamicModel.getShareOriginCard(), position, true);
        }
        else if(dm.getCardType() == 2)
        {
            ViewHolder.ViewHolderAlbum viewHolder = (ViewHolder.ViewHolderAlbum) vh;
            final DynamicModel.DynamicAlbumModel dynamicModel = (DynamicModel.DynamicAlbumModel) dm;

            if(dynamicModel.isCardIsShared())
            {
                viewHolder.album_author.setText(String.format(ctx.getResources().getString(R.string.dynamic_album_author), dynamicModel.getAlbumAuthorName()));
                viewHolder.album_author.setVisibility(View.VISIBLE);
            }
            else
                viewHolder.album_author.setVisibility(View.GONE);

            viewHolder.album_text.setText(dynamicModel.getAlbumText());

            DynamicAlbumImgAdapter.DynamicAlbumImgAdapterListener dynamicAlbumImgAdapterListener = new DynamicAlbumImgAdapter.DynamicAlbumImgAdapterListener()
            {
                @Override
                public void onClick(int position)
                {
                    Intent intent = new Intent(ctx, ImgActivity.class);
                    intent.putExtra("imgUrl", dynamicModel.getAlbumImg().toArray(new String[]{}));
                    intent.putExtra("position", position);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ctx.startActivity(intent);
                }
            };
            LinearLayoutManager layoutManager = new LinearLayoutManager(ctx);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            viewHolder.album_img.setLayoutManager(layoutManager);
            viewHolder.album_img.addItemDecoration(DynamicAlbumDecoration.createHorizontal(ctx, Color.argb(0, 0, 0, 0), DataProcessUtil.dip2px(ctx, 2)));
            viewHolder.album_img.setAdapter(new DynamicAlbumImgAdapter(dynamicModel.getAlbumImg(), viewHolder.album_img, dynamicAlbumImgAdapterListener));
        }
        else if(dm.getCardType() == 4)
        {
            ViewHolder.ViewHolderText viewHolder = (ViewHolder.ViewHolderText) vh;
            DynamicModel.DynamicTextModel dynamicModel = (DynamicModel.DynamicTextModel) dm;

            if(dynamicModel.isCardIsShared())
            {
                viewHolder.text_author.setText(String.format(ctx.getResources().getString(R.string.dynamic_text_author), dynamicModel.getTextAuthorName()));
                viewHolder.text_author.setVisibility(View.VISIBLE);
            }
            else
                viewHolder.text_author.setVisibility(View.GONE);

            viewHolder.text_text.setText(dynamicModel.getTextText());
        }
        else if(dm.getCardType() == 8)
        {
            ViewHolder.ViewHolderVideo viewHolder = (ViewHolder.ViewHolderVideo) vh;
            DynamicModel.DynamicVideoModel dynamicModel = (DynamicModel.DynamicVideoModel) dm;

            if(dynamicModel.isCardIsShared())
            {
                viewHolder.video_author_name.setText(Html.fromHtml(String.format(ctx.getResources().
                        getString(R.string.dynamic_video_author), dynamicModel.getVideoAuthorName())));
                viewHolder.video_author_img.setTag(dynamicModel.getVideoAuthorImg());
                BitmapDrawable a = setImageFormWeb(dynamicModel.getVideoAuthorImg());
                if(a != null) viewHolder.video_author_img.setImageDrawable(a);
                viewHolder.video_author_lay.setVisibility(View.VISIBLE);
            }
            else
                viewHolder.video_author_lay.setVisibility(View.GONE);

            if(!dynamicModel.getVideoDynamic().equals(""))
            {
                viewHolder.video_dynamic.setText(dynamicModel.getVideoDynamic());
                viewHolder.video_dynamic.setVisibility(View.VISIBLE);
            }
            else
                viewHolder.video_dynamic.setVisibility(View.GONE);

            viewHolder.video_desc.setText(String.format(ctx.getResources().getString(R.string.dynamic_video_desc), dynamicModel.getVideoDuration(),
                                                        dynamicModel.getVideoPlay(), dynamicModel.getVideoDanmaku()));
            viewHolder.video_title.setText(dynamicModel.getVideoTitle());

            viewHolder.video_img.setTag(dynamicModel.getVideoImg());
            BitmapDrawable a = setImageFormWeb(dynamicModel.getVideoImg());
            if(a != null) viewHolder.video_img.setImageDrawable(a);
        }
        else if(dm.getCardType() == 64)
        {
            ViewHolder.ViewHolderArticle viewHolder = (ViewHolder.ViewHolderArticle) vh;
            DynamicModel.DynamicArticleModel dynamicModel = (DynamicModel.DynamicArticleModel) dm;

            if(dynamicModel.isCardIsShared())
            {
                viewHolder.article_author_name.setText(Html.fromHtml(String.format(ctx.getResources().
                        getString(R.string.dynamic_article_author), dynamicModel.getArticleAuthorName())));
                viewHolder.article_author_img.setTag(dynamicModel.getArticleAuthorImg());
                BitmapDrawable a = setImageFormWeb(dynamicModel.getArticleAuthorImg());
                if(a != null) viewHolder.article_author_img.setImageDrawable(a);
                viewHolder.article_author_lay.setVisibility(View.VISIBLE);
            }
            else
                viewHolder.article_author_lay.setVisibility(View.GONE);

            if(!dynamicModel.getArticleDynamic().equals(""))
            {
                viewHolder.article_dynamic.setText(dynamicModel.getArticleDynamic());
                viewHolder.article_dynamic.setVisibility(View.VISIBLE);
            }
            else
                viewHolder.article_dynamic.setVisibility(View.GONE);

            viewHolder.article_title.setText(dynamicModel.getArticleTitle());
            viewHolder.article_desc.setText(dynamicModel.getArticleDesc());
            viewHolder.article_view.setText(String.format(ctx.getResources().getString(R.string.dynamic_article_view), dynamicModel.getArticleView()));

            viewHolder.article_img.setTag(dynamicModel.getArticleImg());
            BitmapDrawable a = setImageFormWeb(dynamicModel.getArticleImg());
            if(a != null) viewHolder.article_img.setImageDrawable(a);
        }
        else if(dm.getCardType() == 512 || dm.getCardType() == 4098 || dm.getCardType() == 4099 || dm.getCardType() == 4101)
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
            viewHolder.bangumi_desc.setText(String.format(ctx.getResources().getString(R.string.dynamic_bangumi_desc), dynamicModel.getBangumiView(), dynamicModel.getBangumiDanmaku()));

            viewHolder.bangumi_img.setTag(dynamicModel.getBangumiImg());
            BitmapDrawable a = setImageFormWeb(dynamicModel.getBangumiImg());
            if(a != null) viewHolder.bangumi_img.setImageDrawable(a);
        }
        else if(dm.getCardType() == 2048)
        {
            ViewHolder.ViewHolderUrl viewHolder = (ViewHolder.ViewHolderUrl) vh;
            DynamicModel.DynamicUrlModel dynamicModel = (DynamicModel.DynamicUrlModel) dm;

            if(dynamicModel.isCardIsShared())
            {
                viewHolder.url_author.setText(String.format(ctx.getResources().getString(R.string.dynamic_url_author), dynamicModel.getUrlAuthorName()));
                viewHolder.url_author.setVisibility(View.VISIBLE);
            }
            else
                viewHolder.url_author.setVisibility(View.GONE);

            if(!dynamicModel.getUrlDynamic().equals(""))
            {
                viewHolder.url_dynamic.setText(dynamicModel.getUrlUrl());
                viewHolder.url_dynamic.setVisibility(View.VISIBLE);
            }
            else
                viewHolder.url_dynamic.setVisibility(View.GONE);

            viewHolder.url_title.setText(dynamicModel.getUrlTitle());
            viewHolder.url_desc.setText(dynamicModel.getUrlDesc());

            viewHolder.url_img.setTag(dynamicModel.getUrlImg());
            BitmapDrawable a = setImageFormWeb(dynamicModel.getUrlImg());
            if(a != null) viewHolder.url_img.setImageDrawable(a);
        }
        else if(dm.getCardType() == 4200)
        {
            ViewHolder.ViewHolderLive viewHolder = (ViewHolder.ViewHolderLive) vh;
            DynamicModel.DynamicLiveModel dynamicModel = (DynamicModel.DynamicLiveModel) dm;

            if(dynamicModel.isCardIsShared())
            {
                viewHolder.live_author_name.setText(Html.fromHtml(String.format(ctx.getResources().
                        getString(R.string.dynamic_live_author), dynamicModel.getLiveAuthorName())));
                viewHolder.live_author_img.setTag(dynamicModel.getLiveAuthorImg());
                BitmapDrawable a = setImageFormWeb(dynamicModel.getLiveAuthorImg());
                if(a != null) viewHolder.live_author_img.setImageDrawable(a);
                viewHolder.live_author_lay.setVisibility(View.VISIBLE);
            }
            else
                viewHolder.live_author_lay.setVisibility(View.VISIBLE);

            viewHolder.live_title.setText(dynamicModel.getLiveTitle());
            viewHolder.live_area.setText(dynamicModel.getLiveArea());
            viewHolder.live_online.setText(dynamicModel.getLiveOnline());

            Drawable viewerNumDrawable = ctx.getResources().getDrawable(R.drawable.icon_video_viewer_num_white);
            viewerNumDrawable.setBounds(0,0, DataProcessUtil.dip2px(listView.getContext(), 12), DataProcessUtil.dip2px(listView.getContext(), 12));
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

            viewHolder.live_img.setTag(dynamicModel.getLiveImg());
            BitmapDrawable a = setImageFormWeb(dynamicModel.getLiveImg());
            if(a != null) viewHolder.live_img.setImageDrawable(a);
        }
        else if(dm.getCardType() == 4300)
        {
            ViewHolder.ViewHolderFavor viewHolder = (ViewHolder.ViewHolderFavor) vh;
            DynamicModel.DynamicFavorModel dynamicModel = (DynamicModel.DynamicFavorModel) dm;

            if(dynamicModel.isCardIsShared())
            {
                viewHolder.favor_author_name.setText(Html.fromHtml(String.format(ctx.getResources().
                        getString(R.string.dynamic_favor_author), dynamicModel.getFavorAuthorName())));
                viewHolder.favor_author_img.setTag(dynamicModel.getFavorAuthorImg());
                BitmapDrawable a = setImageFormWeb(dynamicModel.getFavorAuthorImg());
                if(a != null) viewHolder.favor_author_img.setImageDrawable(a);
                viewHolder.dynamic_like_lay.setVisibility(View.VISIBLE);
            }
            else
                viewHolder.favor_author_lay.setVisibility(View.GONE);

            viewHolder.favor_title.setText(dynamicModel.getFavorTitle());
            viewHolder.favor_desc.setText(String.format(ctx.getResources().getString(R.string.dynamic_favor_desc), dynamicModel.getFavorCount()));

            viewHolder.favor_img.setTag(dynamicModel.getFavorImg());
            BitmapDrawable a = setImageFormWeb(dynamicModel.getFavorImg());
            if(a != null) viewHolder.favor_img.setImageDrawable(a);
        }

        if(!isShared)
        {
            vh.dynamic_head.setImageResource(R.drawable.img_default_head);
            vh.dynamic_name.setText(dm.getCardAuthorName());
            vh.dynamic_time.setText(dm.getCardTime());
            if(dm.getCardAuthorOfficial() == 0)
            {
                vh.dynamic_off_1.setVisibility(View.VISIBLE);
                vh.dynamic_off_2.setVisibility(View.GONE);
            }
            else if(dm.getCardAuthorOfficial() == 1)
            {
                vh.dynamic_off_1.setVisibility(View.GONE);
                vh.dynamic_off_2.setVisibility(View.VISIBLE);
            }
            else
            {
                vh.dynamic_off_1.setVisibility(View.GONE);
                vh.dynamic_off_2.setVisibility(View.GONE);
            }

            if(dm.getCardAuthorVipStatus() == 1 && dm.getCardAuthorVipType() == 2)
                vh.dynamic_name.setTextColor(ctx.getResources().getColor(R.color.mainColor));
            else
                vh.dynamic_name.setTextColor(ctx.getResources().getColor(android.R.color.black));

            vh.dynamic_share_text.setText(dm.getCardShareNum());
            vh.dynamic_reply_text.setText(dm.getCardReplyNum());
            vh.dynamic_like_text.setText(DataProcessUtil.getView(dm.getCardLikeNum()));

            if(dm.isCardUserLike())
                vh.dynamic_like_img.setImageResource(R.drawable.icon_like_yes);
            else
                vh.dynamic_like_img.setImageResource(R.drawable.icon_like);

            vh.dynamic_head.setTag(dm.getCardAuthorImg());
            BitmapDrawable h = setImageFormWeb(dm.getCardAuthorImg());
            if(h != null) vh.dynamic_head.setImageDrawable(h);

            vh.dynamic_head.setOnClickListener(onViewClick(position));
            vh.dynamic_share_lay.setOnClickListener(onViewClick(position));
            vh.dynamic_reply_lay.setOnClickListener(onViewClick(position));
            vh.dynamic_like_lay.setOnClickListener(onViewClick(position));
        }
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

    private View.OnClickListener onViewClick(final int position)
    {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dynamicAdapterListener.onClick(v.getId(), position);
            }
        };
    }

    @ToString
    class ViewHolder
    {
        int type;
        RelativeLayout dynamic_lay;
        CircleImageView dynamic_head;
        ImageView dynamic_off_1;
        ImageView dynamic_off_2;
        TextView dynamic_name;
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
            dynamic_head = view.findViewById(R.id.item_dynamic_head);
            dynamic_off_1 = view.findViewById(R.id.item_dynamic_off_1);
            dynamic_off_2 = view.findViewById(R.id.item_dynamic_off_2);
            dynamic_name = view.findViewById(R.id.item_dynamic_name);
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

        class ViewHolderShare extends ViewHolder
        {
            TextView share_dynamic;
            LinearLayout share_share;
            ViewHolderShare(View view, int type)
            {
                super(view, type);
                share_dynamic = view.findViewById(R.id.dynamic_share_dynamic);
                share_share = view.findViewById(R.id.dynamic_share_share);
            }
        }

        class ViewHolderText extends ViewHolder
        {
            TextView text_author;
            TextView text_text;
            ViewHolderText(View view, int type)
            {
                super(view, type);
                text_author = view.findViewById(R.id.dynamic_text_author);
                text_text = view.findViewById(R.id.dynamic_text_text);
            }
        }

        class ViewHolderAlbum extends ViewHolder
        {
            TextView album_author;
            TextView album_text;
            RecyclerView album_img;
            ViewHolderAlbum(View view, int type)
            {
                super(view, type);
                album_author = view.findViewById(R.id.dynamic_album_author);
                album_text = view.findViewById(R.id.dynamic_album_text);
                album_img = view.findViewById(R.id.dynamic_album_img_lay);
            }
        }

        class ViewHolderVideo extends ViewHolder
        {
            LinearLayout video_author_lay;
            CircleImageView video_author_img;
            TextView video_author_name;
            TextView video_dynamic;
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

        class ViewHolderArticle extends ViewHolder
        {
            LinearLayout article_author_lay;
            CircleImageView article_author_img;
            TextView article_author_name;
            TextView article_dynamic;
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

        class ViewHolderBangumi extends ViewHolder
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

        class ViewHolderUrl extends ViewHolder
        {
            TextView url_author;
            TextView url_dynamic;
            ImageView url_img;
            TextView url_title;
            TextView url_desc;
            ViewHolderUrl(View view, int type)
            {
                super(view, type);
                url_author = view.findViewById(R.id.dynamic_url_author);
                url_dynamic = view.findViewById(R.id.dynamic_url_dynamic);
                url_img = view.findViewById(R.id.dynamic_url_img);
                url_title = view.findViewById(R.id.dynamic_url_title);
                url_desc = view.findViewById(R.id.dynamic_url_desc);
            }
        }

        class ViewHolderLive extends ViewHolder
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

        class ViewHolderFavor extends ViewHolder
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

        class ViewHolderUnknown extends ViewHolder
        {
            ViewHolderUnknown(View view, int type)
            {
                super(view, type);
            }
        }
    }


    public interface DynamicAdapterListener
    {
        void onClick(int viewId, int position);
    }
}
