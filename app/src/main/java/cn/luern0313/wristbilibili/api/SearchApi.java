package cn.luern0313.wristbilibili.api;

import android.graphics.BitmapFactory;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.luern0313.wristbilibili.fragment.Search;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by liupe on 2018/11/23.
 * 没有搜索的api！！
 * ...
 * ...
 * ...好像有
 */

public class SearchApi
{
    private String cookie;

    private String keyword;
    private int page;
    private JSONArray searchResultNow;
    public SearchApi(String cookie, String keyword)
    {
        this.cookie = cookie;
        this.keyword = keyword;
        page = 0;
    }

    public static String[] getHotWord() throws IOException
    {
        try
        {
            JSONArray hotwordjson = new JSONObject((String) get_static("https://s.search.bilibili.com/main/hotword", 1)).getJSONArray("list");
            String[] hotword = new String[hotwordjson.length()];
            for(int i = 0; i < hotword.length; i++)
                hotword[i] = hotwordjson.getJSONObject(i).getString("keyword");
            return hotword;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<JSONObject> getSearchResult() throws IOException
    {
        page++;
        JSONObject data = new JSONObject();
        try
        {
            data.put("keyword", URLEncoder.encode(keyword, "UTF-8"));
            data.put("main_ver", "v3");
            data.put("order", "totalrank");
            data.put("page", page);
            data.put("pagesize", 20);
            data.put("search_type", "all");
            data.put("platform", "h5");
            String l = post("https://m.bilibili.com/search/searchengine", data.toString()).body().string();
            JSONArray jsonArray = new JSONObject(l)
                    .getJSONObject("result")
                    .getJSONArray("video");
            ArrayList<JSONObject> arrayList = new ArrayList<>();
            for(int i = 0; i < jsonArray.length(); i++)
                arrayList.add(jsonArray.getJSONObject(i));
            return arrayList;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            page--;
        }
        return null;
    }

    private static Object get_static(String url, int mode) throws IOException
    {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).build();
        Request.Builder requestb = new Request.Builder().url(url).header("Referer", "https://www.bilibili.com/anime/timeline").addHeader("Accept", "*/*").addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
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
        body = RequestBody.create(MediaType.parse("application/json"), data);
        request = new Request.Builder().url(url).post(body).header("Referer", "https://m.bilibili.com/").addHeader("Accept", "*/*").addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36").build();
        response = client.newCall(request).execute();
        if(response.isSuccessful())
        {
            return response;
        }
        return null;
    }

    private static byte[] readStream(InputStream inStream) throws IOException
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
