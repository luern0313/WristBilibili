package cn.luern0313.wristbilibili.models;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * 被 luern0313 创建于 2020/1/15.
 */

public class UserListPeopleModel
{
    public ArrayList<UserListPeoplePeopleModel> userListPeoplePeopleModelArrayList = new ArrayList<>();
    public UserListPeopleModel()
    { }

    public int getPeople(JSONArray peopleJsonArray)
    {
        ArrayList<UserListPeoplePeopleModel> u = new ArrayList<>();
        for(int i = 0; i < peopleJsonArray.length(); i++)
        {
            JSONObject p = peopleJsonArray.optJSONObject(i);
            u.add(new UserListPeoplePeopleModel(p));
        }
        if(u.size() != 0)
        {
            userListPeoplePeopleModelArrayList.addAll(u);
            return 0;
        }
        return -1;
    }

    public class UserListPeoplePeopleModel
    {
        public String people_uid;
        public String people_name;
        public String people_face;
        public String people_sign;
        public String people_mtime;
        public String people_verify_type;
        public String people_verify_name;
        public int vip;
        public UserListPeoplePeopleModel(JSONObject people)
        {
            this.people_uid = String.valueOf(people.optInt("mid", 0));
            this.people_name = people.optString("uname", "");
            this.people_face = people.optString("face", "");
            this.people_sign = people.optString("sign", "");

            Date date = new Date(people.optInt("mtime", 0) * 1000L);
            SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm", Locale.getDefault());
            this.people_mtime = format.format(date);

            this.people_verify_type = String.valueOf(people.optJSONObject("official_verify").optInt("type", 0));
            this.people_verify_name = people.optJSONObject("official_verify").optString("desc", "");
            this.vip = people.optJSONObject("vip").optInt("vipType", 0);
        }
    }
}
