package cn.luern0313.wristbilibili.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import cn.luern0313.wristbilibili.R;

public class ReplyActivity extends AppCompatActivity
{
    Context ctx;
    Intent inIntent;
    Intent reusltIntent = new Intent();
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    EditText uiEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);
        ctx = this;
        inIntent = getIntent();
        sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        setResult(-1, reusltIntent);

        if(!sharedPreferences.getBoolean("tail", true))
            findViewById(R.id.rp_tail).setVisibility(View.GONE);

        uiEditText = findViewById(R.id.rp_edittext);
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
        if(!uiEditText.getText().toString().equals(""))
        {
            String s = uiEditText.getText().toString();
            if(sharedPreferences.getBoolean("tail", true))
                s += "\n\n" + TailActivity.getTail(sharedPreferences, editor, true);
            reusltIntent.putExtra("text", s);
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
                int index = uiEditText.getSelectionStart();
                Editable editable = uiEditText.getText();
                editable.insert(index, result);
            }
            else
                Toast.makeText(ctx, "识别失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }
}
