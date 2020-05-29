package cn.luern0313.wristbilibili.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.BangumiEpisodeAdapter;
import cn.luern0313.wristbilibili.models.bangumi.BangumiModel;
import cn.luern0313.wristbilibili.ui.BangumiActivity;
import cn.luern0313.wristbilibili.ui.PlayerActivity;
import cn.luern0313.wristbilibili.ui.SelectPartActivity;
import cn.luern0313.wristbilibili.ui.TextActivity;
import cn.luern0313.wristbilibili.util.DataProcessUtil;

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

        ((TextView) rootLayout.findViewById(R.id.bgm_detail_title)).setText(bangumiModel.bangumi_title);
        if(bangumiModel.bangumi_score.equals("")) rootLayout.findViewById(R.id.bgm_detail_score).setVisibility(View.GONE);
        else ((TextView) rootLayout.findViewById(R.id.bgm_detail_score)).setText(bangumiModel.bangumi_score);
        ((TextView) rootLayout.findViewById(R.id.bgm_detail_play)).setText(bangumiModel.bangumi_play);
        ((TextView) rootLayout.findViewById(R.id.bgm_detail_like)).setText(bangumiModel.bangumi_like);
        ((TextView) rootLayout.findViewById(R.id.bgm_detail_series)).setText(bangumiModel.bangumi_series);
        if(bangumiModel.bangumi_needvip.equals("")) rootLayout.findViewById(R.id.bgm_detail_needvip).setVisibility(View.GONE);
        else ((TextView) rootLayout.findViewById(R.id.bgm_detail_needvip)).setText(bangumiModel.bangumi_needvip);

        Drawable playNumDrawable = getResources().getDrawable(R.drawable.icon_video_play_num);
        Drawable danmakuNumDrawable = getResources().getDrawable(R.drawable.icon_video_like_num);
        playNumDrawable.setBounds(0, 0, DataProcessUtil.dip2px(12), DataProcessUtil.dip2px(12));
        danmakuNumDrawable.setBounds(0, 0, DataProcessUtil.dip2px(12), DataProcessUtil.dip2px(12));
        ((TextView) rootLayout.findViewById(R.id.bgm_detail_play)).setCompoundDrawables(playNumDrawable, null, null, null);
        ((TextView) rootLayout.findViewById(R.id.bgm_detail_like)).setCompoundDrawables(danmakuNumDrawable, null, null, null);

        setBangumiIcon();

        if(bangumiModel.bangumi_episodes.size() != 0)
        {
            rootLayout.findViewById(R.id.bgm_detail_video_part_layout).setVisibility(View.VISIBLE);
            ((TextView) rootLayout.findViewById(R.id.bgm_detail_video_part_text)).setText("正片-共" + bangumiModel.bangumi_episodes.size() + bangumiModel.bangumi_type_ep);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getParent());
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            uiDetailEpisodesRecyclerView.setLayoutManager(layoutManager);
            episodesRecyclerViewAdapter = new BangumiEpisodeAdapter(bangumiModel.bangumi_episodes, bangumiModel, 1);
            episodesRecyclerViewAdapter.setOnItemClickListener(new BangumiEpisodeAdapter.OnItemClickListener()
            {
                @Override
                public void onItemClick(View view, int position)
                {
                    clickBangumiDetailEpisode(position);
                }
            });
            uiDetailEpisodesRecyclerView.setAdapter(episodesRecyclerViewAdapter);
            if(bangumiModel.bangumi_user_progress_mode == 1)
                ((LinearLayoutManager) uiDetailEpisodesRecyclerView.getLayoutManager()).
                        scrollToPositionWithOffset(bangumiModel.bangumi_user_progress_position, 0);
        }

        if(bangumiModel.bangumi_sections.size() != 0)
        {
            rootLayout.findViewById(R.id.bgm_detail_video_other_layout).setVisibility(View.VISIBLE);
            ((TextView) rootLayout.findViewById(R.id.bgm_detail_video_other_text)).setText(bangumiModel.bangumi_section_name);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getParent());
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            uiDetailSectionsRecyclerView.setLayoutManager(layoutManager);
            sectionsRecyclerViewAdapter = new BangumiEpisodeAdapter(bangumiModel.bangumi_sections, bangumiModel, 2);
            sectionsRecyclerViewAdapter.setOnItemClickListener(new BangumiEpisodeAdapter.OnItemClickListener()
            {
                @Override
                public void onItemClick(View view, int position)
                {
                    clickBangumiDetailOther(position);
                }
            });
            uiDetailSectionsRecyclerView.setAdapter(sectionsRecyclerViewAdapter);
            if(bangumiModel.bangumi_user_progress_mode == 2)
                ((LinearLayoutManager) uiDetailSectionsRecyclerView.getLayoutManager()).
                        scrollToPositionWithOffset(bangumiModel.bangumi_user_progress_position, 0);
        }

        if(bangumiModel.bangumi_seasons.size() > 1)
        {
            rootLayout.findViewById(R.id.bgm_detail_video_season_layout).setVisibility(View.VISIBLE);
            LinearLayout episodesLinearLayout = rootLayout.findViewById(R.id.bgm_detail_video_season);
            for(int i = 0; i < bangumiModel.bangumi_seasons.size(); i++)
                episodesLinearLayout.addView(getVideoSeasonButton(bangumiModel.bangumi_seasons.get(i)));
        }

        rootLayout.findViewById(R.id.bgm_detail_info).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(ctx, TextActivity.class);
                intent.putExtra("title", bangumiModel.bangumi_type_name + "信息");
                intent.putExtra("text", getBangumiInfo());
                startActivity(intent);
            }
        });

        rootLayout.findViewById(R.id.bgm_detail_bt_follow_lay).setOnClickListener(this);
        rootLayout.findViewById(R.id.bgm_detail_bt_cover_lay).setOnClickListener(this);
        rootLayout.findViewById(R.id.bgm_detail_bt_download_lay).setOnClickListener(this);
        rootLayout.findViewById(R.id.bgm_detail_bt_share_lay).setOnClickListener(this);
        rootLayout.findViewById(R.id.bgm_detail_video_part_more).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                clickBangumiMorePart(v);
            }
        });

        rootLayout.findViewById(R.id.bgm_detail_video_other_more).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                clickBangumiMoreOther(v);
            }
        });

        return rootLayout;
    }

    private void setBangumiIcon()
    {
        rootLayout.findViewById(R.id.bgm_detail_loading).setVisibility(View.GONE);
        if(bangumiModel.bangumi_user_is_follow)
        {
            ((ImageView) rootLayout.findViewById(R.id.bgm_detail_bt_follow)).setImageResource(R.drawable.icon_vdd_do_follow_yes);
            ((TextView) rootLayout.findViewById(R.id.bgm_detail_bt_follow_text)).setText("已" + bangumiModel.bangumi_type_follow);
        }
        else
        {
            ((ImageView) rootLayout.findViewById(R.id.bgm_detail_bt_follow)).setImageResource(R.drawable.icon_vdd_do_follow_no);
            ((TextView) rootLayout.findViewById(R.id.bgm_detail_bt_follow_text)).setText(bangumiModel.bangumi_type_follow);
        }
    }


    private void clickBangumiMorePart(View view)
    {
        String[] videoPartNames = new String[bangumiModel.bangumi_episodes.size()];
        for(int i = 0; i < bangumiModel.bangumi_episodes.size(); i++)
            videoPartNames[i] = "第" + (bangumiModel.bangumi_episodes.get(i).position + 1) + bangumiModel.bangumi_type_ep + " " + bangumiModel.bangumi_episodes.get(i).bangumi_episode_title_long;
        Intent intent = new Intent(ctx, SelectPartActivity.class);
        intent.putExtra("title", "选集");
        intent.putExtra("options_name", videoPartNames);
        startActivityForResult(intent, RESULT_DETAIL_EPISODE);
    }

    private void clickBangumiMoreOther(View view)
    {
        String[] videoPartNames = new String[bangumiModel.bangumi_sections.size()];
        for(int i = 0; i < bangumiModel.bangumi_sections.size(); i++)
            videoPartNames[i] = bangumiModel.bangumi_sections.get(i).bangumi_episode_title_long.equals("") ? bangumiModel.bangumi_sections.get(i).bangumi_episode_title : bangumiModel.bangumi_sections.get(i).bangumi_episode_title_long;
        Intent intent = new Intent(ctx, SelectPartActivity.class);
        intent.putExtra("title", "选集");
        intent.putExtra("options_name", videoPartNames);
        startActivityForResult(intent, RESULT_DETAIL_OTHER);
    }

    private void clickBangumiDetailEpisode(int position)
    {
        BangumiModel.BangumiEpisodeModel ep = bangumiModel.bangumi_episodes.get(position);
        bangumiModel.bangumi_user_progress_epid = ep.bangumi_episode_id;
        bangumiModel.bangumi_user_progress_mode = 1;
        bangumiModel.bangumi_user_progress_position = position;
        bangumiModel.bangumi_user_progress_aid = ep.bangumi_episode_aid;
        if(episodesRecyclerViewAdapter != null)
            episodesRecyclerViewAdapter.notifyDataSetChanged();
        if(sectionsRecyclerViewAdapter != null)
            sectionsRecyclerViewAdapter.notifyDataSetChanged();

        bangumiDetailFragmentListener.onBangumiDetailFragmentReplyUpdate(bangumiModel.bangumi_user_progress_epid, bangumiModel.bangumi_user_progress_mode,
                                                                         bangumiModel.bangumi_user_progress_position, bangumiModel.bangumi_user_progress_aid);
        Intent intent = new Intent(ctx, PlayerActivity.class);
        intent.putExtra("title", "第" + (ep.position + 1) +  bangumiModel.bangumi_type_ep + " " + ep.bangumi_episode_title_long);
        intent.putExtra("aid", ep.bangumi_episode_aid);
        intent.putExtra("cid", ep.bangumi_episode_cid);
        startActivity(intent);
    }

    private void clickBangumiDetailOther(int position)
    {
        BangumiModel.BangumiEpisodeModel ss = bangumiModel.bangumi_sections.get(position);
        bangumiModel.bangumi_user_progress_epid = ss.bangumi_episode_id;
        bangumiModel.bangumi_user_progress_mode = 2;
        bangumiModel.bangumi_user_progress_position = position;
        bangumiModel.bangumi_user_progress_aid = ss.bangumi_episode_aid;
        if(episodesRecyclerViewAdapter != null)
            episodesRecyclerViewAdapter.notifyDataSetChanged();
        if(sectionsRecyclerViewAdapter != null)
            sectionsRecyclerViewAdapter.notifyDataSetChanged();

        bangumiDetailFragmentListener.onBangumiDetailFragmentReplyUpdate(bangumiModel.bangumi_user_progress_epid, bangumiModel.bangumi_user_progress_mode,
                                                                         bangumiModel.bangumi_user_progress_position, bangumiModel.bangumi_user_progress_aid);
        Intent intent = new Intent(ctx, PlayerActivity.class);
        intent.putExtra("title", ss.bangumi_episode_title_long.equals("") ? ss.bangumi_episode_title : ss.bangumi_episode_title_long);
        intent.putExtra("aid", ss.bangumi_episode_aid);
        intent.putExtra("cid", ss.bangumi_episode_cid);
        startActivity(intent);
    }

    private TextView getVideoSeasonButton(final BangumiModel.BangumiSeasonModel season)
    {
        TextView textView = new TextView(ctx);
        textView.setWidth(120);
        textView.setBackgroundResource(seasonId.equals(season.bangumi_season_id) ? R.drawable.selector_bg_bangumi_episode_now : R.drawable.selector_bg_bangumi_episode);
        textView.setPadding(12, 6, 12, 6);
        textView.setText(season.bangumi_season_title);
        textView.setLines(1);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setTextSize(14);
        textView.setTextColor(getResources().getColor(seasonId.equals(season.bangumi_season_id) ? R.color.mainColor : R.color.gray_77));
        textView.setOnClickListener(seasonId.equals(season.bangumi_season_id) ? null : new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(ctx, BangumiActivity.class);
                intent.putExtra("season_id", season.bangumi_season_id);
                startActivity(intent);
            }
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 4, 0);
        textView.setLayoutParams(lp);
        return textView;
    }

    private String getBangumiInfo()
    {
        ArrayList<String> detail_info = new ArrayList<>();
        detail_info.add("<b><big><font color=\"#000000\">" + bangumiModel.bangumi_title + "</font></big></b>");
        detail_info.add("");
        detail_info.add(bangumiModel.bangumi_detail_typename + " | " + bangumiModel.bangumi_detail_areas.toString());
        detail_info.add(bangumiModel.bangumi_detail_publish_date);
        detail_info.add(bangumiModel.bangumi_detail_publish_ep);
        detail_info.add("风格：" + bangumiModel.bangumi_detail_styles.toString());
        detail_info.add("");
        detail_info.add("<big><font color=\"#000000\">简介</font></big>");
        detail_info.add(bangumiModel.bangumi_detail_evaluate);
        detail_info.add("");
        detail_info.add("<big><font color=\"#000000\">" + bangumiModel.bangumi_detail_actor_title + "</font></big>");
        detail_info.add(bangumiModel.bangumi_detail_actor_info.replaceAll("\n", "<br/>"));
        detail_info.add("");
        detail_info.add("<big><font color=\"#000000\">" + bangumiModel.bangumi_detail_staff_title + "</font></big>");
        detail_info.add(bangumiModel.bangumi_detail_staff_info.replaceAll("\n", "<br/>"));
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
        void onBangumiDetailFragmentReplyUpdate(String epid, int mode, int position, String aid);
    }
}
