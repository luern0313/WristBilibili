package cn.luern0313.wristbilibili.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.BangumiEpisodeAdapter;
import cn.luern0313.wristbilibili.models.BangumiModel;
import cn.luern0313.wristbilibili.ui.BangumiActivity;
import cn.luern0313.wristbilibili.ui.PlayerActivity;
import cn.luern0313.wristbilibili.ui.SelectPartActivity;
import cn.luern0313.wristbilibili.ui.TextActivity;
import cn.luern0313.wristbilibili.util.ColorUtil;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.widget.CircleButtonView;

/**
 * 被 luern0313 创建于 2020/3/20.
 */
public class BangumiDetailFragment extends Fragment implements View.OnClickListener
{
    private static final String ARG_SEASON_ID = "seasonIdArg";
    private static final String ARG_BANGUMI_MODEL = "bangumiModelArg";
    private final int RESULT_DETAIL_EPISODE = 101;
    private final int RESULT_DETAIL_OTHER = 102;

    private Context ctx;
    private View rootLayout;

    private String seasonId;
    private BangumiModel bangumiModel;

    private RecyclerView uiDetailEpisodesRecyclerView;
    private RecyclerView uiDetailSectionsRecyclerView;
    private BangumiEpisodeAdapter episodesRecyclerViewAdapter;
    private BangumiEpisodeAdapter sectionsRecyclerViewAdapter;

    private BangumiDetailFragmentListener bangumiDetailFragmentListener;

    public BangumiDetailFragment() { }

    public static BangumiDetailFragment newInstance(String seasonId, BangumiModel bangumiModel)
    {
        BangumiDetailFragment fragment = new BangumiDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SEASON_ID, seasonId);
        args.putSerializable(ARG_BANGUMI_MODEL, bangumiModel);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
        {
            seasonId = getArguments().getString(ARG_SEASON_ID);
            bangumiModel = (BangumiModel) getArguments().getSerializable(ARG_BANGUMI_MODEL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_bangumi_detail, container, false);

        uiDetailEpisodesRecyclerView = rootLayout.findViewById(R.id.bgm_detail_video_part);
        uiDetailSectionsRecyclerView = rootLayout.findViewById(R.id.bgm_detail_video_other);

        ((TextView) rootLayout.findViewById(R.id.bgm_detail_title)).setText(bangumiModel.getTitle());
        if(bangumiModel.getScore() == null)
            rootLayout.findViewById(R.id.bgm_detail_score).setVisibility(View.GONE);
        else
            ((TextView) rootLayout.findViewById(R.id.bgm_detail_score)).setText(bangumiModel.getScore());
        ((TextView) rootLayout.findViewById(R.id.bgm_detail_play)).setText(bangumiModel.getPlay());
        ((TextView) rootLayout.findViewById(R.id.bgm_detail_like)).setText(bangumiModel.getLike());
        ((TextView) rootLayout.findViewById(R.id.bgm_detail_series)).setText(bangumiModel.getSeries());
        if(bangumiModel.getNeedVip().equals(""))
            rootLayout.findViewById(R.id.bgm_detail_needvip).setVisibility(View.GONE);
        else
            ((TextView) rootLayout.findViewById(R.id.bgm_detail_needvip)).setText(bangumiModel.getNeedVip());

        if(!bangumiModel.isRightDownload())
        {
            rootLayout.findViewById(R.id.bgm_detail_bt_download).setAlpha(0.5f);
            ((CircleButtonView) rootLayout.findViewById(R.id.bgm_detail_bt_download)).setDefaultName(getString(R.string.bangumi_control_download_notallow));
        }

        Drawable playNumDrawable = ResourcesCompat.getDrawable(ctx.getResources(), R.drawable.icon_number_play, null);
        Drawable danmakuNumDrawable = ResourcesCompat.getDrawable(ctx.getResources(), R.drawable.icon_number_like, null);
        playNumDrawable.setBounds(0, 0, DataProcessUtil.dip2px(12), DataProcessUtil.dip2px(12));
        danmakuNumDrawable.setBounds(0, 0, DataProcessUtil.dip2px(12), DataProcessUtil.dip2px(12));
        ((TextView) rootLayout.findViewById(R.id.bgm_detail_play)).setCompoundDrawables(playNumDrawable, null, null, null);
        ((TextView) rootLayout.findViewById(R.id.bgm_detail_like)).setCompoundDrawables(danmakuNumDrawable, null, null, null);

        setBangumiIcon();

        if(bangumiModel.getEpisodes() != null && bangumiModel.getEpisodes().size() != 0)
        {
            rootLayout.findViewById(R.id.bgm_detail_video_part_layout).setVisibility(View.VISIBLE);
            ((TextView) rootLayout.findViewById(R.id.bgm_detail_video_part_text)).setText(String.format(getString(R.string.bangumi_episodes_title), bangumiModel.getEpisodes().size(), bangumiModel.getTypeEp()));
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getParent());
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            uiDetailEpisodesRecyclerView.setLayoutManager(layoutManager);
            episodesRecyclerViewAdapter = new BangumiEpisodeAdapter(bangumiModel.getEpisodes(), bangumiModel, 1);
            episodesRecyclerViewAdapter.setOnItemClickListener((view, position) -> clickBangumiDetailEpisode(position));
            uiDetailEpisodesRecyclerView.setAdapter(episodesRecyclerViewAdapter);
            if(bangumiModel.getUserProgressMode() == 1)
                layoutManager.scrollToPositionWithOffset(bangumiModel.getUserProgressPosition(), 0);
        }

