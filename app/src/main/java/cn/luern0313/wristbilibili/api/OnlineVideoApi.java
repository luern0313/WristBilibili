package cn.luern0313.wristbilibili.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import cn.luern0313.wristbilibili.util.NetWorkUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;

/**
 * 被 luern0313 创建于 2019/2/28.
 * 播放视频相关
 */

public class OnlineVideoApi
{
    private String csrf;
    private String mid;
    private String aid;
    private String cid;
    private ArrayList<String> webHeaders;

    private JSONObject playUrlJson;
    private String playUrl;

    private static HashMap<String, String> playerHeaders = new HashMap<String, String>()
    {{
        put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36");
        put("Referer", "https://www.bilibili.com/");
        put("Origin", "https://www.bilibili.com/");
    }};


    public OnlineVideoApi(String aid, String cid)
    {
        this.csrf = SharedPreferencesUtil.getString(SharedPreferencesUtil.csrf, "");
        this.mid = SharedPreferencesUtil.getString(SharedPreferencesUtil.mid, "");
        this.aid = aid;
        this.cid = cid;
        webHeaders = new ArrayList<String>(){{
            add("Cookie"); add("buvid3=FE09F518-E432-414C-AF62-4493C27AD0366147infoc" + SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, ""));
            add("Referer"); add("https://www.bilibili.com/");
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_WEB);
        }};
    }

    public void connectionVideoUrl() throws IOException
    {
        try
        {
            String url = "https://api.bilibili.com/x/player/playurl";
            String arg = "avid=" + aid + "&cid=" + cid + "&qn=16&type=mp4";
            playUrlJson = new JSONObject(NetWorkUtil.get(url + "?" + arg, webHeaders).body().string());
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public String getVideoUrl()
    {
        try
        {
            playUrl = playUrlJson.getJSONObject("data").getJSONArray("durl").getJSONObject(0).getString("url");
            return playUrl;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return "";
        }
    }

    public String[] getVideoBackupUrl()
    {
        try
        {
            JSONArray backUpUrl = playUrlJson.getJSONObject("data").getJSONArray("durl").getJSONObject(0).getJSONArray("backup_url");
            ArrayList<String> backUpUrlList = new ArrayList<>();
            for(int i = 0; i < backUpUrl.length(); i++)
                backUpUrlList.add(backUpUrl.optString(i));
            return backUpUrlList.toArray(new String[]{});
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return new String[]{};
        }
    }

    public String getDanmakuUrl()
    {
        return "https://comment.bilibili.com/" + cid + ".xml";
    }

    public String playHistory(int playTime, boolean isFin) throws IOException
    {
        try
        {
            String url = "https://api.bilibili.com/x/report/web/heartbeat";
            if(!isFin)
            {
                String arg = "aid=" + aid + "&cid=" + cid + "&mid=" + mid + "&csrf=" + csrf + "&played_time=" +
                        playTime + "&realtime=" + playTime + "&start_ts=" + (((int) System.currentTimeMillis() / 1000) - playTime) + "&type=3&dt=2&play_type=1";
                JSONObject result = new JSONObject(NetWorkUtil.post(url, arg, webHeaders).body().string());
                if(result.optInt("code") == 0)
                    return "";
            }
            else
            {
                String arg = "aid=" + aid + "&cid=" + cid + "&mid=" + mid + "&csrf=" + csrf + "&played_time=-1&realtime=" +
                        playTime + "&start_ts=" + (((int) System.currentTimeMillis() / 1000) - playTime) + "&type=3&dt=2&play_type=4";
                JSONObject result = new JSONObject(NetWorkUtil.post(url, arg, webHeaders).body().string());
                if(result.optInt("code") == 0)
                    return "";
            }
        }
        catch (JSONException | RuntimeException e)
        {
            e.printStackTrace();
        }
        return "未知错误";
    }

    public static HashMap<String, String> getPlayerHeaders()
    {
        return playerHeaders;
    }
}











