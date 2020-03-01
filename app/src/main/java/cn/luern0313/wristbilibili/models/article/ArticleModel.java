package cn.luern0313.wristbilibili.models.article;


import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * 被 luern0313 创建于 2020/2/20.
 */
public class ArticleModel
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
    public int article_up_official;

    public boolean article_user_like;
    public int article_user_coin;
    public boolean article_user_fav;

    public Element article_article;
    public Elements article_article_nodes;
    public ArrayList<ArticleImageModel> article_article_img_list;
    public ArticleModel(String id, JSONObject article, JSONObject more, Document element)
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
            article_cover[i] = cover.optString(i);
        Elements info = element.getElementsByClass("info").first().children();
        article_channel = info.get(0).text();
        String timestamp = info.get(1).attr("data-ts");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        article_time = format.format(new Date(Integer.valueOf(timestamp) * 1000L));

        JSONObject up = more.has("author") ? more.optJSONObject("author") : new JSONObject();
        article_up_name = up.optString("name");
        article_up_face = up.optString("face");
        article_up_mid = up.optString("mid");
        JSONObject up_off = up.has("official_verify") ? up.optJSONObject("official_verify") : new JSONObject();
        article_up_official = up_off.optInt("type");

        article_user_like = article.optInt("like") == 1;
        article_user_coin = article.optInt("coin");
        article_user_fav = article.optBoolean("favorite");

        article_article = element.getElementsByClass("article-holder").first();
        article_article_nodes = article_article.children();
        for(int i = 0; i < article_article_nodes.size(); i++)
        {
            Element e = article_article_nodes.get(i);
            if(e.tagName().equals("figure") && e.attr("class").equals("img-box"))
            {
                Element img = e.getElementsByTag("img").first();
                article_article_img_list.add(new ArticleImageModel(img.attributes()));
                img.attr("src", img.attr("data-src"));
                img.removeAttr("data-src");
                e.getElementsByTag("img").first().remove();
                e.prependChild(img);
                article_article_nodes.set(i, e);
            }

            Elements es = article_article_nodes.get(i).select("span[class*=color-]");
            for(Element el : es)
            {
                String[] class_list = el.attr("class").split(" ");
                for (String ii : class_list)
                {
                    if(colorMap.containsKey(ii))
                    {
                        el.tagName("font");
                        el.attr("color", colorMap.get(ii));
                    }
                }
            }
            article_article_nodes.get(i).select("figure > img").append("<br>");
            article_article_nodes.get(i).select("blockquote").tagName("em");
            article_article_nodes.get(i).select("span[style*=line-through], font[style*=line-through]").wrap("<s></s>");
            article_article_nodes.get(i).select("figcaption").wrap("<small></small>");
            article_article_nodes.get(i).select("span[class*=font-size-2], font[class*=font-size-2]").wrap("<big></big>");
            article_article_nodes.get(i).select("span[class*=font-size-1], font[class*=font-size-1]").wrap("<small></small>");
            Elements figeles = article_article_nodes.get(i).select("figcaption");
            for(int j = 0; j < figeles.size(); j ++)
                if(!figeles.get(j).text().equals(""))
                    figeles.get(j).wrap("<center></center>");
        }
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
