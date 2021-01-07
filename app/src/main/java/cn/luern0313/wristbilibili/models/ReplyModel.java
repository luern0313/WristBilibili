package cn.luern0313.wristbilibili.models;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.luern0313.lson.LsonUtil;
import cn.luern0313.lson.annotation.field.LsonAddPrefix;
import cn.luern0313.lson.annotation.field.LsonBooleanFormatAsNumber;
import cn.luern0313.lson.annotation.field.LsonDateFormat;
import cn.luern0313.lson.annotation.field.LsonFieldCallMethod;
import cn.luern0313.lson.annotation.field.LsonPath;
import cn.luern0313.lson.annotation.method.LsonCallMethod;
import cn.luern0313.lson.element.LsonArray;
import cn.luern0313.lson.element.LsonObject;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.util.json.ImageUrlFormat;
import lombok.Getter;
import lombok.Setter;

/**
 * 被 luern0313 创建于 2020/1/15.
 */

@Getter
@Setter
public class ReplyModel implements Serializable
{
    private int mode;

    @LsonPath("rpid_str")
    private String id;

    @LsonFieldCallMethod(deserialization = "handlerText")
    @LsonPath("content.")
    private String text;

    private boolean textExpend;

    @LsonDateFormat("yyyy-MM-dd HH:mm")
    @LsonPath("ctime")
    private String time;

    @LsonAddPrefix("#")
    @LsonPath("floor")
    private String floor;

    @LsonPath("like")
    private int likeNum;

    @LsonPath("rcount")
    private int replyNum;

    private ArrayList<String> replyShow = new ArrayList<>();


    @LsonPath("member.mid")
    private String ownerMid;

    @ImageUrlFormat
    @LsonPath("member.avatar")
    private String ownerFace;

    @LsonPath("member.uname")
    private String ownerName;

    @LsonPath("member.level_info.current_level")
    private String ownerLv;

    @LsonPath("member.vip.vipType")
    private int ownerVip;

    @LsonPath("member.official_verify.type")
    private int ownerOfficial;

    private String upMid;

    private boolean isUp;

    @LsonPath("up_action.like")
    private boolean isUpLike;

    @LsonPath("up_action.reply")
    private boolean isUpReply;

    @LsonBooleanFormatAsNumber(equal = 1)
    @LsonPath("action")
    private boolean userLike;

    @LsonBooleanFormatAsNumber(equal = 2)
    @LsonPath("action")
    private boolean userDislike;

    private HashMap<String, Integer> emoteSize = new HashMap<>();

    public ReplyModel(LsonObject replyJson, boolean isUpper, String upUid)
    {
        mode = 0;
        LsonObject contentJson = replyJson.getJsonObject("content");
        text = handlerText((isUpper ? "<img src=\"" + R.drawable.icon_reply_top + "\"/>" : "") +
                                         contentJson.getString("message", ""), contentJson);

        LsonArray replies = replyJson.getJsonArray("replies");
        for(int i = 0; i < replies.size(); i++)
        {
            LsonObject r = replies.getJsonObject(i);
            LsonObject rc = r.getJsonObject("content");
            LsonObject rm = r.getJsonObject("member");
            replyShow.add(handlerText("<a href=\"bilibili://space/" + rm.getString("mid") + "\">" +
                                         rm.getString("uname") + "</a>：" + rc.getString("message"), rc));
        }
        this.upMid = upUid;
    }

    public ReplyModel(int mode)
    {
        this.mode = mode;
    }

    @LsonCallMethod(timing = LsonCallMethod.CallMethodTiming.AFTER_DESERIALIZATION)
    private void initData()
    {
        isUp = ownerMid.equals(upMid);
    }

