package cn.luern0313.wristbilibili.models.article;

import org.jsoup.nodes.Attributes;

import java.io.Serializable;

import cn.luern0313.wristbilibili.util.LruCacheUtil;

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
        article_image_src = LruCacheUtil.getImageUrl(attributes.getIgnoreCase("data-src"), 500);
        article_image_width = attributes.hasKey("width") ? Integer.parseInt(attributes.getIgnoreCase("width")) : 0;
        article_image_height = attributes.hasKey("height") ? Integer.parseInt(attributes.getIgnoreCase("height")) : 0;
        article_image_is_img = !attributes.getIgnoreCase("class").contains("cut-off-");
    }
}
