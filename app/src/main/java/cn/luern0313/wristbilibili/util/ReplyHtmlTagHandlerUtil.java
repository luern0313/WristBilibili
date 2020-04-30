package cn.luern0313.wristbilibili.util;

import android.content.Context;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import org.xml.sax.XMLReader;

import java.util.HashMap;

import androidx.annotation.NonNull;
import cn.luern0313.wristbilibili.R;

/**
 * 被 luern0313 创建于 2020/2/4.
 */

public class ReplyHtmlTagHandlerUtil implements Html.TagHandler
{
    private Context ctx;
    public static final String TAG_A = "tag_a_reply";

    private int startAIndex = 0;
    private int stopAIndex = 0;
    private int startFontIndex = 0;
    private int stopFontIndex = 0;
    final HashMap<String, String> attributes = new HashMap<String, String>();

    public ReplyHtmlTagHandlerUtil(Context ctx)
    {
        this.ctx = ctx;
    }

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader)
    {
        //processAttributes(xmlReader);
        if(opening)
        {
            if(tag.equals(TAG_A))
                startA(output, xmlReader);
        }
        else
        {
            if(tag.equals(TAG_A))
                endA(output, xmlReader);
        }
    }

    private void startA(Editable output, XMLReader xmlReader)
    {
        startAIndex = output.length();
    }

    private void endA(Editable output, final XMLReader xmlReader)
    {
        stopAIndex = output.length();
        output.setSpan(new ClickableSpan()
        {
            @Override
            public void onClick(View widget)
            {

            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds)
            {
                ds.setColor(ctx.getResources().getColor(R.color.colorPrimary));
                ds.setUnderlineText(false);
            }
        }, startAIndex, stopAIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public void startFont(String tag, Editable output, XMLReader xmlReader)
    {
        startFontIndex = output.length();
    }

    public void endFont(String tag, Editable output, XMLReader xmlReader)
    {
        stopFontIndex = output.length();
        output.setSpan(new ForegroundColorSpan(ctx.getResources().getColor(R.color.mainColor)), startFontIndex, stopFontIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
}
