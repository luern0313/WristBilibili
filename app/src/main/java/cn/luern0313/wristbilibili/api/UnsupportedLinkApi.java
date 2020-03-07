package cn.luern0313.wristbilibili.api;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.models.UnsupportedLinkModel;
import cn.luern0313.wristbilibili.util.NetWorkUtil;

/**
 * 被 luern0313 创建于 2020/3/6.
 */
public class UnsupportedLinkApi
{
    private String url;
    private ArrayList<String> webHeaders;

    private UnsupportedLinkModel unsupportedLinkModel;

    public UnsupportedLinkApi(final String url)
    {
        this.url = url;
        webHeaders = new ArrayList<String>(){{
            add("Referer"); add(url);
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_WEB);
        }};
    }

    public UnsupportedLinkModel getUnsupportedLink() throws IOException
    {
        try
        {
            String html = NetWorkUtil.get(url, webHeaders).body().string();
            Document document = Jsoup.parse(html);
            unsupportedLinkModel = new UnsupportedLinkModel(document);
            return unsupportedLinkModel;
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }
        return unsupportedLinkModel;
    }
}
