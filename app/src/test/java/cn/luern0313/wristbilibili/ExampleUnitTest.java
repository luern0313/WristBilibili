package cn.luern0313.wristbilibili;

import android.os.Looper;

import org.junit.Test;

import java.util.logging.Handler;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest
{
    String cookie = "sid=l6oy5dnh; fts=1533611049; buvid3=2681F0C1-3137-4DFB-908D-3E9374BD3A8716075infoc; rpdid=kwqkoxxkqkdoskkxoiipw; UM_distinctid=1651271e3dd10b-0b2cc07c23fe09-2711639-144000-1651271e3dec0; LIVE_BUVID=77ab307dad70fc40417a2750f5f02396; LIVE_BUVID__ckMd5=f1c1ba8f1bddf9c2; im_notify_type_8014831=0; pgv_pvi=6519340032; _ga=GA1.2.1629743447.1534431687; im_notify_type_346082378=0; stardustvideo=1; CURRENT_FNVAL=16; finger=17c9e5f5; _cnt_dyn=undefined; _cnt_pm=0; _cnt_notify=0; uTZ=-480; gr_user_id=a1d0d5db-0ec9-4c53-8a46-a38bfb84051b; grwng_uid=cc21e3e1-d2f3-4174-86a4-0be446eada12; im_local_unread_8014831=0; DedeUserID=8014831; DedeUserID__ckMd5=48c2ec4283359190; SESSDATA=cc4c54ef%2C1548727424%2C632394c1; bili_jct=551cbaa844c2de12d3347dcba4c1a6ce; BANGUMI_SS_25739_REC=250470; Hm_lvt_8a6e55dbd2870f0f5bc9194cddf32a02=1544842089,1544858678,1546251705,1546254221; im_seqno_8014831=7758; bp_t_offset_8014831=203689717367228420; CURRENT_QUALITY=112; _dfcaptcha=c9995d8dab45bbc158534f6c4297d6f9";
    String csrf = "551cbaa844c2de12d3347dcba4c1a6ce";

    android.os.Handler handler = new android.os.Handler();
    Runnable runnableUi;
    String url = "";
    @Test
    public void test() throws InterruptedException
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                OnlineWatchApi onlineWatchApi = new OnlineWatchApi(cookie, csrf, "80148310", "http://www.bilibili.com/video/av39745809");
                url = onlineWatchApi.getPlayURL();
                System.out.println(url);
            }
        }).start();
        Thread.sleep(100000);
    }
}

class OnlineWatchApi {
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