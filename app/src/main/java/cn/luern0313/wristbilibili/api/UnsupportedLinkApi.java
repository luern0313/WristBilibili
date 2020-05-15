package cn.luern0313.wristbilibili.api;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import cn.luern0313.wristbilibili.models.UnsupportedLinkModel;
import cn.luern0313.wristbilibili.ui.ArticleActivity;
import cn.luern0313.wristbilibili.ui.BangumiActivity;
import cn.luern0313.wristbilibili.ui.UserActivity;
import cn.luern0313.wristbilibili.ui.VideoActivity;
import cn.luern0313.wristbilibili.util.NetWorkUtil;

/**
 * 被 luern0313 创建于 2020/3/6.
 */
public class UnsupportedLinkApi
{
    private Context ctx;
    private Uri biliUrl;
    private String url;
    private Intent intent;

    private ArrayList<String> webHeaders;

    private UnsupportedLinkModel unsupportedLinkModel;
    private HashMap<String, HashMap<String, String>> supportedLinkMap = new HashMap<String, HashMap<String, String>>()
    {{
        put("video", new HashMap<String, String>(){{put("support", "true"); put("url", "https://www.bilibili.com/video/%s");}});
        put("bangumi", new HashMap<String, String>(){{put("support", "true"); put("url", "https://www.bilibili.com/bangumi/play/ss%s");}});
        put("article", new HashMap<String, String>(){{put("support", "true"); put("url", "https://www.bilibili.com/read/cv%s");}});
        put("music", new HashMap<String, String>(){{put("support", "false"); put("url", "https://www.bilibili.com/audio/au%s");}});
        put("show", new HashMap<String, String>(){{put("support", "false"); put("url", "https://show.bilibili.com/platform/detail.html?id=%s");}});
        put("mall", new HashMap<String, String>(){{put("support", "false"); put("url", "https://mall.bilibili.com/detail.html?itemsId=%s");}});
        put("manga", new HashMap<String, String>(){{put("support", "false"); put("url", "https://manga.bilibili.com/detail/mc%s");}});
        put("live", new HashMap<String, String>(){{put("support", "false"); put("url", "https://live.bilibili.com/%s");}});
        put("space", new HashMap<String, String>(){{put("support", "true"); put("url", "https://space.bilibili.com/%s");}});
        put("pegasus", new HashMap<String, String>(){{put("support", "false"); put("url", "https://t.bilibili.com/topic/%s");}});
        put("tag", new HashMap<String, String>(){{put("support", "false"); put("url", "https://t.bilibili.com/topic/%s");}});
    }};

    public UnsupportedLinkApi(Context ctx, final Uri uri)
    {
        this.ctx = ctx;
        this.biliUrl = uri;
        url = biliUrl.toString();
        webHeaders = new ArrayList<String>(){{
            add("Referer"); add(url);
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_WEB);
        }};

        if("bilibili".equals(biliUrl.getScheme()))
        {
            String path = biliUrl.getHost() != null ? biliUrl.getHost() : "";
            if(supportedLinkMap.containsKey(path))
            {
                String id = biliUrl.getLastPathSegment() != null ? biliUrl.getLastPathSegment() : "";
                if(path.equals("video") && !id.startsWith("BV"))
                    url = String.format(supportedLinkMap.get(path).get("url"), "av" + biliUrl.getLastPathSegment());
                else
                    url = String.format(supportedLinkMap.get(path).get("url"), biliUrl.getLastPathSegment());
                if(supportedLinkMap.get(path).get("support").equals("true"))
                {
                    switch(path)
                    {
                        case "video":
                        {
                            if(id.startsWith("BV"))
                                intent = VideoActivity.getActivityIntent(ctx, "", id);
                            else
                                intent = VideoActivity.getActivityIntent(ctx, id, "");
                            break;
                        }
                        case "bangumi":
                        {
                            intent = new Intent(ctx, BangumiActivity.class);
                            intent.putExtra("season_id", id);
                            break;
                        }
                        case "article":
                        {
                            intent = new Intent(ctx, ArticleActivity.class);
                            intent.putExtra("article_id", id);
                            break;
                        }
                        case "space":
                        {
                            intent = new Intent(ctx, UserActivity.class);
                            intent.putExtra("mid", id);
                            break;
                        }
                    }
                }
            }
        }
    }

    public Intent getIntent()
    {
        return intent;
    }

    public String getUrl()
    {
        return url;
    }

    public UnsupportedLinkModel getUnsupportedLink() throws IOException, IllegalArgumentException
    {
        try
        {
            String html = NetWorkUtil.get(url, webHeaders).body().string();
            Document document = Jsoup.parseBodyFragment(html);
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
