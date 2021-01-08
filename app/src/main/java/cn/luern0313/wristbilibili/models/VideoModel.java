package cn.luern0313.wristbilibili.models;

import java.io.Serializable;
import java.util.ArrayList;

import cn.luern0313.lson.annotation.field.LsonAddPrefix;
import cn.luern0313.lson.annotation.field.LsonBooleanFormatAsNumber;
import cn.luern0313.lson.annotation.field.LsonDateFormat;
import cn.luern0313.lson.annotation.field.LsonPath;
import cn.luern0313.lson.annotation.method.LsonCallMethod;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.json.ImageUrlFormat;
import cn.luern0313.wristbilibili.util.json.ViewFormat;
import lombok.Getter;
import lombok.Setter;

/**
 * 被 luern0313 创建于 2020/2/2.
 */

@Getter
@Setter
public class VideoModel implements Serializable
{
    @LsonPath
    private String aid;

    @LsonPath
    private String bvid;

    @LsonPath
    private String cid;

    @LsonPath
    private String title;

    @ImageUrlFormat
    @LsonPath("pic")
    private String cover;

    @LsonPath
    private String desc;

    @LsonDateFormat("yyyy-MM-dd HH:mm")
    @LsonPath("pubdate")
    private String time;

    @ViewFormat
    @LsonPath("stat.view")
    private String play;

    @ViewFormat
    @LsonPath
    private String danmaku;

    @LsonPath("argue_msg")
    private String warning = "";

    @LsonPath("pages")
    private ArrayList<VideoPartModel> partList;


    @LsonPath("copyright")
    private int detailCopyright;

    @LsonPath("like")
    private int detailLike;

    @LsonPath("coin")
    private int detailCoin;

    @LsonPath("favorite")
    private int detailFav;


    @LsonPath("owner.name")
    private String upName;

    @ImageUrlFormat
    @LsonPath("owner.face")
    private String upFace;

    @LsonPath("owner.mid")
    private String upMid;

    @LsonPath("owner_ext.official_verify.type")
    private int upOfficial; // -1 0 1

    @LsonPath("owner_ext.vip.vipType")
    private int upVip; // 2

    @LsonPath("owner_ext.fans")
    private int upFanNum;


    @LsonPath("season.season_id")
    private String seasonId;

    @LsonPath("season.title")
    private String seasonTitle;

    @ImageUrlFormat
    @LsonPath("season.cover")
    private String seasonCover;

    @LsonBooleanFormatAsNumber(equal = 1)
    @LsonPath("season.is_finish")
    private boolean seasonIsFinish;

    @LsonPath("season.newest_ep_index")
    private String seasonNewEp;

    @LsonPath("season.total_count")
    private String seasonTotal;


    @LsonBooleanFormatAsNumber(equal = 1)
    @LsonPath("req_user.attention")
    private boolean userFollowUp;

    @LsonBooleanFormatAsNumber(equal = 1)
    @LsonPath("req_user.like")
    private boolean userLike;

    @LsonBooleanFormatAsNumber(equal = 1)
    @LsonPath("req_user.dislike")
    private boolean userDislike;

    @LsonPath("req_user.coin")
    private int userCoin;

    @LsonBooleanFormatAsNumber(equal = 1)
    @LsonPath("req_user.favorite")
    private boolean userFav;

    @LsonPath("history.cid")
    private String userProgressCid;
    private int userProgressPosition;

    @LsonPath("history.progress")
    private int userProgressTime;

    @LsonPath("relates")
    private ArrayList<VideoRecommendModel> recommendList;

    @LsonCallMethod(timing = LsonCallMethod.CallMethodTiming.AFTER_DESERIALIZATION)
    private void initData()
    {
        for(int i = 0; i < partList.size(); i++)
            if(partList.get(i).partCid.equals(userProgressCid))
                userProgressPosition = i;

        DataProcessUtil.printLog(recommendList.toString());
    }

    @Getter
    @Setter
    public static class VideoPartModel implements Serializable
    {
        @LsonPath("cid")
        private String partCid;

        @LsonPath("page")
        private int partNum;

        @LsonAddPrefix("P")
        @LsonPath("part")
        private String partName;
    }

    @Getter
    @Setter
    public static class VideoRecommendModel implements Serializable
    {
        @LsonPath("aid")
        public String recommendVideoAid;

        @LsonPath("title")
        public String recommendVideoTitle;

        @LsonPath("pic")
        public String recommendVideoCover;

        @ViewFormat
        @LsonPath("stat.view")
        public String recommendVideoPlay;

        @ViewFormat
        @LsonPath("stat.danmaku")
        public String recommendVideoDanmaku;

        @LsonPath("owner.name")
        public String recommendVideoOwnerName;
    }
}
