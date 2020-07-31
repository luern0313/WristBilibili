package cn.luern0313.wristbilibili.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 被 luern0313 创建于 2020/5/4.
 */
public class SharedPreferencesUtil
{
    public static String cookies = "cookies";
    public static String mid = "mid";
    public static String csrf = "csrf";
    public static String accessKey = "access_key";
    public static String firstPickUp = "firstPickUp";
    public static String ver = "ver";
    public static String userName = "userName";
    public static String userCoin = "userCoin";
    public static String userLV = "userLV";
    public static String userVip = "userVip";
    public static String tail = "tail";
    public static String tailCustom = "tailCustom";
    public static String tailModel = "tailModel";
    public static String tailAuthor = "tailAuthor";
    public static String tipVd = "tip_vd";
    public static String theme = "theme";

    private static SharedPreferences sharedPreferences = MyApplication.getContext().getSharedPreferences("default", Context.MODE_PRIVATE);
    private static SharedPreferences.Editor editor = sharedPreferences.edit();

    public static boolean contains(String key)
    {
        return sharedPreferences.contains(key);
    }

    public static String getString(String key, String def)
    {
        return sharedPreferences.getString(key, def);
    }

    public static boolean putString(String key, String value)
    {
        return editor.putString(key, value).commit();
    }

    public static int getInt(String key, int def)
    {
        return sharedPreferences.getInt(key, def);
    }

    public static boolean putInt(String key, int value)
    {
        return editor.putInt(key, value).commit();
    }

    public static boolean getBoolean(String key, boolean def)
    {
        return sharedPreferences.getBoolean(key, def);
    }

    public static boolean putBoolean(String key, boolean value)
    {
        return editor.putBoolean(key, value).commit();
    }

    public static boolean removeValue(String key)
    {
        return editor.remove(key).commit();
    }
}
