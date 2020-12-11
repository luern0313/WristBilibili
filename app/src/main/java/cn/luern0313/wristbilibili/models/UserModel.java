package cn.luern0313.wristbilibili.models;

import java.io.Serializable;
import java.util.ArrayList;

import cn.luern0313.lson.annotation.field.LsonBooleanFormatAsNumber;
import cn.luern0313.lson.annotation.field.LsonFieldCallMethod;
import cn.luern0313.lson.annotation.field.LsonPath;
import cn.luern0313.lson.element.LsonArray;
import cn.luern0313.lson.element.LsonObject;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.json.UrlFormat;
import lombok.Getter;
import lombok.Setter;

/**
 * 被 luern0313 创建于 2020/4/23.
 */

@Getter
@Setter
public class UserModel implements Serializable
{
    @LsonPath("card.mid")
    private String cardMid;

    @LsonPath("card.name")
    private String cardName;

    @UrlFormat
    @LsonPath("card.face")
    private String cardFace;

    @LsonPath("card.sign")
    private String cardSign;

    @LsonPath("card.level_info.current_level")
    private int cardLv;

    @LsonPath("card.sex")
    private String cardSex;

    @LsonPath("card.official_verify.type")
    private int cardOfficialType;

    @LsonPath("card.official_verify.title")
    private String cardOfficialVerify;

    @UrlFormat
    @LsonPath("card.nameplate.image_small")
    private String cardNameplateImg;

    @LsonPath("card.vip.vipType")
    private int cardVip;

    @LsonFieldCallMethod(deserialization = "getTab")
    @LsonPath(value = "tab2", preClass = LsonArray.class)
    private ArrayList<ArrayList<String>> tab = new ArrayList<>();

    @LsonPath("archive.count")
    private int videoNum;

    @LsonPath("favourite.count")
    private int favorNum;

    @LsonPath("season.count")
    private int bangumiNum;

    @LsonPath("card.fans")
    private int cardFansNum;

    @LsonPath("card.attention")
    private int cardFollowNum;

    @LsonBooleanFormatAsNumber(equal = 1)
    @LsonPath("card.relation.is_follow")
    private boolean isUserFollow;

    private static ArrayList<ArrayList<String>> getTab(LsonArray tab)
    {
        String[] user_support_tab = new String[]{"dynamic", "contribute", "bangumi", "favorite", "follow", "fans"};

        ArrayList<ArrayList<String>> userTab = new ArrayList<>();
        userTab.add(new ArrayList<String>(){{add("home"); add("主页");}});
        for (int i = 0; i < tab.size(); i++)
        {
            LsonObject t = tab.getAsJsonObject(i);
            final String p = t.getAsString("param");
            final String ti = t.getAsString("title");
            if(DataProcessUtil.getPositionInList(user_support_tab, t.getAsString("param")) > -1)
            {
                userTab.add(new ArrayList<String>(){{add(p); add(ti);}});
                if(p.equals("bangumi"))
                    userTab.add(new ArrayList<String>(){{add("movie"); add("追剧");}});
            }
        }
        userTab.add(new ArrayList<String>(){{add("follow"); add("关注");}});
        userTab.add(new ArrayList<String>(){{add("fans"); add("粉丝");}});
        return userTab;
    }
}
