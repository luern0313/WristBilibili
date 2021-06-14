package cn.luern0313.wristbilibili.api;



import android.content.Context;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

import cn.luern0313.lson.LsonUtil;
import cn.luern0313.lson.TypeReference;
import cn.luern0313.lson.element.LsonObject;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.models.BangumiModel;
import cn.luern0313.wristbilibili.models.BangumiRecommendModel;
import cn.luern0313.wristbilibili.models.BaseModel;
import cn.luern0313.wristbilibili.models.ListBangumiModel;
import cn.luern0313.wristbilibili.util.MyApplication;
import cn.luern0313.wristbilibili.util.NetWorkUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;
import okhttp3.Response;

/**
 * 被 luern0313 创建于 2020/1/19.
 */

public class BangumiApi
{
    private final Context ctx;
    private final String mid;
    private final String csrf;
    private final String access_key;
    private final ArrayList<String> appHeaders;
    private final ArrayList<String> webHeaders;

    private final String season_id;
    private BangumiModel bangumiModel;

    public BangumiApi(String season_id)
    {
        this.ctx = MyApplication.getContext();
        this.mid = SharedPreferencesUtil.getString(SharedPreferencesUtil.mid, "");
        this.csrf = SharedPreferencesUtil.getString(SharedPreferencesUtil.csrf , "");
        this.access_key = SharedPreferencesUtil.getString(SharedPreferencesUtil.accessKey, "");

        this.season_id = season_id;
        appHeaders = new ArrayList<String>(){{
            add("Cookie"); add(SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, ""));
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_OWN);
        }};
        webHeaders = new ArrayList<String>(){{
            add("Cookie"); add(SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, ""));
            add("Referer"); add("https://www.bilibili.com/anime");
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_WEB);
        }};
    }

    public BangumiModel getBangumiInfo() throws IOException
    {
        String url = "https://api.bilibili.com/pgc/view/app/season";
        String temp_per = "access_key=" + access_key + "&appkey=" + ConfInfoApi.getConf("appkey") + "&build=" + ConfInfoApi.getConf("build") + "&platform=android&season_id=" + season_id + "&ts=" + (int) (System.currentTimeMillis() / 1000);
        String sign = ConfInfoApi.calc_sign(temp_per, ConfInfoApi.getConf("app_secret"));
        Response response = NetWorkUtil.get(url + "?" + temp_per + "&sign=" + sign, appHeaders);
        BaseModel<BangumiModel> baseModel = LsonUtil.fromJson(LsonUtil.parse(response.body().string()), new TypeReference<BaseModel<BangumiModel>>(){});
        if(baseModel.isSuccess())
            bangumiModel = baseModel.getData();
        return bangumiModel;
    }

    public ArrayList<ListBangumiModel> getBangumiRecommend() throws IOException
    {
        String url = "https://api.bilibili.com/pgc/web/recommend/related/recommend";
        String arg = "season_id=" + season_id;
        BaseModel<BangumiRecommendModel> baseModel = LsonUtil.fromJson(LsonUtil.parse(NetWorkUtil.get(url + "?" + arg, webHeaders).body().string()), new TypeReference<BaseModel<BangumiRecommendModel>>(){});
        if(baseModel.isSuccess())
            return baseModel.getData().getListBangumiModelArrayList();
        return null;
    }

    public String followBangumi(boolean isFollow) throws IOException
    {
        String url = isFollow ? "https://api.bilibili.com/pgc/web/follow/add" : "https://api.bilibili.com/pgc/web/follow/del";
        String per = "season_id=" + season_id + "&csrf=" + csrf;
        BaseModel<LsonObject> baseModel = LsonUtil.fromJson(LsonUtil.parse(NetWorkUtil.post(url, per, webHeaders).body().string()), new TypeReference<BaseModel<LsonObject>>(){});
        if(baseModel.isSuccess())
        {
            bangumiModel.setUserIsFollow(isFollow);
            return baseModel.getData().getString("toast");
        }
        return (isFollow ? "" : "取消") + "追番错误";
    }

    public String shareBangumi(String text) throws IOException
    {
        String url = "https://api.vc.bilibili.com/dynamic_repost/v1/dynamic_repost/share";
        String per = "csrf_token=" + csrf + "&platform=pc&type=8&uid=&share_uid=" + mid + "&content=" + URLEncoder.encode(text, "UTF-8") + "&repost_code=20000&rid=" + bangumiModel.getUserProgressAid();
        BaseModel<?> baseModel = LsonUtil.fromJson(LsonUtil.parse(NetWorkUtil.post(url, per, webHeaders).body().string()), new TypeReference<BaseModel<?>>(){});
        if(baseModel.isSuccess())
            return "";
        return ctx.getString(R.string.main_error_unknown);
    }

    public String sendReply(String text) throws IOException
    {
        String url = "https://api.bilibili.com/x/v2/reply/add";
        String per = "oid=" + bangumiModel.getUserProgressAid() + "&type=1&message=" + text + "&plat=1&jsonp=jsonp&csrf=" + csrf;
        BaseModel<?> baseModel = LsonUtil.fromJson(LsonUtil.parse(NetWorkUtil.post(url, per, webHeaders).body().string()), new TypeReference<BaseModel<?>>(){});
        if(baseModel.isSuccess())
            return "";
        return ctx.getString(R.string.main_error_unknown);
    }
}
