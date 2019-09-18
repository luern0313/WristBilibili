package cn.luern0313.wristbilibili.api;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 * 被 luern0313 创建于 2019/8/25.
 * 绝 · 密 · 档 · 案
 */

class ConfInfoApi
{
    static String getConf(String key)
    {
        HashMap<String, String> conf =new HashMap<String, String>(){{
            put("appkey", "1d8b6e7d45233436");
            put("actionKey", "appkey");
            put("build", "520001");
            put("device", "android");
            put("mobi_app", "android");
            put("platform", "android");
            put("app_secret", "560c52ccd288fed045859ed18bffd973");
        }};
        return conf.get(key);
    }

    static String calc_sign(String str)
    {
        str += getConf("app_secret");
        return md5(str);
    }

    private static String md5(String plainText) {
        byte[] secretBytes = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            secretBytes = md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("没有md5这个算法！");
        }
        StringBuilder md5code = new StringBuilder(new BigInteger(1, secretBytes).toString(16));
        for (int i = 0; i < 32 - md5code.length(); i++) {
            md5code.insert(0, "0");
        }
        return md5code.toString();
    }
}
