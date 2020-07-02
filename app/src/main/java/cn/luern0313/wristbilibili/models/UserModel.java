package cn.luern0313.wristbilibili.models;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.LruCacheUtil;

/**
 * 被 luern0313 创建于 2020/4/23.
 */
public class UserModel implements Serializable
{
    public String user_card_mid;
    public String user_card_name;
    public String user_card_face;
    public String user_card_sign;
    public int user_card_lv;
    public String user_card_sex;
    public int user_card_official_type;
    public String user_card_official_verify;
    public String user_card_nameplate_img;
    public int user_card_vip;

    public ArrayList<ArrayList<String>> user_tab = new ArrayList<>();
    public int user_video_num;
    public int user_favor_num;
    public int user_bangumi_num;
    public int user_card_fans_num;
    public int user_card_follow_num;

    public boolean user_user_follow;
    public UserModel(JSONObject user)
    {
        JSONObject card = user.has("card") ? user.optJSONObject("card") : new JSONObject();
        user_card_mid = String.valueOf(card.optInt("mid"));
        user_card_name = card.optString("name");
        user_card_face = LruCacheUtil.getImageUrl(card.optString("face"));
        user_card_sign = card.optString("sign");
        user_card_fans_num = card.optInt("fans");
        user_card_follow_num = card.optInt("attention");
        JSONObject card_lv = card.has("level_info") ? card.optJSONObject("level_info") : new JSONObject();
        user_card_lv = card_lv.optInt("current_level");
        user_card_sex = card.optString("sex");
        JSONObject card_official = card.has("official_verify") ? card.optJSONObject("official_verify") : new JSONObject();
        user_card_official_type = card_official.optInt("type");
        user_card_official_verify = card_official.optString("title");
        JSONObject card_vip = card.has("vip") ? card.optJSONObject("vip") : new JSONObject();
        JSONObject nameplate = card.has("nameplate") ? card.optJSONObject("nameplate") : new JSONObject();
        user_card_nameplate_img = LruCacheUtil.getImageUrl(nameplate.optString("image_small"));
        user_card_vip = card_vip.optInt("vipType");

        JSONArray tab = user.has("tab2") ? user.optJSONArray("tab2") : new JSONArray();
        String[] user_support_tab = new String[]{"dynamic", "contribute", "bangumi", "favorite", "follow", "fans"};
        user_tab.add(new ArrayList<String>(){{add("home"); add("主页");}});
        for (int i = 0; i < tab.length(); i++)
        {
            JSONObject t = tab.optJSONObject(i);
            final String p = t.optString("param");
            final String ti = t.optString("title");
            if(DataProcessUtil.getPositionInList(user_support_tab, t.optString("param")) != -1)
            {
                user_tab.add(new ArrayList<String>() {{add(p);add(ti);}});
                if(p.equals("bangumi"))
                    user_tab.add(new ArrayList<String>(){{add("movie");add("追剧");}});
            }
        }
        user_tab.add(new ArrayList<String>(){{add("follow"); add("关注");}});
        user_tab.add(new ArrayList<String>(){{add("fans"); add("粉丝");}});

        JSONObject archive = user.has("archive") ? user.optJSONObject("archive") : new JSONObject();
        user_video_num = archive.optInt("count");
        JSONObject favourite = user.has("favourite") ? user.optJSONObject("favourite") : new JSONObject();
        user_favor_num = favourite.optInt("count");
        JSONObject bangumi = user.has("season") ? user.optJSONObject("season") : new JSONObject();
        user_bangumi_num = bangumi.optInt("count");

        JSONObject user_user = card.has("relation") ? card.optJSONObject("relation") : new JSONObject();
        user_user_follow = user_user.optInt("is_follow") == 1;
    }
}
