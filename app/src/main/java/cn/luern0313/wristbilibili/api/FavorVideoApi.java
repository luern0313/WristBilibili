package cn.luern0313.wristbilibili.api;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.lson.LsonUtil;
import cn.luern0313.lson.element.LsonArray;
import cn.luern0313.lson.element.LsonObject;
import cn.luern0313.wristbilibili.models.ListVideoModel;
import cn.luern0313.wristbilibili.util.NetWorkUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;

/**
 * Created by liupe on 2018/11/26.
 * 视频还要单独出来一个api。。
 */

public class FavorVideoApi
{
    private String csrf;
    private String mid;
    private String fid;
    private ArrayList<String> webHeaders;

    public FavorVideoApi(String mid, String fid)
    {
        this.csrf = SharedPreferencesUtil.getString(SharedPreferencesUtil.csrf, "");
        this.mid = mid;
        this.fid = fid;
        webHeaders = new ArrayList<String>(){{
            add("Cookie"); add(SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, ""));
            add("Referer"); add("https://space.bilibili.com/");
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_WEB);
        }};
    }

    public ArrayList<ListVideoModel> getFavorVideo(int page) throws IOException
    {
        String url = "http://api.bilibili.com/x/space/fav/arc";
        String arg = "vmid=" + mid + "&ps=30&fid=" + fid + "&tid=0&keyword=&pn=" + page + "&order=fav_time";
        LsonObject result = LsonUtil.parseAsObject(NetWorkUtil.get(url + "?" + arg, webHeaders).body().string());
        ArrayList<ListVideoModel> arrayList = new ArrayList<>();
        if(result.getAsInt("code", -1) == 0)
        {
            LsonArray videoArray = result.getAsJsonObject("data").getAsJsonArray("archives");
            for(int i = 0; i < videoArray.size(); i++)
                arrayList.add(LsonUtil.fromJson(videoArray.getAsJsonObject(i), ListVideoModel.class));
        }
        return arrayList;
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
