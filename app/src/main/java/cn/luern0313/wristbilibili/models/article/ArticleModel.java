package cn.luern0313.wristbilibili.models.article;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import cn.luern0313.lson.LsonUtil;
import cn.luern0313.lson.annotation.field.LsonBooleanFormatAsNumber;
import cn.luern0313.lson.annotation.field.LsonPath;
import cn.luern0313.lson.element.LsonObject;
import cn.luern0313.wristbilibili.api.ArticleApi;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.LruCacheUtil;
import cn.luern0313.wristbilibili.util.json.ImageUrlFormat;
import lombok.Getter;
import lombok.Setter;

/**
 * 被 luern0313 创建于 2020/2/20.
 */

@Getter
@Setter
public class ArticleModel implements Serializable
{
    private String id;

    @LsonPath("article.title")
    private String title;

    @LsonPath("article.stats.view")
    private int view;

    @LsonPath("article.stats.like")
    private int like;

    @LsonPath("article.stats.coin")
    private int coin;

    @LsonPath("article.stats.favorite")
    private int favor;

    @ImageUrlFormat
    @LsonPath("article.origin_image_urls")
    private String[] cover;

    private String channel;
    private String time;

    @LsonPath("more.author.name")
    private String upName;

    @ImageUrlFormat
    @LsonPath("more.author.face")
    private String upFace;

    @LsonPath("more.author.mid")
    private String upMid;

    @LsonPath("more.official_verify.type")
    private int upOfficial; // -1 0 1

    @LsonPath("more.author.vip.vipType")
    private int upVip; // 2

    @LsonPath("more.author.fans")
    private int upFansNum;

    @LsonPath("more.attention")
    private boolean userFollowUp;

    @LsonBooleanFormatAsNumber(equal = 1)
    @LsonPath("article.like")
    private boolean userLike;

    @LsonPath("article.coin")
    private int userCoin;

    @LsonPath("article.favorite")
    private boolean userFavor;

    private String article;
    private ArrayList<String> articleImgUrl = new ArrayList<>();
    private ArrayList<ArticleCardModel.ArticleCardBaseModel> articleCardModelList = new ArrayList<>();

