package cn.luern0313.wristbilibili.api;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by gtf35 on 18-12-31.
 * Email is gtfdeyouxiang@Gmail.com
 */
public class OnlineWatchApi {
    private String cookie;
    /*csrf原名bili_jct，是跨站请求伪造，把表单做个唯一标记，保证是你提交的*/
    private String csrf;
    /*mid就是UP主的id号,原名DedeUserID，在这个里是用户登陆后自己的ID，在第一次登录时从coookice获取并保存在SharedPreferences里,在此处是用传参的方式获得*/
    private String mid;
    /*就是那个AV XXXXXX*/
    //废弃，因为我发现QRActivity里有个参数，不用自己构造了
    //private String aid;
    /*请求的食品页的URL*/
    private String mWebUrl;
    /*视频的HTML源代码*/
    private String mWebHtmlCode;
    /*视频的主下载网址*/
    private String mPlayMainUrl;

    public OnlineWatchApi(String cookie, String csrf, String mid, String url)
    {
        this.cookie = cookie;
        this.csrf = csrf;
        this.mid = mid;
        this.mWebUrl = url;
        getWebHtmlCode();
        parseHtmlToGetUrls();
    }

    /*获取web端播放页HTML源代码*/
    private void getWebHtmlCode(){
        //拼接播放页的网址
        mWebHtmlCode = sendPostRequest(mWebUrl);
    }

    /*发送post请求*/
    private String sendPostRequest(String address) {
        try{
            OkHttpClient client = new OkHttpClient.Builder()
                    .build();
            Request.Builder requestBuiler = new Request.Builder()
                    .header("Referer","https://www.bilibili.com/")
                    .addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)")
                    .url(address);
            if(!cookie.equals("")) requestBuiler.addHeader("Cookie", cookie);
            Request request = requestBuiler.build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        }catch (Exception e){
            e.printStackTrace();
            return "###NetWorkError" + e.toString();
        }
    }

    private void parseHtmlToGetUrls(){
        /*哭了，本来想用jsoup，HTML太特喵复杂了，用正则表达式吧*/
        String mainUrlZZ = "\"url\":\"http://.+\",\"backup_url\"";
        Pattern pattern = Pattern.compile(mainUrlZZ);
        Matcher matcher = pattern.matcher(mWebHtmlCode);
        mPlayMainUrl = "###解析视频失败";
        if(matcher.find()) {
            mPlayMainUrl = (matcher.group(0));
            //掐头去尾
            mPlayMainUrl = mPlayMainUrl.replace("\"url\":\"", "");
            mPlayMainUrl = mPlayMainUrl.replace("\",\"backup_url\"", "");
        }

    }

    public String getPlayURL(){
        return mPlayMainUrl;
    }

    public String toString(){
        return " \n" +
                "cookie = " + cookie +"\n" +
                "csrf = " + csrf + "\n" +
                "mid = " + mid + "\n" +
                "mWebUrl = " + mWebUrl + "\n" +
                "mPlayMainUrl = " + mPlayMainUrl + "\n" +
                "mWebHtmlCode = " + mWebHtmlCode + "\n";
    }

}