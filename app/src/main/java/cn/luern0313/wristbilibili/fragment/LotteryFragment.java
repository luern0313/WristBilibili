package cn.luern0313.wristbilibili.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.LotteryAdapter;
import cn.luern0313.wristbilibili.api.DynamicApi;
import cn.luern0313.wristbilibili.api.LotteryApi;
import cn.luern0313.wristbilibili.api.SendDynamicApi;
import cn.luern0313.wristbilibili.api.UserApi;
import cn.luern0313.wristbilibili.models.DynamicModel;
import cn.luern0313.wristbilibili.models.LotteryModel;
import cn.luern0313.wristbilibili.ui.DynamicDetailActivity;
import cn.luern0313.wristbilibili.ui.ImgActivity;
import cn.luern0313.wristbilibili.ui.UserActivity;
import cn.luern0313.wristbilibili.util.ColorUtil;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.ImageTaskUtil;
import cn.luern0313.wristbilibili.util.LruCacheUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;
import cn.luern0313.wristbilibili.util.ViewTouchListener;
import cn.luern0313.wristbilibili.widget.ExceptionHandlerView;
import cn.luern0313.wristbilibili.widget.ExpandableTextView;
import cn.luern0313.wristbilibili.widget.TitleView;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

import static cn.luern0313.wristbilibili.ui.LotteryActivity.ARG_LOTTERY_ID;

/**
 * 被 luern0313 创建于 2021/01/11.
 */

public class LotteryFragment extends Fragment
{
    private Context ctx;
    private String id, selfMid;
    private DynamicApi dynamicApi;
    private LotteryApi lotteryApi;
    private LotteryModel lotteryModel;
    private LotteryAdapter.LotteryListener lotteryListener;
    private TitleView.TitleViewListener titleViewListener;

    private View rootLayout;
    private ExceptionHandlerView exceptionHandlerView;
    private ExpandableListView lotteryListView;
    private WaveSwipeRefreshLayout waveSwipeRefreshLayout;
    private View lotteryHeaderView, lotteryFooterView;
    private ExpandableTextView lotteryHeaderExpandableTextView;

    private final Handler handler = new Handler();
    private Runnable runnableUi, runnableNoWeb, runnableNoData, runnableJoin;

    public LotteryFragment() {}

