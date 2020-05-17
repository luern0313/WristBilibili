package cn.luern0313.wristbilibili.api;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.models.VideoModel;
import cn.luern0313.wristbilibili.util.NetWorkUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;

/**
 * Created by liupe on 2018/11/13.
 * 不给title的api！！！
 * ...
 * ...
 * ...我错了，有这个api，我自罚重写
 */

public class VideoApi
{
    private String csrf;
    private String mid;
    private String access_key;
    public String aid;
    public String bvid;
    private ArrayList<String> appHeaders;
    private ArrayList<String> webHeaders;

    private VideoModel videoModel;

    public VideoApi(String aid, String bvid)
    {
        this.csrf = SharedPreferencesUtil.getString(SharedPreferencesUtil.csrf, "");
        this.mid = SharedPreferencesUtil.getString(SharedPreferencesUtil.mid, "");
        this.access_key = SharedPreferencesUtil.getString(SharedPreferencesUtil.accessKey, "");
        this.aid = aid;
        this.bvid = bvid;
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

    public VideoModel getVideoDetails() throws IOException
    {
        try
        {
            String url = "https://app.bilibili.com/x/v2/view";
            String temp_per;
            if(!bvid.equals(""))
                temp_per = "access_key=" + access_key + "&appkey=" + ConfInfoApi.getConf("appkey") +
                        "&build=" + ConfInfoApi.getConf("build") + "&bvid=" + bvid + "&mobi_app=" + ConfInfoApi.getConf("mobi_app") +
                        "&plat=0&platform=" + ConfInfoApi.getConf("platform") + "&ts=" + (int) (System.currentTimeMillis() / 1000);
            else
                temp_per = "access_key=" + access_key + "&aid=" + aid + "&appkey=" + ConfInfoApi.getConf("appkey") +
                        "&build=" + ConfInfoApi.getConf("build") + "&mobi_app=" + ConfInfoApi.getConf("mobi_app") +
                        "&plat=0&platform=" + ConfInfoApi.getConf("platform") + "&ts=" + (int) (System.currentTimeMillis() / 1000);
            String sign = ConfInfoApi.calc_sign(temp_per, ConfInfoApi.getConf("app_secret"));
            JSONObject result = new JSONObject(NetWorkUtil.get(url + "?" + temp_per + "&sign=" + sign, appHeaders).body().string());
            if(result.optInt("code") == 0)
            {
                videoModel = new VideoModel(result.optJSONObject("data"));
                this.aid = videoModel.video_aid;
                this.bvid = videoModel.video_bvid;
                return videoModel;
            }
        }
        catch (JSONException | NullPointerException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public String likeVideo(int mode) throws IOException  //1好评，2取消差评，3差评，4取消差评，后一个会覆盖前一个
    {
        try
        {
            String url = "https://api.bilibili.com/x/web-interface/archive/like";
            String per = "aid=" + aid + "&like=" + mode + "&csrf=" + csrf;
            JSONObject result = new JSONObject(NetWorkUtil.post(url, per, webHeaders).body().string());
            if(result.optInt("code") == 0)
                return "";
        }
        catch (JSONException | NullPointerException e)
        {
            e.printStackTrace();
        }
        return "未知错误";
    }

    public String coinVideo(int how) throws IOException
    {
        try
        {
            String url = "https://api.bilibili.com/x/web-interface/coin/add";
            String per = "aid=" + aid + "&multiply=" + how + "&cross_domain=true&csrf=" + csrf;
            JSONObject result = new JSONObject(NetWorkUtil.post(url, per, webHeaders).body().string());
            if(result.optInt("code") == 0)
                return "";
        }
        catch (JSONException | NullPointerException e)
        {
            e.printStackTrace();
        }
        return "未知错误";
    }

    public String favVideo(String favId) throws IOException
    {
        try
        {
            String url = "https://api.bilibili.com/medialist/gateway/coll/resource/deal";
            String per = "rid=" + aid + "&type=2&add_media_ids=" + favId + "&del_media_ids=&csrf=" + csrf;
            JSONObject result = new JSONObject(NetWorkUtil.post(url, per, webHeaders).body().string());
            if(result.optInt("code") == 0)
                return "";
        }
        catch (JSONException | NullPointerException e)
        {
            e.printStackTrace();
        }
        return "未知错误";
    }

    public JSONObject tripleVideo() throws IOException
    {
        try
        {
            String url = "https://api.bilibili.com/x/web-interface/archive/like/triple";
            String per = "aid=" + aid + "&csrf=" + csrf;
            return new JSONObject(NetWorkUtil.post(url, per, webHeaders).body().string());
        }
        catch (JSONException | NullPointerException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public String playLater() throws IOException
    {
        try
        {
            String url = "https://api.bilibili.com/x/v2/history/toview/add";
            String per = "aid=" + aid + "&csrf=" + csrf;
            JSONObject result = new JSONObject(NetWorkUtil.post(url, per, webHeaders).body().string());
            if(result.optInt("code") == 0)
                return "";
        }
        catch (JSONException | NullPointerException e)
        {
            e.printStackTrace();
        }
        return "未知错误";
    }

    public String playHistory() throws IOException
    {
        try
        {
            String url = "https://api.bilibili.com/x/report/web/heartbeat";
            String per = "aid=" + aid + "&cid=" + videoModel.video_cid + "&mid=" + mid + "&csrf=" + csrf + "&played_time=0&realtime=0&start_ts=" + (System.currentTimeMillis() / 1000) + "&type=3&dt=2&play_type=1";
            JSONObject result = new JSONObject(NetWorkUtil.post(url, per, webHeaders).body().string());
            if(result.optInt("code") == 0)
                return "";
        }
        catch (JSONException | NullPointerException e)
        {
            e.printStackTrace();
        }
        return "未知错误";
    }

    public String shareVideo(String text) throws IOException
    {
        try
        {
            String url = "https://api.vc.bilibili.com/dynamic_repost/v1/dynamic_repost/share";
            String per = "csrf_token=" + csrf + "&platform=pc&uid=" + videoModel.video_up_mid + "&type=8&share_uid=" + mid + "&content=" + URLEncoder.encode(text, "UTF-8") + "&repost_code=20000&rid=" + videoModel.video_aid;
            JSONObject result = new JSONObject(NetWorkUtil.post(url, per, webHeaders).body().string());
            if(result.optInt("code") == 0)
                return "";
        }
        catch (JSONException | NullPointerException e)
        {
            e.printStackTrace();
        }
        return "未知错误";
    }

    public String scoreVideo(int score) throws IOException
    {
        try
        {
            String url = "https://api.bilibili.com/x/stein/mark";
            String per = "aid=" + aid + "&mark=" + score + "&csrf=" + csrf;
            JSONObject result = new JSONObject(NetWorkUtil.post(url, per, webHeaders).body().string());
            if(result.optInt("code") == 0)
                return "";
        }
        catch (JSONException | NullPointerException e)
        {
            e.printStackTrace();
        }
        return "未知错误";
    }
}
