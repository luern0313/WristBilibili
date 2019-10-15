package cn.luern0313.wristbilibili;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.zip.Inflater;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest
{
    @Test
    public void test() throws InterruptedException
    {
        try
        {
            System.out.println(get("https://comment.bilibili.com/1548861.xml", 1));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static Object get(String url, int mode) throws IOException
    {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS).build();
        Request.Builder requestb = new Request.Builder().url(url).header("Accept-Encoding", "gzip, deflate, flate").addHeader("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7,zh-TW;q=0.6");
        Request request = requestb.build();
        Response response = client.newCall(request).execute();
        ResponseBody responseBody = response.body();

        return new String(uncompress(responseBody.bytes()), "UTF-8");
    }

    private static byte[] uncompress(byte[] inputByte) throws IOException
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(inputByte.length);
        try
        {
            Inflater inflater = new Inflater(true);
            inflater.setInput(inputByte);
            byte[] buffer = new byte[4 * 1024];
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        byte[] output = outputStream.toByteArray();
        outputStream.close();
        return output;
    }
}