    public static LotteryFragment newInstance(String id)
    {
        LotteryFragment fragment = new LotteryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LOTTERY_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
        {
            id = getArguments().getString(ARG_LOTTERY_ID);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_lottery, container, false);
        selfMid = SharedPreferencesUtil.getString(SharedPreferencesUtil.mid, "");

        exceptionHandlerView = rootLayout.findViewById(R.id.lottery_exception);
        lotteryListView = rootLayout.findViewById(R.id.lottery_listview);
        lotteryHeaderView = inflater.inflate(R.layout.widget_lottery_header, null);
        lotteryFooterView = inflater.inflate(R.layout.widget_lottery_footer, null);
        lotteryHeaderExpandableTextView = lotteryHeaderView.findViewById(R.id.lottery_dynamic);

        waveSwipeRefreshLayout = rootLayout.findViewById(R.id.lottery_swipe);
        waveSwipeRefreshLayout.setColorSchemeColors(Color.WHITE, Color.WHITE);
        waveSwipeRefreshLayout.setWaveColor(ColorUtil.getColor(R.attr.colorTitleBackground, ctx));
        waveSwipeRefreshLayout.setTopOffsetOfWave(getResources().getDimensionPixelSize(R.dimen.titleHeight));
        waveSwipeRefreshLayout.setOnRefreshListener(() -> handler.post(() -> {
            lotteryListView.setVisibility(View.GONE);
            getLotteryDetail();
        }));

        lotteryListView.addHeaderView(lotteryHeaderView);
        lotteryListView.addFooterView(lotteryFooterView);

        lotteryListener = new LotteryAdapter.LotteryListener()
        {
            @Override
            public void onImgClick(String url)
            {
                Intent intent = new Intent(ctx, ImgActivity.class);
                intent.putExtra("imgUrl", new String[]{url});
                startActivity(intent);
            }

            @Override
            public void onUserClick(String uid)
            {
                Intent intent = new Intent(ctx, UserActivity.class);
                intent.putExtra("mid", uid);
                startActivity(intent);
            }
        };

        runnableUi = () -> {
            exceptionHandlerView.hideAllView();
            ((TextView) lotteryHeaderView.findViewById(R.id.lottery_card_name)).setText(lotteryModel.getDescModel().getSenderName());
            ((TextView) lotteryHeaderView.findViewById(R.id.lottery_card_time)).setText(DataProcessUtil.getFriendlyTime(lotteryModel.getDescModel().getTime()));
            lotteryHeaderExpandableTextView.setOrigText(lotteryModel.getDescModel().getDynamicText());

            lotteryHeaderExpandableTextView.setOnTouchListener(lotteryHeaderExpandableTextView.new LinkTouchMovementMethod());

            lotteryHeaderView.findViewById(R.id.lottery_card_off_1).setVisibility(View.GONE);
            lotteryHeaderView.findViewById(R.id.lottery_card_off_2).setVisibility(View.GONE);
            if(lotteryModel.getDescModel().getSenderOfficial() == 0)
                lotteryHeaderView.findViewById(R.id.lottery_card_off_1).setVisibility(View.VISIBLE);
            else if(lotteryModel.getDescModel().getSenderOfficial() == 1)
                lotteryHeaderView.findViewById(R.id.lottery_card_off_2).setVisibility(View.VISIBLE);

            if(lotteryModel.getDescModel().getSenderVipStatus() == 2)
                ((TextView) lotteryHeaderView.findViewById(R.id.lottery_card_name)).setTextColor(ColorUtil.getColor(R.attr.colorVip, ctx));

            ((HtmlTextView) lotteryHeaderView.findViewById(R.id.lottery_countdown)).setHtml(DataProcessUtil.getCountDown(
                    lotteryModel.getTimeCurrent(), lotteryModel.getTimeResult(), R.string.time_countdown_html));
            ((HtmlTextView) lotteryHeaderView.findViewById(R.id.lottery_desc)).setHtml(String.format(
                    getString(R.string.lottery_desc), DataProcessUtil.getTime(
                            lotteryModel.getTimeResult(), "yyyy月MM月dd日 HH:mm"), getLimit(true), getResources().getStringArray(
                                    R.array.lottery_range)[lotteryModel.getLimit()], lotteryModel.getTotalNum(), getGiftNum()));

            ((TextView) lotteryFooterView.findViewById(R.id.lottery_footer_join_tip)).setText(String.format(getString(R.string.lottery_join_tip), getLimit(false)));

            if(lotteryModel.getStatus() != 0)
            {
                lotteryHeaderView.findViewById(R.id.lottery_countdown).setVisibility(View.GONE);
                lotteryHeaderView.findViewById(R.id.lottery_desc).setVisibility(View.GONE);
                lotteryFooterView.findViewById(R.id.lottery_footer_join).setVisibility(View.GONE);
                lotteryFooterView.findViewById(R.id.lottery_footer_join_tip).setVisibility(View.GONE);
                ((TextView) lotteryHeaderView.findViewById(R.id.lottery_countdown_text)).setText(getResult());
            }
            lotteryListView.setAdapter(new LotteryAdapter(getLayoutInflater(), lotteryModel, lotteryListView, lotteryListener));
            lotteryListView.setVisibility(View.VISIBLE);
            waveSwipeRefreshLayout.setRefreshing(false);

            for (int i = 0; i < lotteryListView.getExpandableListAdapter().getGroupCount(); i++)
                lotteryListView.expandGroup(i);

            ImageView head = lotteryHeaderView.findViewById(R.id.lottery_card_head);
            head.setTag(lotteryModel.getDescModel().getSenderImg());
            BitmapDrawable c = setImageFormWeb(lotteryModel.getDescModel().getSenderImg());
            if(c != null) head.setImageDrawable(c);
        };

        runnableJoin = () -> {
            exceptionHandlerView.progressLoadingEnd();
            lotteryFooterView.findViewById(R.id.lottery_footer_join).setEnabled(false);
            ((TextView) lotteryFooterView.findViewById(R.id.lottery_footer_join)).setText(getString(R.string.lottery_join_done));
        };

        lotteryHeaderView.findViewById(R.id.lottery_card_lay).setOnClickListener(v -> {
            Intent intent = new Intent(ctx, UserActivity.class);
            intent.putExtra("mid", lotteryModel.getDescModel().getSenderId());
            startActivity(intent);
        });

        lotteryHeaderExpandableTextView.setOnClickListener(v -> {
            Intent intent = new Intent(ctx, DynamicDetailActivity.class);
            intent.putExtra("dynamic_id", lotteryModel.getDescModel().getDynamicId());
            startActivity(intent);
        });

        lotteryFooterView.findViewById(R.id.lottery_footer_join).setOnClickListener(v -> join());
        lotteryListView.setOnTouchListener(new ViewTouchListener(lotteryListView, titleViewListener));

        waveSwipeRefreshLayout.setRefreshing(true);
        getLotteryDetail();

        return rootLayout;
    }

