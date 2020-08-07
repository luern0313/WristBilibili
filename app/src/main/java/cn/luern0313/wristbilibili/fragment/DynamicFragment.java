package cn.luern0313.wristbilibili.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

import androidx.fragment.app.Fragment;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.DynamicAdapter;
import cn.luern0313.wristbilibili.api.DynamicApi;
import cn.luern0313.wristbilibili.api.SendDynamicApi;
import cn.luern0313.wristbilibili.models.DynamicModel;
import cn.luern0313.wristbilibili.ui.BangumiActivity;
import cn.luern0313.wristbilibili.ui.SendDynamicActivity;
import cn.luern0313.wristbilibili.ui.UnsupportedLinkActivity;
import cn.luern0313.wristbilibili.ui.UserActivity;
import cn.luern0313.wristbilibili.util.ColorUtil;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

/**
 * 被 luern0313 创建于 不知道什么时候.
 */

public class DynamicFragment extends Fragment
{
    private static final String ARG_DYNAMIC_IS_SHOW_SEND_BUTTON = "argDynamicIsShowSendButton";
    private static final String ARG_DYNAMIC_MID = "argDynamicMid";

    private Context ctx;
    private boolean isShowSendButton;
    private String mid;

    private DynamicApi dynamicApi;
    private SendDynamicApi sendDynamicApi;
    private ArrayList<DynamicModel> dynamicList;

    private View rootLayout;
    private ListView dyListView;
    private WaveSwipeRefreshLayout waveSwipeRefreshLayout;
    private View sendDynamicView;
    private TextView sendDynamicButton;
    private View loadingView;
    private DynamicAdapter dynamicAdapter;
    private DynamicAdapter.DynamicAdapterListener adapterListener;

    private Handler handler = new Handler();
    private Runnable runnableUi, runnableNoWeb, runnableMore, runnableNoData, runnableMoreNoWeb, runnableMoreNoData;

    private boolean isLoading = true;
    public static boolean isLogin = false;
    private int dynamicWidth;

    final private int RESULT_DYNAMIC_SEND_DYNAMIC = 101;
    final private int RESULT_DYNAMIC_SHARE = 102;

    public DynamicFragment() {}

