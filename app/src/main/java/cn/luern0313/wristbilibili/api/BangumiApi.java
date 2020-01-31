package cn.luern0313.wristbilibili.api;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.models.BangumiModel;
import cn.luern0313.wristbilibili.util.NetWorkUtil;
import okhttp3.Response;

/**
 * 被 luern0313 创建于 2020/1/19.
 */

public class BangumiApi
{
    private String cookie;
    private String mid;
    private String csrf;
    private String access_key;
    private ArrayList<String> phoneHeaders = new ArrayList<String>();
    private ArrayList<String> webHeaders = new ArrayList<String>();

    private String season_id;
    private BangumiModel bangumiModel;

    public BangumiApi(String cookies, String mid, String csrf, String access_key, String season_id)
    {
        this.mid = mid;
        this.cookie = cookies;
        this.csrf = csrf;
        this.access_key = access_key;

        this.season_id = season_id;
        phoneHeaders = new ArrayList<String>(){{
            add("Cookie"); add(cookie);
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_OWN);
        }};
        webHeaders = new ArrayList<String>(){{
            add("Cookie"); add(cookie);
            add("Referer"); add("https://www.bilibili.com/anime");
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_WEB);
        }};
    }

    public BangumiModel getBangumiInfo() throws IOException
    {
        try
        {
            String temp_per = "access_key=" + access_key + "&appkey=" + ConfInfoApi.getConf("appkey") +
                "&build=" + ConfInfoApi.getConf("build") + "&platform=android&season_id=" + season_id +
                "&ts=" + (int) (System.currentTimeMillis() / 1000);
            String sign = ConfInfoApi.calc_sign(temp_per);
            Response response = NetWorkUtil.get("https://api.bilibili.com/pgc/view/app/season?" + temp_per + "&sign=" + sign, phoneHeaders);
            JSONObject result = new JSONObject(response.body().string());
            if(result.optInt("code") == 0)
            {
                bangumiModel = new BangumiModel(result.getJSONObject("result"));
                return bangumiModel;
            }
            else
                return null;
        }
        catch (JSONException | NullPointerException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public String followBangumi(boolean isFollow) throws IOException
    {
        try
        {
            String url = isFollow ? "https://api.bilibili.com/pgc/web/follow/add" : "https://api.bilibili.com/pgc/web/follow/del";
            String per = "season_id=" + season_id + "&csrf=" + csrf;
            JSONObject result = new JSONObject(NetWorkUtil.post(url, per, webHeaders).body().string());
            if(result.getInt("code") == 0)
            {
                bangumiModel.bangumi_user_is_follow = isFollow;
                return result.getJSONObject("result").getString("toast");
            }
        }
        catch(JSONException | NullPointerException e)
        {
            e.printStackTrace();
        }
        return (isFollow ? "" : "取消") + "追番错误";
    }

    public String shareBangumi(String text) throws IOException
    {
        //TODO 123
        try
        {
            String url = "https://api.vc.bilibili.com/dynamic_repost/v1/dynamic_repost/share";
            String per = "csrf_token=" + csrf + "&platform=pc&uid="  + "&type=8&share_uid=" + mid + "&content=" + URLEncoder.encode(text, "UTF-8") + "&repost_code=20000&rid=";
            JSONObject result = new JSONObject(NetWorkUtil.post(url, per, webHeaders).body().string());
            if(result.getInt("code") == 0)
                return "";
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
        return "未知错误";
    }
}
