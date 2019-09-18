package cn.luern0313.wristbilibili.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.util.LruCache;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import cn.carbs.android.expandabletextview.library.ExpandableTextView;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.SendDynamicApi;
import cn.luern0313.wristbilibili.api.UserDynamicApi;
import cn.luern0313.wristbilibili.ui.CheckreplyActivity;
import cn.luern0313.wristbilibili.ui.ImgActivity;
import cn.luern0313.wristbilibili.ui.MainActivity;
import cn.luern0313.wristbilibili.ui.OtheruserActivity;
import cn.luern0313.wristbilibili.ui.SendDynamicActivity;
import cn.luern0313.wristbilibili.ui.VideodetailsActivity;
import cn.luern0313.wristbilibili.widget.ImageDownloader;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

/**
 * 被 luern0313 创建于 不知道什么时候.
 * 收藏的fragment
 * 畜生！你收藏了甚么！
 */

public class Dynamic extends Fragment
{
    Context ctx;

    UserDynamicApi userDynamicApi;
    SendDynamicApi sendDynamicApi;
    ArrayList<Object> dynamicList;

    View rootLayout;
    ListView dyListView;
    WaveSwipeRefreshLayout waveSwipeRefreshLayout;
    View sendDynamicView;
    Button sendDynamicButton;
    View loadingView;
    mAdapter adapter;

    Handler handler = new Handler();
    Runnable runnableUi;
    Runnable runnableNoWeb;
    Runnable runnableNoWebH;
    Runnable runnableAddlist;
    Runnable runnableNodata;

