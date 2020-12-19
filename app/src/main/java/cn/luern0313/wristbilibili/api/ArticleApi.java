package cn.luern0313.wristbilibili.api;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

import cn.luern0313.lson.LsonUtil;
import cn.luern0313.lson.element.LsonObject;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.models.article.ArticleModel;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.MyApplication;
import cn.luern0313.wristbilibili.util.NetWorkUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;

/**
 * 被 luern0313 创建于 2020/2/20.
 */
public class ArticleApi
{
    private final String mid;
    private final String csrf;
    private final ArrayList<String> webHeaders;

    private final String article_id;
    private ArticleModel articleModel;

    private static int ARTICLE_IMAGE_WIDTH_SIZE = 400;

    public ArticleApi(String article_id, int width)
    {
        this.mid = SharedPreferencesUtil.getString(SharedPreferencesUtil.mid, "");
        this.csrf = SharedPreferencesUtil.getString(SharedPreferencesUtil.csrf, "");
        this.article_id = article_id;
        ARTICLE_IMAGE_WIDTH_SIZE = width;

        webHeaders = new ArrayList<String>(){{
            add("Cookie"); add(SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, ""));
            add("Referer"); add("https://www.bilibili.com/");
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_WEB);
        }};
    }

    public ArticleModel getArticleModel() throws IOException
    {
        String html = NetWorkUtil.get("https://www.bilibili.com/read/mobile/" + article_id, webHeaders).body().string();
        Document document = Jsoup.parseBodyFragment(html);
        Elements elements = document.selectFirst("*[class~=(:?(^)|(\\s))article-holder(:?(\\s)|($))]").select("figure[class=img-box] > img[class~=^(?:(video)|(fanju)|(article)|(music)|(shop)|(caricature)|(live))-card]");
        ArrayList<String> perList = new ArrayList<>();
        for (Element ele : elements)
        {
            String[] ids = ele.attr("aid").split(",");
            String type = ele.attr("class");
            for(String id : ids)
            {
                if(type.indexOf("video") == 0) perList.add("av" + id);
                else if(type.indexOf("article") == 0) perList.add("cv" + id);
                else if(type.indexOf("caricature") == 0) perList.add("mc" + id);
                else if(type.indexOf("live") == 0) perList.add("lv" + id);
                else perList.add(id);
            }
        }

        String cardUrl = "https://api.bilibili.com/x/article/cards?ids=" + DataProcessUtil.joinArrayList(perList, ",");
        LsonObject card = LsonUtil.parseAsObject(NetWorkUtil.get(cardUrl, webHeaders).body().string()).getAsJsonObject("data");

        LsonObject article = new LsonObject();
        String infoUrl = "https://api.bilibili.com/x/article/viewinfo?id=" + article_id;
        article.put("article", LsonUtil.parseAsObject(NetWorkUtil.get(infoUrl, webHeaders).body().string()).getAsJsonObject("data"));

        String upUrl = "https://api.bilibili.com/x/article/more?aid=" + article_id;
        article.put("more", LsonUtil.parseAsObject(NetWorkUtil.get(upUrl, webHeaders).body().string()).getAsJsonObject("data"));
        articleModel = LsonUtil.packFromJson(article, new ArticleModel(article_id, document, card));
        return articleModel;
    }

    public String likeArticle(int mode) throws IOException  //1好评，2取消差评
    {
        String url = "https://api.bilibili.com/x/article/like";
        String per = "id=" + article_id + "&type=" + mode + "&csrf=" + csrf;
        LsonObject result = LsonUtil.parseAsObject(NetWorkUtil.post(url, per, webHeaders).body().string());
        if(result.getAsInt("code") == 0)
            return "";
        return MyApplication.getContext().getString(R.string.main_error_unknown);
    }

    public String coinArticle() throws IOException
    {
        try
        {
            String url = "https://api.bilibili.com/x/web-interface/coin/add";
            String per = "aid=" + article_id + "&multiply=1&upid=" + articleModel.getUpMid() + "&avtype=2&csrf=" + csrf;
            LsonObject result = LsonUtil.parseAsObject(NetWorkUtil.post(url, per, webHeaders).body().string());
            if(result.getAsInt("code") == 0)
                return "";
        }
        catch (RuntimeException ignore)
        {
        }
        return MyApplication.getContext().getString(R.string.main_error_unknown);
    }

    public String favArticle(int mode) throws IOException //1添加 2删除
    {
        try
        {
            String url = mode == 1 ? "https://api.bilibili.com/x/article/favorites/add" : "https://api.bilibili.com/x/article/favorites/del";
            String per = "id=" + article_id + "&csrf=" + csrf;
            LsonObject result = LsonUtil.parseAsObject(NetWorkUtil.post(url, per, webHeaders).body().string());
            if(result.getAsInt("code") == 0)
                return "";
            else
                return result.getAsString("message");
        }
        catch (RuntimeException ignored)
        {
        }
        return MyApplication.getContext().getString(R.string.main_error_unknown);
    }

    public String shareArticle(String text) throws IOException
    {
        try
        {
            String url = "https://api.vc.bilibili.com/dynamic_repost/v1/dynamic_repost/share";
            String per = "csrf_token=" + csrf + "&platform=pc&type=64&uid=&share_uid=" + mid + "&content=" + URLEncoder.encode(text, "UTF-8") + "&repost_code=20000&rid=" + articleModel.getId();
            LsonObject result = LsonUtil.parseAsObject(NetWorkUtil.post(url, per, webHeaders).body().string());
            if(result.getAsInt("code") == 0)
                return "";
        }
        catch(RuntimeException ignore)
        {
        }
        return MyApplication.getContext().getString(R.string.main_error_unknown);
    }

    public String followUp() throws IOException
    {
        try
        {
            String url = "https://api.bilibili.com/x/relation/modify";
            String per = "fid=" + articleModel.getUpMid() + "&act=1&re_src=115&jsonp=jsonp&csrf=" + csrf;
            LsonObject result = LsonUtil.parseAsObject(NetWorkUtil.post(url, per, webHeaders).body().string());
            if(result.getAsInt("code") == 0)
                return "";
        }
        catch (RuntimeException ignored)
        {
        }
        return "关注UP主失败";
    }

    public static int getArticleImageWidthSize()
    {
        return ARTICLE_IMAGE_WIDTH_SIZE;
    }
}
