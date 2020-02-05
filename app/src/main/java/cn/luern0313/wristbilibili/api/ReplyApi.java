package cn.luern0313.wristbilibili.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.models.ReplyModel;
import cn.luern0313.wristbilibili.util.NetWorkUtil;

/**
 * 被 luern0313 创建于 2019/2/20.
 * 好麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦
 * 麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦啊
 */

public class ReplyApi
{
    private String cookie;
    private String csrf;
    private String oid;
    private String type;

    private JSONObject replyJson;
    private ArrayList<String> webHeaders = new ArrayList<>();

    public ReplyApi(final String cookie, String csrf, String oid, String type)
    {
        this.cookie = cookie;
        this.csrf = csrf;
        this.oid = oid;
        this.type = type;
        webHeaders = new ArrayList<String>(){{
            add("Cookie"); add(cookie);
            add("Referer"); add("https://www.bilibili.com/");
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_WEB);
        }};
    }

    public String getOid()
    {
        return oid;
    }

    public ArrayList<ReplyModel> getReply(int page, String sort, int limit, String root) throws IOException
    {
        try
        {
            String url = "https://api.bilibili.com/x/v2" + (root.equals("") ? "" : "/reply") + "/reply?pn=" + page + "&type=" + type + "&oid=" + oid + "&sort=" + sort + (root.equals("") ? "" : ("&root=" + root));
            replyJson = new JSONObject(NetWorkUtil.get(url, webHeaders).body().string()).getJSONObject("data");
            JSONArray replyJsonArray = replyJson.getJSONArray("replies");
            ArrayList<ReplyModel> replyArrayList = new ArrayList<>();
            for (int i = 0; i < (limit != 0 ? Math.min(limit, replyJsonArray.length()) : replyJsonArray.length()); i++)
                replyArrayList.add(new ReplyModel(cookie, csrf, replyJsonArray.getJSONObject(i), oid));
            return replyArrayList;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public String sendReply(String rpid, String text)
    {
        try
        {
            String url = "https://api.bilibili.com/x/v2/reply/add";
            String per = "oid=" + oid + "&type=" + type + (rpid.equals("") ? "" : ("&root=" + rpid + "&parent=" + rpid)) + "&message=" + text + "&jsonp=jsonp&csrf=" + csrf;
            JSONObject j = new JSONObject(NetWorkUtil.post(url, per, webHeaders).body().string());
            if(j.getInt("code") == 0)
                return "";
            else
                return j.getString("message");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return "未知问题，请重试？";
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return "网络错误！";
        }
    }

    public boolean isShowFloor()
    {
        try
        {
            return !replyJson.has("config") || replyJson.optJSONObject("config").optInt("showfloor") == 1;
        }
        catch(NullPointerException e)
        {
            return true;
        }
    }
}
