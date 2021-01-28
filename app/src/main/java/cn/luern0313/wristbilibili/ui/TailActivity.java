package cn.luern0313.wristbilibili.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;

public class TailActivity extends BaseActivity
{
    Context ctx;
    SwitchCompat uiSwitch;
    EditText uiEditText;
    ImageView uiVoice;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tail);
        ctx = this;

        uiSwitch = findViewById(R.id.tail_switch);
        uiEditText = findViewById(R.id.tail_preview);
        uiVoice = findViewById(R.id.tail_voice);
        ((SwitchCompat) findViewById(R.id.tail_switch)).setChecked(SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.tail, true));
        ((TextView) findViewById(R.id.tail_preview)).setText(getTail(false));

        uiVoice.setOnClickListener(v -> {
            Intent voiceInputIntent = new Intent("com.mobvoi.ticwear.action.SPEECH");
            voiceInputIntent.putExtra("start_mode", "start_mode_with_voice_input");
            try
            {
                startActivityForResult(voiceInputIntent, 0);
            }
            catch (Exception e)
            {
                Toast.makeText(ctx, getString(R.string.main_tip_voice_input), Toast.LENGTH_SHORT).show();
            }
        });

        uiEditText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                SharedPreferencesUtil.putString(SharedPreferencesUtil.tailCustom, uiEditText.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        uiSwitch.setChecked(SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.tail, true));
        uiSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> SharedPreferencesUtil.putBoolean(SharedPreferencesUtil.tail, isChecked));
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
                uiEditText.setText(uiEditText.getText().toString() + result);
            }
            else
            {
                Toast.makeText(ctx, "识别失败，请重试", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static String getTail(boolean isChange)
    {
        if(!SharedPreferencesUtil.contains(SharedPreferencesUtil.tailCustom))
        {
            SharedPreferencesUtil.putString(SharedPreferencesUtil.tailCustom, "————该评论来自" + SharedPreferencesUtil.getString(SharedPreferencesUtil.tailModel, "") + "{{device}}端{{appname}}" + (SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.tailAuthor, true) ? "，{{appauthor}} {{videoid}}" : ""));
            SharedPreferencesUtil.removeValue(SharedPreferencesUtil.tailModel);
            SharedPreferencesUtil.removeValue(SharedPreferencesUtil.tailAuthor);
        }
        String tail = SharedPreferencesUtil.getString(SharedPreferencesUtil.tailCustom, "");
        if(isChange)
        {
            tail = tail.replace("{{device}}", Build.MODEL);
            tail = tail.replace("{{appname}}", "腕上哔哩");
            tail = tail.replace("{{appauthor}}", "@luern0313 ");
            tail = tail.replace("{{videoid}}", "av37132444 ");
        }
        return tail;
    }
}
