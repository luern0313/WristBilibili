package cn.luern0313.wristbilibili.models;


import org.jsoup.nodes.Element;

/**
 * 被 luern0313 创建于 2020/3/6.
 */
public class UnsupportedLinkModel
{
    public String title;
    public Element description;
    public Element keywords;
    public Element author;
    public Element icon;
    public UnsupportedLinkModel(Element element)
    {
        title = element.select("head > title").text();
        description = element.selectFirst("head > meta[name~=(:?(^)|(\\s))description(:?(\\s)|($))]");
        keywords = element.selectFirst("head > meta[name~=(:?(^)|(\\s))keywords(:?(\\s)|($))]");
        author = element.selectFirst("head > meta[name~=(:?(^)|(\\s))author(:?(\\s)|($))]");
        icon = element.selectFirst("head > link[rel~=(:?(^)|(\\s))icon(:?(\\s)|($))]");
    }

    public String getDetail()
    {
        StringBuilder stringBuilder = new StringBuilder();
        if(title != null)
            stringBuilder.append("<big><b>").append(title).append("</b></big><br><br>");
        if(description != null)
            stringBuilder.append("<b>简介</b><br>").append(description.attr("content")).append("<br><br>");
        if(keywords != null)
            stringBuilder.append("<b>关键字</b><br>").append(keywords.attr("content")).append("<br><br>");
        if(author != null)
            stringBuilder.append("<b>作者</b><br>").append(author.attr("content"));
        return stringBuilder.toString();
    }
}
