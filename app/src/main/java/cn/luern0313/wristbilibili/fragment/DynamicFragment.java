package cn.luern0313.wristbilibili.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.DynamicAdapter;
import cn.luern0313.wristbilibili.api.SendDynamicApi;
import cn.luern0313.wristbilibili.api.UserDynamicApi;
import cn.luern0313.wristbilibili.ui.CheckreplyActivity;
import cn.luern0313.wristbilibili.ui.ImgActivity;
import cn.luern0313.wristbilibili.ui.MainActivity;
import cn.luern0313.wristbilibili.ui.OtherUserActivity;
import cn.luern0313.wristbilibili.ui.SendDynamicActivity;
import cn.luern0313.wristbilibili.ui.VideodetailsActivity;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

/**
 * 被 luern0313 创建于 不知道什么时候.
 */

public class DynamicFragment extends Fragment
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
    DynamicAdapter adapter;
    DynamicAdapter.DynamicAdapterListener adapterListener;

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
        loadingView = inflater.inflate(R.layout.widget_loading, null);
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
                adapter = new DynamicAdapter(inflater, dynamicList, dyListView, adapterListener);
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
                ((TextView) loadingView.findViewById(R.id.wid_load_button)).setText("好像没有网络...\n检查下网络？");
                loadingView.findViewById(R.id.wid_load_button).setVisibility(View.VISIBLE);
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

        loadingView.findViewById(R.id.wid_load_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((TextView) loadingView.findViewById(R.id.wid_load_button)).setText(" 加载中. . .");
                loadingView.findViewById(R.id.wid_load_button).setVisibility(View.GONE);
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

        adapterListener = new DynamicAdapter.DynamicAdapterListener()
        {
            @Override
            public void onClick(int viewId, int position, int mode)
            {
                onViewClick(viewId, position, mode);
            }
        };

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
                    userDynamicApi = new UserDynamicApi(MainActivity.sharedPreferences.getString("cookies", ""),
                                                        MainActivity.sharedPreferences.getString("csrf", ""),
                                                        MainActivity.sharedPreferences.getString("mid", ""),
                                                        MainActivity.sharedPreferences.getString("mid", ""), true);
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

    void onViewClick(int id, int position, int mode)
    {
        if(mode == 4)
        {
            final UserDynamicApi.cardOriginalVideo dy = (UserDynamicApi.cardOriginalVideo) dynamicList.get(position);
            if(id == R.id.liov_lay)
            {
                Intent intent = new Intent(ctx, VideodetailsActivity.class);
                intent.putExtra("aid", dy.getVideoAid());
                startActivity(intent);
            }
            else if(id == R.id.liov_head)
            {
                Intent intent = new Intent(ctx, OtherUserActivity.class);
                intent.putExtra("mid", dy.getOwnerUid());
                startActivity(intent);
            }
            else if(id == R.id.liov_likebu)
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
        }
        else if(mode == 3)
        {
            final UserDynamicApi.cardOriginalText dy = (UserDynamicApi.cardOriginalText) dynamicList.get(position);
            if(id == R.id.liot_textimg)
            {
                Intent intent = new Intent(ctx, ImgActivity.class);
                intent.putExtra("imgUrl", dy.getImgsSrc());
                startActivity(intent);
            }
            else if(id == R.id.liot_head)
            {
                Intent intent = new Intent(ctx, OtherUserActivity.class);
                intent.putExtra("mid", dy.getUserUid());
                startActivity(intent);
            }
            else if(id == R.id.liot_sharei)
            {
                Intent intent = new Intent(ctx, SendDynamicActivity.class);
                intent.putExtra("is_share", true);
                intent.putExtra("share_up", dy.getUserName());
                intent.putExtra("share_img", Integer.valueOf(dy.getTextImgCount()) == 0 ? "" : dy.getImgsSrc()[0]);
                intent.putExtra("share_title", dy.getDynamicText());
                intent.putExtra("share_dyid", dy.getDynamicId(2));
                startActivityForResult(intent, 0);
            }
            else if(id == R.id.liot_replybu)
            {
                Intent intent = new Intent(ctx, CheckreplyActivity.class);
                intent.putExtra("oid", dy.getDynamicId(1));
                intent.putExtra("type", dy.getReplyType());
                intent.putExtra("root", "");
                startActivity(intent);
            }
            else if(id == R.id.liot_likebu)
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
        }
        else if(mode == 2)
        {
            final UserDynamicApi.cardUnknow dy = (UserDynamicApi.cardUnknow) dynamicList.get(position);
            if(id == R.id.liuk_head)
            {
                Intent intent = new Intent(ctx, OtherUserActivity.class);
                intent.putExtra("mid", dy.getOwnerUid());
                startActivity(intent);
            }
        }
        else if(mode == 1)
        {
            final UserDynamicApi.cardShareVideo dy = (UserDynamicApi.cardShareVideo) dynamicList.get(position);
            final UserDynamicApi.cardOriginalVideo sdy = dy.getOriginalVideo();
            if(id == R.id.lisv_share_lay)
            {
                Intent intent = new Intent(ctx, VideodetailsActivity.class);
                intent.putExtra("aid", sdy.getVideoAid());
                startActivity(intent);
            }
            else if(id == R.id.lisv_head)
            {
                Intent intent = new Intent(ctx, OtherUserActivity.class);
                intent.putExtra("mid", dy.getUserUid());
                startActivity(intent);
            }
            else if(id == R.id.lisv_share_user)
            {
                Intent intent = new Intent(ctx, OtherUserActivity.class);
                intent.putExtra("mid", sdy.getOwnerUid());
                startActivity(intent);
            }
            else if(id == R.id.lisv_sharei)
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
            else if(id == R.id.lisv_replybu)
            {
                Intent intent = new Intent(ctx, CheckreplyActivity.class);
                intent.putExtra("oid", dy.getDynamicId());
                intent.putExtra("type", dy.getReplyType());
                intent.putExtra("root", "");
                startActivity(intent);
            }
            else if(id == R.id.lisv_likebu)
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
        }
        else if(mode == 0)
        {
            final UserDynamicApi.cardShareText dy = (UserDynamicApi.cardShareText) dynamicList.get(position);
            final UserDynamicApi.cardOriginalText sdy = dy.getOriginalText();
            if(id == R.id.list_share_textimg)
            {
                Intent intent = new Intent(ctx, ImgActivity.class);
                intent.putExtra("imgUrl", sdy.getImgsSrc());
                startActivity(intent);
            }
            else if(id == R.id.list_head)
            {
                Intent intent = new Intent(ctx, OtherUserActivity.class);
                intent.putExtra("mid", dy.getUserUid());
                startActivity(intent);
            }
            else if(id == R.id.list_share_user)
            {
                Intent intent = new Intent(ctx, OtherUserActivity.class);
                intent.putExtra("mid", sdy.getUserUid());
                startActivity(intent);
            }
            else if(id == R.id.list_sharei)
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
            else if(id == R.id.list_replybu)
            {
                Intent intent = new Intent(ctx, CheckreplyActivity.class);
                intent.putExtra("oid", dy.getDynamicId());
                intent.putExtra("type", dy.getReplyType());
                intent.putExtra("root", "");
                startActivity(intent);
            }
            else if(id == R.id.list_likebu)
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
        }
    }
}
