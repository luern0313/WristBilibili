package cn.luern0313.wristbilibili.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.ReplyAdapter;
import cn.luern0313.wristbilibili.adapter.TailReplyAdapter;
import cn.luern0313.wristbilibili.api.ReplyApi;
import cn.luern0313.wristbilibili.models.ReplyModel;
import cn.luern0313.wristbilibili.ui.BangumiActivity;
import cn.luern0313.wristbilibili.ui.CheckreplyActivity;
import cn.luern0313.wristbilibili.ui.ReplyActivity;
import cn.luern0313.wristbilibili.ui.SelectPartActivity;
import cn.luern0313.wristbilibili.ui.TailActivity;
import cn.luern0313.wristbilibili.ui.UserActivity;
import cn.luern0313.wristbilibili.util.ColorUtil;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;
import cn.luern0313.wristbilibili.util.ViewScrollListener;
import cn.luern0313.wristbilibili.util.ViewTouchListener;
import cn.luern0313.wristbilibili.widget.ExceptionHandlerView;
import cn.luern0313.wristbilibili.widget.TitleView;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

/**
 * 被 luern0313 创建于 2020/3/7.
 */
public class ReplyFragment extends Fragment implements ViewScrollListener.CustomScrollResult
{
    private static final String ARG_TAIL_MODE = "tailModeArg";
    private static final String ARG_OID = "oidArg";
    private static final String ARG_TYPE = "typeArg";
    private static final String ARG_ROOT = "rootArg";
    private static final String ARG_POSITION = "positionArg";
    private final int RESULT_SEND = 101;
    private final int RESULT_VIEW = 102;
    private final int RESULT_REPORT = 103;

    private Context ctx;
    private View rootLayout;
    private Intent resultIntent;

    private int tailMode;
    private String oid, type, sort;
    private ReplyModel root;
    private int position;
    private ReplyApi replyApi;

    private ExceptionHandlerView exceptionHandlerView;
    private ListView listView;
    private WaveSwipeRefreshLayout waveSwipeRefreshLayout;
    private View layoutLoading;

    private ReplyAdapter replyAdapter;
    private ReplyAdapter.ReplyAdapterListener replyAdapterListener;
    private TitleView.TitleViewListener titleViewListener;

    private final Handler handler = new Handler();
    private Runnable runnableUi, runnableMoreErr, runnableUpdate;

    private int replyPage = 1;
    private boolean isReplyLoading = true;
    private int replyWidth;

