package cn.luern0313.wristbilibili.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.widget.TitleView;

public class TextActivity extends BaseActivity
{
    Context ctx;
    Intent intent;

    TitleView uiTitle;
    TextView uiText;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);
        ctx = this;
        intent = getIntent();

        uiTitle = findViewById(R.id.text_title);
        uiText = findViewById(R.id.text_textview);
        uiTitle.setTitle(intent.getStringExtra("title"));
        uiText.setText(Html.fromHtml(intent.getStringExtra("text")));
        //fromHtml(, new MImageGetter(uiText, ctx), null)
    }
}