    public ArticleModel(String id, Document element, LsonObject card)
    {
        this.id = id;

        Elements info = element.getElementsByClass("info").first().children();
        channel = info.get(0).text();
        channel = channel.substring(0, channel.length() - 2);
        time = DataProcessUtil.getTime(Integer.parseInt(info.get(1).attr("data-ts")), "yyyy-MM-dd HH:mm");

        Element article_element = element.getElementsByClass("article-holder").first();

        Elements imgs = article_element.select("figure[class=img-box] > img");
        for(Element img : imgs)
        {
            img.attr("src", img.attr("data-src"));
            img.append("<br>");
        }
        Elements styles = article_element.select("span[class~=(color-)|(font-size-)]");
        for(Element style : styles)
        {
            String[] style_list = style.className().split(" ");
            for (String ii : style_list)
            {
                if(colorMap.containsKey(ii))
                {
                    style.tagName("font");
                    style.attr("color", colorMap.get(ii));
                }
                else if(ii.contains("font-size-"))
                {
                    style.tagName("font");
                    style.attr("size", ii.substring(10));
                }
            }
        }
        article_element.select("blockquote").tagName("em");
        article_element.select("span[style*=line-through], font[style*=line-through]").wrap("<s></s>");
        article_element.select("figcaption").wrap("<small></small>");
        Elements figeles = article_element.select("figcaption");
        for(int j = 0; j < figeles.size(); j++)
            if(!figeles.get(j).text().equals(""))
                figeles.get(j).wrap("<center></center>");

        Elements arts = article_element.children();
        for(Element art : arts)
        {
            if(art.tagName().equals("figure") && art.className().equals("img-box") && art.child(0).tagName().equals("img") && art.child(0).hasAttr("aid"))
            {
                Element img = art.child(0);
                String[] tagids = img.attr("aid").split(",");
                String type = img.attr("class");
                for(String tagid : tagids)
                {
                    if(type.indexOf("video") == 0)
                        articleCardModelList.add(LsonUtil.fromJson(card.getAsJsonObject("av" + tagid), ArticleCardModel.ArticleCardVideoCardModel.class, "av" + tagid));
                    else if(type.indexOf("article") == 0)
                        articleCardModelList.add(LsonUtil.fromJson(card.getAsJsonObject("cv" + tagid), ArticleCardModel.ArticleArticleCardCardModel.class, "cv" + tagid));
                    else if(type.indexOf("fanju") == 0)
                        articleCardModelList.add(LsonUtil.fromJson(card.getAsJsonObject(tagid), ArticleCardModel.ArticleCardBangumiCardModel.class, tagid));
                    else if(type.indexOf("music") == 0)
                        articleCardModelList.add(LsonUtil.fromJson(card.getAsJsonObject(tagid), ArticleCardModel.ArticleCardMusicCardModel.class, tagid));
                    else if(type.indexOf("shop") == 0)
                    {
                        if(tagid.indexOf("pw") == 0)
                            articleCardModelList.add(LsonUtil.fromJson(card.getAsJsonObject(tagid), ArticleCardModel.ArticleCardTicketCardModel.class, tagid));
                        if(tagid.indexOf("sp") == 0)
                            articleCardModelList.add(LsonUtil.fromJson(card.getAsJsonObject(tagid), ArticleCardModel.ArticleCardShopCardModel.class, tagid));
                    }
                    else if(type.indexOf("caricature") == 0)
                        articleCardModelList.add(LsonUtil.fromJson(card.getAsJsonObject("mc" + tagid), ArticleCardModel.ArticleCardContainerCardModel.class, "mc" + tagid));
                    else if(type.indexOf("live") == 0)
                        articleCardModelList.add(LsonUtil.fromJson(card.getAsJsonObject("lv" + tagid), ArticleCardModel.ArticleCardLiveCardModel.class, "lv" + tagid));
                }
            }
            else
            {
                Element imgElement = art.selectFirst("img");
                if(imgElement != null && (!imgElement.hasAttr("class") ||
                        (imgElement.hasAttr("class") &&
                                (!imgElement.attr("class").contains("cut-off-") && !imgElement.attr("class").contains("vote-display")))))
                {
                    String url = imgElement.attr("data-src");
                    if(url.indexOf("//") == 0)
                        url = "http:" + url;
                    if(url.endsWith(".webp"))
                        url = url.substring(0, url.lastIndexOf("@"));
                    url = LruCacheUtil.getImageUrl(url, ArticleApi.getArticleImageWidthSize());
                    articleImgUrl.add(url);
                    imgElement.attr("src", url);
                    imgElement.attr("data-src", url);
                }
                articleCardModelList.add(new ArticleCardModel.ArticleCardTextModel(art));
            }
        }
        article = article_element.outerHtml();
    }

    private static final HashMap<String, String> colorMap = new HashMap<String, String>()
    {{
        put("color-blue-01", "#56c1fe");
        put("color-lblue-01", "#73fdea");
        put("color-green-01", "#89fa4e");
        put("color-yellow-01", "#fff359");
        put("color-pink-01", "#ff968d");
        put("color-purple-01", "#ff8cc6");
        put("color-blue-02", "#02a2ff");
        put("color-lblue-02", "#18e7cf");
        put("color-green-02", "#60d837");
        put("color-yellow-02", "#fbe231");
        put("color-pink-02", "#ff654e");
        put("color-purple-02", "#ef5fa8");
        put("color-blue-03", "#0176ba");
        put("color-lblue-03", "#068f86");
        put("color-green-03", "#1db100");
        put("color-yellow-03", "#f8ba00");
        put("color-pink-03", "#ee230d");
        put("color-purple-03", "#cb297a");
        put("color-blue-04", "#004e80");
        put("color-lblue-04", "#017c76");
        put("color-green-04", "#017001");
        put("color-yellow-04", "#ff9201");
        put("color-pink-04", "#b41700");
        put("color-purple-04", "#99195e");
        put("color-gray-01", "#d6d5d5");
        put("color-gray-02", "#929292");
        put("color-gray-03", "#5f5f5f");
        put("color-default", "#222222");
    }};
}
