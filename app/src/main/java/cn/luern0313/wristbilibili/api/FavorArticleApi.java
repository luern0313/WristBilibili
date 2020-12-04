package cn.luern0313.wristbilibili.api;

import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.lson.LsonUtil;
import cn.luern0313.lson.element.LsonArray;
import cn.luern0313.lson.element.LsonObject;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.models.ListArticleModel;
import cn.luern0313.wristbilibili.util.MyApplication;
import cn.luern0313.wristbilibili.util.NetWorkUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;

/**
 * 被 luern0313 创建于 2020/7/29.
 */

public class FavorArticleApi
{
    private Context ctx;
    private String csrf;
    private ArrayList<String> webHeaders;

    public FavorArticleApi()
    {
        this.ctx = MyApplication.getContext();
        this.csrf = SharedPreferencesUtil.getString(SharedPreferencesUtil.csrf, "");
        webHeaders = new ArrayList<String>(){{
            add("Cookie"); add(SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, ""));
            add("Referer"); add("https://space.bilibili.com/");
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_WEB);
        }};
    }

    public ArrayList<ListArticleModel> getFavorArticle(int page) throws IOException
    {
        String url = "https://api.bilibili.com/x/article/favorites/list/all";
        String arg = "ps=16&pn=" + page;
        LsonObject result = LsonUtil.parseAsObject(NetWorkUtil.get(url + "?" + arg, webHeaders).body().string());
        ArrayList<ListArticleModel> arrayList = new ArrayList<>();
        if(result.getAsInt("code", -1) == 0)
        {
            LsonArray v = result.getAsJsonObject("data").getAsJsonArray("favorites");
            for(int i = 0; i < v.size(); i++)
                arrayList.add(LsonUtil.fromJson(v.getAsJsonObject(i), ListArticleModel.class));
            return arrayList;
        }
        return null;
    }

    public String cancelFavorArticle(String id) throws IOException
    {
        String url = "https://api.bilibili.com/x/article/favorites/del";
        String per = "id=" + id + "&csrf=" + csrf;
        LsonObject result = LsonUtil.parseAsObject(NetWorkUtil.post(url, per, webHeaders).body().string());
        if(result.getAsInt("code", -1) == 0)
            return "";
        return ctx.getString(R.string.main_error_unknown);
    }
}
