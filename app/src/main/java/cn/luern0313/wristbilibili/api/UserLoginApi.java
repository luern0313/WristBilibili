package cn.luern0313.wristbilibili.api;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;

import cn.luern0313.wristbilibili.ui.MainActivity;
import cn.luern0313.wristbilibili.util.NetWorkUtil;
import cn.luern0313.wristbilibili.util.QRCodeUtil;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by liupe on 2018/10/6.
 * 各位大佬好
 */

public class UserLoginApi
{
    private String oauthKey;
    private String sid;
    private static ArrayList<String> defaultHeaders = new ArrayList<String>();

    public UserLoginApi()
    {
        sid = String.valueOf(Math.round(Math.random() * 100000000));
        defaultHeaders = new ArrayList<String>()
        {{
            add("User-Agent");
            add("Wrist Bilibili Client/2.6 (liupeiran0313@163.com)");
        }};
    }

    public Bitmap getLoginQR() throws Exception
    {
        JSONObject loginUrlJson = new JSONObject(
                (String) get("https://passport.bilibili.com/qrcode/getLoginUrl", "sid=" + sid, 1))
                .getJSONObject("data");
        oauthKey = (String) loginUrlJson.get("oauthKey");
        return QRCodeUtil.createQRCodeBitmap((String) loginUrlJson.get("url"), 120, 120);
    }

    public Response getLoginState() throws IOException
    {
        return post("https://passport.bilibili.com/qrcode/getLoginInfo",
                    "oauthKey=" + oauthKey + "&gourl=https://www.bilibili.com/", defaultHeaders);
    }

