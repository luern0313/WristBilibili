package cn.luern0313.wristbilibili.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.ui.TailActivity;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;

public class TailFragment extends Fragment implements View.OnClickListener
{
    Context ctx;
    View rootLayout;
    SwitchCompat uiSwitch;
    EditText uiEditText;

    public static Fragment newInstance()
    {
        return new TailFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = this.getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_tail, container, false);

        uiSwitch = rootLayout.findViewById(R.id.tail_switch);
        uiEditText = rootLayout.findViewById(R.id.tail_preview);
        ((SwitchCompat) rootLayout.findViewById(R.id.tail_switch)).setChecked(SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.tail, true));

        rootLayout.findViewById(R.id.tail_voice).setOnClickListener(this);
        rootLayout.findViewById(R.id.tail_market).setOnClickListener(this);
        rootLayout.findViewById(R.id.tail_remove).setOnClickListener(this);

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

        return rootLayout;
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

    @Override
    public void onResume()
    {
        super.onResume();
        uiEditText.setText(getTail(false));
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

    public static boolean isDefault()
    {
        String tail = getTail(false);
        return tail.equals("") ||
                tail.equals("————该评论来自{{device}}端{{appname}}，{{appauthor}} {{videoid}}") ||
                tail.equals("————该评论来自{{device}}端{{appname}}");
    }

    @Override
    public void onClick(View v)
    {
        if(v.getId() == R.id.tail_voice)
        {
            try
            {
                Intent voiceInputIntent = new Intent("com.mobvoi.ticwear.action.SPEECH");
                voiceInputIntent.putExtra("start_mode", "start_mode_with_voice_input");
                startActivityForResult(voiceInputIntent, 0);
            }
            catch (Exception e)
            {
                Toast.makeText(ctx, getString(R.string.main_tip_voice_input), Toast.LENGTH_SHORT).show();
            }
        }
        else if(v.getId() == R.id.tail_market)
        {
            ((TailActivity) getActivity()).tailMarket();
        }
        else if(v.getId() == R.id.tail_remove)
        {
            SharedPreferencesUtil.putString(SharedPreferencesUtil.tailCustom, "————该评论来自{{device}}端{{appname}}，{{appauthor}} {{videoid}}");
            uiEditText.setText(getTail(false));
        }
    }
}