    boolean isLoading = true;
    public static boolean isLogin = false;

    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_dynamic, container, false);
        dyListView = rootLayout.findViewById(R.id.dy_listview);
        loadingView = inflater.inflate(R.layout.widget_dy_loading, null);
        sendDynamicView = inflater.inflate(R.layout.widget_dy_senddynamic, null);
        sendDynamicButton = sendDynamicView.findViewById(R.id.wid_dy_senddynamic);
        dyListView.addHeaderView(sendDynamicView);
        dyListView.addFooterView(loadingView);
        dyListView.setHeaderDividersEnabled(false);

        isLogin = MainActivity.sharedPreferences.contains("cookies");

        sendDynamicApi = new SendDynamicApi(MainActivity.sharedPreferences.getString("cookies", ""),
                MainActivity.sharedPreferences.getString("mid", ""),
                MainActivity.sharedPreferences.getString("csrf", ""));

        runnableUi = new Runnable()
        {
            @Override
            public void run()
            {
                rootLayout.findViewById(R.id.dy_noweb).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.dy_nologin).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.dy_nonthing).setVisibility(View.GONE);
                dyListView.setVisibility(View.VISIBLE);

                waveSwipeRefreshLayout.setRefreshing(false);
                adapter = new mAdapter(inflater, dynamicList);
                dyListView.setAdapter(adapter);
            }
        };

        runnableNoWeb = new Runnable()
        {
            @Override
            public void run()
            {
                waveSwipeRefreshLayout.setRefreshing(false);
                rootLayout.findViewById(R.id.dy_noweb).setVisibility(View.VISIBLE);
                rootLayout.findViewById(R.id.dy_nologin).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.dy_nonthing).setVisibility(View.GONE);
                dyListView.setVisibility(View.GONE);
            }
        };

        runnableNoWebH = new Runnable()
        {
            @Override
            public void run()
            {
                ((TextView) loadingView.findViewById(R.id.wid_dy_load_button)).setText("好像没有网络...\n检查下网络？");
                loadingView.findViewById(R.id.wid_dy_load_button).setVisibility(View.VISIBLE);
                isLoading = false;
            }
        };

        runnableNodata = new Runnable()
        {
            @Override
            public void run()
            {
                rootLayout.findViewById(R.id.dy_noweb).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.dy_nologin).setVisibility(View.VISIBLE);
                rootLayout.findViewById(R.id.dy_nonthing).setVisibility(View.GONE);
                dyListView.setVisibility(View.GONE);
                waveSwipeRefreshLayout.setRefreshing(false);
            }
        };

        runnableAddlist = new Runnable()
        {
            @Override
            public void run()
            {
                adapter.notifyDataSetChanged();
            }
        };

        waveSwipeRefreshLayout = rootLayout.findViewById(R.id.dy_swipe);
        waveSwipeRefreshLayout.setColorSchemeColors(Color.WHITE, Color.WHITE);
        waveSwipeRefreshLayout.setWaveColor(Color.argb(255, 250, 114, 152));
        waveSwipeRefreshLayout.setOnRefreshListener(new WaveSwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                handler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if(isLogin)
                        {
                            dyListView.setVisibility(View.GONE);
                            getDynamic();
                        }
                        else waveSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });

        loadingView.findViewById(R.id.wid_dy_load_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((TextView) loadingView.findViewById(R.id.wid_dy_load_button)).setText(" 加载中. . .");
                loadingView.findViewById(R.id.wid_dy_load_button).setVisibility(View.GONE);
                getMoreDynamic();
            }
        });

        dyListView.setOnScrollListener(new AbsListView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState)
            {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
            {
                if(visibleItemCount + firstVisibleItem == totalItemCount && !isLoading && isLogin)
                {
                    isLoading = true;
                    getMoreDynamic();
                }
            }
        });

        sendDynamicButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(ctx, SendDynamicActivity.class);
                intent.putExtra("is_share", false);
                startActivityForResult(intent, 0);
            }
        });

        if(isLogin)
        {
            waveSwipeRefreshLayout.setRefreshing(true);
            getDynamic();
        }
        else
        {
            rootLayout.findViewById(R.id.dy_noweb).setVisibility(View.GONE);
            rootLayout.findViewById(R.id.dy_nologin).setVisibility(View.VISIBLE);
            rootLayout.findViewById(R.id.dy_nonthing).setVisibility(View.GONE);
            dyListView.setVisibility(View.GONE);
        }

        return rootLayout;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        //dyid都是传过去再传回来
        //我王境泽传数据就是乱死！也不建多余的变量！（没有真香）
        if(requestCode == 0 && resultCode == 0 && data != null)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(!data.getStringExtra("text").equals(""))
                        {
                            String result;
                            if(!data.getBooleanExtra("is_share", false))
                                result = sendDynamicApi.sendDynamic(data.getStringExtra("text"));
                            else
                                result = sendDynamicApi.sendDynamicWithDynamic(data.getStringExtra("share_dyid"), data.getStringExtra("text"));
                            if(result.equals(""))
                            {
                                Looper.prepare();
                                Toast.makeText(ctx, "发送成功！", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                                getDynamic();
                            }
                            else
                            {
                                Looper.prepare();
                                Toast.makeText(ctx, "发送失败，" + result, Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        Looper.prepare();
                        Toast.makeText(ctx, "发送失败，请检查网络？", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }
            }).start();
        }
    }

    void getDynamic()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    userDynamicApi = new UserDynamicApi(MainActivity.sharedPreferences.getString("cookies", ""), MainActivity.sharedPreferences.getString("csrf", ""), MainActivity.sharedPreferences.getString("mid", ""), MainActivity.sharedPreferences.getString("mid", ""), true);
                    userDynamicApi.getDynamic();
                    dynamicList = userDynamicApi.getDynamicList();
                    if(dynamicList != null && dynamicList.size() != 0)
                    {
                        isLoading = false;
                        handler.post(runnableUi);
                    }
                    else
                    {
                        handler.post(runnableNodata);
                    }
                }
                catch (NullPointerException e)
                {
                    handler.post(runnableNodata);
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    handler.post(runnableNoWeb);
                    e.printStackTrace();
                }
            }
        }).start();
    }

    void getMoreDynamic()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    userDynamicApi.getHistoryDynamic();
                    dynamicList.addAll(userDynamicApi.getDynamicList());
                    isLoading = false;
                    handler.post(runnableAddlist);
                }
                catch (IOException e)
                {
                    handler.post(runnableNoWebH);
                    e.printStackTrace();
                }
            }
        }).start();
    }

    class mAdapter extends BaseAdapter
    {
        private LayoutInflater mInflater;

        private LruCache<String, BitmapDrawable> mImageCache;

        private ArrayList<Object> dyList;

        public mAdapter(LayoutInflater inflater, ArrayList<Object> dyList)
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

                viewHolderOriVid.lay.setOnClickListener(new View.OnClickListener()
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
                                String s = userDynamicApi.likeDynamic(dy.getDynamicId(), dy.isLike ? "2" : "1");
                                if(s.equals(""))
                                {
                                    dy.isLike = !dy.isLike;
                                    dy.likeDynamic(dy.isLike ? 1 : -1);
                                    handler.post(runnableAddlist);
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
                });

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

                viewHolderOriText.textimg.setOnClickListener(new View.OnClickListener()
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

                viewHolderOriText.sharei.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(ctx, SendDynamicActivity.class);
                        intent.putExtra("is_share", true);
                        intent.putExtra("share_up", dy.getUserName());
                        intent.putExtra("share_img", Integer.valueOf(dy.getTextImgCount()) == 0 ? "" : dy.getImgsSrc()[0]);
                        intent.putExtra("share_title", dy.getDynamicText());
                        intent.putExtra("share_dyid", dy.getDynamicId(2));
                        startActivityForResult(intent, 0);
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
                                String s = userDynamicApi.likeDynamic(dy.getDynamicId(2), dy.isLike ? "2" : "1");
                                if(s.equals(""))
                                {
                                    dy.isLike = !dy.isLike;
                                    dy.likeDynamic(dy.isLike ? 1 : -1);
                                    handler.post(runnableAddlist);
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
                });
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

                viewHolderUnktyp.head.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(ctx, OtheruserActivity.class);
                        intent.putExtra("mid", dy.getOwnerUid());
                        startActivity(intent);
                    }
                });
            }
            else if(type == 1) //转发视频
            {
                final UserDynamicApi.cardShareVideo dy = (UserDynamicApi.cardShareVideo) dyList.get(position);
                final UserDynamicApi.cardOriginalVideo sdy = (UserDynamicApi.cardOriginalVideo) userDynamicApi.getDynamicClass(dy.getOriginalVideo(), 1);
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

                viewHolderShaVid.slay.setOnClickListener(new View.OnClickListener()
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

                viewHolderShaVid.sharei.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(ctx, SendDynamicActivity.class);
                        intent.putExtra("is_share", true);
                        intent.putExtra("share_text", "//@" + dy.getUserName() + ":" + dy.getDynamicText());
                        intent.putExtra("share_up", sdy.getOwnerName());
                        intent.putExtra("share_img", sdy.getVideoImg());
                        intent.putExtra("share_title", sdy.getVideoTitle());
                        intent.putExtra("share_dyid", dy.getDynamicId());
                        startActivityForResult(intent, 0);
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
                                String s = userDynamicApi.likeDynamic(dy.getDynamicId(), dy.isLike ? "2" : "1");
                                if(s.equals(""))
                                {
                                    dy.isLike = !dy.isLike;
                                    dy.likeDynamic(dy.isLike ? 1 : -1);
                                    handler.post(runnableAddlist);
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
                });
            }
            else if(type == 0) //转发文字
            {
                final UserDynamicApi.cardShareText dy = (UserDynamicApi.cardShareText) dyList.get(position);
                final UserDynamicApi.cardOriginalText sdy = (UserDynamicApi.cardOriginalText) userDynamicApi.getDynamicClass(dy.getOriginalText(), 2);
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

                viewHolderShaText.stextimg.setOnClickListener(new View.OnClickListener()
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

                viewHolderShaText.sharei.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(ctx, SendDynamicActivity.class);
                        intent.putExtra("is_share", true);
                        intent.putExtra("share_text", "//@" + dy.getUserName() + ":" + dy.getDynamicText());
                        intent.putExtra("share_up", sdy.getUserName());
                        intent.putExtra("share_img", Integer.valueOf(sdy.getTextImgCount()) == 0 ? "" : sdy.getImgsSrc()[0]);
                        intent.putExtra("share_title", sdy.getDynamicText());
                        intent.putExtra("share_dyid", dy.getDynamicId());
                        startActivityForResult(intent, 0);
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
                                String s = userDynamicApi.likeDynamic(dy.getDynamicId(), dy.isLike ? "2" : "1");
                                if(s.equals(""))
                                {
                                    dy.isLike = !dy.isLike;
                                    dy.likeDynamic(dy.isLike ? 1 : -1);
                                    handler.post(runnableAddlist);
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
                });
            }
            return convertView;
        }

        BitmapDrawable setImageFormWeb(String url)
        {
            if(url != null && mImageCache.get(url) != null)
            {
                return mImageCache.get(url);
            }
            else
            {
                ImageTask it = new ImageTask();
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
                    bitmap = ImageDownloader.downloadImage(imageUrl);
                    BitmapDrawable db = new BitmapDrawable(dyListView.getResources(), bitmap);
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
                ImageView iv = dyListView.findViewWithTag(imageUrl);
                if(iv != null && result != null)
                {
                    iv.setImageDrawable(result);
                }
            }
        }
    }
}
