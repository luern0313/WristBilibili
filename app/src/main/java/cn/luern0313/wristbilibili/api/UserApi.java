package cn.luern0313.wristbilibili.api;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.lson.LsonUtil;
import cn.luern0313.lson.TypeReference;
import cn.luern0313.wristbilibili.models.BaseModel;
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
        BaseModel<UserModel> baseModel = LsonUtil.fromJson(LsonUtil.parse(NetWorkUtil.get(url + "?" + temp_per + "&sign=" + sign, webHeaders).body().string()), new TypeReference<BaseModel<UserModel>>(){});
        if(baseModel.isSuccess())
            userModel = baseModel.getData();
        return userModel;
    }

    public ArrayList<ListVideoModel> getUserVideo(int page) throws IOException
    {
        String url = "https://api.bilibili.com/x/space/arc/search";
        String arg = "mid=" + mid + "&ps=30&tid=0&pn=" + page + "&keyword=&order=pubdate&jsonp=jsonp";
        BaseModel<ArrayList<ListVideoModel>> baseModel = LsonUtil.fromJson(LsonUtil.parse(NetWorkUtil.get(url + "?" + arg, webHeaders).body().string()), new TypeReference<BaseModel<ArrayList<ListVideoModel>>>(){});
        if(baseModel.isSuccess())
            return baseModel.getData();
        return null;
    }

    public ArrayList<ListBangumiModel> getUserBangumi(int page, int mode) throws IOException
    {
        String url = "https://api.bilibili.com/x/space/bangumi/follow/list";
        String arg = "type=" + mode + "&pn=" + page + "&ps=20&vmid=" + mid;
        BaseModel<ArrayList<ListBangumiModel>> baseModel = LsonUtil.fromJson(LsonUtil.parse(NetWorkUtil.get(url + "?" + arg, webHeaders).body().string()), new TypeReference<BaseModel<ArrayList<ListBangumiModel>>>(){});
        if(baseModel.isSuccess())
            return baseModel.getData();
        return null;
    }

    public ArrayList<UserListPeopleModel> getUserFollow(int page) throws IOException
    {
        String url = "https://api.bilibili.com/x/relation/followings";
        String arg = "vmid=" + mid + "&pn=" + page + "&ps=20&order=desc";
        BaseModel<ArrayList<UserListPeopleModel>> baseModel = LsonUtil.fromJson(LsonUtil.parse(NetWorkUtil.get(url + "?" + arg, webHeaders).body().string()), new TypeReference<BaseModel<ArrayList<UserListPeopleModel>>>(){});
        if(baseModel.isSuccess())
            return baseModel.getData();
        return null;
    }

    public ArrayList<UserListPeopleModel> getUserFans(int page) throws IOException, NullPointerException
    {
        String url = "https://api.bilibili.com/x/relation/followers";
        String arg = "vmid=" + mid + "&pn=" + page + "&ps=20&order=desc";
        BaseModel<ArrayList<UserListPeopleModel>> baseModel = LsonUtil.fromJson(LsonUtil.parse(NetWorkUtil.get(url + "?" + arg, webHeaders).body().string()), new TypeReference<BaseModel<ArrayList<UserListPeopleModel>>>(){});
        if(baseModel.isSuccess())
            return baseModel.getData();
        return null;
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