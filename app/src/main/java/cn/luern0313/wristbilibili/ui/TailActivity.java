package cn.luern0313.wristbilibili.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import cn.luern0313.wristbilibili.R;

public class TailActivity extends Activity
{
    Context ctx;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    Switch uiSwitch;
    EditText uiEdittext;
    ImageView uiVoice;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tail);
        ctx = this;
        sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        uiSwitch = findViewById(R.id.tail_switch);
        uiEdittext = findViewById(R.id.tail_preet);
        uiVoice = findViewById(R.id.tail_voice);
        ((Switch) findViewById(R.id.tail_switch)).setChecked(sharedPreferences.getBoolean("tail", true));
        ((TextView) findViewById(R.id.tail_preview)).setText("小尾巴预览：\n\n" + getTail(sharedPreferences));
        uiEdittext.setText(sharedPreferences.getString("tailModel", ""));

        uiVoice.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
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
        });

        uiSwitch.setChecked(sharedPreferences.getBoolean("tail", true));
        uiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                editor.putBoolean("tail", isChecked);
                editor.commit();
            }
        });

        uiEdittext.addTextChangedListener(new TextWatcher()
        {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                editor.putString("tailModel", s.toString());
                editor.commit();
                ((TextView) findViewById(R.id.tail_preview)).setText("小尾巴预览：\n\n" + getTail(sharedPreferences));
            }
        });
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
                uiEdittext.setText(result);
            }
            else
            {
                Toast.makeText(ctx, "识别失败，请重试", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static String getTail(SharedPreferences sharedPreferences)
    {
        return "————该评论来自" + sharedPreferences.getString("tailModel", "") + Build.MODEL + "端腕上哔哩，@luern0313 av37132444";
    }
}
