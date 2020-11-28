package cn.luern0313.wristbilibili.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

import cn.luern0313.lson.LsonUtil;
import cn.luern0313.lson.element.LsonArray;
import cn.luern0313.lson.element.LsonObject;
import cn.luern0313.wristbilibili.models.SearchModel;
import cn.luern0313.wristbilibili.util.NetWorkUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;

/**
 * Created by liupe on 2018/11/23.
 * 没有搜索的api！！
 * ...
 * ...
 * ...好像有
 */

public class SearchApi
{
    private String keyword;
    private int page;
    private JSONArray searchResultNow;
    private ArrayList<String> webHeaders;
    public SearchApi()
    {
        webHeaders = new ArrayList<String>(){{
            add("Cookie"); add(SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, ""));
            add("Referer"); add("https://search.bilibili.com/");
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_WEB);
        }};
    }

    public void setSearchWord(String keyword)
    {
        this.keyword = keyword;
        page = 0;
    }

    public String[] getHotWord() throws IOException
    {
        try
        {
            String url = "https://s.search.bilibili.com/main/hotword";
            JSONObject result = new JSONObject(NetWorkUtil.get(url, webHeaders).body().string());
            if(result.optInt("code") == 0)
            {
                JSONArray hotWordArray = result.getJSONArray("list");
                String[] hotWords = new String[hotWordArray.length()];
                for(int i = 0; i < hotWords.length; i++)
                    hotWords[i] = hotWordArray.getJSONObject(i).getString("keyword");
                return hotWords;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<SearchModel.SearchBaseModel> getSearchResult() throws IOException
    {
        page++;
        try
        {
            String url = "https://api.bilibili.com/x/web-interface/search/all/v2";
            String per = "context=&page=" + page + "&order=&keyword=" + URLEncoder.encode(keyword, "UTF-8") + "&duration=&tids_1=&tids_2=&__refresh__=true&highlight=1&single_column=0";
            String result = NetWorkUtil.get(url + "?" + per, webHeaders).body().string();
            LsonArray resultArray = LsonUtil.parseAsObject(result).getAsJsonObject("data").getAsJsonArray("result");
            ArrayList<SearchModel.SearchBaseModel> searchResult = new ArrayList<>();
            for(int i = 0; i < resultArray.size(); i++)
            {
                LsonObject all_search = resultArray.getAsJsonObject(i);
                LsonArray search = all_search.getAsJsonArray("data");
                if((all_search.getAsString("result_type").equals("media_bangumi") || all_search.getAsString("result_type").equals("media_ft")) && page == 1)
                    for(int j = 0; j < search.size(); j++)
                        searchResult.add(LsonUtil.fromJson(search.getAsJsonObject(j), SearchModel.SearchBangumiModel.class));
                else if(all_search.getAsString("result_type").equals("bili_user") && page == 1)
                    for(int j = 0; j < search.size(); j++)
                        searchResult.add(LsonUtil.fromJson(search.getAsJsonObject(j), SearchModel.SearchUserModel.class));
                else if(all_search.getAsString("result_type").equals("video"))
                    for(int j = 0; j < search.size(); j++)
                        searchResult.add(LsonUtil.fromJson(search.getAsJsonObject(j), SearchModel.SearchVideoModel.class));
            }
            return searchResult;
        }
        catch (RuntimeException e)
        {
            e.printStackTrace();
            page--;
        }
        return null;
    }
}
