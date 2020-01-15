package cn.luern0313.wristbilibili.models;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 被 luern0313 创建于 2020/1/15.
 */

public class OthersUserModel
{
    public String uid;
    public String nmae;
    public String face;
    public String sign;
    public String mtime;
    public String verifyType;
    public String verifyName;
    public int vip;
    public OthersUserModel(JSONObject p)
    {
        this.uid = String.valueOf(p.optInt("mid", 0));
        this.nmae = p.optString("uname", "");
        this.face = p.optString("face", "");
        this.sign = p.optString("sign", "");
        this.mtime = getTime(p.optInt("mtime", 0));
        this.verifyType = String.valueOf(p.optJSONObject("official_verify").optInt("type", 0));
        this.verifyName = p.optJSONObject("official_verify").optString("desc", "");
        this.vip = p.optJSONObject("vip").optInt("vipType", 0);
    }

    private String getTime(int timeStamp)
    {
        try
        {
            Date date = new Date(timeStamp * 1000L);
            SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm");
            return format.format(date);
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }
        return "";
    }
}
