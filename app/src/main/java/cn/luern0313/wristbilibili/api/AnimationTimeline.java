package cn.luern0313.wristbilibili.api;

import android.graphics.BitmapFactory;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by liupe on 2018/11/10.
 */

public class AnimationTimeline
{
    private final String TIMELINEAPI = "https://bangumi.bilibili.com/web_api/timeline_global";
    private String cookie;
    private JSONArray timelineJson;
    public AnimationTimeline(String cookie)
    {
        this.cookie = cookie;
    }

    public ArrayList<Anim> getAnimTimelineList() throws IOException
    {
        try
        {
            timelineJson = new JSONObject((String) get(TIMELINEAPI, 1)).getJSONArray("result");
            int nowTime = (int) (System.currentTimeMillis() / 1000);
            ArrayList<Anim> anims = new ArrayList<>();
            for(int i = 6; i >= 0; i--)
            {
                JSONArray dailyAnims = timelineJson.getJSONObject(i).getJSONArray("seasons");
                String day = "";
                if(timelineJson.getJSONObject(i).getInt("is_today") == 0) day = timelineJson.getJSONObject(i).getString("date") + " ";
                for(int j = dailyAnims.length() - 1; j >= 0; j--)
                {
                    JSONObject anim = dailyAnims.getJSONObject(j);
                    if(anim.getInt("pub_ts") <= nowTime && anim.getInt("is_published") == 1)
                        anims.add(new Anim(anim.getString("title"), anim.getString("cover"), anim.getString("pub_index"), (int) anim.get("follow"), day + anim.getString("pub_time")));
                }
            }
            return anims;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public class Anim
    {
        public String name;
        public String coverUrl;
        public String lastEpisode;
        public int isfollow;
        public String time;
        Anim(String n, String c, String l, int f, String t)
        {
            name = n;
            coverUrl = c;
            lastEpisode = l;
            isfollow = f;
            time = t;
        }
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
        request = new Request.Builder().url(url).post(body).header("Referer", "https://www.bilibili.com/").addHeader("Accept", "*/*").addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)").addHeader("Referer", "https://passport.bilibili.com/login").addHeader("Cookie", cookie).build();
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
}
