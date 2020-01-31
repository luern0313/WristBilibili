package cn.luern0313.wristbilibili.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.models.AnimationTimelineModel;
import cn.luern0313.wristbilibili.util.NetWorkUtil;

/**
 * Created by liupe on 2018/11/10.
 * 番剧时间表
 */

public class AnimationTimelineApi
{
    private String cookie;
    private JSONArray timelineJson;
    private ArrayList<String> webHeaders = new ArrayList<String>();
    public AnimationTimelineApi(final String cookie)
    {
        this.cookie = cookie;
        webHeaders = new ArrayList<String>(){{
            add("Cookie"); add(cookie);
            add("Referer"); add("https://www.bilibili.com/anime");
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_WEB);
        }};
    }

    public ArrayList<AnimationTimelineModel> getAnimTimelineList() throws IOException
    {
        try
        {
            String url = "https://bangumi.bilibili.com/web_api/timeline_global";
            timelineJson = new JSONObject(NetWorkUtil.get(url, webHeaders).body().string()).getJSONArray("result");
            int nowTime = (int) (System.currentTimeMillis() / 1000);
            ArrayList<AnimationTimelineModel> anims = new ArrayList<>();
            for(int i = 6; i >= 0; i--)
            {
                JSONArray dailyAnims = timelineJson.getJSONObject(i).getJSONArray("seasons");
                String day = "";
                if(timelineJson.getJSONObject(i).getInt("is_today") == 0) day = timelineJson.getJSONObject(i).getString("date") + " ";
                for(int j = dailyAnims.length() - 1; j >= 0; j--)
                {
                    JSONObject anim = dailyAnims.getJSONObject(j);
                    if(anim.getInt("pub_ts") <= nowTime && anim.getInt("is_published") == 1)
                        anims.add(new AnimationTimelineModel(anim, day));
                }
            }
            return anims;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
