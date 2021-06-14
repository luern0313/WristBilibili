package cn.luern0313.wristbilibili.api;


import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.lson.LsonUtil;
import cn.luern0313.lson.TypeReference;
import cn.luern0313.wristbilibili.models.BaseModel;
import cn.luern0313.wristbilibili.models.FavorBoxModel;
import cn.luern0313.wristbilibili.util.NetWorkUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;

/**
 * Created by liupe on 2018/11/25.
 * 收藏还不能用分p助手做好的api。。因为这个是获取自己的。。。
 */

public class FavorBoxApi
{
    private final String mid;
    private final boolean isShowOthersBox;
    private final ArrayList<String> webHeaders;

    private FavorBoxModel favorBoxModel;

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

    public FavorBoxModel getFavorBox() throws IOException
    {
        String url = "http://space.bilibili.com/ajax/fav/getBoxList";
        String arg = "mid=" + mid;
        String a = NetWorkUtil.get(url + "?" + arg, webHeaders).body().string();
        BaseModel<FavorBoxModel> baseModel = LsonUtil.fromJson(LsonUtil.parse(a), new TypeReference<BaseModel<FavorBoxModel>>(){});
        if(baseModel.isSuccess())
            favorBoxModel = baseModel.getData();
        if(isShowOthersBox)
        {
            favorBoxModel.getBoxModelArrayList().add(new FavorBoxModel.BoxModel(1));
            favorBoxModel.getBoxModelArrayList().add(new FavorBoxModel.BoxModel(2));
        }
        return favorBoxModel;
    }
}
