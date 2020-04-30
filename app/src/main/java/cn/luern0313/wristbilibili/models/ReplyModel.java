package cn.luern0313.wristbilibili.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.ReplyHtmlTagHandlerUtil;

/**
 * 被 luern0313 创建于 2020/1/15.
 */

public class ReplyModel implements Serializable
{
    public int reply_mode;
    public String reply_id;
    public String reply_text;
    public String reply_time;
    public String reply_floor;
    public int reply_like_num;
    public String reply_reply_num;
    public ArrayList<String> reply_reply_show = new ArrayList<>();

    public String reply_owner_mid;
    public String reply_owner_face;
    public String reply_owner_name;
    public String reply_owner_lv;
    public int reply_owner_vip;

    public boolean reply_is_up;
    public boolean reply_is_up_like;
    public boolean reply_is_up_reply;
    public boolean reply_user_like;
    public boolean reply_user_dislike;
    public HashMap<String, Integer> reply_emote_size = new HashMap<String, Integer>();

    public ReplyModel(JSONObject replyJson, boolean isUpper, String upUid)
    {
        reply_mode = 0;
        reply_id = replyJson.optString("rpid_str");
        JSONObject contentJson = replyJson.has("content") ? replyJson.optJSONObject("content") : new JSONObject();
        reply_text = handlerText((isUpper ? "<img src=\"" + R.drawable.icon_reply_top + "\"/>" : "") +
                                         contentJson.optString("message", ""), contentJson);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        reply_time = format.format(new Date(replyJson.optInt("ctime") * 1000L));

        JSONArray replies = (replyJson.has("replies") && replyJson.optJSONArray("replies") != null) ?
                replyJson.optJSONArray("replies") : new JSONArray();
        for(int i = 0; i < replies.length(); i++)
        {
            JSONObject r = replies.optJSONObject(i);
            JSONObject rc = r.has("content") ? r.optJSONObject("content") : new JSONObject();
            JSONObject rm = r.has("member") ? r.optJSONObject("member") : new JSONObject();
            reply_reply_show.add(handlerText("<" + ReplyHtmlTagHandlerUtil.TAG_A + " href=\"bilibili://space/" + rm.optString("mid") + "\">" +
                                         rm.optString("uname") + "</" + ReplyHtmlTagHandlerUtil.TAG_A + ">：" + rc.optString("message"), rc));
        }

        reply_floor = replyJson.has("floor") ? ("#" + replyJson.optInt("floor")) : "";
        reply_like_num = replyJson.optInt("like", 0);
        reply_reply_num = DataProcessUtil.getView(replyJson.optInt("rcount", 0));

        JSONObject reply_owner_json = replyJson.has("member") ? replyJson.optJSONObject("member") : new JSONObject();
        reply_owner_mid = reply_owner_json.optString("mid");
        reply_owner_face = reply_owner_json.optString("avatar");
        reply_owner_name = reply_owner_json.optString("uname");
        reply_owner_lv = reply_owner_json.has("level_info") ?
                String.valueOf(reply_owner_json.optJSONObject("level_info").optInt("current_level")) : "0";
        reply_owner_vip = reply_owner_json.has("vip") ?
                reply_owner_json.optJSONObject("vip").optInt("vipType") : 0;

        reply_is_up = reply_owner_mid.equals(upUid);
        JSONObject up_action = replyJson.has("up_action") ? replyJson.optJSONObject("up_action") : new JSONObject();
        reply_is_up_like = up_action.optBoolean("like", false);
        reply_is_up_reply = up_action.optBoolean("reply", false);
        reply_user_like = replyJson.optInt("action") == 1;
        reply_user_dislike = replyJson.optInt("action") == 2;
    }

    public ReplyModel(int mode)
    {
        reply_mode = mode;
    }

