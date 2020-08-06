package cn.luern0313.wristbilibili.util;

import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.util.Log;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Random;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.widget.LinkSpan;

/**
 * 被 luern0313 创建于 2020/2/3.
 */

public class DataProcessUtil
{
    public static String getView(int view)
    {
        if(view > 100000000) return view / 10000000 / 10.0 + "亿";
        else if(view > 10000) return view / 1000 / 10.0 + "万";
        else return String.valueOf(view);
    }

    public static float getFloatRandom(Random r, float lrange, float urange)
    {
        return r.nextFloat() * (urange - lrange) + lrange;
    }

    public static String getMinFromSec(int sec)
    {
        String m = String.valueOf(sec / 60);
        String s = String.valueOf(sec - sec / 60 * 60);
        if(m.length() == 1) m = "0" + m;
        if(s.length() == 1) s = "0" + s;
        return m + ":" + s;
    }

    public static String joinList(String[] list, String split)
    {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < list.length; i++)
            stringBuilder.append(i == 0 ? "" : split).append(list[i]);
        return stringBuilder.toString();
    }

    public static String joinArrayList(ArrayList<String> list, String split)
    {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < list.size(); i++)
            stringBuilder.append(i == 0 ? "" : split).append(list.get(i));
        return stringBuilder.toString();
    }

    public static int dip2px(float dpValue)
    {
        final float scale = MyApplication.getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int sp2px(float spValue)
    {
        final float fontScale = MyApplication.getContext().getResources()
                .getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static int getPositionInArrayList(ArrayList arrayList, String string)
    {
        for (int i = 0; i < arrayList.size(); i++)
            if(arrayList.get(i).equals(string)) return i;
        return -1;
    }

    public static <T extends Serializable> int getPositionInList(T[] list, T element)
    {
        for (int i = 0; i < list.length; i++)
            if(list[i].equals(element)) return i;
        return -1;
    }

    public static String handleUrl(String url)
    {
        if(url.indexOf("//") == 0) url = "http:" + url;
        if(url.endsWith(".webp")) url = url.substring(0, url.lastIndexOf("@"));
        return url;
    }

    public static String getSize(long size)
    {
        String[] unit = new String[]{"B", "KB", "MB", "GB"};
        long s = size * 10;
        int u = 0;
        while (s > 10240 && u < unit.length - 1)
        {
            s /= 1024;
            u++;
        }
        return s / 10.0 + unit[u];
    }

    public static String getSurplusTime(long surplusByte, int speed)
    {
        if(speed <= 0) return "未知";
        long time = surplusByte / speed;

        String sec = String.valueOf(time % 60);
        if(sec.length() == 1) sec = "0" + sec;
        String min = String.valueOf(time / 60 % 60);
        if(min.length() == 1) min = "0" + min;
        String hour = String.valueOf(time / 3600 % 60);
        if(hour.length() == 1) hour = "0" + hour;

        if(hour.equals("00")) return min + ":" + sec;
        else return hour + ":" + min + ":" + sec;
    }

    public static String getTime(int timeStamp, String pattern)
    {
        try
        {
            Date date = new Date(timeStamp * 1000L);
            SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.getDefault());
            return format.format(date);
        }
        catch (RuntimeException e)
        {
            e.printStackTrace();
            return "";
        }
    }

    public static String getFriendlyTime(int timeStamp)
    {
        try
        {
            GregorianCalendar now = new GregorianCalendar();
            GregorianCalendar target = new GregorianCalendar();
            target.setTimeInMillis(timeStamp);
            for (int i = 0; i < 3; i++)
            {
                if(now.get(Calendar.YEAR) == target.get(Calendar.YEAR) && now.get(Calendar.MONTH) == target.get(Calendar.MONTH) && now.get(Calendar.DAY_OF_MONTH) == target.get(Calendar.DAY_OF_MONTH))
                {
                    if(i == 0)
                        return String.format(MyApplication.getContext().getString(R.string.time_0), getTime(timeStamp, "HH:mm"));
                    else if(i == 1)
                        return String.format(MyApplication.getContext().getString(R.string.time_1), getTime(timeStamp, "HH:mm"));
                    else
                        return String.format(MyApplication.getContext().getString(R.string.time_2), getTime(timeStamp, "HH:mm"));
                }
                target.add(Calendar.DAY_OF_MONTH, 1);
            }
        }
        catch (RuntimeException e)
        {
            e.printStackTrace();
        }
        return getTime(timeStamp, "MM-dd HH:mm");
    }

    public static CharSequence getClickableHtml(String html, Html.ImageGetter imageGetter)
    {
        Spanned spannedHtml = Html.fromHtml(html, imageGetter, null);
        SpannableStringBuilder clickableHtmlBuilder = new SpannableStringBuilder(spannedHtml);
        URLSpan[] urls = clickableHtmlBuilder.getSpans(0, spannedHtml.length(), URLSpan.class);
        for (final URLSpan span : urls)
        {
            setLinkClickable(clickableHtmlBuilder, span);
        }
        return clickableHtmlBuilder;
    }

    private static void setLinkClickable(final SpannableStringBuilder clickableHtmlBuilder, final URLSpan urlSpan)
    {
        final int start = clickableHtmlBuilder.getSpanStart(urlSpan);
        int end = clickableHtmlBuilder.getSpanEnd(urlSpan);
        int flags = clickableHtmlBuilder.getSpanFlags(urlSpan);

        LinkSpan linkSpan = new LinkSpan(urlSpan.getURL());
        clickableHtmlBuilder.removeSpan(urlSpan);

        clickableHtmlBuilder.setSpan(linkSpan, start, end, flags);
    }

    public static void printLog(String log)
    {
        for (int i = 0; i < log.length(); i += 3000)
            Log.w("bilibili", log.substring(i, Math.min(i + 3000, log.length())));
    }
}
