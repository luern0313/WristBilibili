package cn.luern0313.wristbilibili.api;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import cn.luern0313.lson.LsonUtil;
import cn.luern0313.lson.TypeReference;
import cn.luern0313.wristbilibili.models.UnsupportedLinkModel;
import cn.luern0313.wristbilibili.ui.ArticleActivity;
import cn.luern0313.wristbilibili.ui.BangumiActivity;
import cn.luern0313.wristbilibili.ui.DynamicDetailActivity;
import cn.luern0313.wristbilibili.ui.FavorVideoActivity;
import cn.luern0313.wristbilibili.ui.LotteryActivity;
import cn.luern0313.wristbilibili.ui.UserActivity;
import cn.luern0313.wristbilibili.ui.VideoActivity;
import cn.luern0313.wristbilibili.util.FileUtil;
import cn.luern0313.wristbilibili.util.MyApplication;
import cn.luern0313.wristbilibili.util.NetWorkUtil;

/**
 * 被 luern0313 创建于 2020/3/6.
 */
public class UnsupportedLinkApi
{
    private final Context ctx;
    private String url;
    private Intent intent;

    private final ArrayList<String> webHeaders;

    private UnsupportedLinkModel unsupportedLinkModel;
    private static HashMap<String, UnsupportedLinkModel.UnsupportedLinkConfigModel> unsupportedLinkConfigModelHashMap;

    public UnsupportedLinkApi(final Uri uri)
    {
        Context ctx = MyApplication.getContext();
        url = uri.toString();
        webHeaders = new ArrayList<String>(){{
            add("Referer"); add(url);
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_WEB);
        }};

        if("bilibili".equals(uri.getScheme()))
        {
            String path = uri.getHost() != null ? uri.getHost() : "";
            if(supportedLinkMap.containsKey(path))
            {
                String id = uri.getLastPathSegment() != null ? uri.getLastPathSegment() : "";
                String[] parameter = uri.getQueryParameterNames().toArray(new String[]{});
                if(path.equals("video") && !id.startsWith("BV"))
                    url = String.format(supportedLinkMap.get(path).get("url"), "av" + uri.getLastPathSegment());
                else
                    url = String.format(supportedLinkMap.get(path).get("url"), uri.getLastPathSegment());
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
                        case "collect":
                        {
                            intent = new Intent(ctx, FavorVideoActivity.class);
                            intent.putExtra("mid", uri.getQueryParameter("uid"));
                            intent.putExtra("fid", id);
                            break;
                        }
                        case "following":
                        {
                            intent = new Intent(ctx, DynamicDetailActivity.class);
                            intent.putExtra("dynamic_id", id);
                            break;
                        }
                    }
                    if(intent != null)
                        for(String i : parameter)
                            intent.putExtra(i, uri.getQueryParameter(i));
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