    private String handlerText(String text, JSONObject content)
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
                String tag = "<" + ReplyHtmlTagHandlerUtil.TAG_A + " href=\"bilibili://video/BV" + bvMatcherResult.group(0).substring(2) +
                        "\">BV" + bvMatcherResult.group().substring(2) + "</" + ReplyHtmlTagHandlerUtil.TAG_A + ">";
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
                String tag = "<" + ReplyHtmlTagHandlerUtil.TAG_A + " href=\"bilibili://video/" + avMatcherResult.group(0).substring(2) +
                        "\">av" + avMatcherResult.group(0).substring(2) + "</" + ReplyHtmlTagHandlerUtil.TAG_A + ">";
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
                String tag = "<a" + ReplyHtmlTagHandlerUtil.TAG_A + " href=\"bilibili://article/" + cvMatcherResult.group(0).substring(2) +
                        "\">cv" + cvMatcherResult.group(0).substring(2) + "</" + ReplyHtmlTagHandlerUtil.TAG_A + ">";
                textNode.before(textNode.getWholeText().substring(0, cvMatcherResult.start(0)));
                textNode.before(tag);
                textNode.text(textNode.getWholeText().substring(cvMatcherResult.end(0)));
                textNodes = document.textNodes();
                i--;
            }
        }

        JSONObject emote = content.has("emote") ? content.optJSONObject("emote") : new JSONObject();
        Iterator<String> emoteKeys = emote.keys();
        while(emoteKeys.hasNext())
        {
            String key = emoteKeys.next();
            JSONObject emoteJson = emote.optJSONObject(key);
            String tag = "<img src=\"" + emoteJson.optString("url") + "\"/>";
            for(int i = 0; i < textNodes.size(); i++)
            {
                TextNode textNode = textNodes.get(i);
                if(textNode.getWholeText().contains(key))
                {
                    reply_emote_size.put(emoteJson.optString("url"), emoteJson.has("meta") ? emoteJson.optJSONObject("meta").optInt("size") : 1);
                    textNode.before(textNode.getWholeText().substring(0, textNode.getWholeText().indexOf(key)));
                    textNode.before(tag);
                    textNode.text(textNode.getWholeText().substring(textNode.getWholeText().indexOf(key) + key.length()));
                    textNodes = document.textNodes();
                    i--;
                }
            }
        }

        JSONArray members = content.has("members") ? content.optJSONArray("members") : new JSONArray();
        for(int i = 0; i < members.length(); i++)
        {
            if(members.optJSONObject(i) != null)
            {
                String name = members.optJSONObject(i).optString("uname");
                String key = "@" + name;
                String uid = members.optJSONObject(i).optString("mid");
                String tag = "<" + ReplyHtmlTagHandlerUtil.TAG_A + " href=\"bilibili://space/" + uid + "\">@" + name + "</" + ReplyHtmlTagHandlerUtil.TAG_A + ">";
                for (int j = 0; j < textNodes.size(); j++)
                {
                    TextNode textNode = textNodes.get(j);
                    if(textNode.getWholeText().contains(key))
                    {
                        textNode.before(textNode.getWholeText().substring(0, textNode.getWholeText()
                                .indexOf(key)));
                        textNode.before(tag);
                        textNode.text(textNode.getWholeText().substring(textNode.getWholeText().indexOf(key) + key.length()));
                        textNodes = document.textNodes();
                        i--;
                    }
                }
            }
        }

        JSONObject topics = content.has("topics_uri") ? content.optJSONObject("topics_uri") : new JSONObject();
        Iterator<String> topicsKeys = topics.keys();
        while(topicsKeys.hasNext())
        {
            String key = topicsKeys.next();
            String tag = "<" + ReplyHtmlTagHandlerUtil.TAG_A + " href=\"" + topics.optString(key) + "\">#" + key + "#</" + ReplyHtmlTagHandlerUtil.TAG_A + ">";
            for(int i = 0; i < textNodes.size(); i++)
            {
                TextNode textNode = textNodes.get(i);
                if(textNode.getWholeText().contains(key))
                {
                    textNode.before(textNode.getWholeText().substring(0, textNode.getWholeText().indexOf(key) - 1));
                    textNode.before(tag);
                    textNode.text(textNode.getWholeText().substring(textNode.getWholeText().indexOf(key) + key.length() + 1));
                    textNodes = document.textNodes();
                    i--;
                }
            }
        }

        JSONObject jump = content.has("jump_url") ? content.optJSONObject("jump_url") : new JSONObject();
        Iterator<String> jumpKeys = jump.keys();
        while(jumpKeys.hasNext())
        {
            String key = jumpKeys.next();
            try
            {
                String name = jump.optJSONObject(key).optString("title");
                String season_id = new JSONObject(jump.optJSONObject(key).optString("click_report")).optString("season_id");
                String tag = "<" + ReplyHtmlTagHandlerUtil.TAG_A + " href=\"bilibili://bangumi/season/" + season_id + "\">" + name + "</" + ReplyHtmlTagHandlerUtil.TAG_A + ">";
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
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }

        Pattern urlPattern = Pattern.compile("((?:https?://)?[^/\\s\\u4e00-\\u9fa5#!@?,%&*\\-+_=]+?\\.(?:com|cn|top|org|gov|edu|net)(?:/[^\\u4e00-\\u9fa5\\s。]+)*)");
        for(int i = 0; i < textNodes.size(); i++)
        {
            TextNode textNode = textNodes.get(i);
            Matcher urlMatcher = urlPattern.matcher(textNode.getWholeText());
            if(urlMatcher.find())
            {
                MatchResult urlMatcherResult = urlMatcher.toMatchResult();
                String tag = "<" + ReplyHtmlTagHandlerUtil.TAG_A + " href=\"" + urlMatcherResult.group(0) + "\">" + urlMatcherResult.group() + "</" + ReplyHtmlTagHandlerUtil.TAG_A + ">";
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