    public static DynamicFragment newInstance(boolean isShowSendButton, String mid)
    {
        DynamicFragment fragment = new DynamicFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_DYNAMIC_IS_SHOW_SEND_BUTTON, isShowSendButton);
        args.putString(ARG_DYNAMIC_MID, mid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
        {
            isShowSendButton = getArguments().getBoolean(ARG_DYNAMIC_IS_SHOW_SEND_BUTTON);
            mid = getArguments().getString(ARG_DYNAMIC_MID);
        }
    }

    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_dynamic, container, false);

        dyListView = rootLayout.findViewById(R.id.dy_listview);
        loadingView = inflater.inflate(R.layout.widget_loading, null);
        sendDynamicView = inflater.inflate(R.layout.widget_dy_senddynamic, null);
        sendDynamicButton = sendDynamicView.findViewById(R.id.wid_dy_senddynamic);

        WindowManager manager = getActivity().getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        dynamicWidth = outMetrics.widthPixels - DataProcessUtil.dip2px(18) * 2;

        if(isShowSendButton)
            dyListView.addHeaderView(sendDynamicView);
        dyListView.addFooterView(loadingView);
        dyListView.setHeaderDividersEnabled(false);

        isLogin = SharedPreferencesUtil.contains("cookies");

        sendDynamicApi = new SendDynamicApi();

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
                dynamicAdapter = new DynamicAdapter(inflater, dynamicList, dyListView, dynamicWidth, adapterListener);
                dyListView.setAdapter(dynamicAdapter);
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

        runnableMoreNoWeb = new Runnable()
        {
            @Override
            public void run()
            {
                ((TextView) loadingView.findViewById(R.id.wid_load_button)).setText("好像没有网络...\n检查下网络？");
                loadingView.findViewById(R.id.wid_load_button).setVisibility(View.VISIBLE);
                isLoading = false;
            }
        };

        runnableNoData = new Runnable()
        {
            @Override
            public void run()
            {
                rootLayout.findViewById(R.id.dy_noweb).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.dy_nologin).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.dy_nonthing).setVisibility(View.VISIBLE);
                dyListView.setVisibility(View.GONE);
                waveSwipeRefreshLayout.setRefreshing(false);
            }
        };

        runnableMoreNoWeb = new Runnable()
        {
            @Override
            public void run()
            {
                isLoading = false;
                ((TextView) loadingView.findViewById(R.id.wid_load_button)).setText("好像没有网络...\n检查下网络？");
                loadingView.findViewById(R.id.wid_load_button).setVisibility(View.VISIBLE);
            }
        };

        runnableMoreNoData = new Runnable()
        {
            @Override
            public void run()
            {
                ((TextView) loadingView.findViewById(R.id.wid_load_text)).setText("  没有更多了...");
            }
        };

        loadingView.findViewById(R.id.wid_load_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((TextView) loadingView.findViewById(R.id.wid_load_button)).setText("  加载中...");
                loadingView.findViewById(R.id.wid_load_button).setVisibility(View.GONE);
                getMoreDynamic();
            }
        });

        runnableMore = new Runnable()
        {
            @Override
            public void run()
            {
                dynamicAdapter.notifyDataSetChanged();
            }
        };

        waveSwipeRefreshLayout = rootLayout.findViewById(R.id.dy_swipe);
        waveSwipeRefreshLayout.setColorSchemeColors(Color.WHITE, Color.WHITE);
        //noinspection ConstantConditions
        waveSwipeRefreshLayout.setWaveColor(ColorUtil.getColor(R.attr.colorPrimary, getContext()));
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
                        isLogin = SharedPreferencesUtil.contains(SharedPreferencesUtil.cookies);
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
                ((TextView) loadingView.findViewById(R.id.wid_load_button)).setText("  加载中...");
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
                startActivityForResult(intent, RESULT_DYNAMIC_SEND_DYNAMIC);
            }
        });

        adapterListener = new DynamicAdapter.DynamicAdapterListener()
        {
            @Override
            public void onClick(int viewId, int position, boolean isShared)
            {
                onViewClick(viewId, position, isShared);
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

    private void getDynamic()
    {
        isLoading = true;
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    dynamicApi = new DynamicApi(mid, isShowSendButton);
                    dynamicApi.getDynamic();
                    dynamicList = dynamicApi.getDynamicList();
                    if(dynamicList != null && dynamicList.size() != 0)
                    {
                        isLoading = false;
                        handler.post(runnableUi);
                    }
                    else
                    {
                        handler.post(runnableNoData);
                    }
                }
                catch (NullPointerException e)
                {
                    handler.post(runnableNoData);
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

    private void getMoreDynamic()
    {
        isLoading = true;
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    dynamicApi.getHistoryDynamic();
                    dynamicList.addAll(dynamicApi.getDynamicList());
                    isLoading = false;
                    handler.post(runnableMore);
                }
                catch (IOException e)
                {
                    handler.post(runnableMoreNoWeb);
                    e.printStackTrace();
                }
                catch (JSONException e)
                {
                    handler.post(runnableMoreNoData);
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void onViewClick(int viewId, int position, boolean isShared)
    {
        final DynamicModel dynamicModel = dynamicList.get(position);
        if(viewId == R.id.item_dynamic_author_lay)
        {
            if(dynamicModel.getCardType() != 512 && dynamicModel.getCardType() != 4098 && dynamicModel.getCardType() != 4099 && dynamicModel.getCardType() != 4101)
            {
                Intent intent = new Intent(ctx, UserActivity.class);
                intent.putExtra("mid", dynamicModel.getCardAuthorUid());
                startActivity(intent);
            }
            else
            {
                Intent intent = new Intent(ctx, BangumiActivity.class);
                intent.putExtra("season_id", ((DynamicModel.DynamicBangumiModel) dynamicModel).getBangumiSeasonId());
                startActivity(intent);
            }
        }
        else if(viewId == R.id.item_dynamic_share_lay)
        {
            Intent intent = new Intent(ctx, SendDynamicActivity.class);
            intent.putExtra("is_share", true);
            intent.putExtra("share_up", dynamicModel.getCardAuthorName());
            intent.putExtra("share_id", dynamicModel.getCardId());
            getShareIntent(intent, dynamicModel);
            startActivityForResult(intent, RESULT_DYNAMIC_SHARE);
        }
        else if(viewId == R.id.item_dynamic_reply_lay)
        {
            Intent intent = new Intent(ctx, UnsupportedLinkActivity.class);
            intent.putExtra("url", dynamicModel.getCardUrl() + "?page=1");
            startActivity(intent);
        }
        else if(viewId == R.id.item_dynamic_like_lay)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        String result = dynamicApi.likeDynamic(dynamicModel.getCardId(), dynamicModel.isCardUserLike() ? "2" : "1");
                        if(result.equals(""))
                        {
                            dynamicModel.setCardUserLike(!dynamicModel.isCardUserLike());
                            dynamicModel.setCardLikeNum(dynamicModel.getCardLikeNum() + 1);
                            handler.post(runnableMore);
                        }
                        else
                        {
                            Looper.prepare();
                            Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        Looper.prepare();
                        Toast.makeText(ctx, "操作失败，请检查网络...", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }
            }).start();
        }
        else if(viewId == R.id.item_dynamic_lay)
        {
            Intent intent = new Intent(ctx, UnsupportedLinkActivity.class);
            intent.putExtra("url", dynamicModel.getCardUrl());
            startActivity(intent);
        }
        else if(viewId == R.id.dynamic_share_share)
        {
            Intent intent = new Intent(ctx, UnsupportedLinkActivity.class);
            intent.putExtra("url", ((DynamicModel.DynamicShareModel) dynamicModel).getShareOriginCard().getCardUrl());
            startActivity(intent);
        }
        else if(viewId == R.id.dynamic_album_author)
        {
            Intent intent = new Intent(ctx, UserActivity.class);
            intent.putExtra("mid", ((DynamicModel.DynamicAlbumModel) ((DynamicModel.DynamicShareModel) dynamicModel).getShareOriginCard()).getAlbumAuthorUid());
            startActivity(intent);
        }
        else if(viewId == R.id.dynamic_text_author)
        {
            Intent intent = new Intent(ctx, UserActivity.class);
            intent.putExtra("mid", ((DynamicModel.DynamicTextModel) ((DynamicModel.DynamicShareModel) dynamicModel).getShareOriginCard()).getTextAuthorUid());
            startActivity(intent);
        }
        else if(viewId == R.id.dynamic_video_author)
        {
            Intent intent = new Intent(ctx, UserActivity.class);
            intent.putExtra("mid", ((DynamicModel.DynamicVideoModel) ((DynamicModel.DynamicShareModel) dynamicModel).getShareOriginCard()).getVideoAuthorUid());
            startActivity(intent);
        }
        else if(viewId == R.id.dynamic_article_author)
        {
            Intent intent = new Intent(ctx, UserActivity.class);
            intent.putExtra("mid", ((DynamicModel.DynamicArticleModel) ((DynamicModel.DynamicShareModel) dynamicModel).getShareOriginCard()).getArticleAuthorUid());
            startActivity(intent);
        }
        else if(viewId == R.id.dynamic_url_author)
        {
            Intent intent = new Intent(ctx, UserActivity.class);
            intent.putExtra("mid", ((DynamicModel.DynamicUrlModel) ((DynamicModel.DynamicShareModel) dynamicModel).getShareOriginCard()).getUrlAuthorUid());
            startActivity(intent);
        }
        else if(viewId == R.id.dynamic_url_url)
        {
            if(!isShared)
            {
                Intent intent = new Intent(ctx, UnsupportedLinkActivity.class);
                intent.putExtra("url", ((DynamicModel.DynamicUrlModel) dynamicModel).getUrlUrl());
                startActivity(intent);
            }
            else
            {
                Intent intent = new Intent(ctx, UnsupportedLinkActivity.class);
                intent.putExtra("url", ((DynamicModel.DynamicUrlModel) ((DynamicModel.DynamicShareModel) dynamicModel).getShareOriginCard()).getUrlUrl());
                startActivity(intent);
            }
        }
        else if(viewId == R.id.dynamic_live_author)
        {
            Intent intent = new Intent(ctx, UserActivity.class);
            intent.putExtra("mid", ((DynamicModel.DynamicLiveModel) ((DynamicModel.DynamicShareModel) dynamicModel).getShareOriginCard()).getLiveAuthorUid());
            startActivity(intent);
        }
        else if(viewId == R.id.dynamic_favor_author)
        {
            Intent intent = new Intent(ctx, UserActivity.class);
            intent.putExtra("mid", ((DynamicModel.DynamicFavorModel) ((DynamicModel.DynamicShareModel) dynamicModel).getShareOriginCard()).getFavorAuthorUid());
            startActivity(intent);
        }
    }

    @Override
    public void onActivityResult(final int requestCode, int resultCode, final Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        //dyid都是传过去再传回来
        //我王境泽传数据就是乱死！也不建多余的变量！（没有真香）
        if(resultCode != 0) return;
        if(requestCode == RESULT_DYNAMIC_SEND_DYNAMIC)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        String result = sendDynamicApi.sendDynamic(data.getStringExtra("text"));
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
                            Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        Looper.prepare();
                        Toast.makeText(ctx, "发送失败，请检查网络...", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }
            }).start();
        }
        else if(requestCode == RESULT_DYNAMIC_SHARE)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        String result = sendDynamicApi.sendDynamicWithDynamic(data.getStringExtra("share_id"), data.getStringExtra("text"));
                        if(result.equals(""))
                        {
                            Looper.prepare();
                            Toast.makeText(ctx, "转发成功！", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                        else
                        {
                            Looper.prepare();
                            Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        Looper.prepare();
                        Toast.makeText(ctx, "转发失败，请检查网络...", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }
            }).start();
        }
    }

    private void getShareIntent(Intent intent, DynamicModel dynamicModel)
    {
        switch (dynamicModel.getCardType())
        {
            case 1:
            {
                DynamicModel.DynamicShareModel dm = (DynamicModel.DynamicShareModel) dynamicModel;
                intent.putExtra("share_text", "//@" + dm.getCardAuthorName() + ":" + dm.getShareTextOrg());
                getShareIntent(intent, dm.getShareOriginCard());
                break;
            }
            case 2:
            {
                DynamicModel.DynamicAlbumModel dm = (DynamicModel.DynamicAlbumModel) dynamicModel;
                if(dm.getAlbumImg().size() > 0)
                    intent.putExtra("share_img", dm.getAlbumImg().get(0));
                intent.putExtra("share_title", dm.getAlbumTextOrg());
                break;
            }
            case 4:
            {
                DynamicModel.DynamicTextModel dm = (DynamicModel.DynamicTextModel) dynamicModel;
                intent.putExtra("share_title", dm.getTextTextOrg());
                break;
            }
            case 8:
            {
                DynamicModel.DynamicVideoModel dm = (DynamicModel.DynamicVideoModel) dynamicModel;
                intent.putExtra("share_img", dm.getVideoImg());
                intent.putExtra("share_title", dm.getVideoTitle());
                break;
            }
            case 64:
            {
                DynamicModel.DynamicArticleModel dm = (DynamicModel.DynamicArticleModel) dynamicModel;
                intent.putExtra("share_img", dm.getArticleImg());
                intent.putExtra("share_title", dm.getArticleTitle());
                break;
            }
            case 512:
            case 4098:
            case 4099:
            case 4101:
            {
                DynamicModel.DynamicBangumiModel dm = (DynamicModel.DynamicBangumiModel) dynamicModel;
                intent.putExtra("share_img", dm.getBangumiImg());
                intent.putExtra("share_title", dm.getBangumiTitle());
                break;
            }
            case 2048:
            {
                DynamicModel.DynamicUrlModel dm = (DynamicModel.DynamicUrlModel) dynamicModel;
                intent.putExtra("share_img", dm.getUrlImg());
                intent.putExtra("share_title", dm.getUrlDynamicOrg());
                break;
            }
            case 4200:
            {
                DynamicModel.DynamicLiveModel dm = (DynamicModel.DynamicLiveModel) dynamicModel;
                intent.putExtra("share_img", dm.getLiveImg());
                intent.putExtra("share_title", dm.getLiveTitle());
                break;
            }
            case 4300:
            {
                DynamicModel.DynamicFavorModel dm = (DynamicModel.DynamicFavorModel) dynamicModel;
                intent.putExtra("share_img", dm.getFavorImg());
                intent.putExtra("share_title", dm.getFavorTitle());
                break;
            }
        }
    }
}
