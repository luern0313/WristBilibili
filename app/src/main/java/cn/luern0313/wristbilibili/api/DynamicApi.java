package cn.luern0313.wristbilibili.api;

import android.graphics.BitmapFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.NetWorkUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 被 luern0313 创建于 2018/10/30.
 * 动态的api
 * 写完这个文件我才知道为什么程序员被叫成代码民工。。
 * 这绝对就只是个力气活啊。。
 * 不过好在不用动脑子（滑稽）
 * 辛苦b站程序们，动态有至少十几种
 * 我只做了五种23333
 */

public class DynamicApi
{
    private final String DYNAMICTYPE = "268435455";
    private String csrf;
    private String selfMid;
    private String mid;
    private JSONArray dynamicJsonArray;
    private boolean isSelf;

    private String lastDynamicId;
    private ArrayList<String> webHeaders;

    public DynamicApi(String mid, boolean isSelf)
    {
        this.csrf = SharedPreferencesUtil.getString(SharedPreferencesUtil.csrf, "");
        this.selfMid = SharedPreferencesUtil.getString(SharedPreferencesUtil.mid, "");
        this.mid = mid;
        this.isSelf = isSelf;
        webHeaders = new ArrayList<String>(){{
            add("Cookie"); add(SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, ""));
            add("Referer"); add("https://www.bilibili.com/anime");
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_WEB);
        }};
    }

    public void getDynamic() throws IOException
    {
        try
        {
            if(isSelf)
            {
                String url = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/dynamic_new";
                String arg = "uid=" + mid + "&type=" + DYNAMICTYPE;
                dynamicJsonArray = new JSONObject(NetWorkUtil.get(url + "?" + arg, webHeaders).body().string()).getJSONObject("data").getJSONArray("cards");
            }
            else
            {
                String url = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history";
                String arg = "visitor_uid=" + selfMid + "&host_uid=" + mid + "&offset_dynamic_id=0";
                dynamicJsonArray = new JSONObject(NetWorkUtil.get(url + "?" + arg, webHeaders).body().string()).getJSONObject("data").getJSONArray("cards");
            }
            if(dynamicJsonArray.length() == 0) dynamicJsonArray = null;
        }
        catch (JSONException e)
        {
            dynamicJsonArray = null;
            e.printStackTrace();
        }
    }

    public void getHistoryDynamic() throws IOException
    {
        try
        {
            if(isSelf)
            {
                String url = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/dynamic_history";
                String arg = "uid=" + mid + "&offset_dynamic_id=" + lastDynamicId + "&type=" + DYNAMICTYPE;
                dynamicJsonArray = new JSONObject(NetWorkUtil.get(url + "?" + arg, webHeaders).body().string()).getJSONObject("data").getJSONArray("cards");
            }
            else
            {
                String url = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history";
                String arg = "visitor_uid=" + selfMid + "&host_uid=" + mid + "&offset_dynamic_id=" + lastDynamicId;
                dynamicJsonArray = new JSONObject(NetWorkUtil.get(url + "?" + arg, webHeaders).body().string()).getJSONObject("data").getJSONArray("cards");
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            dynamicJsonArray = new JSONArray();
        }
    }

    public ArrayList<Object> getDynamicList()
    {
        try
        {
            ArrayList<Object> dynamicList = new ArrayList<Object>();
            for (int i = 0; i < dynamicJsonArray.length(); i++)
            {
                JSONObject dy = (JSONObject) dynamicJsonArray.get(i);
                Object d = getDynamicClass(new JSONObject((String) dy.get("card")), dy.getJSONObject("desc"));
                if(d != null) dynamicList.add(d);
                if(i == dynamicJsonArray.length() - 1)
                    lastDynamicId = dy.getJSONObject("desc").getString("dynamic_id_str");
            }
            return dynamicList;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public Object getDynamicClass(String cardJson, int which)
    {
        try
        {
            if(which == 1)  //video
                return new cardOriginalVideo(new JSONObject(cardJson));
            else  //text
                return new cardOriginalText(new JSONObject(cardJson));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private Object getDynamicClass(JSONObject cardJson, JSONObject descJson)
    {
        if(descJson.optInt("type") == 1)  //转载
        {
            if(descJson.optInt("orig_type") == 2 || descJson.optInt("orig_type") == 4)  //文字
                return new cardShareText(cardJson, descJson);
            else if(descJson.optInt("orig_type") == 8)  //视频
                return new cardShareVideo(cardJson, descJson);
            else return new cardUnknow(cardJson, descJson);
        }
        else if((descJson.optInt("type") == 2 || descJson.optInt("type") == 4))  //文字
            return new cardOriginalText(cardJson, descJson);
        else if((descJson.optInt("type") == 8))  //视频
            return new cardOriginalVideo(cardJson, descJson);
        else if(descJson.optInt( "type") == 512 && descJson.optInt("orig_type") == 0)//番剧，暂时不处理
            return null;
        else return new cardUnknow(cardJson, descJson);
    }

    public String likeDynamic(String dynamicId, String action) throws IOException
    {
        try
        {
            String url = "https://api.vc.bilibili.com/dynamic_like/v1/dynamic_like/thumb";
            String arg = "uid=" + mid + "&dynamic_id=" + dynamicId + "&up=" + action + "&csrf_token=" + csrf;
            JSONObject result = new JSONObject(NetWorkUtil.post(url, arg, webHeaders).body().string());
            if(result.getInt("code") == 0)
                return "";
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return "未知错误，点赞失败。。";
    }

    public String sendReply(String oid, String type, String text) throws IOException
    {
        try
        {
            String url = "https://api.bilibili.com/x/v2/reply/add";
            String arg = "oid=" + oid + "&type=" + type + "&message=" + text + "&plat=1&jsonp=jsonp&csrf=" + csrf;
            JSONObject result = new JSONObject(NetWorkUtil.post(url, arg, webHeaders).body().string());
            if(result.getInt("code") == 0)
                return "";
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return "未知错误，评论失败。。";
    }

    public class cardOriginalVideo //原创视频
    {
        private int mode;
        private JSONObject oriVideoJson;
        private JSONObject oriVideoDesc;
        private JSONObject oriVideoDescUser;
        public boolean isLike;
        private int like;

        cardOriginalVideo(JSONObject oriVideoJson, JSONObject oriVideoDesc)
        {
            mode = 1;
            this.oriVideoJson = oriVideoJson;
            this.oriVideoDesc = oriVideoDesc;
            this.oriVideoDescUser = getJsonFromJson(oriVideoDesc, "user_profile");
            this.isLike = oriVideoDesc.optInt("is_liked") == 1;
            this.like = (int) getInfoFromJson(oriVideoDesc, "like");
        }

        public cardOriginalVideo(JSONObject oriVideoJson)
        {
            mode = 2;
            this.oriVideoJson = oriVideoJson;
        }

        public String getDynamicId()
        {
            return oriVideoDesc.optString("dynamic_id_str");
        }

        public String getVideoAid()
        {
            return String.valueOf(getInfoFromJson(oriVideoJson, "aid"));
        }

        public String getVideoTitle()
        {
            return (String) getInfoFromJson(oriVideoJson, "title");
        }

        public String getVideoImg()
        {
            return (String) getInfoFromJson(oriVideoJson, "pic");
        }

        public String getDynamic()
        {
            return (String) getInfoFromJson(oriVideoJson, "dynamic");
        }

        public String getVideoView()
        {
            int view = (int) getInfoFromJson(getJsonFromJson(oriVideoJson, "stat"), "view");
            if(view > 10000) return view / 1000 / 10.0 + "万";
            else return String.valueOf(view);
        }

        public String getVideoDuration()
        {
            return getMinFromSec((int) getInfoFromJson(oriVideoJson, "duration"));
        }

        public String getOwnerUid()
        {
            if(mode == 1)
                return String.valueOf(getInfoFromJson(getJsonFromJson(oriVideoDescUser, "info"), "uid"));
            else
                return String.valueOf(getInfoFromJson(getJsonFromJson(oriVideoJson, "owner"), "mid"));
        }

        public String getOwnerName()
        {
            if(mode == 1)
                return (String) getInfoFromJson(getJsonFromJson(oriVideoDescUser, "info"), "uname");
            else return (String) getInfoFromJson(getJsonFromJson(oriVideoJson, "owner"), "name");
        }

        public String getOwnerHead()
        {
            if(mode == 1)
                return (String) getInfoFromJson(getJsonFromJson(oriVideoDescUser, "info"), "face");
            else return (String) getInfoFromJson(getJsonFromJson(oriVideoJson, "owner"), "face");
        }

        public String getDynamicTime()
        {
            return getTime((int) getInfoFromJson(oriVideoDesc, "timestamp"));
        }

        public int getBeLiked()
        {
            return like;
        }

        public void likeDynamic(int action)
        {
            like += action;
        }

        private String getMinFromSec(int sec)
        {
            String m = String.valueOf(sec / 60);
            String s = String.valueOf(sec - sec / 60 * 60);
            if(m.length() == 1) m = "0" + m;
            if(s.length() == 1) s = "0" + s;
            return m + ":" + s;
        }
    }

    public class cardOriginalText //原创文字/图片
    {
        private int mode;
        private JSONObject oriTextUserJson;
        private JSONObject oriTextItemJson;
        private JSONObject oriTextDesc;
        private JSONObject oriTextDescUser;
        public boolean isLike;
        private int like;

        cardOriginalText(JSONObject oriTextJson, JSONObject oriTextDesc)
        {
            mode = 1;
            this.oriTextItemJson = getJsonFromJson(oriTextJson, "item");
            this.oriTextDesc = oriTextDesc;
            this.oriTextDescUser = getJsonFromJson(oriTextDesc, "user_profile");
            this.isLike = oriTextDesc.optInt("is_liked") == 1;
            this.like = (int) getInfoFromJson(oriTextDesc, "like");
        }

        cardOriginalText(JSONObject oriTextJson)
        {
            mode = 2;
            this.oriTextUserJson = getJsonFromJson(oriTextJson, "user");
            this.oriTextItemJson = getJsonFromJson(oriTextJson, "item");
        }

        public String getDynamicId(int mode)
        {
            if(mode == 1)
            {
                if(getTextImgCount().equals("0")) return oriTextDesc.optString("dynamic_id_str");
                else return String.valueOf(oriTextItemJson.optInt("id"));
            }
            else
            {
                return oriTextDesc.optString("dynamic_id_str");
            }
        }

        public String getDynamicText()
        {
            if(getTextImgCount().equals("0"))
                return (String) getInfoFromJson(oriTextItemJson, "content");
            else return (String) getInfoFromJson(oriTextItemJson, "description");
        }

        public String getTextImgCount()
        {
            if(oriTextItemJson.has("pictures_count"))
                return String.valueOf(getInfoFromJson(oriTextItemJson, "pictures_count"));
            else return "0";
        }

        public String[] getImgsSrc()
        {
            try
            {
                ArrayList<String> picssrc = new ArrayList<>();
                JSONArray pics = oriTextItemJson.getJSONArray("pictures");
                for (int i = 0; i < pics.length(); i++)
                    picssrc.add((String) ((JSONObject) pics.get(i)).get("img_src"));
                return picssrc.toArray(new String[]{});
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            return null;
        }

        public String getUserUid()
        {
            if(mode == 1)
                return String.valueOf(getInfoFromJson(getJsonFromJson(oriTextDescUser, "info"), "uid"));
            else
            {
                return String.valueOf(getInfoFromJson(oriTextUserJson, "uid"));
            }
        }

        public String getUserName()
        {
            if(mode == 1)
                return (String) getInfoFromJson(getJsonFromJson(oriTextDescUser, "info"), "uname");
            else
            {
                if(getTextImgCount().equals("0"))
                    return (String) getInfoFromJson(oriTextUserJson, "uname");
                else return (String) getInfoFromJson(oriTextUserJson, "name");
            }
        }

        public String getUserHead()
        {
            if(mode == 1)
                return (String) getInfoFromJson(getJsonFromJson(oriTextDescUser, "info"), "face");
            else
            {
                if(getTextImgCount().equals("0"))
                    return (String) getInfoFromJson(oriTextUserJson, "face");
                else return (String) getInfoFromJson(oriTextUserJson, "head_url");
            }
        }

        public String getDynamicTime()
        {
            return getTime((int) getInfoFromJson(oriTextDesc, "timestamp"));
        }

        public int getBeLiked()
        {
            return like;
        }

        public void likeDynamic(int action)
        {
            like += action;
        }

        public int getBeReply()
        {
            return oriTextItemJson.optInt("reply");
        }

        public String getReplyType()
        {
            if(getTextImgCount().equals("0")) return "17";
            else return "11";
        }
    }

    public class cardShareVideo //转发视频
    {
        private JSONObject shareVideoJson;
        private JSONObject shareVideoItemJson;
        private JSONObject shareVideoDesc;
        private JSONObject shareVideoDescUser;
        public boolean isLike;
        private int like;

        cardShareVideo(JSONObject shareVideoJson, JSONObject shareVideoDesc)
        {
            this.shareVideoJson = shareVideoJson;
            this.shareVideoItemJson = getJsonFromJson(shareVideoJson, "item");
            this.shareVideoDesc = shareVideoDesc;
            this.shareVideoDescUser = getJsonFromJson(shareVideoDesc, "user_profile");
            this.isLike = shareVideoDesc.optInt("is_liked") == 1;
            this.like = (int) getInfoFromJson(shareVideoDesc, "like");
        }

        public String getDynamicId()
        {
            return shareVideoDesc.optString("dynamic_id_str");
        }

        public String getDynamicText()
        {
            return (String) getInfoFromJson(shareVideoItemJson, "content");
        }

        public String getUserUid()
        {
            return String.valueOf(getInfoFromJson(getJsonFromJson(shareVideoDescUser, "info"), "uid"));
        }

        public String getUserName()
        {
            return (String) getInfoFromJson(getJsonFromJson(shareVideoDescUser, "info"), "uname");
        }

        public String getUserHead()
        {
            return (String) getInfoFromJson(getJsonFromJson(shareVideoDescUser, "info"), "face");
        }

        public String getDynamicTime()
        {
            return getTime((int) getInfoFromJson(shareVideoDesc, "timestamp"));
        }

        public cardOriginalVideo getOriginalVideo()
        {
            try
            {
                return new cardOriginalVideo(new JSONObject((String) getInfoFromJson(shareVideoJson, "origin")));
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            return null;
        }

        public int getBeLiked()
        {
            return like;
        }

        public void likeDynamic(int action)
        {
            like += action;
        }

        public int getBeReply()
        {
            return shareVideoItemJson.optInt("reply");
        }

        public String getReplyType()
        {
            return "17";
        }
    }

    public class cardShareText //转发文字/图片
    {
        private JSONObject shareTextJson;
        private JSONObject shareTextItemJson;
        private JSONObject shareTextDesc;
        private JSONObject shareTextDescUser;
        public boolean isLike;
        private int like;

        cardShareText(JSONObject shareTextJson, JSONObject shareTextDesc)
        {
            this.shareTextJson = shareTextJson;
            this.shareTextItemJson = getJsonFromJson(shareTextJson, "item");
            this.shareTextDesc = shareTextDesc;
            this.shareTextDescUser = getJsonFromJson(shareTextDesc, "user_profile");
            this.isLike = shareTextDesc.optInt("is_liked") == 1;
            this.like = (int) getInfoFromJson(shareTextDesc, "like");
        }

        public String getDynamicId()
        {
            return shareTextDesc.optString("dynamic_id_str");
        }

        public String getDynamicText()
        {
            return (String) getInfoFromJson(shareTextItemJson, "content");
        }

        public String getUserUid()
        {
            return String.valueOf(getInfoFromJson(getJsonFromJson(shareTextDescUser, "info"), "uid"));
        }

        public String getUserName()
        {
            return (String) getInfoFromJson(getJsonFromJson(shareTextDescUser, "info"), "uname");
        }

        public String getUserHead()
        {
            return (String) getInfoFromJson(getJsonFromJson(shareTextDescUser, "info"), "face");
        }

        public String getDynamicTime()
        {
            return getTime((int) getInfoFromJson(shareTextDesc, "timestamp"));
        }

        public cardOriginalText getOriginalText()
        {
            try
            {
                return new cardOriginalText(new JSONObject((String) getInfoFromJson(shareTextJson, "origin")));
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            return null;
        }

        public int getBeLiked()
        {
            return like;
        }

        public void likeDynamic(int action)
        {
            like += action;
        }

        public int getBeReply()
        {
            return shareTextItemJson.optInt("reply");
        }

        public String getReplyType()
        {
            return "17";
        }
    }


    public class cardUnknow //不支持的动态类型
    {
        private JSONObject unknowJson;
        private JSONObject unknowDesc;
        private JSONObject unknowDescUser;

        cardUnknow(JSONObject unknowJson, JSONObject unknowDesc)
        {
            this.unknowJson = unknowJson;
            this.unknowDesc = unknowDesc;
            this.unknowDescUser = getJsonFromJson(unknowDesc, "user_profile");
        }

        public String getOwnerUid()
        {
            try
            {
                return String.valueOf(unknowDescUser.getJSONObject("info").get("uid"));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return "0";
        }

        public String getOwnerName()
        {
            try
            {
                return (String) unknowDescUser.getJSONObject("info").get("uname");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return "未知UP";
        }

        public String getOwnerHead()
        {
            try
            {
                return (String) getInfoFromJson(getJsonFromJson(unknowDescUser, "info"), "face");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return "";
        }

        public String getDynamicTime()
        {
            try
            {
                return DataProcessUtil.getTime(unknowDesc.getInt("timestamp"), "MM-dd HH:mm");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return "未知时间";
        }
    }
}
