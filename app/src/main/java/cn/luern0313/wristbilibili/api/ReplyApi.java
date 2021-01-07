package cn.luern0313.wristbilibili.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import cn.luern0313.lson.LsonUtil;
import cn.luern0313.lson.element.LsonArray;
import cn.luern0313.lson.element.LsonObject;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.models.ReplyModel;
import cn.luern0313.wristbilibili.util.MyApplication;
import cn.luern0313.wristbilibili.util.NetWorkUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;

/**
 * 被 luern0313 创建于 2019/2/20.
 * 好麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦
 * 麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦啊
 */

public class ReplyApi
{
    private final String csrf;
    private final String oid;
    private final String type;

    public ArrayList<ReplyModel> replyArrayList = new ArrayList<>();
    public int replyCount;
    public boolean replyIsShowFloor;
    public boolean replyIsEnd;
    private final ArrayList<String> webHeaders;

    public static HashMap<Integer, String> typeMap = new HashMap<Integer, String>()
    {{
        put(1, "17");
        put(2, "11");
        put(4, "17");
        put(8, "1");
        put(64, "12");
        put(512, "1");
        put(4098, "1");
        put(4099, "1");
        put(4101, "1");
        put(2048, "17");
    }};

    public static String[] reportReason = new String[]{"引战", "人身攻击", "垃圾广告", "抢楼", "剧透", "刷屏",
            "侵犯隐私", "内容不相关", "色情", "违法违规", "低俗", "赌博诈骗", "青少年不良信息"};
    public static String[] reportReasonId = new String[]{"4", "7", "1", "16", "5", "3", "15", "8", "2", "9", "10", "12", "17"};

    public ReplyApi(String oid, String type)
    {
        this.csrf = SharedPreferencesUtil.getString(SharedPreferencesUtil.csrf, "");
        this.oid = oid;
        this.type = type;
        this.webHeaders = new ArrayList<String>(){{
            add("Cookie"); add(SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, ""));
            add("Referer"); add("https://www.bilibili.com/");
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_WEB);
        }};
    }

    public String getOid()
    {
        return oid;
    }

    public int getReply(int page, String sort, int limit, ReplyModel root) throws IOException
    {
        String url = "https://api.bilibili.com/x/v2" + (root == null ? "" : "/reply") + "/reply?pn=" + page + "&type=" + type + "&oid=" + oid + "&sort=" + sort + (root == null ? "" : ("&root=" + root.getId()));
        LsonObject result = LsonUtil.parseAsObject(NetWorkUtil.get(url, webHeaders).body().string());
        if(result.getInt("code") == 0)
        {
            LsonObject replyJson = result.getJsonObject("data");
            replyIsShowFloor = !replyJson.has("config") || replyJson.getJsonObject("config").getInt("showfloor") == 1;
            LsonObject replyCountJson = replyJson.getJsonObject("page");
            replyCount = replyCountJson.has("acount") ? replyCountJson.getInt("acount") : replyCountJson.getInt("count");
            LsonObject upper = replyJson.getJsonObject("upper");

            LsonArray replyJsonArray = replyJson.getJsonArray("replies");
            if(page == 1)
            {
                replyArrayList.clear();
                replyIsEnd = false;
                if(root != null) replyArrayList.add(root);

                replyArrayList.add(new ReplyModel(3));
                replyArrayList.add(new ReplyModel(sort.equals("2") ? 1 : 2));
                if(upper.has("top") && !upper.isNull("top") && sort.equals("2"))
                {
                    ReplyModel replyModel = new ReplyModel(upper.getJsonObject("top"), true, String.valueOf(upper.getInt("mid")));
                    replyArrayList.add(LsonUtil.packFromJson(upper.getJsonObject("top"), replyModel));
                }
            }
            int l = limit != 0 ? Math.min(limit, replyJsonArray.size()) : replyJsonArray.size();
            for (int i = 0; i < l; i++)
            {
                ReplyModel replyModel = new ReplyModel(replyJsonArray.getJsonObject(i), false, String.valueOf(upper.getInt("mid")));
                replyArrayList.add(LsonUtil.packFromJson(replyJsonArray.getJsonObject(i), replyModel));
            }

            if(replyCountJson.getInt("num") * replyCountJson.getInt("size") >= replyCountJson.getInt("count"))
                replyIsEnd = true;

            return l;
        }
        return 0;
    }

    public String sendReply(String rpid, String text) throws IOException
    {
        String url = "https://api.bilibili.com/x/v2/reply/add";
        String arg = "oid=" + oid + "&type=" + type + ((rpid == null || rpid.equals("")) ? "" : ("&root=" + rpid + "&parent=" + rpid)) + "&message=" + text + "&jsonp=jsonp&csrf=" + csrf;
        LsonObject result = LsonUtil.parseAsObject(NetWorkUtil.post(url, arg, webHeaders).body().string());
        if(result.getInt("code") == 0)
            return "";
        return MyApplication.getContext().getString(R.string.main_error_unknown);
    }

    public String reportReply(String replyId, String reason) throws IOException
    {
        String url = "https://api.bilibili.com/x/v2/reply/report";
        String arg = "oid=" + oid + "&type=" + type + "&rpid=" + replyId + "&reason=" + reason + "&content=&csrf=" + csrf;

        LsonObject result = LsonUtil.parseAsObject(NetWorkUtil.post(url, arg, webHeaders).body().string());
        if(result.getInt("code") == 0)
            return "";
        return MyApplication.getContext().getString(R.string.main_error_unknown);
    }

    public String likeReply(ReplyModel replyModel, int action, String type)
    {
        try
        {
            String url = "https://api.bilibili.com/x/v2/reply/action";
            String per = "oid=" + oid + "&type=" + type + "&rpid=" + replyModel.getId() + "&action=" + action + "&jsonp=jsonp&csrf=" + csrf;
            LsonObject result = LsonUtil.parseAsObject(NetWorkUtil.post(url, per, webHeaders).body().string());
            if(result.getInt("code") == 0)
            {
                replyModel.setUserLike(action == 1);
                replyModel.setLikeNum(replyModel.getLikeNum() + action * 2 - 1);
                replyModel.setUserDislike(false);
                return "";
            }
            else
                return result.getString("message");
        }
        catch (IOException e)
        {
            return MyApplication.getContext().getString(R.string.main_error_web);
        }
    }

    public String hateReply(ReplyModel replyModel, int action, String type)
    {
        try
        {
            String url = "https://api.bilibili.com/x/v2/reply/hate";
            String per = "oid=" + oid + "&type=" + type + "&rpid=" + replyModel.getId() + "&action=" + action + "&jsonp=jsonp&csrf=" + csrf;
            LsonObject j = LsonUtil.parseAsObject(NetWorkUtil.post(url, per, webHeaders).body().string());
            if(j.getInt("code") == 0)
            {
                if(replyModel.isUserLike())
                {
                    replyModel.setLikeNum(replyModel.getLikeNum() - 1);
                    replyModel.setUserLike(false);
                }
                replyModel.setUserDislike(action == 1);
                return "";
            }
            else
                return j.getString("message");
        }
        catch (IOException e)
        {
            return MyApplication.getContext().getString(R.string.main_error_web);
        }
    }
}
