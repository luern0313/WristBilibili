package cn.luern0313.wristbilibili.models;

import java.io.Serializable;
import java.util.ArrayList;

import cn.luern0313.lson.annotation.field.LsonAddSuffix;
import cn.luern0313.lson.annotation.field.LsonBooleanFormatAsNumber;
import cn.luern0313.lson.annotation.field.LsonJoinArray;
import cn.luern0313.lson.annotation.field.LsonPath;
import cn.luern0313.lson.annotation.method.LsonCallMethod;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.json.ImageUrlFormat;
import cn.luern0313.wristbilibili.util.json.ViewFormat;
import lombok.Getter;
import lombok.Setter;

/**
 * 被 luern0313 创建于 2020/1/21.
 */

@Getter
@Setter
public class BangumiModel implements Serializable
{
    @LsonPath("season_title")
    private String title;

    @LsonAddSuffix("分")
    @LsonPath("rating.score")
    private String score;

    @ViewFormat
    @LsonPath("stat.views")
    private String play;

    @LsonPath("stat.favorites")
    private String like;

    @LsonPath("publish.time_length_show")
    private String series;

    @LsonPath("badge")
    private String needVip;

    @ImageUrlFormat
    @LsonPath("cover")
    private String cover;

    @ImageUrlFormat
    @LsonPath("square_cover")
    private String coverSmall;

    @LsonPath("episodes[*]")
    private ArrayList<BangumiEpisodeModel> episodes;

    @LsonPath("section[*].episodes[*]")
    private ArrayList<BangumiEpisodeModel> sections;

    @LsonJoinArray("&")
    @LsonPath(value = "section[*].title", preClass = String[].class)
    private String sectionName;

    @LsonPath("season")
    private ArrayList<BangumiSeasonModel> seasons;

    @LsonBooleanFormatAsNumber(equal = 1)
    @LsonPath("user_status.follow")
    private boolean userIsFollow;

    @LsonPath("user_status.progress.last_ep_id")
    private String userProgressEpId;
    private int userProgressMode; //1ep 2se
    private int userProgressPosition;
    private String userProgressAid;

    @LsonPath("type_name")
    private String detailTypename; //类型

    @LsonJoinArray(" ")
    @LsonPath(value = "areas[*].name", preClass = String[].class)
    private String detailAreas; //地区

    @LsonPath("publish.release_date_show")
    private String detailPublishDate;

    @LsonPath("publish.time_length_show")
    private String detailPublishEp;

    @LsonJoinArray(" ")
    @LsonPath(value = "styles.name", preClass = String[].class)
    private String detailStyles; //风格

    @LsonPath("evaluate")
    private String detailEvaluate; //简介

    @LsonPath("actor.title")
    private String detailActorTitle; //声优栏标题

    @LsonPath("actor.info")
    private String detailActorInfo;//声优栏信息

    @LsonPath("staff.title")
    private String detailStaffTitle; //staff栏标题

    @LsonPath("staff.info")
    private String detailStaffInfo;//staff栏信息

    private String typeName;
    private String typeFollow;
    private String typeEp;

    @LsonBooleanFormatAsNumber(equal = 1)
    @LsonPath("rights.allow_download")
    private boolean rightDownload;

    @LsonCallMethod(timing = LsonCallMethod.CallMethodTiming.AFTER_DESERIALIZATION)
    private void initData()
    {
        for(int i = 0; i < episodes.size(); i++)
        {
            BangumiEpisodeModel bangumiEpisodeModel = episodes.get(i);
            if(i == 0 && userProgressEpId.equals("0"))
                userProgressEpId = bangumiEpisodeModel.episodeId;
            if(userProgressEpId.equals(bangumiEpisodeModel.episodeId))
            {
                userProgressMode = 1;
                userProgressPosition = i;
                userProgressAid = bangumiEpisodeModel.episodeAid;
            }
        }

        for(int i = 0; i < sections.size(); i++)
        {
            BangumiEpisodeModel bangumiEpisodeModel = sections.get(i);
            if(userProgressEpId.equals(bangumiEpisodeModel.episodeId))
            {
                userProgressMode = 2;
                userProgressPosition = sections.size();
                userProgressAid = bangumiEpisodeModel.episodeAid;
            }
        }

        typeName = (detailTypename.equals("番剧") || detailTypename.equals("国创")) ? "番剧" : "影视";
        typeFollow = typeName.equals("番剧") ? "追番" : "追剧";
        typeEp = typeName.equals("番剧") ? "话" : "集";
    }

    public static String getTitle(int mode, BangumiModel bangumiModel, BangumiModel.BangumiEpisodeModel bangumiEpisodeModel, String separator)
    {
        if(mode == 1)
        {
            if(DataProcessUtil.isInt(bangumiEpisodeModel.episodeTitle))
                return "第" + bangumiEpisodeModel.episodeTitle + bangumiModel.typeEp + separator + bangumiEpisodeModel.episodeTitleLong;
            else
                return bangumiEpisodeModel.episodeTitle + separator + bangumiEpisodeModel.episodeTitleLong;
        }
        else
            return bangumiEpisodeModel.episodeTitleLong.equals("") ? bangumiEpisodeModel.episodeTitle : bangumiEpisodeModel.episodeTitleLong;
    }

    @Getter
    @Setter
    public static class BangumiEpisodeModel implements Serializable
    {
        @LsonPath("title")
        private String episodeTitle;

        @LsonPath("long_title")
        private String episodeTitleLong;

        @LsonPath("id")
        private String episodeId;

        @LsonPath("aid")
        private String episodeAid;

        @LsonPath("cid")
        private String episodeCid;

        @LsonPath("badge")
        private String episodeVip;
    }

    @Getter
    @Setter
    public static class BangumiSeasonModel implements Serializable
    {
        @LsonPath("season_id")
        private String seasonId;

        @LsonPath("season_title")
        private String seasonTitle;
    }
}
