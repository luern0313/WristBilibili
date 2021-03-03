package cn.luern0313.wristbilibili.api;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.lson.LsonUtil;
import cn.luern0313.lson.TypeReference;
import cn.luern0313.wristbilibili.models.SettingModel;
import cn.luern0313.wristbilibili.util.FileUtil;
import cn.luern0313.wristbilibili.util.MyApplication;

/**
 * 被 luern0313 创建于 2021/2/10.
 */

public class SettingApi
{
    private static ArrayList<SettingModel> settingModelArrayList;

    public SettingApi()
    {
        initConfig();
    }

    public ArrayList<SettingModel> getSettingModelArrayList()
    {
        return settingModelArrayList;
    }

    private void initConfig()
    {
        try
        {
            if(settingModelArrayList == null)
                settingModelArrayList = LsonUtil.fromJson(LsonUtil.parse(FileUtil.fileReader(
                        MyApplication.getContext().getAssets().open("setting.json"))), new TypeReference<ArrayList<SettingModel>>(){});
        }
        catch (IOException ignored)
        {
        }
    }
}