    public String Login(String name, String pw)
    {
        try
        {
            JSONObject value = getRequestKey();
            String key = value.optString("key");
            String hash = value.optString("hash");
            //String key = "-----BEGIN PUBLIC KEY-----\n" + "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDjb4V7EidX/ym28t2ybo0U6t0n\n" + "6p4ej8VjqKHg100va6jkNbNTrLQqMCQCAYtXMXXp2Fwkk6WR+12N9zknLjf+C9sx\n" + "/+l48mjUU8RqahiFD1XT/u2e0m2EN029OhCgkHx3Fc/KlFSIbak93EH/XlYis0w+\n" + "Xl69GV6klzgxW6d2xQIDAQAB\n" + "-----END PUBLIC KEY-----\n";
            //String hash = "84875218f2deaa1c";
            pw = encrypt(hash + pw, key);
            name = URLEncoder.encode(name, "UTF-8");
            pw = URLEncoder.encode(pw, "UTF-8");

            ArrayList<String> headers = new ArrayList<String>()
            {{
                add("Referer");
                add("http://www.bilibili.com/");
                add("Cookie");
                add("sid=" + sid);
                add("User-Agent");
                add("");
            }};

            String temp_params = "appkey=" + ConfInfoApi.getConf(
                    "appkey") + "&password=" + pw + "&username=" + name;
            String sign = ConfInfoApi.calc_sign(temp_params);
            JSONObject loginResult = new JSONObject(post(
                    "https://passport.bilibili.com/api/oauth2/login", temp_params + "&sign=" + sign,
                    headers).body().string());
            if(loginResult.getInt("code") == -629) return "账号或密码错误";
            else if(loginResult.getInt("code") != 0)
            {
                Log.i("bilibili", loginResult.toString());
                return loginResult.getInt("code") + "错误，请使用扫码登录";
            }
            Log.i("bilibili", loginResult.toString());

            JSONArray cookieJsonArray = loginResult.optJSONObject("data").optJSONObject(
                    "cookie_info").optJSONArray("cookies");
            StringBuilder cookie = new StringBuilder();
            for (int i = 0; i < cookieJsonArray.length(); i++)
            {
                JSONObject j = cookieJsonArray.getJSONObject(i);
                cookie.append(j.getString("name")).append("=").append(j.getString("value"));
                if(j.getString("name").equals("DedeUserID")) MainActivity.editor.putString("mid",
                                                                                           j.getString(
                                                                                                   "value"));
                else if(j.getString("name").equals("bili_jct")) MainActivity.editor.putString(
                        "csrf", j.getString("value"));
                if(i != cookieJsonArray.length() - 1) cookie.append("; ");
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

    private static String encrypt(String str, String key) throws Exception
    {
        key = key.replace("-----BEGIN PUBLIC KEY-----", "");
        key = key.replace("-----END PUBLIC KEY-----", "");
        key = key.replaceAll("\n", "");
        key = key.replaceAll("\r", "");
        byte[] decoded = Base64.decode(key, Base64.DEFAULT);
        RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(
                new X509EncodedKeySpec(decoded));
        //RSA加密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        return Base64.encodeToString(cipher.doFinal(str.getBytes("UTF-8")), Base64.DEFAULT);
    }

    private JSONObject getRequestKey() throws IOException
    {
        try
        {
            ArrayList<String> headers = new ArrayList<String>()
            {{
                add("Referer");
                add("https://passport.bilibili.com/login");
                add("User-Agent");
                add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36");
            }};

            String url = "https://passport.bilibili.com/api/oauth2/getKey";
            String temp_per = "appkey=" + ConfInfoApi.getConf("appkey");
            String sign = ConfInfoApi.calc_sign(temp_per);
            Response response = post(url,
                                     "appkey=" + ConfInfoApi.getConf("appkey") + "&sign=" + sign,
                                     headers);
            sid = response.header("set-header");
            return new JSONObject(response.body().string()).getJSONObject("data");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public String getAccessKey(final String cookie) throws IOException
    {
        try
        {
            ArrayList<String> headers1 = new ArrayList<String>()
            {{
                add("Cookie");
                add(cookie);
                add("Host");
                add("passport.bilibili.com");
                add("Referer");
                add("http://www.bilibili.com/");
                add("User-Agent");
                add(ConfInfoApi.USER_AGENT_OWN);
            }};
            String url = "https://passport.bilibili.com/login/app/third";
            String temp_per = "api=http://link.acg.tv/forum.php&appkey=27eb53fc9058f8c3&sign=67ec798004373253d60114caaad89a8c";
            Response response = NetWorkUtil.get(url + "?" + temp_per, headers1);

            ArrayList<String> headers2 = new ArrayList<String>()
            {{
                add("Cookie");
                add(cookie);
                add("User-Agent");
                add(ConfInfoApi.USER_AGENT_WEB);
            }};
            url = new JSONObject(response.body().string()).getJSONObject("data").getString("confirm_uri");

            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)//设置读取超时时间
                    .followRedirects(false)  //禁制OkHttp的重定向操作，我们自己处理重定向
                    .followSslRedirects(false).build();
            Request.Builder requestBuilder = new Request.Builder().url(url);
            for (int i = 0; i < headers2.size(); i += 2)
                requestBuilder = requestBuilder.addHeader(headers2.get(i), headers2.get(i + 1));
            Request request = requestBuilder.build();
            response = client.newCall(request).execute();
            String url_location = response.header("location");
            return url_location.substring(url_location.indexOf("access_key=")+11, url_location.indexOf("&", url_location.indexOf("access_key=")));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return "";
    }

    public String getOauthKey()
    {
        return oauthKey;
    }

    private Object get(String url, String cookie, int mode) throws Exception
    {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS).build();
        Request.Builder requestb = new Request.Builder().url(url).header("Referer", "https://www.bilibili.com/")
                .addHeader("Accept", "*/*").addHeader("User-Agent",
                                                      "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        if(!cookie.equals("")) requestb.addHeader("Cookie", cookie);
        Request request = requestb.build();
        Response response = client.newCall(request).execute();

        if(response.isSuccessful())
        {
            if(mode == 1) return response.body().string();
            else if(mode == 2)
            {
                byte[] buffer = readStream(response.body().byteStream());
                return BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
            }
        }
        return null;
    }

    private Response post(String url, String data, ArrayList<String> headers) throws IOException
    {
        Request.Builder requestBuilder;
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(15,
                                                                        TimeUnit.SECONDS)//设置连接超时时间
                .readTimeout(15, TimeUnit.SECONDS)//设置读取超时时间
                .build();
        //参数传递
        RequestBody body = RequestBody.create(
                MediaType.parse("application/x-www-form-urlencoded; charset=utf-8"), data);
        requestBuilder = new Request.Builder().url(url).post(body);
        for (int i = 0; i < headers.size(); i += 2)
            requestBuilder = requestBuilder.addHeader(headers.get(i), headers.get(i + 1));
        Request request = requestBuilder.build();
        Response response = client.newCall(request).execute();
        if(response.isSuccessful()) return response;
        return null;
    }

    private byte[] readStream(InputStream inStream) throws Exception
    {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1)
        {
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        inStream.close();
        return outStream.toByteArray();
    }

}