package cn.luern0313.wristbilibili.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.OthersUser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OtheruserActivity extends Activity
{
    Context ctx;
    Intent intent;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    OthersUser othersUser;
    JSONObject otherUserJson;
    JSONObject otherUserCardJson;
    Bitmap userHead;

    Handler handler = new Handler();
    Runnable runnableUi;
    Runnable runnableHead;

    TextView uiFollow;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otheruser);

        ctx = this;
        intent = getIntent();
        sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        othersUser = new OthersUser(sharedPreferences.getString("cookie", ""), sharedPreferences.getString("csrf", ""), intent.getStringExtra("mid"));
        uiFollow = findViewById(R.id.ou_follow);

        runnableUi = new Runnable()
        {
            @Override
            public void run()
            {
                ((TextView) findViewById(R.id.ou_name)).setText(otherUserCardJson.optString("name"));
                ((TextView) findViewById(R.id.ou_lv)).setText("LV" + otherUserCardJson.optJSONObject("level_info").optInt("current_level"));
                if(otherUserJson.optBoolean("following"))
                {
                    ((TextView) findViewById(R.id.ou_follow)).setText("已关注");
                    ((TextView) findViewById(R.id.ou_follow)).setTextColor(getResources().getColor(R.color.textcolor3));
                    findViewById(R.id.ou_follow).setBackgroundResource(R.drawable.shape_anre_followbgyes);
                }
                ((TextView) findViewById(R.id.ou_anth)).setText(otherUserCardJson.optJSONObject("Official").optString("title"));
                ((TextView) findViewById(R.id.ou_sign)).setText(otherUserCardJson.optString("sign"));
                ((TextView) findViewById(R.id.ou_other)).setText("关注:" + "  粉丝:" + "\n投稿:");
            }
        };

        runnableHead = new Runnable()
        {
            @Override
            public void run()
            {
                ((ImageView) findViewById(R.id.ou_head)).setImageBitmap(userHead);
            }
        };

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    otherUserJson = new JSONObject(othersUser.getOtheruserInfo()).getJSONObject("data");
                    otherUserCardJson = otherUserJson.getJSONObject("card");
                    handler.post(runnableUi);
                    userHead = (Bitmap) get(otherUserCardJson.optString("face"), 2);
                    handler.post(runnableHead);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private Object get(String url, int mode) throws IOException
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