    public static Fragment newInstance(String oid, String type, ReplyModel root, int position)
    {
        ReplyFragment fragment = new ReplyFragment();
        Bundle args = new Bundle();
        args.putString(ARG_OID, oid);
        args.putString(ARG_TYPE, type);
        args.putSerializable(ARG_ROOT, root);
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    public static Fragment newInstanceForTail()
    {
        ReplyFragment fragment = new ReplyFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_TAIL_MODE, true);
        args.putString(ARG_OID, TailActivity.TAIL_REPLY_ARRAY[0]);
        args.putString(ARG_TYPE, "17");
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
        {
            tailMode = getArguments().getBoolean(ARG_TAIL_MODE) ? 0 : -1;
            oid = getArguments().getString(ARG_OID, "0");
            type = getArguments().getString(ARG_TYPE, "0");
            root = (ReplyModel) getArguments().getSerializable(ARG_ROOT);
            position = getArguments().getInt(ARG_POSITION);
            sort = root == null ? "2" : "0";
        }
        if(getActivity() instanceof BangumiActivity)
        {
            ((BangumiActivity) getActivity()).setBangumiReplyActivityListener((oid, type) -> getReply(oid, type, sort));
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_reply, container, false);
        resultIntent = new Intent();
        resultIntent.putExtra("position", position);
        getActivity().setResult(-1, resultIntent);
        if(tailMode == -1)
            replyApi = new ReplyApi(oid, type);
        else
            replyApi = new ReplyApi(TailActivity.TAIL_REPLY_ARRAY[0], "17");

        WindowManager manager = getActivity().getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        replyWidth = outMetrics.widthPixels - DataProcessUtil.dip2px(22) * 2;

        replyAdapterListener = new ReplyAdapter.ReplyAdapterListener()
        {
            @Override
            public void onClick(int viewId, int position, int mode)
            {
                onReplyViewClick(viewId, position, mode);
            }

            @Override
            public void onSortModeChange()
            {
                sort = sort.equals("0") ? "2" : "0";
                waveSwipeRefreshLayout.setRefreshing(true);
                getReply(oid, type, sort);
            }
        };

        layoutLoading = inflater.inflate(R.layout.widget_loading, null);
        exceptionHandlerView = rootLayout.findViewById(R.id.reply_exception);
        listView = rootLayout.findViewById(R.id.reply_listview);
        listView.addFooterView(layoutLoading, null, true);
        listView.setHeaderDividersEnabled(false);

        waveSwipeRefreshLayout = rootLayout.findViewById(R.id.reply_swipe);
        waveSwipeRefreshLayout.setColorSchemeColors(Color.WHITE, Color.WHITE);
        waveSwipeRefreshLayout.setWaveColor(ColorUtil.getColor(R.attr.colorPrimary, ctx));
        waveSwipeRefreshLayout.setTopOffsetOfWave(getResources().getDimensionPixelSize(R.dimen.titleHeight));
        waveSwipeRefreshLayout.setOnRefreshListener(() -> handler.post(() -> {
            rootLayout.findViewById(R.id.reply_listview).setVisibility(View.GONE);
            getReply(oid, type, sort);
        }));

        layoutLoading.findViewById(R.id.wid_load_button).setOnClickListener(v -> {
            ((TextView) layoutLoading.findViewById(R.id.wid_load_text)).setText(getString(R.string.main_tip_no_more_data_loading));
            layoutLoading.findViewById(R.id.wid_load_button).setVisibility(View.GONE);
            getMoreReply();
        });

        listView.setOnScrollListener(new ViewScrollListener(this));
        listView.setOnTouchListener(new ViewTouchListener(listView, titleViewListener).setViewTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN)
                v.clearFocus();
            return false;
        }));

        runnableUi = () -> {
            try
            {
                isReplyLoading = false;
                exceptionHandlerView.hideAllView();
                rootLayout.findViewById(R.id.reply_listview).setVisibility(View.VISIBLE);
                if(tailMode == -1)
                    replyAdapter = new ReplyAdapter(inflater, listView, replyApi.replyArrayList, replyApi.replyIsShowFloor, root != null, replyApi.replyCount, replyWidth, replyAdapterListener);
                else
                    replyAdapter = new TailReplyAdapter(inflater, listView, replyApi.replyArrayList, replyApi.replyIsShowFloor, false, replyApi.replyCount, replyWidth, replyAdapterListener);
                listView.setAdapter(replyAdapter);
                if(replyApi.replyIsEnd)
                    ((TextView) layoutLoading.findViewById(R.id.wid_load_text)).setText(getString(R.string.main_tip_no_more_data));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        };

        runnableMoreErr = () -> {
            ((TextView) layoutLoading.findViewById(R.id.wid_load_text)).setText(getString(R.string.main_tip_no_more_web));
            layoutLoading.findViewById(R.id.wid_load_button).setVisibility(View.VISIBLE);
        };

        runnableUpdate = () -> {
            try
            {
                if(root != null)
                {
                    resultIntent.putExtra("replyModel", replyApi.replyArrayList.get(0));
                    getActivity().setResult(0, resultIntent);
                }

                isReplyLoading = false;
                if(replyApi.replyIsEnd)
                    ((TextView) layoutLoading.findViewById(R.id.wid_load_text)).setText(getString(R.string.main_tip_no_more_data));
                replyAdapter.notifyDataSetChanged();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        };

        waveSwipeRefreshLayout.setRefreshing(true);
        waveSwipeRefreshLayout.setRefreshing(false);
        getReply(oid, type, sort);

        return rootLayout;
    }

    private void getReply(String oid, String type, final String sort)
    {
        isReplyLoading = true;
        this.oid = oid;
        this.type = type;
        replyPage = 1;
        new Thread(() -> {
            try
            {
                if(!oid.equals(replyApi.getOid()) || !type.equals(replyApi.getType()))
                    replyApi = new ReplyApi(oid, type);
                ReplyModel r = (root != null ? (replyApi.replyArrayList.size() > 0 ? replyApi.replyArrayList.get(0) : root) : null);
                int l = replyApi.getReply(1, sort, 0, r);
                if(l == -1 && tailMode > -1 && tailMode <= TailActivity.TAIL_REPLY_ARRAY.length - 2)
                {
                    tailMode++;
                    getReply(TailActivity.TAIL_REPLY_ARRAY[tailMode], "17", sort);
                }
                handler.post(runnableUi);
            }
            catch (IOException | NullPointerException e)
            {
                e.printStackTrace();
                exceptionHandlerView.noWeb();
            }
        }).start();
    }

    private void getMoreReply()
    {
        isReplyLoading = true;
        replyPage++;
        new Thread(() -> {
            try
            {
                ReplyModel r = (root != null ? (replyApi.replyArrayList.size() > 0 ? replyApi.replyArrayList.get(0) : root) : null);
                replyApi.getReply(replyPage, sort, 0, r);
                handler.post(runnableUpdate);
            }
            catch (IOException e)
            {
                handler.post(runnableMoreErr);
                e.printStackTrace();
            }
        }).start();
    }

    private void onReplyViewClick(int viewId, final int position, int mode)
    {
        if(mode == 0)
        {
            final ReplyModel replyModel = replyApi.replyArrayList.get(position);
            if(viewId == R.id.item_reply_head)
            {
                Intent intent = new Intent(ctx, UserActivity.class);
                intent.putExtra("mid", replyModel.getOwnerMid());
                startActivity(intent);
            }
            else if(viewId == R.id.item_reply_report)
            {
                Intent intent = new Intent(ctx, SelectPartActivity.class);
                intent.putExtra("title", "举报");
                intent.putExtra("tip", "请选择举报理由");
                intent.putExtra("options_name", ReplyApi.reportReason);
                intent.putExtra("options_id", ReplyApi.reportReasonId);
                intent.putExtra("reply_id", replyApi.replyArrayList.get(position).getId());
                startActivityForResult(intent, RESULT_REPORT);
            }
            else if(viewId == R.id.item_reply_like)
            {
                new Thread(() -> {
                    String va = replyApi.likeReply(replyModel, replyModel.isUserLike() ? 0 : 1, type);
                    if(va.equals("")) handler.post(runnableUpdate);
                    else
                    {
                        Looper.prepare();
                        Toast.makeText(ctx, (replyModel.isUserLike() ? "取消" : "点赞") + "失败：\n" + va, Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }).start();
            }
            else if(viewId == R.id.item_reply_dislike)
            {
                new Thread(() -> {
                    String va = replyApi.hateReply(replyModel, replyModel.isUserDislike() ? 0 : 1, type);
                    if(va.equals("")) handler.post(runnableUpdate);
                    else
                    {
                        Looper.prepare();
                        Toast.makeText(ctx, (replyModel.isUserDislike() ? "取消" : "点踩") + "失败：\n" + va, Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }).start();
            }
            else if(viewId == R.id.item_reply_reply_show_1 || viewId == R.id.item_reply_reply_show_2 ||
                    viewId == R.id.item_reply_reply_show_3 || viewId == R.id.item_reply_reply_show_show || viewId == R.id.item_reply_reply)
            {
                if(root == null)
                {
                    Intent intent = new Intent(ctx, CheckreplyActivity.class);
                    intent.putExtra("oid", oid);
                    intent.putExtra("type", type);
                    intent.putExtra("root", replyModel);
                    intent.putExtra("position", position);
                    startActivityForResult(intent, RESULT_VIEW);
                }
                else
                {
                    Intent replyIntent = new Intent(ctx, ReplyActivity.class);
                    replyIntent.putExtra("rpid", replyApi.replyArrayList.get(position).getId());
                    replyIntent.putExtra("type", type);
                    if(position != 0)
                        replyIntent.putExtra("text", String.format(getString(R.string.reply_reply_template), replyApi.replyArrayList.get(position).getOwnerName()));
                    startActivityForResult(replyIntent, RESULT_SEND);
                }
            }
            else if(viewId == R.id.item_reply_tail_apply)
            {
                SharedPreferencesUtil.putString(SharedPreferencesUtil.tailCustom, replyApi.replyArrayList.get(position).getTextOrg());
            }
        }
        else if(mode == 1)
        {
            if(viewId == R.id.reply_toolbar_sendreply)
            {
                Intent replyIntent = new Intent(ctx, ReplyActivity.class);
                replyIntent.putExtra("rpid", root != null ? root.getId() : "");
                replyIntent.putExtra("type", type);
                startActivityForResult(replyIntent, RESULT_SEND);
            }
            else if(viewId == R.id.reply_tail_toolbar_sendreply)
            {
                new AlertDialog.Builder(ctx)
                        .setMessage(getString(R.string.tail_reply_toolbar_send_tip))
                        .setPositiveButton("分享", (dialog, which) -> {
                            if(!TailFragment.isDefault())
                            {
                                new Thread(() -> {
                                    String flag = null;
                                    for (int i = 0; i < TailActivity.TAIL_REPLY_ARRAY.length; i++)
                                    {
                                        try
                                        {
                                            String result = new ReplyApi(TailActivity.TAIL_REPLY_ARRAY[i], "17").sendReply("", TailFragment.getTail(false));
                                            if(i == tailMode)
                                                flag = result.equals("") ? "发送成功！" : result;
                                        }
                                        catch (IOException e)
                                        {
                                            e.printStackTrace();
                                            if(i == tailMode)
                                                flag = getString(R.string.main_error_web);
                                        }
                                    }
                                    getReply(oid, type, sort);
                                    showToast(ctx, flag);
                                }).start();
                            }
                            else
                                Toast.makeText(ctx, getString(R.string.tail_reply_toolbar_send_err), Toast.LENGTH_LONG).show();
                        })
                        .setNegativeButton("取消", null).show();
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        if(context instanceof TitleView.TitleViewListener)
            titleViewListener = (TitleView.TitleViewListener) context;
    }

    public static void showToast(Context context, String text)
    {
        Looper myLooper = Looper.myLooper();
        if(myLooper == null)
        {
            Looper.prepare();
            myLooper = Looper.myLooper();
        }

        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
        if(myLooper != null)
        {
            Looper.loop();
            myLooper.quit();
        }
    }

    @Override
    public void onActivityResult(final int requestCode, int resultCode, final Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != 0 || data == null) return;
        if(requestCode == RESULT_SEND)
        {
            new Thread(() -> {
                try
                {
                    String result = replyApi.sendReply(data.getStringExtra("rpid"), data.getStringExtra("text"));
                    if(result.equals(""))
                    {
                        Looper.prepare();
                        Toast.makeText(ctx, "发送成功！", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ctx, "评论发送失败，请检查网络", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }).start();
        }
        else if(requestCode == RESULT_VIEW)
        {
            int p = data.getIntExtra("position", -1);
            ReplyModel r = data.hasExtra("replyModel") ? (ReplyModel) data.getSerializableExtra("replyModel") : null;
            if(p != -1 && r != null)
            {
                r.setMode(0);
                replyApi.replyArrayList.set(p, r);
            }
            replyAdapter.notifyDataSetChanged();
        }
        else if(requestCode == RESULT_REPORT)
        {
            new Thread(() -> {
                try
                {
                    String result = replyApi.reportReply(data.getStringExtra("reply_id"), data.getStringExtra("option_id"));
                    if(result.equals(""))
                    {
                        Looper.prepare();
                        Toast.makeText(ctx, "举报成功！", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ctx, "举报失败，请检查网络...", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }).start();
        }
    }

    @Override
    public boolean rule()
    {
        return !isReplyLoading && !replyApi.replyIsEnd;
    }

    @Override
    public void result()
    {
        getMoreReply();
    }
}

