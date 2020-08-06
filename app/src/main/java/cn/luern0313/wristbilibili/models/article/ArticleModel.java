package cn.luern0313.wristbilibili.models.article;


import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import cn.luern0313.wristbilibili.api.ArticleApi;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.LruCacheUtil;

/**
 * 被 luern0313 创建于 2020/2/20.
 */
public class ArticleModel implements Serializable
{
    public String article_id;
    public String article_title;
    public int article_view;
    public int article_like;
    public int article_coin;
    public int article_fav;
    public String[] article_cover;
    public String article_channel;
    public String article_time;

    public String article_up_name;
    public String article_up_face;
    public String article_up_mid;
    public int article_up_official; // -1 0 1
    public int article_up_vip; // 2
    public int article_up_fans_num;

    public boolean article_user_follow_up;
    public boolean article_user_like;
    public int article_user_coin;
    public boolean article_user_fav;

    public String article_article;
    public ArrayList<String> article_article_img_url = new ArrayList<>();
    public ArrayList<ArticleCardModel> article_article_card_model_list = new ArrayList<>();
    public ArticleModel(String id, JSONObject article, JSONObject more, Document element, JSONObject card)
    {
        article_id = id;
        article_title = article.optString("title");
        JSONObject stat = article.has("stats") ? article.optJSONObject("stats") : new JSONObject();
        article_view = stat.optInt("view");
        article_like = stat.optInt("like");
        article_coin = stat.optInt("coin");
        article_fav = stat.optInt("favorite");
        JSONArray cover = article.optJSONArray("origin_image_urls");
        article_cover = new String[cover.length()];
        for(int i = 0; i < cover.length(); i++)
            article_cover[i] = LruCacheUtil.getImageUrl(cover.optString(i));
        Elements info = element.getElementsByClass("info").first().children();
        article_channel = info.get(0).text();
        article_channel = article_channel.substring(0, article_channel.length() - 2);
        article_time = DataProcessUtil.getTime(Integer.parseInt(info.get(1).attr("data-ts")), "yyyy-MM-dd HH:mm");

        JSONObject up = more.has("author") ? more.optJSONObject("author") : new JSONObject();
        JSONObject up_off = up.has("official_verify") ? up.optJSONObject("official_verify") : new JSONObject();
        JSONObject up_vip = up.has("vip") ? up.optJSONObject("vip") : new JSONObject();
        article_up_name = up.optString("name");
        article_up_face = LruCacheUtil.getImageUrl(up.optString("face"));
        article_up_mid = up.optString("mid");
        article_up_fans_num = up.optInt("fans");
        article_up_official = up_off.optInt("type");
        article_up_vip = up_vip.optInt("vipType");

        article_user_follow_up = more.optBoolean("attention", false);
        article_user_like = article.optInt("like") == 1;
        article_user_coin = article.optInt("coin");
        article_user_fav = article.optBoolean("favorite");

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
        ArticleCardModel articleCardModel = new ArticleCardModel();
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
                        article_article_card_model_list.add(articleCardModel.new ArticleVideoCardModel("av" + tagid, card.optJSONObject("av" + tagid)));
                    else if(type.indexOf("article") == 0)
                        article_article_card_model_list.add(articleCardModel.new ArticleArticleCardModel("cv" + tagid, card.optJSONObject("cv" + tagid)));
                    else if(type.indexOf("fanju") == 0)
                        article_article_card_model_list.add(articleCardModel.new ArticleBangumiCardModel(tagid, card.optJSONObject(tagid)));
                    else if(type.indexOf("music") == 0)
                        article_article_card_model_list.add(articleCardModel.new ArticleMusicCardModel(tagid, card.optJSONObject(tagid)));
                    else if(type.indexOf("shop") == 0)
                    {
                        if(tagid.indexOf("pw") == 0)
                            article_article_card_model_list.add(articleCardModel.new ArticleTicketCardModel(tagid, card.optJSONObject(tagid)));
                        else if(tagid.indexOf("sp") == 0)
                            article_article_card_model_list.add(articleCardModel.new ArticleShopCardModel(tagid, card.optJSONObject(tagid)));
                    }
                    else if(type.indexOf("caricature") == 0)
                        article_article_card_model_list.add(articleCardModel.new ArticleContainerCardModel("mc" + tagid, card.optJSONObject("mc" + tagid)));
                    else if(type.indexOf("live") == 0)
                        article_article_card_model_list.add(articleCardModel.new ArticleLiveCardModel("lv" + tagid, card.optJSONObject("lv" + tagid)));
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
                    article_article_img_url.add(url);
                    imgElement.attr("src", url);
                    imgElement.attr("data-src", url);
                }
                article_article_card_model_list.add(articleCardModel.new ArticleTextModel(art));
            }
        }
        article_article = article_element.outerHtml();
    }

    private HashMap<String, String> colorMap = new HashMap<String, String>()
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
