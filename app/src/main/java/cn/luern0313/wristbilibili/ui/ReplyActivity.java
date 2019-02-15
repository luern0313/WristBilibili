package cn.luern0313.wristbilibili.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import cn.luern0313.wristbilibili.R;

public class ReplyActivity extends Activity
{
    Context ctx;
    Intent intent;
    Intent reusltIntent = new Intent();

    EditText replyEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);
        ctx = this;
        intent = getIntent();
        reusltIntent.putExtra("oid", intent.getStringExtra("oid"));
        reusltIntent.putExtra("type", intent.getStringExtra("type"));
        reusltIntent.putExtra("text", "");
        setResult(0, reusltIntent);

        replyEditText = findViewById(R.id.rp_edittext);
    }

    public void rp_clickVoiceInput(View view)
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

    public void rp_clickSend(View view)
    {
        if(!replyEditText.getText().toString().equals(""))
        {
            reusltIntent.putExtra("text", replyEditText.getText().toString());
            setResult(0, reusltIntent);
            finish();
        }
        else
            Toast.makeText(ctx, "评论内容为空", Toast.LENGTH_SHORT).show();
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
                replyEditText.setText(replyEditText.getText().toString() + result);
            }
            else
                Toast.makeText(ctx, "识别失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }
}