    private String handlerText(String text, LsonObject content)
    {
        text = text.replace("\n", "<br>");
        Element document = Jsoup.parseBodyFragment(text).body();
        List<TextNode> textNodes = document.textNodes();

        Pattern bvPattern = Pattern.compile("([Bb][Vv][a-zA-Z0-9]{10})");
        for(int i = 0; i < textNodes.size(); i++)
        {
            TextNode textNode = textNodes.get(i);
            Matcher bvMatcher = bvPattern.matcher(textNode.getWholeText());
            if(bvMatcher.find())
            {
                MatchResult bvMatcherResult = bvMatcher.toMatchResult();
                String tag = "<a href=\"bilibili://video/BV" + bvMatcherResult.group(0).substring(2) +
                        "\">BV" + bvMatcherResult.group().substring(2) + "</a>";
                textNode.before(textNode.getWholeText().substring(0, bvMatcherResult.start(0)));
                textNode.before(tag);
                textNode.text(textNode.getWholeText().substring(bvMatcherResult.end(0)));
                textNodes = document.textNodes();
                i--;
            }
        }

        Pattern avPattern = Pattern.compile("([Aa][Vv][0-9]+(?=[^1-9]|$))");
        for(int i = 0; i < textNodes.size(); i++)
        {
            TextNode textNode = textNodes.get(i);
            Matcher avMatcher = avPattern.matcher(textNode.getWholeText());
            if(avMatcher.find())
            {
                MatchResult avMatcherResult = avMatcher.toMatchResult();
                String tag = "<a href=\"bilibili://video/" + avMatcherResult.group(0).substring(2) +
                        "\">av" + avMatcherResult.group(0).substring(2) + "</a>";
                textNode.before(textNode.getWholeText().substring(0, avMatcherResult.start(0)));
                textNode.before(tag);
                textNode.text(textNode.getWholeText().substring(avMatcherResult.end(0)));
                textNodes = document.textNodes();
                i--;
            }
        }

        Pattern cvPattern = Pattern.compile("([Cc][Vv][0-9]+(?=[^1-9]|$))");
        for(int i = 0; i < textNodes.size(); i++)
        {
            TextNode textNode = textNodes.get(i);
            Matcher cvMatcher = cvPattern.matcher(textNode.getWholeText());
            if(cvMatcher.find())
            {
                MatchResult cvMatcherResult = cvMatcher.toMatchResult();
                String tag = "<a href=\"bilibili://article/" + cvMatcherResult.group(0).substring(2) +
                        "\">cv" + cvMatcherResult.group(0).substring(2) + "</a>";
                textNode.before(textNode.getWholeText().substring(0, cvMatcherResult.start(0)));
                textNode.before(tag);
                textNode.text(textNode.getWholeText().substring(cvMatcherResult.end(0)));
                textNodes = document.textNodes();
                i--;
            }
        }

        LsonObject emote = content.getJsonObject("emote");
        String[] emoteKeys = emote.getKeys();
        for (String key : emoteKeys)
        {
            LsonObject emoteJson = emote.getJsonObject(key);
            String tag = "<img src=\"" + emoteJson.getString("url") + "\"/>";
            for(int i = 0; i < textNodes.size(); i++)
            {
                TextNode textNode = textNodes.get(i);
                if(textNode.getWholeText().contains(key))
                {
                    emoteSize.put(emoteJson.getString("url"), emoteJson.getJsonObject("meta").getInt("size", 1));
                    textNode.before(textNode.getWholeText().substring(0, textNode.getWholeText().indexOf(key)));
                    textNode.before(tag);
                    textNode.text(textNode.getWholeText().substring(textNode.getWholeText().indexOf(key) + key.length()));
                    textNodes = document.textNodes();
                    i--;
                }
            }
        }

        LsonArray members = content.getJsonArray("members");
        for(int i = 0; i < members.size(); i++)
        {
            if(!members.isNull(i))
            {
                String name = members.getJsonObject(i).getString("uname");
                String key = "@" + name;
                String uid = members.getJsonObject(i).getString("mid");
                String tag = "<a href=\"bilibili://space/" + uid + "\">@" + name + "</a>";
                for (int j = 0; j < textNodes.size(); j++)
                {
                    TextNode textNode = textNodes.get(j);
                    if(textNode.getWholeText().contains(key))
                    {
                        textNode.before(textNode.getWholeText().substring(0, textNode.getWholeText().indexOf(key)));
                        textNode.before(tag);
                        textNode.text(textNode.getWholeText().substring(textNode.getWholeText().indexOf(key) + key.length()));
                        textNodes = document.textNodes();
                        i--;
                    }
                }
            }
        }

        LsonObject topics = content.getJsonObject("topics_uri");
        String[] topicsKeys = topics.getKeys();
        for (String key : topicsKeys)
        {
            String tagName = "#" + key + "#";
            String tag = "<a href=\"" + topics.getString(key) + "\">" + tagName + "</a>";
            for(int i = 0; i < textNodes.size(); i++)
            {
                TextNode textNode = textNodes.get(i);
                if(textNode.getWholeText().contains(tagName))
                {
                    textNode.before(textNode.getWholeText().substring(0, textNode.getWholeText().indexOf(tagName)));
                    textNode.before(tag);
                    textNode.text(textNode.getWholeText().substring(textNode.getWholeText().indexOf(tagName) + tagName.length()));
                    textNodes = document.textNodes();
                    i--;
                }
            }
        }

        LsonObject jump =  content.getJsonObject("jump_url");
        String[] jumpKeys = jump.getKeys();
        for (String key : jumpKeys)
        {
            String name = jump.getJsonObject(key).getString("title");
            String season_id = LsonUtil.parseAsObject(jump.getJsonObject(key).getString("click_report")).getString("season_id");
            String tag = "<a href=\"bilibili://bangumi/season/" + season_id + "\">" + name + "</a>";
            for(int i = 0; i < textNodes.size(); i++)
            {
                TextNode textNode = textNodes.get(i);
                if(textNode.getWholeText().contains(key))
                {
                    textNode.before(textNode.getWholeText().substring(0, textNode.getWholeText().indexOf(key)));
                    textNode.before(tag);
                    textNode.text(textNode.getWholeText().substring(textNode.getWholeText().indexOf(key) + key.length()));
                    textNodes = document.textNodes();
                    i--;
                }
            }
        }

        Pattern urlPattern = Pattern.compile("((?:https?://)?[a-zA-Z0-9.]+?\\.(?:com|cn|top|org|gov|edu|net)(?:/[a-zA-Z0-9\\-_.~!*'();:@&=+$,/?#\\[\\]]*)*)");
        for(int i = 0; i < textNodes.size(); i++)
        {
            TextNode textNode = textNodes.get(i);
            Matcher urlMatcher = urlPattern.matcher(textNode.getWholeText());
            if(urlMatcher.find())
            {
                MatchResult urlMatcherResult = urlMatcher.toMatchResult();
                String tag = "<a href=\"" + urlMatcherResult.group(0) + "\">" + urlMatcherResult.group() + "</a>";
                textNode.before(textNode.getWholeText().substring(0, urlMatcherResult.start(0)));
                textNode.before(tag);
                textNode.text(textNode.getWholeText().substring(urlMatcherResult.end(0)));
                textNodes = document.textNodes();
                i--;
            }
        }

        return document.outerHtml();
    }

}
