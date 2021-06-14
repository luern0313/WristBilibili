package cn.luern0313.wristbilibili.api;

import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.lson.LsonUtil;
import cn.luern0313.lson.TypeReference;
import cn.luern0313.lson.element.LsonArray;
import cn.luern0313.lson.element.LsonObject;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.models.BaseModel;
import cn.luern0313.wristbilibili.models.ListVideoModel;
import cn.luern0313.wristbilibili.util.MyApplication;
import cn.luern0313.wristbilibili.util.NetWorkUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;

/**
 * 被 luern0313 创建于 2020/4/30.
 */
public class HistoryApi
{
    private final Context ctx;
    private final String csrf;

    private final ArrayList<String> webHeaders;
    public HistoryApi()
    {
        this.ctx = MyApplication.getContext();
        this.csrf = SharedPreferencesUtil.getString(SharedPreferencesUtil.csrf, "");

        this.webHeaders = new ArrayList<String>(){{
            add("Cookie"); add(SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, ""));
            add("Referer"); add("https://www.bilibili.com/");
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_WEB);
        }};
    }

    public ArrayList<ListVideoModel> getHistory(int pn) throws IOException
    {
        String url = "https://api.bilibili.com/x/v2/history";
        String arg = "pn=" + pn + "&ps=30";
        LsonObject result = LsonUtil.parseAsObject(NetWorkUtil.get(url + "?" + arg, webHeaders).body().string());
        ArrayList<ListVideoModel> videoModelArrayList = new ArrayList<>();
        if(result.getInt("code", -1) == 0)
        {
            LsonArray videoJSONArray = result.getJsonArray("data");
            for(int i = 0; i < videoJSONArray.size(); i++)
                videoModelArrayList.add(LsonUtil.fromJson(videoJSONArray.getJsonObject(i), ListVideoModel.class));
        }
        return videoModelArrayList;
    }

    public String delHistory(String aid) throws IOException
    {
        String url = "https://api.bilibili.com/x/v2/history/delete";
        String per = "kid=archive_" + aid + "&jsonp=jsonp&csrf=" + csrf;
        BaseModel<?> baseModel = LsonUtil.fromJson(LsonUtil.parse(NetWorkUtil.post(url, per, webHeaders).body().string()), new TypeReference<BaseModel<?>>(){});
        if(baseModel.isSuccess())
            return "";
        return ctx.getString(R.string.main_error_unknown);
    }
}
