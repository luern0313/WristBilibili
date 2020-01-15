package cn.luern0313.wristbilibili.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cn.luern0313.wristbilibili.api.ConfInfoApi;
import cn.luern0313.wristbilibili.util.NetWorkUtil;

/**
 * 被 luern0313 创建于 2020/1/15.
 */

public class ReplyModel
{
    private ArrayList<String> defaultHeaders;
    private String csrf;

    private JSONObject replyJson;
    private JSONObject replyUserJson;

    private boolean isLike;
    private boolean isHate;
    private int likeCount;
    private int replyCount;

    private int mode;
    private String oid;

    public ReplyModel(final String cookie, String csrf, JSONObject replyJson, String oid)
    {
        this.csrf = csrf;
        defaultHeaders = new ArrayList<String>(){{
            add("Accept"); add("*/*");
            add("Cookie"); add(cookie);
            add("Referer"); add("https://www.bilibili.com/");
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_WEB);
        }};

        this.replyJson = replyJson;
        this.replyUserJson = replyJson.optJSONObject("member");
        this.isLike = replyJson.optInt("action") == 1;
        this.isHate = replyJson.optInt("action") == 2;
        this.likeCount = replyJson.optInt("like", 0);
        this.replyCount = replyJson.optInt("rcount", 0);
        this.mode = 0;
        this.oid = oid;
    }

    public ReplyModel(int mode)
    {
        this.mode = mode;
    }

    public int getMode()
    {
        return mode;
    }

    public String getReplyId()
    {
        return replyJson.optString("rpid_str");
    }

    public String getUserMid()
    {
        return replyUserJson.optString("mid");
    }

    public String getUserHead()
    {
        return replyUserJson.optString("avatar");
    }

    public String getUserName()
    {
        return replyUserJson.optString("uname");
    }

    public int getUserVip()
    {
        return replyUserJson.optJSONObject("vip").optInt("vipType");
    }

    public String getReplyTime()
    {
        try
        {
            Date date = new Date(replyJson.optInt("ctime") * 1000L);
            SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
            return format.format(date);
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
            return "";
        }
    }

    public int getUserLv()
    {
        return replyUserJson.optJSONObject("level_info").optInt("current_level");
    }

    public String getReplyText()
    {
        return replyJson.optJSONObject("content").optString("message", "");
    }

    public String getReplyFloor(boolean showfloor)
    {
        return replyJson.has("floor") && showfloor ? ("#" + String.valueOf(replyJson.optInt("floor"))) : "";
    }

    public String getReplyBeLiked()
    {
        if(likeCount > 10000) return likeCount / 1000 / 10.0 + "万";
        else return String.valueOf(likeCount);
    }

    public String getReplyBeReply()
    {
        if(replyCount > 10000) return replyCount / 1000 / 10.0 + "万";
        else return String.valueOf(replyCount);
    }

    public boolean isReplyLike()
    {
        return isLike;
    }

    public boolean isReplyDislike()
    {
        return isHate;
    }

    public String likeReply(String rpid, int action, String type)
    {
        try
        {
            JSONObject j = new JSONObject(NetWorkUtil.post("https://api.bilibili.com/x/v2/reply/action", "oid=" + oid + "&type=" + type + "&rpid=" + rpid + "&action=" + action + "&jsonp=jsonp&csrf=" + csrf, defaultHeaders).body().string());
            if(j.getInt("code") == 0)
            {
                isLike = action == 1;
                likeCount += action * 2 - 1;
                isHate = false;
                return "";
            }
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

    public String hateReply(String rpid, int action, String type)
    {
        try
        {
            JSONObject j = new JSONObject(NetWorkUtil.post("https://api.bilibili.com/x/v2/reply/hate", "oid=" + oid + "&type=" + type + "&rpid=" + rpid + "&action=" + action + "&jsonp=jsonp&csrf=" + csrf, defaultHeaders).body().string());
            if(j.getInt("code") == 0)
            {
                if(isLike)
                {
                    likeCount--;
                    isLike = false;
                }
                isHate = action == 1;
                return "";
            }
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
}
