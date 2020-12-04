package cn.luern0313.wristbilibili.api;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.lson.LsonUtil;
import cn.luern0313.lson.element.LsonArray;
import cn.luern0313.lson.element.LsonObject;
import cn.luern0313.wristbilibili.models.ListBangumiModel;
import cn.luern0313.wristbilibili.models.ListVideoModel;
import cn.luern0313.wristbilibili.models.UserListPeopleModel;
import cn.luern0313.wristbilibili.models.UserModel;
import cn.luern0313.wristbilibili.util.NetWorkUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;

/**
 * Created by liupe on 2018/11/13.
 * 好像用不到了。。
 * 谁说的！！
 */

public class UserApi
{
    private String csrf;
    private String access_key;
    private String mid;
    private ArrayList<String> webHeaders;

    private UserModel userModel;

    public UserApi(String mid)
    {
        this.csrf = SharedPreferencesUtil.getString(SharedPreferencesUtil.csrf, "");
        this.access_key = SharedPreferencesUtil.getString(SharedPreferencesUtil.accessKey, "");
        this.mid = mid;

        webHeaders = new ArrayList<String>(){{
            add("Cookie"); add(SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, ""));
            add("Referer"); add("https://www.bilibili.com/");
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_WEB);
        }};
    }

    public UserModel getUserInfo() throws IOException
    {
        String url = "http://app.bilibili.com/x/v2/space";
        String temp_per = "access_key=" + access_key + "&appkey=" + ConfInfoApi.getConf("appkey") + "&build=" +
                ConfInfoApi.getConf("build") + "&mobi_app=" + ConfInfoApi.getConf("mobi_app") + "&platform=" +
                ConfInfoApi.getConf("platform") + "&ps=20&ts=" + (int) (System.currentTimeMillis() / 1000) + "&vmid=" + mid;
        String sign = ConfInfoApi.calc_sign(temp_per, ConfInfoApi.getConf("app_secret"));
        LsonObject result = LsonUtil.parseAsObject(NetWorkUtil.get(url + "?" + temp_per + "&sign=" + sign, webHeaders).body().string());
        if(result.getAsInt("code") == 0)
            userModel = LsonUtil.fromJson(result.getAsJsonObject("data"), UserModel.class);
        return userModel;
    }

    public ArrayList<ListVideoModel> getUserVideo(int page) throws IOException
    {
        String url = "https://api.bilibili.com/x/space/arc/search";
        String arg = "mid=" + mid + "&ps=30&tid=0&pn=" + page + "&keyword=&order=pubdate&jsonp=jsonp";
        LsonObject result = LsonUtil.parseAsObject(NetWorkUtil.get(url + "?" + arg, webHeaders).body().string());
        ArrayList<ListVideoModel> videoModelArrayList = new ArrayList<>();
        if(result.getAsInt("code", -1) == 0)
        {
            LsonArray videoArray = result.getAsJsonObject("data").getAsJsonObject("list").getAsJsonArray("vlist");
            for(int i = 0; i < videoArray.size(); i++)
                videoModelArrayList.add(LsonUtil.fromJson(videoArray.getAsJsonObject(i), ListVideoModel.class));
        }
        return videoModelArrayList;
    }

    public ArrayList<ListBangumiModel> getUserBangumi(int page, int mode) throws IOException
    {
        String url = "https://api.bilibili.com/x/space/bangumi/follow/list";
        String arg = "type=" + mode + "&pn=" + page + "&ps=20&vmid=" + mid;
        LsonObject result = LsonUtil.parseAsObject(NetWorkUtil.get(url + "?" + arg, webHeaders).body().string());
        ArrayList<ListBangumiModel> listBangumiModelArrayList = new ArrayList<>();
        if(result.getAsInt("code", -1) == 0)
        {
            LsonArray bangumiJsonArray = result.getAsJsonObject("data").getAsJsonArray("list");
            for(int i = 0; i < bangumiJsonArray.size(); i++)
                listBangumiModelArrayList.add(LsonUtil.fromJson(bangumiJsonArray.getAsJsonObject(i), ListBangumiModel.class));
            return listBangumiModelArrayList;
        }
        return listBangumiModelArrayList;
    }

    public ArrayList<UserListPeopleModel> getUserFollow(int page) throws IOException
    {
        String url = "https://api.bilibili.com/x/relation/followings";
        String arg = "vmid=" + mid + "&pn=" + page + "&ps=20&order=desc";
        LsonObject result = LsonUtil.parseAsObject(NetWorkUtil.get(url + "?" + arg, webHeaders).body().string());
        ArrayList<UserListPeopleModel> peopleModelArrayList = new ArrayList<>();
        if(result.getAsInt("code", -1) == 0)
        {
            LsonArray peopleJsonArray = result.getAsJsonObject("data").getAsJsonArray("list");
            for (int i = 0; i < peopleJsonArray.size(); i++)
                peopleModelArrayList.add(LsonUtil.fromJson(peopleJsonArray.getAsJsonObject(i), UserListPeopleModel.class));
            return peopleModelArrayList;
        }
        return peopleModelArrayList;
    }

    public ArrayList<UserListPeopleModel> getUserFans(int page) throws IOException, NullPointerException
    {
        String url = "https://api.bilibili.com/x/relation/followers";
        String arg = "vmid=" + mid + "&pn=" + page + "&ps=20&order=desc";
        LsonObject result = LsonUtil.parseAsObject(NetWorkUtil.get(url + "?" + arg, webHeaders).body().string());
        ArrayList<UserListPeopleModel> peopleModelArrayList = new ArrayList<>();
        if(result.getAsInt("code", -1) == 0)
        {
            LsonArray peopleJsonArray = result.getAsJsonObject("data").getAsJsonArray("list");
            for (int i = 0; i < peopleJsonArray.size(); i++)
                peopleModelArrayList.add(LsonUtil.fromJson(peopleJsonArray.getAsJsonObject(i), UserListPeopleModel.class));
            return peopleModelArrayList;
        }
        return peopleModelArrayList;
    }

    public String follow() throws IOException
    {
        try
        {
            String url = "https://api.bilibili.com/x/relation/modify";
            String arg = "fid=" + mid + "&act=1&re_src=11&jsonp=jsonp&csrf=" + csrf;
            JSONObject result = new JSONObject(NetWorkUtil.post(url, arg, webHeaders).body().string());
            if(result.optInt("code") == 0)
                return "";
        }
        catch(JSONException | NullPointerException e)
        {
            e.printStackTrace();
        }
        return "未知错误";
    }

    public String unfollow() throws IOException
    {
        try
        {
            String url = "https://api.bilibili.com/x/relation/modify";
            String arg = "fid=" + mid + "&act=2&re_src=11&jsonp=jsonp&csrf=" + csrf;
            JSONObject result = new JSONObject(NetWorkUtil.post(url, arg, webHeaders).body().string());
            if(result.optInt("code") == 0)
                return "";
        }
        catch (JSONException | NullPointerException e)
        {
            e.printStackTrace();
        }
        return "未知错误";
    }
}
