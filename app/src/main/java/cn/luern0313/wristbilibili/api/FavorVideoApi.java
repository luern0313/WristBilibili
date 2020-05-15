package cn.luern0313.wristbilibili.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.models.ListVideoModel;
import cn.luern0313.wristbilibili.util.NetWorkUtil;

/**
 * Created by liupe on 2018/11/26.
 * 视频还要单独出来一个api。。
 */

public class FavorVideoApi
{
    private String cookie;
    private String mid;
    private String csrf;
    private String fid;
    private ArrayList<String> webHeaders;

    public FavorVideoApi(final String cookie, String mid, String csrf, String fid)
    {
        this.cookie = cookie;
        this.mid = mid;
        this.csrf = csrf;
        this.fid = fid;
        webHeaders = new ArrayList<String>(){{
            add("Cookie"); add(cookie);
            add("Referer"); add("https://space.bilibili.com/");
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_WEB);
        }};
    }

    public ArrayList<ListVideoModel> getFavorVideo(int page) throws IOException
    {
        try
        {
            String url = "http://api.bilibili.com/x/space/fav/arc";
            String arg = "vmid=" + mid + "&ps=30&fid=" + fid + "&tid=0&keyword=&pn=" + page + "&order=fav_time";
            JSONObject result = new JSONObject(NetWorkUtil.get(url + "?" + arg, webHeaders).body().string());
            ArrayList<ListVideoModel> arrayList = new ArrayList<>();
            if(result.optInt("code") == 0)
            {
                JSONArray v = result.getJSONObject("data").getJSONArray("archives");
                for(int i = 0; i < v.length(); i++)
                    arrayList.add(new ListVideoModel(v.getJSONObject(i), 0));
                return arrayList;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public String cancelFavVideo(String aid) throws IOException
    {
        try
        {
            String url = "https://api.bilibili.com/medialist/gateway/coll/resource/batch/del";
            String per = "resources=" + aid + ":2&media_id=" + fid + "31&csrf=" + csrf;
            JSONObject result = new JSONObject(NetWorkUtil.post(url, per, webHeaders).body().string());
            if(result.optInt("code") == 0)
                return "";
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return "未知错误";
    }
}
