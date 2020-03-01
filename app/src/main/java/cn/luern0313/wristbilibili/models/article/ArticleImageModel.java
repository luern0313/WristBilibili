package cn.luern0313.wristbilibili.models.article;

import org.jsoup.nodes.Attributes;

/**
 * 被 luern0313 创建于 2020/2/28.
 */
public class ArticleImageModel
{
    public String article_image_src;
    public int article_image_width;
    public int article_image_height;
    public ArticleImageModel(Attributes attributes)
    {
        article_image_src = attributes.getIgnoreCase("data-src");
        article_image_width = Integer.valueOf(attributes.getIgnoreCase("width"));
        article_image_height = Integer.valueOf(attributes.getIgnoreCase("height"));
    }
}
