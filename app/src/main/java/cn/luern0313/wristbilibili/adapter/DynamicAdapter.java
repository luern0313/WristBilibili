package cn.luern0313.wristbilibili.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import cn.carbs.android.expandabletextview.library.ExpandableTextView;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.UserDynamicApi;

/**
 * 被 luern0313 创建于 2019/8/31.
 */

class DynamicAdapter extends BaseAdapter
{
    private LayoutInflater mInflater;

    private LruCache<String, BitmapDrawable> mImageCache;

    private ArrayList<Object> dyList;

    private ListView listView;

    public DynamicAdapter(LayoutInflater inflater, ArrayList<Object> dyList, ListView listView)
    {
        mInflater = inflater;
        this.dyList = dyList;

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
        DynamicAdapter.ViewHolderOriText viewHolderOriText = null;
        DynamicAdapter.ViewHolderOriVid viewHolderOriVid = null;
        DynamicAdapter.ViewHolderShaText viewHolderShaText = null;
        DynamicAdapter.ViewHolderShaVid viewHolderShaVid = null;
        DynamicAdapter.ViewHolderUnktyp viewHolderUnktyp = null;
        int type = getItemViewType(position);

        // 若无可重用的 view 则进行加载
        if(convertView == null)
        {
            switch (type)
            {
                case 4:
                    //原创视频
                    convertView = mInflater.inflate(R.layout.item_news_original_video, null);
                    viewHolderOriVid = new DynamicAdapter.ViewHolderOriVid();
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
                    viewHolderOriText = new DynamicAdapter.ViewHolderOriText();
                    convertView.setTag(viewHolderOriText);

                    viewHolderOriText.head = convertView.findViewById(R.id.liot_head);
                    viewHolderOriText.name = convertView.findViewById(R.id.liot_name);
                    viewHolderOriText.time = convertView.findViewById(R.id.liot_time);
                    viewHolderOriText.text = convertView.findViewById(R.id.liot_text);
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
                    viewHolderUnktyp = new DynamicAdapter.ViewHolderUnktyp();
                    convertView.setTag(viewHolderUnktyp);

                    viewHolderUnktyp.head = convertView.findViewById(R.id.liuk_head);
                    viewHolderUnktyp.name = convertView.findViewById(R.id.liuk_name);
                    viewHolderUnktyp.time = convertView.findViewById(R.id.liuk_time);
                    break;

                case 1:
                    //转发视频
                    convertView = mInflater.inflate(R.layout.item_news_share_video, null);
                    viewHolderShaVid = new DynamicAdapter.ViewHolderShaVid();
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
                    viewHolderShaVid.replybu = convertView.findViewById(R.id.lisv_replybu);
                    viewHolderShaVid.reply = convertView.findViewById(R.id.lisv_reply);
                    viewHolderShaVid.likebu = convertView.findViewById(R.id.lisv_likebu);
                    viewHolderShaVid.likei = convertView.findViewById(R.id.lisv_likei);
                    viewHolderShaVid.like = convertView.findViewById(R.id.lisv_like);
                    break;

                case 0:
                    //转发文字
                    convertView = mInflater.inflate(R.layout.item_news_share_text, null);
                    viewHolderShaText = new DynamicAdapter.ViewHolderShaText();
                    convertView.setTag(viewHolderShaText);

                    viewHolderShaText.head = convertView.findViewById(R.id.list_head);
                    viewHolderShaText.name = convertView.findViewById(R.id.list_name);
                    viewHolderShaText.time = convertView.findViewById(R.id.list_time);
                    viewHolderShaText.text = convertView.findViewById(R.id.list_text);
                    viewHolderShaText.shead = convertView.findViewById(R.id.list_share_head);
                    viewHolderShaText.sname = convertView.findViewById(R.id.list_share_name);
                    viewHolderShaText.stext = convertView.findViewById(R.id.list_share_text);
                    viewHolderShaText.stextimg = convertView.findViewById(R.id.list_share_textimg);
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
                    viewHolderOriVid = (DynamicAdapter.ViewHolderOriVid) convertView.getTag();
                    break;
                case 3:
                    viewHolderOriText = (DynamicAdapter.ViewHolderOriText) convertView.getTag();
                    break;
                case 2:
                    viewHolderUnktyp = (DynamicAdapter.ViewHolderUnktyp) convertView.getTag();
                    break;
                case 1:
                    viewHolderShaVid = (DynamicAdapter.ViewHolderShaVid) convertView.getTag();
                    break;
                case 0:
                    viewHolderShaText = (DynamicAdapter.ViewHolderShaText) convertView.getTag();
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
            viewHolderOriVid.imgtext.setText(dy.getVideoDuration() + "  " + dy.getVideoView() + "观看");
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

            /*viewHolderOriVid.lay.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(ctx, VideodetailsActivity.class);
                    intent.putExtra("aid", dy.getVideoAid());
                    startActivity(intent);
                }
            });

            viewHolderOriVid.head.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(ctx, OtheruserActivity.class);
                    intent.putExtra("mid", dy.getOwnerUid());
                    startActivity(intent);
                }
            });

            viewHolderOriVid.likebu.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            String s = userDynamic.likeDynamic(dy.getDynamicId(), dy.isLike ? "2" : "1");
                            if(s.equals(""))
                            {
                                dy.isLike = !dy.isLike;
                                dy.likeDynamic(dy.isLike ? 1 : -1);
                                handler.post(runnableDynamicAddlist);
                            }
                            else
                            {
                                Looper.prepare();
                                Toast.makeText(ctx, (dy.isLike ? "取消" : "点赞") + "失败：\n" + s, Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        }
                    }).start();
                }
            });*/

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

            /*viewHolderOriText.textimg.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(ctx, ImgActivity.class);
                    intent.putExtra("imgUrl", dy.getImgsSrc());
                    startActivity(intent);
                }
            });

            viewHolderOriText.head.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(ctx, OtheruserActivity.class);
                    intent.putExtra("mid", dy.getUserUid());
                    startActivity(intent);
                }
            });

            viewHolderOriText.replybu.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(ctx, CheckreplyActivity.class);
                    intent.putExtra("oid", dy.getDynamicId(1));
                    intent.putExtra("type", dy.getReplyType());
                    intent.putExtra("root", "");
                    startActivity(intent);
                }
            });

            viewHolderOriText.likebu.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            String s = userDynamic.likeDynamic(dy.getDynamicId(2), dy.isLike ? "2" : "1");
                            if(s.equals(""))
                            {
                                dy.isLike = !dy.isLike;
                                dy.likeDynamic(dy.isLike ? 1 : -1);
                                handler.post(runnableDynamicAddlist);
                            }
                            else
                            {
                                Looper.prepare();
                                Toast.makeText(ctx, (dy.isLike ? "取消" : "点赞") + "失败：\n" + s, Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        }
                    }).start();
                }
            });*/
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

            /*viewHolderUnktyp.head.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(ctx, OtheruserActivity.class);
                    intent.putExtra("mid", dy.getOwnerUid());
                    startActivity(intent);
                }
            });*/
        }
        else if(type == 1) //转发视频
        {
            final UserDynamicApi.cardShareVideo dy = (UserDynamicApi.cardShareVideo) dyList.get(position);
            final UserDynamicApi.cardOriginalVideo sdy = null; //(UserDynamicApi.cardOriginalVideo) userDynamic.getDynamicClass(dy.getOriginalVideo(), 1);
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

            /*viewHolderShaVid.slay.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(ctx, VideodetailsActivity.class);
                    intent.putExtra("aid", sdy.getVideoAid());
                    startActivity(intent);
                }
            });

            viewHolderShaVid.head.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(ctx, OtheruserActivity.class);
                    intent.putExtra("mid", dy.getUserUid());
                    startActivity(intent);
                }
            });

            convertView.findViewById(R.id.lisv_share_user).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(ctx, OtheruserActivity.class);
                    intent.putExtra("mid", sdy.getOwnerUid());
                    startActivity(intent);
                }
            });

            viewHolderShaVid.replybu.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(ctx, CheckreplyActivity.class);
                    intent.putExtra("oid", dy.getDynamicId());
                    intent.putExtra("type", dy.getReplyType());
                    intent.putExtra("root", "");
                    startActivity(intent);
                }
            });

            viewHolderShaVid.likebu.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            String s = userDynamic.likeDynamic(dy.getDynamicId(), dy.isLike ? "2" : "1");
                            if(s.equals(""))
                            {
                                dy.isLike = !dy.isLike;
                                dy.likeDynamic(dy.isLike ? 1 : -1);
                                handler.post(runnableDynamicAddlist);
                            }
                            else
                            {
                                Looper.prepare();
                                Toast.makeText(ctx, (dy.isLike ? "取消" : "点赞") + "失败：\n" + s, Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        }
                    }).start();
                }
            });*/
        }
        else if(type == 0) //转发文字
        {
            final UserDynamicApi.cardShareText dy = (UserDynamicApi.cardShareText) dyList.get(position);
            final UserDynamicApi.cardOriginalText sdy = null;//(UserDynamicApi.cardOriginalText) userDynamic.getDynamicClass(dy.getOriginalText(), 2);
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

            /*viewHolderShaText.stextimg.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(ctx, ImgActivity.class);
                    intent.putExtra("imgUrl", sdy.getImgsSrc());
                    startActivity(intent);
                }
            });

            viewHolderShaText.head.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(ctx, OtheruserActivity.class);
                    intent.putExtra("mid", dy.getUserUid());
                    startActivity(intent);
                }
            });

            convertView.findViewById(R.id.list_share_user).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(ctx, OtheruserActivity.class);
                    intent.putExtra("mid", sdy.getUserUid());
                    startActivity(intent);
                }
            });

            viewHolderShaText.replybu.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(ctx, CheckreplyActivity.class);
                    intent.putExtra("oid", dy.getDynamicId());
                    intent.putExtra("type", dy.getReplyType());
                    intent.putExtra("root", "");
                    startActivity(intent);
                }
            });

            viewHolderShaText.likebu.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            String s = userDynamic.likeDynamic(dy.getDynamicId(), dy.isLike ? "2" : "1");
                            if(s.equals(""))
                            {
                                dy.isLike = !dy.isLike;
                                dy.likeDynamic(dy.isLike ? 1 : -1);
                                handler.post(runnableDynamicAddlist);
                            }
                            else
                            {
                                Looper.prepare();
                                Toast.makeText(ctx, (dy.isLike ? "取消" : "点赞") + "失败：\n" + s, Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        }
                    }).start();
                }
            });*/
        }
        return convertView;
    }

    BitmapDrawable setImageFormWeb(String url)
    {
        if(mImageCache.get(url) != null)
        {
            return mImageCache.get(url);
        }
        else
        {
            DynamicAdapter.ImageTask it = new DynamicAdapter.ImageTask();
            it.execute(url);
            return null;
        }
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
        LinearLayout replybu;
        TextView reply;
        LinearLayout likebu;
        ImageView likei;
        TextView like;
    }

    class ImageTask extends AsyncTask<String, Void, BitmapDrawable>
    {
        private String imageUrl;

        @Override
        protected BitmapDrawable doInBackground(String... params)
        {
            try
            {
                imageUrl = params[0];
                Bitmap bitmap = null;
                bitmap = downloadImage();
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

        /**
         * 获得需要压缩的比率
         *
         * @param options 需要传入已经BitmapFactory.decodeStream(is, null, options);
         * @return 返回压缩的比率，最小为1
         */
        public int getInSampleSize(BitmapFactory.Options options) {
            int inSampleSize = 1;
            int realWith = 170;
            int realHeight = 170;

            int outWidth = options.outWidth;
            int outHeight = options.outHeight;

            //获取比率最大的那个
            if (outWidth > realWith || outHeight > realHeight) {
                int withRadio = Math.round(outWidth / realWith);
                int heightRadio = Math.round(outHeight / realHeight);
                inSampleSize = withRadio > heightRadio ? withRadio : heightRadio;
            }
            return inSampleSize;
        }

        /**
         * 根据输入流返回一个压缩的图片
         * @param input 图片的输入流
         * @return 压缩的图片
         */
        public Bitmap getCompressBitmap(InputStream input)
        {
            //因为InputStream要使用两次，但是使用一次就无效了，所以需要复制两个
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try
            {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = input.read(buffer)) > -1)
                {
                    baos.write(buffer, 0, len);
                }
                baos.flush();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            //复制新的输入流
            InputStream is = new ByteArrayInputStream(baos.toByteArray());
            InputStream is2 = new ByteArrayInputStream(baos.toByteArray());

            //只是获取网络图片的大小，并没有真正获取图片
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);
            //获取图片并进行压缩
            options.inSampleSize = getInSampleSize(options);
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeStream(is2, null, options);
        }

        /**
         * 根据url从网络上下载图片
         *
         * @return 图片
         */
        private Bitmap downloadImage() throws IOException
        {
            HttpURLConnection con = null;
            Bitmap bitmap = null;
            URL url = new URL(imageUrl);
            con = (HttpURLConnection) url.openConnection();
            if(con != null)
                con.disconnect();
            con.setConnectTimeout(5 * 1000);
            con.setReadTimeout(10 * 1000);
            bitmap = getCompressBitmap(con.getInputStream());
            return bitmap;
        }
    }
}
