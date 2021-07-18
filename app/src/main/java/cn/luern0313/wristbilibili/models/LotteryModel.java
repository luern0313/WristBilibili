package cn.luern0313.wristbilibili.models;

import java.io.Serializable;
import java.util.ArrayList;

import cn.luern0313.lson.annotation.field.LsonPath;
import cn.luern0313.lson.annotation.method.LsonCallMethod;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 被 luern0313 创建于 2021/1/11.
 */

@Getter
@Setter
public class LotteryModel
{
    @LsonPath("lottery_id")
    private String lotteryId;

    @LsonPath("business_id")
    private String businessId;

    private LotteryDescModel descModel;

    @LsonPath("status")
    private int status;

    @LsonPath("lottery_time")
    private int timeResult;

    @LsonPath("ts")
    private int timeCurrent;

    @LsonPath("lottery_at_num")
    private int atNum;

    @LsonPath("lottery_feed_limit")
    private int limit;


    private int totalNum;

    @LsonPath("first_prize")
    private int giftFirstNum;

    @LsonPath("first_prize_cmt")
    private String giftFirstName;

    @LsonPath("first_prize_pic")
    private String giftFirstImg;

    @LsonPath("lottery_result.first_prize_result")
    private ArrayList<LotteryUserModel> giftFirstResult;

    @LsonPath("second_prize")
    private int giftSecondNum;

    @LsonPath("second_prize_cmt")
    private String giftSecondName;

    @LsonPath("second_prize_pic")
    private String giftSecondImg;

    @LsonPath("lottery_result.second_prize_result")
    private ArrayList<LotteryUserModel> giftSecondResult;

    @LsonPath("third_prize")
    private int giftThirdNum;

    @LsonPath("third_prize_cmt")
    private String giftThirdName;

    @LsonPath("third_prize_pic")
    private String giftThirdImg;

    @LsonPath("lottery_result.third_prize_result")
    private ArrayList<LotteryUserModel> giftThirdResult;

    public LotteryModel(LotteryDescModel sender)
    {
        this.descModel = sender;
    }

    @LsonCallMethod(timing = LsonCallMethod.CallMethodTiming.AFTER_DESERIALIZATION)
    private void initData()
    {
        totalNum = giftFirstNum + giftSecondNum + giftThirdNum;
    }

    @Getter
    @Setter
    public static class LotteryDescModel implements Serializable
    {
        private String senderId;
        private String senderName;
        private String senderImg;
        private int senderOfficial;
        private int senderVipStatus;
        private String dynamicId;
        private String dynamicText;
        private int time;

        public LotteryDescModel(String senderId, String senderName, String senderImg, int senderOfficial, int senderVipStatus, String dynamicId, String dynamicText, int time)
        {
            this.senderId = senderId;
            this.senderName = senderName;
            this.senderImg = senderImg;
            this.senderOfficial = senderOfficial;
            this.senderVipStatus = senderVipStatus;
            this.dynamicId = dynamicId;
            this.dynamicText = dynamicText;
            this.time = time;
        }
    }

    @Getter
    @Setter
    @ToString
    public static class LotteryUserModel
    {
        @LsonPath("uid")
        private String uid;

        @LsonPath("name")
        private String name;

        @LsonPath("face")
        private String img;
    }

    @Getter
    @Setter
    public static class LotteryGiftModel
    {
        private final String giftName;
        private final String giftImg;
        private final ArrayList<LotteryUserModel> giftResult;

        public LotteryGiftModel(String giftName, String giftImg, ArrayList<LotteryUserModel> giftResult)
        {
            this.giftName = giftName;
            this.giftImg = giftImg;
            this.giftResult = giftResult;
        }
    }
}
