package cn.luern0313.wristbilibili.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.models.ListVideoModel;
import cn.luern0313.wristbilibili.util.NetWorkUtil;

/**
 * 被 luern0313 创建于 2020/4/30.
 */
public class HistoryApi
{
    private String cookie;
    private String csrf;
    private String mid;

    private ArrayList<String> webHeaders;
    public HistoryApi(final String cookie, String csrf, String mid)
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

    public ArrayList<ListVideoModel> getHistory(int pn) throws IOException
    {
        try
        {
            String url = "https://api.bilibili.com/x/v2/history";
            String arg = "pn=" + pn + "&ps=30";
            JSONObject result = new JSONObject(NetWorkUtil.get(url + "?" + arg, webHeaders).body().string());
            ArrayList<ListVideoModel> videos = new ArrayList<>();
            if(result.optInt("code") == 0)
            {
                JSONArray videoJSONArray = result.getJSONArray("data");
                for(int i = 0; i < videoJSONArray.length(); i++)
                    videos.add(new ListVideoModel(videoJSONArray.getJSONObject(i), 0));
                return videos;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
