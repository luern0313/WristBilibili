package cn.luern0313.wristbilibili.api;

import android.graphics.Bitmap;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;

import cn.luern0313.wristbilibili.ui.MainActivity;
import cn.luern0313.wristbilibili.util.NetWorkUtil;
import cn.luern0313.wristbilibili.util.QRCodeUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
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
            add(ConfInfoApi.USER_AGENT_OWN);
        }};
    }

    public Bitmap getLoginQR() throws Exception
    {
        ArrayList<String> headers = new ArrayList<String>()
        {{
            add("Cookie"); add("sid=" + sid);
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_OWN);
        }};

        String url = "https://passport.bilibili.com/qrcode/getLoginUrl";
        JSONObject loginUrlJson = new JSONObject(NetWorkUtil.get(url, headers).body().string()).getJSONObject("data");
        oauthKey = (String) loginUrlJson.get("oauthKey");
        return QRCodeUtil.createQRCodeBitmap((String) loginUrlJson.get("url"), 120, 120);
    }

    public Response getLoginState() throws IOException
    {
        return NetWorkUtil.post("https://passport.bilibili.com/qrcode/getLoginInfo", "oauthKey=" + oauthKey + "&gourl=https://www.bilibili.com/", defaultHeaders);
    }

    public String Login(String name, String pw)
    {
        try
        {
            JSONObject value = getRequestKey();
            String key = value.getString("key");
            String hash = value.getString("hash");
            pw = encrypt(hash + pw, key);
            name = URLEncoder.encode(name, "UTF-8");
            pw = URLEncoder.encode(pw, "UTF-8");

            String url = "https://passport.bilibili.com/api/oauth2/login";
            ArrayList<String> headers = new ArrayList<String>()
            {{
                add("Referer"); add("http://www.bilibili.com/");
                add("Cookie"); add("sid=" + sid);
                add("User-Agent"); add("");
            }};
            String temp_params = "appkey=" + ConfInfoApi.getBConf("appkey") + "&build=" + ConfInfoApi.getBConf("build") +
                    "&captcha=&mobi_app=" + ConfInfoApi.getBConf("mobi_app") + "&password=" + pw + "&platform=" +
                    ConfInfoApi.getBConf("platform") + "&ts=" + (int) (System.currentTimeMillis() / 1000) + "&username=" + name;
            String sign = ConfInfoApi.calc_sign(temp_params, ConfInfoApi.getBConf("app_secret"));
            JSONObject loginResult = new JSONObject(NetWorkUtil.post(url, temp_params + "&sign=" + sign, headers).body().string());

            if(loginResult.getInt("code") == -629) return "账号或密码错误";
            else if(loginResult.getInt("code") == -105) return "重试次数达到上线，请使用扫码登录或稍后再试";
            else if(loginResult.getInt("code") != 0) return loginResult.getInt("code") + "错误，请使用扫码登录";

            JSONObject resultJSON = loginResult.optJSONObject("data");
            String access_token = resultJSON.getString("access_token");
            String cookie = getCookie(access_token);
            if(cookie.equals(""))
                return "未知错误，请使用扫码登录";

            MainActivity.editor.putString("access_key", access_token);
            MainActivity.editor.putString("cookies", cookie);
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

    private String getCookie(String access_key)
    {
        try
        {
            String url = "https://passport.bilibili.com/api/login/sso";
            ArrayList<String> headers = new ArrayList<String>()
            {{
                add("Content-type"); add("application/x-www-form-urlencoded; charset=UTF-8");
                add("Cookie"); add("sid=" + sid);
                add("user-agent"); add("Mozilla/5.0 BiliDroid/4.34.0 (bbcallen@gmail.com)");
                add("Referer"); add("http://www.bilibili.com/");
                add("Connection"); add("Keep-Alive");
            }};
            String temp_params = "access_key=" + access_key + "&appkey=" + ConfInfoApi.getBConf("appkey") +
                    "&build=" + ConfInfoApi.getBConf("build") + "&gourl=" + URLEncoder.encode("https://account.bilibili.com/account/home", "utf-8") +
                    "&mobi_app=" + ConfInfoApi.getBConf("mobi_app") + "&platform=" + ConfInfoApi.getBConf("platform") +
                    "&ts=" + (int) (System.currentTimeMillis() / 1000);
            String sign = ConfInfoApi.calc_sign(temp_params, ConfInfoApi.getBConf("app_secret"));

            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)
                    .followRedirects(false).readTimeout(15, TimeUnit.SECONDS).build();
            Request.Builder requestBuilder = new Request.Builder().url(url + "?" + temp_params + "&sign=" + sign).get();
            for(int i = 0; i < headers.size(); i+=2)
                requestBuilder = requestBuilder.addHeader(headers.get(i), headers.get(i+1));
            Request request = requestBuilder.build();
            Response response = client.newCall(request).execute();

            List<String> cookieList = response.headers("Set-Cookie");
            StringBuilder cookies = new StringBuilder();
            for(int i = 0; i < cookieList.size(); i++)
            {
                String cookie = cookieList.get(i).split("; ")[0];
                String[] cookie_list = cookie.split("=");
                if(cookie_list[0].equals("DedeUserID"))
                    MainActivity.editor.putString("mid", cookie_list[1]);
                else if(cookie_list[0].equals("bili_jct"))
                    MainActivity.editor.putString("csrf", cookie_list[1]);
                cookies.append(i == 0 ? "" : "; ").append(cookie);
            }
            MainActivity.editor.commit();
            return cookies.toString();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return "";
    }

    private static String encrypt(String str, String key) throws Exception
    {
        key = key.replace("-----BEGIN PUBLIC KEY-----", "");
        key = key.replace("-----END PUBLIC KEY-----", "");
        byte[] decoded = Base64.decode(key, Base64.DEFAULT);
        RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        return Base64.encodeToString(cipher.doFinal(str.getBytes("UTF-8")), Base64.NO_PADDING);
    }

    private JSONObject getRequestKey() throws IOException
    {
        try
        {
            ArrayList<String> headers = new ArrayList<String>()
            {{
                add("Referer"); add("https://passport.bilibili.com/login");
                add("User-Agent"); add(ConfInfoApi.USER_AGENT_WEB);
            }};

            String url = "https://passport.bilibili.com/api/oauth2/getKey";
            String temp_per = "appkey=" + ConfInfoApi.getBConf("appkey");
            String sign = ConfInfoApi.calc_sign(temp_per, ConfInfoApi.getBConf("app_secret"));
            Response response = NetWorkUtil.post(url, "appkey=" + ConfInfoApi.getBConf("appkey") + "&sign=" + sign, headers);
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
                add("Cookie"); add(cookie);
                add("Host"); add("passport.bilibili.com");
                add("Referer"); add("http://www.bilibili.com/");
                add("User-Agent"); add(ConfInfoApi.USER_AGENT_OWN);
            }};
            String url = "https://passport.bilibili.com/login/app/third";
            String temp_per = "api=http://link.acg.tv/forum.php&appkey=27eb53fc9058f8c3&sign=67ec798004373253d60114caaad89a8c";
            Response response = NetWorkUtil.get(url + "?" + temp_per, headers1);

            ArrayList<String> headers2 = new ArrayList<String>()
            {{
                add("Cookie"); add(cookie);
                add("User-Agent"); add(ConfInfoApi.USER_AGENT_WEB);
            }};
            url = new JSONObject(response.body().string()).getJSONObject("data").getString("confirm_uri");

            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .followRedirects(false)
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
}