package cn.luern0313.wristbilibili.widget;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.util.MyApplication;

/**
 * 被 luern0313 创建于 2020/5/25.
 */

public class LinkSpan extends ClickableSpan
{
    private String url;

    public LinkSpan(String url)
    {
        this.url = url;
    }

    public String getUrl()
    {
        return url;
    }

    @Override
    public void onClick(View widget)
    {

    }

    @Override
    public void updateDrawState(TextPaint ds)
    {
        ds.setColor(MyApplication.getContext().getResources().getColor(R.color.colorHyperlinks));
        ds.setUnderlineText(false);
    }
}