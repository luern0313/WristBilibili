package cn.luern0313.wristbilibili.api;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.lson.LsonUtil;
import cn.luern0313.lson.element.LsonObject;
import cn.luern0313.wristbilibili.models.LotteryModel;
import cn.luern0313.wristbilibili.util.NetWorkUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;

/**
 * 被 luern0313 创建于 2020/4/30.
 */
public class LotteryApi
{
    private final String dynamicId;
    private final LotteryModel.LotteryDescModel sender;

    private final ArrayList<String> webHeaders;
    public LotteryApi(String dynamicId, LotteryModel.LotteryDescModel sender)
    {
        this.dynamicId = dynamicId;
        this.sender = sender;

        webHeaders = new ArrayList<String>(){{
            add("Cookie"); add(SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, ""));
            add("Referer"); add("https://www.bilibili.com/");
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_WEB);
        }};
    }

    public LotteryModel getLottery() throws IOException
    {
        String url = "https://api.vc.bilibili.com/lottery_svr/v1/lottery_svr/lottery_notice";
        String arg = "dynamic_id=" + dynamicId;
        LsonObject result = LsonUtil.parseAsObject(NetWorkUtil.get(url + "?" + arg, webHeaders).body().string());
        if(result.getInt("code", -1) == 0)
            return LsonUtil.fromJson(result.getJsonObject("data"), LotteryModel.class, sender);
        return null;
    }
}
