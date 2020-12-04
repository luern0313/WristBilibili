package cn.luern0313.wristbilibili.api;


import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.lson.LsonUtil;
import cn.luern0313.lson.element.LsonArray;
import cn.luern0313.lson.element.LsonObject;
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
    private boolean isShowOthersBox;
    private ArrayList<String> webHeaders;

    private ArrayList<FavorBoxModel> favorBoxArrayList = new ArrayList<>();

    public FavorBoxApi(String mid, boolean isShowOthersBox)
    {
        this.mid = mid;
        this.isShowOthersBox = isShowOthersBox;
        webHeaders = new ArrayList<String>(){{
            add("Cookie"); add(SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, ""));
            add("Referer"); add("https://www.bilibili.com/");
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_WEB);
        }};
    }

    public ArrayList<FavorBoxModel> getFavorBox() throws IOException
    {
        String url = "http://space.bilibili.com/ajax/fav/getBoxList";
        String arg = "mid=" + mid;
        String a = NetWorkUtil.get(url + "?" + arg, webHeaders).body().string();
        Log.w("bilibili", a);
        LsonObject result = LsonUtil.parseAsObject(a);
        if(result.getAsBoolean("status", false))
        {
            LsonArray favorBoxJSONArray = result.getAsJsonObject("data").getAsJsonArray("list");
            for(int i = 0; i < favorBoxJSONArray.size(); i++)
                favorBoxArrayList.add(LsonUtil.fromJson(favorBoxJSONArray.getAsJsonObject(i), FavorBoxModel.class));
        }
        if(isShowOthersBox)
        {
            favorBoxArrayList.add(new FavorBoxModel(1));
            favorBoxArrayList.add(new FavorBoxModel(2));
        }
        return favorBoxArrayList;
    }
}
