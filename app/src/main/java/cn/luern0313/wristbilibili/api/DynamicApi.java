package cn.luern0313.wristbilibili.api;

import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import cn.luern0313.lson.LsonUtil;
import cn.luern0313.lson.element.LsonArray;
import cn.luern0313.lson.element.LsonObject;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.models.DynamicModel;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.MyApplication;
import cn.luern0313.wristbilibili.util.NetWorkUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;

/**
 * 被 luern0313 创建于 2018/10/30.
 * 动态的api
 * 写完这个文件我才知道为什么程序员被叫成代码民工。。
 * 这绝对就只是个力气活啊。。
 * 不过好在不用动脑子（滑稽）
 * 辛苦b站程序们，动态有至少十几种
 * 我只做了五种23333
 * ——————————————————————————
 * 已经重新制作了
 */

public class DynamicApi
{
    public static final String[] DYNAMIC_TYPES = new String[]{"268435455", "1", "8", "512,4097,4098,4099,4100,4101", "64"};
    private final Context ctx;
    private final String csrf;
    private final String selfMid;
    private final String mid;
    private LsonArray dynamicJsonArray;
    private final boolean isSelf;

    private String lastDynamicId;
    private final ArrayList<String> webHeaders;

    public DynamicApi(String mid, boolean isSelf)
    {
        this.ctx = MyApplication.getContext();
        this.csrf = SharedPreferencesUtil.getString(SharedPreferencesUtil.csrf, "");
        this.selfMid = SharedPreferencesUtil.getString(SharedPreferencesUtil.mid, "");
        this.mid = mid;
        this.isSelf = isSelf;
        webHeaders = new ArrayList<String>(){{
            add("Cookie"); add(SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, ""));
            add("Referer"); add("https://www.bilibili.com/anime");
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_WEB);
        }};
    }

    public void getDynamic() throws IOException
    {
        String url, arg;
        if(isSelf)
        {
            url = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/dynamic_new";
            arg = "uid=" + mid + "&type=" + DYNAMICTYPE;
        }
        else
        {
            url = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history";
            arg = "visitor_uid=" + selfMid + "&host_uid=" + mid + "&offset_dynamic_id=0";
        }
        dynamicJsonArray = LsonUtil.parseAsObject(NetWorkUtil.get(url + "?" + arg, webHeaders).body().string()).getJsonObject("data").getJsonArray("cards");
        if(dynamicJsonArray.size() == 0) dynamicJsonArray = null;
    }

    public void getHistoryDynamic() throws IOException
    {
        String url, arg;
        if(isSelf)
        {
            url = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/dynamic_history";
            arg = "uid=" + mid + "&offset_dynamic_id=" + lastDynamicId + "&type=" + DYNAMICTYPE;
        }
        else {
            url = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history";
            arg = "visitor_uid=" + selfMid + "&host_uid=" + mid + "&offset_dynamic_id=" + lastDynamicId;
        }
        dynamicJsonArray = LsonUtil.parseAsObject(NetWorkUtil.get(url + "?" + arg, webHeaders).body().string()).getJsonObject("data").getJsonArray("cards");
    }

    public ArrayList<DynamicModel.DynamicBaseModel> getDynamicList()
    {
        ArrayList<DynamicModel.DynamicBaseModel> dynamicList = new ArrayList<>();
        for (int i = 0; i < dynamicJsonArray.size(); i++)
        {
            LsonObject dy = dynamicJsonArray.getJsonObject(i);
            LsonObject display = dy.getJsonObject("display");
            DynamicModel.DynamicBaseModel dynamic = getDynamicClass(dy.getString("card", "{}"), dy.getJsonObject("desc"), display, dy.getString("extend_json", "{}"));
            dynamicList.add(dynamic);
            if(i == dynamicJsonArray.size() - 1)
                lastDynamicId = dy.getJsonObject("desc").getString("dynamic_id_str");
        }
        return dynamicList;
    }

    public DynamicModel.DynamicBaseModel getDynamicDetail(String dynamicId) throws IOException
    {
        String url = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/get_dynamic_detail";
        String arg = "dynamic_id=" + dynamicId;
        LsonObject result = LsonUtil.parseAsObject(NetWorkUtil.get(url + "?" + arg, webHeaders).body().string());
        if(result.getInt("code") == 0)
        {
            LsonObject card = result.getJsonObject("data").getJsonObject("card");
            LsonObject display = card.getJsonObject("display");
            return getDynamicClass(card.getString("card", "{}"), card.getJsonObject("desc"), display, card.getString("extend_json", "{}"));
        }
        return null;
    }

    private DynamicModel.DynamicBaseModel getDynamicClass(String cardStr, LsonObject desc, LsonObject display, String extendStr)
    {
        LsonObject dynamic = new LsonObject();
        dynamic.put("card", LsonUtil.parseAsObject(cardStr));
        dynamic.put("desc", desc);
        dynamic.put("display", display);
        dynamic.put("extend", LsonUtil.parseAsObject(extendStr));
        switch(desc.getInt("type"))
        {
            case 1:
                return LsonUtil.fromJson(dynamic, DynamicModel.DynamicShareModel.class, dynamic, false);
            case 2:
                return LsonUtil.fromJson(dynamic, DynamicModel.DynamicAlbumModel.class, dynamic, false);
            case 4:
                return LsonUtil.fromJson(dynamic, DynamicModel.DynamicTextModel.class, dynamic, false);
            case 8:
                return LsonUtil.fromJson(dynamic, DynamicModel.DynamicVideoModel.class, dynamic, false);
            case 64:
                return LsonUtil.fromJson(dynamic, DynamicModel.DynamicArticleModel.class, dynamic, false);
            case 512:
            case 4098:
            case 4099:
            case 4101:
                return LsonUtil.fromJson(dynamic, DynamicModel.DynamicBangumiModel.class, dynamic, false);
            case 2048:
                return LsonUtil.fromJson(dynamic, DynamicModel.DynamicUrlModel.class, dynamic, false);
            case 4200:
                return LsonUtil.fromJson(dynamic, DynamicModel.DynamicLiveModel.class, dynamic, false);
            case 4300:
                return LsonUtil.fromJson(dynamic, DynamicModel.DynamicFavorModel.class, dynamic, false);
        }
        return LsonUtil.fromJson(dynamic, DynamicModel.DynamicUnknownModel.class, dynamic, false);
    }

    public String likeDynamic(String dynamicId, String action) throws IOException //1点赞 2取消
    {
        String url = "https://api.vc.bilibili.com/dynamic_like/v1/dynamic_like/thumb";
        String arg = "uid=" + mid + "&dynamic_id=" + dynamicId + "&up=" + action + "&csrf_token=" + csrf;
        LsonObject result = LsonUtil.parseAsObject(NetWorkUtil.post(url, arg, webHeaders).body().string());
        if(result.getInt("code") == 0)
            return "";
        return "未知错误，点赞失败。。";
    }
}
