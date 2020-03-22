package cn.luern0313.wristbilibili.models.article;

import org.jsoup.nodes.Attributes;

import java.io.Serializable;

/**
 * 被 luern0313 创建于 2020/2/28.
 */
public class ArticleImageModel implements Serializable
{
    public String article_image_src;
    public int article_image_width;
    public int article_image_height;
    public boolean article_image_is_img;
    public ArticleImageModel(Attributes attributes)
    {
        article_image_src = attributes.getIgnoreCase("data-src");
        article_image_width = Integer.valueOf(attributes.hasKey("width") ? attributes.getIgnoreCase("width") : "0");
        article_image_height = Integer.valueOf(attributes.hasKey("height") ? attributes.getIgnoreCase("height") : "0");
        article_image_is_img = !attributes.getIgnoreCase("class").contains("cut-off-");
    }
}
