package cn.luern0313.wristbilibili.api;

import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by liupe on 2018/11/29.
 * emmmm
 */

public class SendDynamic
{
    private String cookie;
    private String mid;
    private String csrf;

    private final String URL = "https://api.vc.bilibili.com/dynamic_repost/v1/dynamic_repost/share";
    private int lastRandom;

    public SendDynamic(String cookie, String mid, String csrf)
    {
        this.cookie = cookie;
        this.mid = mid;
        this.csrf = csrf;
    }

    public String getNextShareText()
    {
        int random = (int) (Math.random() * SHARETEXT.length);
        if(random != lastRandom)
        {
            lastRandom = random;
            return SHARETEXT[random];
        }
        else
        {
            return this.getNextShareText();
        }
    }

    public String getNowShareText()
    {
        return SHARETEXT[lastRandom];
    }

    public void shardVideo(String text) throws IOException  //最后视频rid(aid)待补充！！！！！！
    {
        post(URL, "csrf_token=" + csrf + "&platform=pc&uid=8014831&type=8&share_uid=" + mid + "&content=" + URLEncoder.encode(text, "UTF-8") + "&repost_code=20000&rid=37132444");
    }

    private Object get(String url, int mode) throws IOException
    {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).build();
        Request.Builder requestb = new Request.Builder().url(url).header("Referer", "https://www.bilibili.com/anime/timeline").addHeader("Accept", "*/*").addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
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

    private Response post(String url, String data) throws IOException
    {
        Response response;
        OkHttpClient client;
        RequestBody body;
        Request request;
        client = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).build();
        body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded; charset=utf-8"), data);
        request = new Request.Builder().url(url).post(body).header("Referer", "https://www.bilibili.com/").addHeader("Accept", "*/*").addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)").addHeader("Referer", "https://www.bilibili.com").addHeader("Cookie", cookie).build();
        response = client.newCall(request).execute();
        if(response.isSuccessful())
        {
            return response;
        }
        return null;
    }

    private byte[] readStream(InputStream inStream) throws IOException
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

    private final String[] SHARETEXT = new String[]{
            "你永远也猜不到我在用什么东西发这条动态๑乛◡乛๑",
            "论上b站的100种骚操作(っ*'ω'*c)",
            "震惊！我竟然在手表上做了这种事！睿总：下午来我办公室",
            "老师在上面讲课，用手表发条动态来提提神(￣▽￣)",
            "腕上哔哩这个应用做的啊，exciting！发个动态来测试一下(～￣▽￣)～",
            "你们啊，还是图样图森破，要像我一样，多提高自己的知识水平！用手表上b站才是坠吼的！",
            "你为什么用手表上b站这么熟练啊！你到底和b站交♂易过多少次啊！",
            "不要打架，不要打架，快说说手表上b站的好处都有啥，谁说对了下载连接就发给他！",
            "贝爷：沙漠里好无聊啊，只能用手表上上b站打发时间了┌( ´_ゝ` )┐",
            "贝爷：你看这个应用的开发者，蛋白质含量是别的开发者的两倍，去掉头就可以吃了",
            "啊，乖乖♂站好，让我给你手表里下一个b站van♂一♂van",
            "Do you like van♂b站？看来我们是老♂乡( •̀ ω •́ )✧",
            "我王境泽今天就算是无聊死！也不会用手表刷一下b站！.......真香",
            "我原本以为你身为开发者，在两军用户面前能有高论，没想到你竟爆粗鄙之语！",
            "我从未见过有如此厚颜无耻之开发者！",
            "妈妈问我为什么跪在手表前面......",
            "自从手表上下载了这个应用，上课再也听不进去了(#｀-_ゝ-)",
            "腕上哔哩这个软件真是太棒了，我要玩到爆！(一条五毛，括号内删除)(滑稽)",
            "这个应用的出现看来也是命运石之门的选择啊",
            "爆裂吧 现实！粉碎吧 精神！Banishiment this world！腕上哔哩！出现吧！",
            "前方高能！非战斗人员请迅速离开！我要开始用手表上b站了！！",
            "我们未来科技有限公司居然还有贱民员工能写出这么优秀的应用，我们王总很满意",
            "我们成都养鸡二厂还有员工能写出这么优秀的应用，我们敖总很满意",
            "我在用腕上哔哩这个应用，写这个应用的人头上还剩多少头发呢",
            "青春猪头少年不会梦到兔女郎学姐，更不会用手表上b站",
            "因为面临头发危机，腕上哔哩的作者写了个应用，出道成为了偶像！",
            "如果奇迹有颜色....那一定是腕上哔哩的开发者，头发掉光后秃头锃亮的白色！",
            "关于一个人转生变成程序员写腕上哔哩掉光头发这档事......",
            "真相只有一个！这一地头发一定是腕上哔哩的开发者熬夜肝代码掉光的！",
            "你看腕上哔哩这个应用真漂亮，不如我们......",
            "用手表刷b站是种怎样的体验？",
            "世界上有些事，还是让它永远成为谜比较好，比如我是怎么用手表上b站的。",
            "我常常因为用手表上b站而感到与你们格格不入(#｀-_ゝ-)",
            "你们一定是因为家里有矿才能用手机上b站，像我，只能用手表上(#｀-_ゝ-)"
    };
}