    private void getLotteryDetail()
    {
        new Thread(() -> {
            try
            {
                dynamicApi = new DynamicApi(SharedPreferencesUtil.getString(SharedPreferencesUtil.mid, ""), false);
                DynamicModel.DynamicBaseModel dynamicModel = dynamicApi.getDynamicDetail(id, null);
                lotteryApi = new LotteryApi(id, new LotteryModel.LotteryDescModel(
                        dynamicModel.getCardAuthorUid(), dynamicModel.getCardAuthorName(), dynamicModel.getCardAuthorImg(), dynamicModel.getCardAuthorOfficial(),
                        dynamicModel.getCardAuthorVipType(), dynamicModel.getCardId(),
                        dynamicModel instanceof DynamicModel.DynamicAlbumModel ? ((DynamicModel.DynamicAlbumModel) dynamicModel).getAlbumTextOrg() : "",
                        dynamicModel.getCardTimeOrg()));
                lotteryModel = lotteryApi.getLottery();
                if(lotteryModel != null)
                    handler.post(runnableUi);
                else
                    handler.post(runnableNoData);
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
        }).start();
    }

    private void join()
    {
        exceptionHandlerView.progressLoadingStart();
        new Thread(() -> {
            try
            {
                if(lotteryModel.getLimit() == 1)
                    new UserApi(lotteryModel.getDescModel().getSenderId()).follow();
                new SendDynamicApi().sendDynamicWithDynamic(id, "@luern0313 ");
                handler.post(runnableJoin);
            }
            catch (IOException e)
            {
                Looper.prepare();
                Toast.makeText(ctx, getString(R.string.main_error_web), Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }).start();
    }

    private String getLimit(boolean isDisplay)
    {
        ArrayList<String> limit = new ArrayList<>();
        switch (lotteryModel.getLimit())
        {
            case 1:
                limit.add(String.format(getString(R.string.lottery_limit_follow), lotteryModel.getDescModel().getSenderName()));
            case 0:
                limit.add(getString(R.string.lottery_limit_post));
        }
        if(lotteryModel.getAtNum() > 0 && isDisplay)
            limit.add(String.format(getString(R.string.lottery_limit_at_display), lotteryModel.getAtNum()));
        else if(lotteryModel.getAtNum() > 0)
            limit.add(String.format(getString(R.string.lottery_limit_at_do), lotteryModel.getAtNum()));

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i <  limit.size(); i++)
        {
            stringBuilder.append(limit.get(i));
            if(i == limit.size() - 2)
                stringBuilder.append(getString(R.string.lottery_gift_num_split_last));
            else if(i < limit.size() - 2)
                stringBuilder.append(getString(R.string.lottery_gift_num_split));
        }
        return stringBuilder.toString();
    }

    private String getGiftNum()
    {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 3; i ++)
        {
            if(i == 0 && lotteryModel.getGiftFirstNum() > 0)
                stringBuilder.append(String.format(getString(R.string.lottery_gift_num), getResources().getStringArray(R.array.lottery_ranking)[i], lotteryModel.getGiftFirstNum()));
            else if(i == 1 && lotteryModel.getGiftSecondNum() > 0)
                stringBuilder.append(getString(R.string.lottery_gift_num_split)).append(String.format(getString(R.string.lottery_gift_num), getResources().getStringArray(R.array.lottery_ranking)[i], lotteryModel.getGiftSecondNum()));
            else if(i == 2 && lotteryModel.getGiftThirdNum() > 0)
                stringBuilder.append(getString(R.string.lottery_gift_num_split)).append(String.format(getString(R.string.lottery_gift_num), getResources().getStringArray(R.array.lottery_ranking)[i], lotteryModel.getGiftThirdNum()));
            else
                break;
        }
        return stringBuilder.toString();
    }

    private String getResult()
    {
        ArrayList<LotteryModel.LotteryUserModel> lotteryUserModelArrayList;
        for (int i = 0; i < 3; i++)
        {
            lotteryUserModelArrayList = i == 0 ? lotteryModel.getGiftFirstResult() : (i == 1 ? lotteryModel.getGiftSecondResult() : lotteryModel.getGiftThirdResult());
            if(lotteryUserModelArrayList != null)
                for (int j = 0; j < lotteryUserModelArrayList.size(); j++)
                    if(selfMid.equals(lotteryUserModelArrayList.get(j).getUid()))
                        return String.format(getString(R.string.lottery_result_winning), getResources().getStringArray(R.array.lottery_ranking)[i]);
        }
        return getString(R.string.lottery_result_lose);
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        if(context instanceof TitleView.TitleViewListener)
            titleViewListener = (TitleView.TitleViewListener) context;
    }

    private BitmapDrawable setImageFormWeb(String url)
    {
        if(url != null && LruCacheUtil.getLruCache().get(url) != null)
            return LruCacheUtil.getLruCache().get(url);
        else
        {
            ImageTaskUtil it = new ImageTaskUtil(lotteryListView);
            it.execute(url);
            return null;
        }
    }
}
