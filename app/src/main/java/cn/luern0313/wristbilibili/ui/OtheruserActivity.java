package cn.luern0313.wristbilibili.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    Runnable runnableNoweb;
    Runnable runnableFollow;
    Runnable runnableUnfollow;

    ImageView uiLoading;
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

        othersUser = new OthersUser(sharedPreferences.getString("cookies", ""), sharedPreferences.getString("csrf", ""), intent.getStringExtra("mid"));
        uiLoading = findViewById(R.id.ou_loading_img);
        uiFollow = findViewById(R.id.ou_follow);

        runnableUi = new Runnable()
        {
            @Override
            public void run()
            {
                findViewById(R.id.ou_loading).setVisibility(View.GONE);
                findViewById(R.id.ou_noweb).setVisibility(View.GONE);

                if(otherUserCardJson.optJSONObject("vip").optInt("vipStatus") == 0)
                    ((TextView) findViewById(R.id.ou_name)).setTextColor(getResources().getColor(R.color.textcolor3));
                ((TextView) findViewById(R.id.ou_name)).setText(otherUserCardJson.optString("name"));
                ((TextView) findViewById(R.id.ou_lv)).setText("LV" + otherUserCardJson.optJSONObject("level_info").optInt("current_level"));
                if(otherUserJson.optBoolean("following"))
                {
                    uiFollow.setText("已关注");
                    uiFollow.setBackgroundResource(R.drawable.shape_anre_followbgyes);
                }
                if(!otherUserCardJson.optJSONObject("Official").optString("title").equals(""))
                {
                    findViewById(R.id.ou_anth).setVisibility(View.VISIBLE);
                    ((TextView) findViewById(R.id.ou_anth)).setText(otherUserCardJson.optJSONObject("Official").optString("title"));
                }
                if(!otherUserCardJson.optString("sign").equals(""))
                {
                    findViewById(R.id.ou_sign).setVisibility(View.VISIBLE);
                    ((TextView) findViewById(R.id.ou_sign)).setText(otherUserCardJson.optString("sign"));
                }
                ((TextView) findViewById(R.id.ou_other)).setText("关注 : " + otherUserCardJson.optString("friend") + "  粉丝 : " + otherUserCardJson.optString("fans")+ "\n投稿 : " + otherUserJson.optString("archive_count"));
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

        runnableNoweb = new Runnable()
        {
            @Override
            public void run()
            {
                findViewById(R.id.ou_loading).setVisibility(View.GONE);
                findViewById(R.id.ou_noweb).setVisibility(View.VISIBLE);
            }
        };

        runnableFollow = new Runnable()
        {
            @Override
            public void run()
            {
                uiFollow.setText("已关注");
                uiFollow.setBackgroundResource(R.drawable.shape_anre_followbgyes);
            }
        };

        runnableUnfollow = new Runnable()
        {
            @Override
            public void run()
            {
                uiFollow.setText("+关注");
                uiFollow.setBackgroundResource(R.drawable.shape_anre_followbg);
            }
        };

        findViewById(R.id.ou_head).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent i = new Intent(ctx, ImgActivity.class);
                i.putExtra("imgUrl", new String[]{otherUserCardJson.optString("face")});
                startActivity(i);
            }
        });

        uiFollow.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            if(uiFollow.getText().toString().equals("已关注"))
                            {
                                othersUser.unfollow();
                                handler.post(runnableUnfollow);
                            }
                            else
                            {
                                othersUser.follow();
                                handler.post(runnableFollow);
                            }
                        }
                        catch (IOException e)
                        {
                            Looper.prepare();
                            Toast.makeText(ctx, "操作失败...", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        uiLoading.setImageResource(R.drawable.anim_loading);
        AnimationDrawable loadingImgAnim = (AnimationDrawable) uiLoading.getDrawable();
        loadingImgAnim.start();

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
                    Looper.prepare();
                    Toast.makeText(ctx, "查无此人. . .", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    handler.post(runnableNoweb);
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
