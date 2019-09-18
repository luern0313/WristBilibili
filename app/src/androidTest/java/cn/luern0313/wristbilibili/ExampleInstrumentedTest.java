package cn.luern0313.wristbilibili;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;

import cn.luern0313.wristbilibili.ui.MainActivity;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest
{
    @Test
    public void useAppContext() throws Exception
    {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("cn.luern0313.wristbilibili", appContext.getPackageName());
    }

    public String Login(String name, String pw)
    {
        try
        {
            //JSONObject value = getRequestKey();
            //String key = value.optString("key");
            //String hash = value.optString("hash");
            String key = "-----BEGIN PUBLIC KEY-----\nMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDjb4V7EidX/ym28t2ybo0U6t0n\n6p4ej8VjqKHg100va6jkNbNTrLQqMCQCAYtXMXXp2Fwkk6WR+12N9zknLjf+C9sx\n/+l48mjUU8RqahiFD1XT/u2e0m2EN029OhCgkHx3Fc/KlFSIbak93EH/XlYis0w+\nXl69GV6klzgxW6d2xQIDAQAB\n-----END PUBLIC KEY-----\n";
            String hash = "2948a33d72c457b4";
            pw = encrypt(hash + pw, key);
            name = URLEncoder.encode(name, "UTF-8");
            pw = URLEncoder.encode(pw, "UTF-8");

            String temp_params = "appkey=" + ConfInfoApi.getConf("appkey") + "&password=" + pw + "&username=" + name;
            String sign = ConfInfoApi.calc_sign(temp_params);
            System.out.println(temp_params);
            System.out.println(sign);
            JSONObject loginResult = new JSONObject(post("https://passport.bilibili.com/api/v2/oauth2/login", temp_params + "&sign=" + sign, 0).body().string());
            if(loginResult.getInt("code") == -629)
                return "账号或密码错误";
            else if(loginResult.getInt("code") != 0)
            {
                Log.i("bilibili", loginResult.toString());
                return loginResult.getInt("code") + "错误，请使用扫码登录";
            }

            JSONArray cookieJsonArray = loginResult.optJSONObject("data").optJSONObject("cookie_info").optJSONArray("cookies");
            StringBuilder cookie = new StringBuilder();
            for(int i = 0; i < cookieJsonArray.length(); i++)
            {
                JSONObject j = cookieJsonArray.getJSONObject(i);
                cookie.append(j.getString("name")).append("=").append(j.getString("value"));
                if(j.getString("name").equals("DedeUserID"))
                    MainActivity.editor.putString("mid", j.getString("value"));
                else if(j.getString("name").equals("bili_jct"))
                    MainActivity.editor.putString("csrf", j.getString("value"));
                if(i != cookieJsonArray.length() - 1)
                    cookie.append("; ");
            }
            MainActivity.editor.putString("cookies", cookie.toString());
            MainActivity.editor.commit();
            return "";
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return "网络错误，请检查网络";
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "未知错误，请使用扫码登录";
        }
    }

    private static String encrypt(String str, String key_b) throws Exception
    {
        String key = key_b.replace("-----BEGIN PUBLIC KEY-----\n", "");
        key = key.replace("\n-----END PUBLIC KEY-----\n", "");
        key = key.replaceAll("\n", "");
        byte[] decoded = Base64.decode(key, Base64.DEFAULT);
        RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
        //RSA加密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        return Base64.encodeToString(cipher.doFinal(str.getBytes("UTF-8")), Base64.DEFAULT);
    }

    private JSONObject getRequestKey() throws IOException
    {
        try
        {
            String url = "https://passport.bilibili.com/api/oauth2/getKey";
            String temp_per = "appkey=" + ConfInfoApi.getConf("appkey");
            String sign = ConfInfoApi.calc_sign(temp_per);
            return new JSONObject(post(url, "appkey=" + ConfInfoApi.getConf("appkey") + "&sign=" + sign, 2).body().string()).getJSONObject("data");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private Response post(String url, String data, int mode) throws IOException
    {
        Response response;
        OkHttpClient client;
        RequestBody body;
        Request.Builder requestBuilder;
        Request request;
        client = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)//设置连接超时时间
                .readTimeout(15, TimeUnit.SECONDS)//设置读取超时时间
                .build();
        //参数传递
        body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded; charset=utf-8"), data);
        requestBuilder = new Request.Builder().url(url).post(body).addHeader("Accept", "*/*").addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)").addHeader("Referer", "https://passport.bilibili.com/login");
        if(mode == 1) requestBuilder.addHeader("Cookie", "sid:");
        request = requestBuilder.build();
        response = client.newCall(request).execute();
        if(response.isSuccessful()) return response;
        return null;
    }

    static class ConfInfoApi
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
}
