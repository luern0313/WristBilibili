package cn.luern0313.wristbilibili.util;

import android.content.Context;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import org.xml.sax.XMLReader;

import java.lang.reflect.Field;
import java.util.HashMap;

import cn.luern0313.wristbilibili.R;

/**
 * 被 luern0313 创建于 2020/2/4.
 */

public class SearchHtmlTagHandlerUtil implements Html.TagHandler
{
    private Context ctx;
    private static final String TAG_KEYWORD = "keyword";

    private int startIndex = 0;
    private int stopIndex = 0;
    final HashMap<String, String> attributes = new HashMap<String, String>();

    public SearchHtmlTagHandlerUtil(Context ctx)
    {
        this.ctx = ctx;
    }

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader)
    {
        //processAttributes(xmlReader);

        if(tag.equalsIgnoreCase(TAG_KEYWORD))
        {
            if(opening)
            {
                startFont(tag, output, xmlReader);
            }
            else
            {
                endFont(tag, output, xmlReader);
            }
        }
    }

    public void startFont(String tag, Editable output, XMLReader xmlReader)
    {
        startIndex = output.length();
    }

    public void endFont(String tag, Editable output, XMLReader xmlReader)
    {
        stopIndex = output.length();
        output.setSpan(new ForegroundColorSpan(ctx.getResources().getColor(R.color.mainColor)), startIndex, stopIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void processAttributes(final XMLReader xmlReader)
    {
        try
        {
            Field elementField = xmlReader.getClass().getDeclaredField("theNewElement");
            elementField.setAccessible(true);
            Object element = elementField.get(xmlReader);
            Field attsField = element.getClass().getDeclaredField("theAtts");
            attsField.setAccessible(true);
            Object atts = attsField.get(element);
            Field dataField = atts.getClass().getDeclaredField("data");
            dataField.setAccessible(true);
            String[] data = (String[]) dataField.get(atts);
            Field lengthField = atts.getClass().getDeclaredField("length");
            lengthField.setAccessible(true);
            int len = (Integer) lengthField.get(atts);

            for (int i = 0; i < len; i++)
            {
                attributes.put(data[i * 5 + 1], data[i * 5 + 4]);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
