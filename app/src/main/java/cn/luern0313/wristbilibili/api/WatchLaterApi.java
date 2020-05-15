package cn.luern0313.wristbilibili.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.models.WatchLaterModel;
import cn.luern0313.wristbilibili.util.NetWorkUtil;

/**
 * 被 luern0313 创建于 2019/8/31.
 * 仅用于<稍后再看>界面
 * 添加稍后再看在视频详情界面和视频详情api
 */

public class WatchLaterApi
{
    private String cookie;
    private String csrf;
    private String mid;

    private ArrayList<String> webHeaders;

    public WatchLaterApi(final String cookie, String csrf, String mid)
    {
        this.cookie = cookie;
        this.csrf = csrf;
        this.mid = mid;
        webHeaders = new ArrayList<String>(){{
            add("Cookie"); add(cookie);
            add("Referer"); add("https://www.bilibili.com/");
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_WEB);
        }};
    }

    public ArrayList<WatchLaterModel> getWatchLater() throws IOException
    {
        try
        {
            String url = "https://api.bilibili.com/x/v2/history/toview/web";
            ArrayList<WatchLaterModel> videoArrayList = new ArrayList<>();
            JSONObject result = new JSONObject(NetWorkUtil.get(url, webHeaders).body().string());
            if(result.optInt("code") == 0)
            {
                JSONArray wlJsonArray = result.getJSONObject("data").getJSONArray("list");
                for(int i = 0; i < wlJsonArray.length(); i++)
                {
                    JSONObject j = wlJsonArray.optJSONObject(i);
                    videoArrayList.add(new WatchLaterModel(j));
                }
                return videoArrayList;
            }

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
