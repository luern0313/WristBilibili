package cn.luern0313.wristbilibili.models.article;

import org.jsoup.nodes.Attributes;

import java.io.Serializable;

import cn.luern0313.wristbilibili.api.ArticleApi;
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
        article_image_src = LruCacheUtil.getImageUrl(attributes.getIgnoreCase("data-src"), ArticleApi.getArticleImageWidthSize());
        int width = attributes.hasKey("width") ? Integer.parseInt(attributes.getIgnoreCase("width")) : 0;
        int height = attributes.hasKey("height") ? Integer.parseInt(attributes.getIgnoreCase("height")) : 0;
        if(width > ArticleApi.getArticleImageWidthSize())
        {
            article_image_width = ArticleApi.getArticleImageWidthSize();
            article_image_height = Math.round((float) height * ArticleApi.getArticleImageWidthSize() / width);
        }
        else
        {
            article_image_width = width;
            article_image_height = height;
        }
        article_image_is_img = !attributes.getIgnoreCase("class").contains("cut-off-");

    }
}