        if(bangumiModel.getSections() != null && bangumiModel.getSections().size() != 0)
        {
            rootLayout.findViewById(R.id.bgm_detail_video_other_layout).setVisibility(View.VISIBLE);
            ((TextView) rootLayout.findViewById(R.id.bgm_detail_video_other_text)).setText(bangumiModel.getSectionName());
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getParent());
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            uiDetailSectionsRecyclerView.setLayoutManager(layoutManager);
            sectionsRecyclerViewAdapter = new BangumiEpisodeAdapter(bangumiModel.getSections(), bangumiModel, 2);
            sectionsRecyclerViewAdapter.setOnItemClickListener((view, position) -> clickBangumiDetailOther(position));
            uiDetailSectionsRecyclerView.setAdapter(sectionsRecyclerViewAdapter);
            if(bangumiModel.getUserProgressMode() == 2)
                layoutManager.scrollToPositionWithOffset(bangumiModel.getUserProgressPosition(), 0);
        }

        if(bangumiModel.getSeasons() != null && bangumiModel.getSeasons().size() > 1)
        {
            rootLayout.findViewById(R.id.bgm_detail_video_season_layout).setVisibility(View.VISIBLE);
            LinearLayout episodesLinearLayout = rootLayout.findViewById(R.id.bgm_detail_video_season);
            for(int i = 0; i < bangumiModel.getSeasons().size(); i++)
                episodesLinearLayout.addView(getVideoSeasonButton(bangumiModel.getSeasons().get(i)));
        }

        rootLayout.findViewById(R.id.bgm_detail_info).setOnClickListener(v -> {
            Intent intent = new Intent(ctx, TextActivity.class);
            intent.putExtra("title", bangumiModel.getTypeName() + "信息");
            intent.putExtra("text", getBangumiInfo());
            startActivity(intent);
        });

        rootLayout.findViewById(R.id.bgm_detail_bt_follow).setOnClickListener(this);
        rootLayout.findViewById(R.id.bgm_detail_bt_cover).setOnClickListener(this);
        rootLayout.findViewById(R.id.bgm_detail_bt_download).setOnClickListener(this);
        rootLayout.findViewById(R.id.bgm_detail_bt_share).setOnClickListener(this);
        rootLayout.findViewById(R.id.bgm_detail_video_part_more).setOnClickListener(this::clickBangumiMorePart);
        rootLayout.findViewById(R.id.bgm_detail_video_other_more).setOnClickListener(this::clickBangumiMoreOther);

        return rootLayout;
    }

    private void setBangumiIcon()
    {
        ((CircleButtonView) rootLayout.findViewById(R.id.bgm_detail_bt_follow)).setChecked(bangumiModel.isUserIsFollow());
        if(bangumiModel.isUserIsFollow())
            ((CircleButtonView) rootLayout.findViewById(R.id.bgm_detail_bt_follow)).setDefaultName(String.format(getString(R.string.bangumi_control_follow_unfollow), bangumiModel.getTypeFollow()));
        else
            ((CircleButtonView) rootLayout.findViewById(R.id.bgm_detail_bt_follow)).setDefaultName(bangumiModel.getTypeFollow());
    }

    private void clickBangumiMorePart(View view)
    {
        String[] videoPartNames = new String[bangumiModel.getEpisodes().size()];
        for(int i = 0; i < bangumiModel.getEpisodes().size(); i++)
            videoPartNames[i] = BangumiModel.getTitle(1, bangumiModel, bangumiModel.getEpisodes().get(i), " ");
        Intent intent = new Intent(ctx, SelectPartActivity.class);
        intent.putExtra("title", "选集");
        intent.putExtra("options_name", videoPartNames);
        startActivityForResult(intent, RESULT_DETAIL_EPISODE);
    }

    private void clickBangumiMoreOther(View view)
    {
        String[] videoPartNames = new String[bangumiModel.getSections().size()];
        for(int i = 0; i < bangumiModel.getSections().size(); i++)
            videoPartNames[i] = BangumiModel.getTitle(2, bangumiModel, bangumiModel.getSections().get(i), " ");
        Intent intent = new Intent(ctx, SelectPartActivity.class);
        intent.putExtra("title", "选集");
        intent.putExtra("options_name", videoPartNames);
        startActivityForResult(intent, RESULT_DETAIL_OTHER);
    }

    private void clickBangumiDetailEpisode(int position)
    {
        BangumiModel.BangumiEpisodeModel ep = bangumiModel.getEpisodes().get(position);
        bangumiModel.setUserProgressEpId(ep.getEpisodeId());
        bangumiModel.setUserProgressMode(1);
        bangumiModel.setUserProgressPosition(position);
        bangumiModel.setUserProgressAid(ep.getEpisodeAid());
        if(episodesRecyclerViewAdapter != null)
            episodesRecyclerViewAdapter.notifyDataSetChanged();
        if(sectionsRecyclerViewAdapter != null)
            sectionsRecyclerViewAdapter.notifyDataSetChanged();

        bangumiDetailFragmentListener.onBangumiDetailFragmentReplyUpdate(bangumiModel.getUserProgressEpId(), bangumiModel.getUserProgressMode(),
                                                                         bangumiModel.getUserProgressPosition(), bangumiModel.getUserProgressAid());
        Intent intent = new Intent(ctx, PlayerActivity.class);
        intent.putExtra("title", BangumiModel.getTitle(1, bangumiModel, ep, " "));
        intent.putExtra("aid", ep.getEpisodeAid());
        intent.putExtra("cid", ep.getEpisodeCid());
        startActivity(intent);
    }

    private void clickBangumiDetailOther(int position)
    {
        BangumiModel.BangumiEpisodeModel ss = bangumiModel.getSections().get(position);
        bangumiModel.setUserProgressEpId(ss.getEpisodeId());
        bangumiModel.setUserProgressMode(2);
        bangumiModel.setUserProgressPosition(position);
        bangumiModel.setUserProgressAid(ss.getEpisodeAid());
        if(episodesRecyclerViewAdapter != null)
            episodesRecyclerViewAdapter.notifyDataSetChanged();
        if(sectionsRecyclerViewAdapter != null)
            sectionsRecyclerViewAdapter.notifyDataSetChanged();

        bangumiDetailFragmentListener.onBangumiDetailFragmentReplyUpdate(bangumiModel.getUserProgressEpId(), bangumiModel.getUserProgressMode(),
                                                                         bangumiModel.getUserProgressPosition(), bangumiModel.getUserProgressAid());
        Intent intent = new Intent(ctx, PlayerActivity.class);
        intent.putExtra("title", BangumiModel.getTitle(2, bangumiModel, ss, " "));
        intent.putExtra("aid", ss.getEpisodeAid());
        intent.putExtra("cid", ss.getEpisodeCid());
        startActivity(intent);
    }

    private TextView getVideoSeasonButton(final BangumiModel.BangumiSeasonModel season)
    {
        TextView textView = new TextView(ctx);
        textView.setWidth(DataProcessUtil.dip2px(80));
        textView.setBackgroundResource(seasonId.equals(season.getSeasonId()) ? R.drawable.selector_bg_bangumi_episode_now : R.drawable.selector_bg_bangumi_episode);
        textView.setPadding(12, 6, 12, 6);
        textView.setText(season.getSeasonTitle());
        textView.setLines(1);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setTextSize(14);
        //textView.setTextColor(seasonId.equals(season.getSeasonId()) ? ColorUtil.getColor(R.attr.colorPrimary, textView.getContext()) : getResources().getColor(R.color.gray_77));
        textView.setOnClickListener(seasonId.equals(season.getSeasonId()) ? null : (View.OnClickListener) v -> {
            Intent intent = new Intent(ctx, BangumiActivity.class);
            intent.putExtra("season_id", season.getSeasonId());
            startActivity(intent);
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 4, 0);
        textView.setLayoutParams(lp);
        return textView;
    }

    private String getBangumiInfo()
    {
        String colorTitle = ColorUtil.getColorString(R.attr.colorTitle, ctx);
        ArrayList<String> detail_info = new ArrayList<>();
        detail_info.add("<b><big><font color=\"" + colorTitle + "\">" + bangumiModel.getTitle() + "</font></big></b>");
        detail_info.add("");
        detail_info.add(bangumiModel.getTypeName() + " | " + bangumiModel.getDetailAreas());
        detail_info.add(bangumiModel.getDetailPublishDate());
        detail_info.add(bangumiModel.getDetailPublishEp());
        detail_info.add("风格：" + bangumiModel.getDetailStyles());
        detail_info.add("");
        detail_info.add("<big><font color=\"" + colorTitle + "\">简介</font></big>");
        detail_info.add(bangumiModel.getDetailEvaluate());
        detail_info.add("");
        detail_info.add("<big><font color=\"" + colorTitle + "\">" + bangumiModel.getDetailActorTitle() + "</font></big>");
        detail_info.add(bangumiModel.getDetailActorInfo().replaceAll("\n", "<br/>"));
        detail_info.add("");
        detail_info.add("<big><font color=\"" + colorTitle + "\">" + bangumiModel.getDetailStaffTitle() + "</font></big>");
        detail_info.add(bangumiModel.getDetailStaffInfo().replaceAll("\n", "<br/>"));
        return DataProcessUtil.joinArrayList(detail_info, "<br/>");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(BangumiModel bangumiModel)
    {
        this.bangumiModel = bangumiModel;
        setBangumiIcon();
    }

    @Override
    public void onClick(View v)
    {
        bangumiDetailFragmentListener.onBangumiDetailFragmentViewClick(v.getId());
    }

    @Override
    public void onActivityResult(final int requestCode, int resultCode, final Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != 0) return;
        if(requestCode == RESULT_DETAIL_EPISODE)
        {
            clickBangumiDetailEpisode(data.getIntExtra("option_position", 0));
        }
        else if(requestCode == RESULT_DETAIL_OTHER)
        {
            clickBangumiDetailOther(data.getIntExtra("option_position", 0));
        }
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        if(context instanceof BangumiDetailFragmentListener)
        {
            bangumiDetailFragmentListener = (BangumiDetailFragmentListener) context;
        }
        else
        {
            throw new RuntimeException(context.toString() + " must implement BangumiDetailFragmentListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        bangumiDetailFragmentListener = null;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    public interface BangumiDetailFragmentListener
    {
        void onBangumiDetailFragmentViewClick(int viewId);
        void onBangumiDetailFragmentReplyUpdate(String epId, int mode, int position, String aid);
    }
}
