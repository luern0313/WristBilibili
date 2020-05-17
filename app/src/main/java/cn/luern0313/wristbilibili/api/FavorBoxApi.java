package cn.luern0313.wristbilibili.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.models.FavorBoxModel;
import cn.luern0313.wristbilibili.util.NetWorkUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;

/**
 * Created by liupe on 2018/11/25.
 * 收藏还不能用分p助手做好的api。。因为这个是获取自己的。。。
 */

public class FavorBoxApi
{
    private String mid;
    private ArrayList<String> webHeaders;

    private ArrayList<FavorBoxModel> favorBoxArrayList = new ArrayList<FavorBoxModel>();

    public FavorBoxApi(String mid)
    {
        this.mid = mid;
        webHeaders = new ArrayList<String>(){{
            add("Cookie"); add(SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, ""));
            add("Referer"); add("https://www.bilibili.com/");
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_WEB);
        }};
    }

    public ArrayList<FavorBoxModel> getFavorbox() throws IOException
    {
        try
        {
            String url = "http://space.bilibili.com/ajax/fav/getBoxList";
            String arg = "mid=" + mid;
            JSONObject result = new JSONObject(NetWorkUtil.get(url + "?" + arg, webHeaders).body().string());
            if(result.optInt("code") == 0)
            {
                JSONArray favorBoxJSONArray = result.getJSONObject("data").getJSONArray("list");
                for(int i = 0; i < favorBoxJSONArray.length(); i++)
                    favorBoxArrayList.add(new FavorBoxModel(favorBoxJSONArray.getJSONObject(i)));
                return favorBoxArrayList;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
