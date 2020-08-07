package cn.luern0313.wristbilibili.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.util.ImageDownloaderUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;

public class SendDynamicActivity extends BaseActivity
{
    Context ctx;
    Intent inIntent;

    EditText uiEdittext;
    TextView uiShareUp;
    CardView uiShareCardview;
    ImageView uiShareImg;
    TextView uiShareTitle;

    String up;
    String img;
    String title;
    Bitmap image;
    String text;

    Handler handler = new Handler();
    Runnable runnableUi;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_senddynamic);
        ctx = this;
        inIntent = getIntent();
        setResult(-1, inIntent);

        uiEdittext = findViewById(R.id.senddy_edittext);

        if(inIntent.getBooleanExtra("is_share", false))
        {
            findViewById(R.id.senddy_share).setVisibility(View.VISIBLE);
            up = inIntent.hasExtra("share_up") ? inIntent.getStringExtra("share_up") : "";
            img = inIntent.hasExtra("share_img") ? inIntent.getStringExtra("share_img") : "";
            title = inIntent.hasExtra("share_title") ? inIntent.getStringExtra("share_title") : "";
            text = inIntent.hasExtra("share_text") ? inIntent.getStringExtra("share_text") : "";

            uiEdittext.setText(text);
            uiShareUp = findViewById(R.id.senddy_share_up);
            uiShareCardview = findViewById(R.id.senddy_share_img);
            uiShareImg = findViewById(R.id.senddy_share_img_img);
            uiShareTitle = findViewById(R.id.senddy_share_title);

            if(!up.equals(""))
                uiShareUp.setText(Html.fromHtml("转发自 <font color=#3f51b5>@" + up + "</font>："));
            else
                uiShareUp.setVisibility(View.GONE);
            uiShareTitle.setText(title);
            if(!img.equals(""))
            {
                uiShareCardview.setVisibility(View.VISIBLE);
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            image = ImageDownloaderUtil.downloadImage(img);
                            handler.post(runnableUi);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
            else uiShareImg.setVisibility(View.GONE);
        }

        if(!SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.tail, true))
            findViewById(R.id.senddy_tail).setVisibility(View.GONE);

        runnableUi = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    uiShareImg.setImageBitmap(image);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };
    }

    public void senddy_clickVoiceInput(View view)
    {
        Intent voiceInputIntent = new Intent("com.mobvoi.ticwear.action.SPEECH");
        voiceInputIntent.putExtra("start_mode", "start_mode_with_voice_input");
        try
        {
            startActivityForResult(voiceInputIntent, 0);
        }
        catch (Exception e)
        {
            Toast.makeText(ctx, "抱歉，该手表不支持语音输入", Toast.LENGTH_SHORT).show();
        }
    }

    public void senddy_clickSend(View view)
    {
        String s = uiEdittext.getText().toString();
        if(s.equals("") && inIntent.getBooleanExtra("is_share", false))
            s = "转发动态";

        if(!s.equals(""))
        {
            if(SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.tail, true))
                s += "\n\n" + TailActivity.getTail(true);
            inIntent.putExtra("text", s);
            setResult(0, inIntent);
            finish();
        }
        else Toast.makeText(ctx, "动态内容为空", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0)
        {
            if(data != null)
            {
                String result = data.getExtras().getString("speech_content");
                if(result.endsWith("。")) result = result.substring(0, result.length() - 1);
                int index = uiEdittext.getSelectionStart();
                Editable editable = uiEdittext.getText();
                editable.insert(index, result);
            }
            else Toast.makeText(ctx, "识别失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }
}
