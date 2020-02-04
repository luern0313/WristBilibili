package cn.luern0313.wristbilibili.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.SearchAdapter;
import cn.luern0313.wristbilibili.api.SearchApi;
import cn.luern0313.wristbilibili.models.SearchModel;
import cn.luern0313.wristbilibili.ui.BangumiActivity;
import cn.luern0313.wristbilibili.ui.MainActivity;
import cn.luern0313.wristbilibili.ui.OtherUserActivity;
import cn.luern0313.wristbilibili.ui.VideodetailsActivity;
import cn.luern0313.wristbilibili.util.HtmlTagHandlerUtil;

/**
 * Created by liupe on 2018/11/16.
 * 搜索。。
 */

public class SearchFragment extends Fragment
{
    Context ctx;
    SearchApi searchApi;
    SearchAdapter searchAdapter;
    SearchAdapter.SearchAdapterListener searchAdapterListener;
    HtmlTagHandlerUtil htmlTagHandlerUtil;

    View rootLayout;
    TextView seaHotWordText;
    GridView seaHotWordList;
    ListView seaListView;
    View loadingView;
    EditText seaEdittext;
    TextView inButton;
    TextView seaButton;
    ImageView seaLoadImg;

    Handler handler = new Handler();
    Runnable runnableHotWord;
    Runnable runnableHotWordErr;
    Runnable runnableNoweb;
    Runnable runnableUi;
    Runnable runnableAddlist;
    Runnable runnableNoWebH;
    Runnable runnableNoresult;
    Runnable runnableNomore;

    String[] hotWordArray;
    ArrayList<SearchModel> searchResult;
    HotWordAdapter hotwordAdapter;

    boolean isLoading = true;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_search, container, false);
        searchApi = new SearchApi(MainActivity.sharedPreferences.getString("cookies", ""));
        htmlTagHandlerUtil = new HtmlTagHandlerUtil(ctx);
        searchAdapterListener = new SearchAdapter.SearchAdapterListener()
        {
            @Override
            public void onClick(int viewId, int position)
            {
                onViewClick(viewId, position);
            }
        };

        seaHotWordText = rootLayout.findViewById(R.id.sea_hotwordtext);
        seaHotWordList = rootLayout.findViewById(R.id.sea_hotword);
        seaListView = rootLayout.findViewById(R.id.sea_listview);
        loadingView = inflater.inflate(R.layout.widget_loading, null);
        seaEdittext = rootLayout.findViewById(R.id.sea_edittext);
        inButton = rootLayout.findViewById(R.id.sea_inbutton);
        seaButton = rootLayout.findViewById(R.id.sea_seabutton);
        seaLoadImg = rootLayout.findViewById(R.id.sea_searching_img);

        runnableHotWord = new Runnable()
        {
            @Override
            public void run()
            {
                hotwordAdapter = new HotWordAdapter(ctx, android.R.layout.simple_list_item_1, hotWordArray);
                seaHotWordText.setText("大家都在搜");
                seaHotWordList.setAdapter(hotwordAdapter);
            }
        };

        runnableHotWordErr = new Runnable()
        {
            @Override
            public void run()
            {
                seaHotWordText.setText("搜索热词加载失败");
            }
        };

        runnableNoweb = new Runnable()
        {
            @Override
            public void run()
            {
                rootLayout.findViewById(R.id.sea_noweb).setVisibility(View.VISIBLE);
                rootLayout.findViewById(R.id.sea_searching).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.sea_nonthing).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.sea_listview).setVisibility(View.GONE);
            }
        };

        runnableNoWebH = new Runnable()
        {
            @Override
            public void run()
            {
                ((TextView) loadingView.findViewById(R.id.wid_load_text)).setText("好像没有网络...\n检查下网络？");
                loadingView.findViewById(R.id.wid_load_button).setVisibility(View.VISIBLE);
            }
        };

        runnableUi = new Runnable()
        {
            @Override
            public void run()
            {
                searchAdapter = new SearchAdapter(inflater, searchResult, seaListView, searchAdapterListener, htmlTagHandlerUtil);
                seaListView.setAdapter(searchAdapter);
                rootLayout.findViewById(R.id.sea_noweb).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.sea_searching).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.sea_nonthing).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.sea_listview).setVisibility(View.VISIBLE);
            }
        };

        runnableNoresult = new Runnable()
        {
            @Override
            public void run()
            {
                rootLayout.findViewById(R.id.sea_noweb).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.sea_searching).setVisibility(View.GONE);
                rootLayout.findViewById(R.id.sea_nonthing).setVisibility(View.VISIBLE);
                rootLayout.findViewById(R.id.sea_listview).setVisibility(View.GONE);
            }
        };

        runnableNomore = new Runnable()
        {
            @Override
            public void run()
            {
                ((TextView) loadingView.findViewById(R.id.wid_load_text)).setText("  没有更多了...");
            }
        };

        runnableAddlist = new Runnable()
        {
            @Override
            public void run()
            {
                searchAdapter.notifyDataSetChanged();
            }
        };

        loadingView.findViewById(R.id.wid_load_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((TextView) loadingView.findViewById(R.id.wid_load_text)).setText(" 加载中. . .");
                loadingView.findViewById(R.id.wid_load_button).setVisibility(View.GONE);
                getMoreSearch();
            }
        });

        inButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    Intent voiceInputIntent = new Intent("com.mobvoi.ticwear.action.SPEECH");
                    voiceInputIntent.putExtra("start_mode", "start_mode_with_voice_input");
                    startActivityForResult(voiceInputIntent, 0);
                }
                catch (Exception e)
                {
                    Toast.makeText(ctx, "抱歉，该手表不支持语音输入", Toast.LENGTH_SHORT).show();
                }
            }
        });

        seaButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getSearch();
            }
        });

        seaListView.setOnScrollListener(new AbsListView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState)
            {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
            {
                if(visibleItemCount + firstVisibleItem == totalItemCount && !isLoading)
                {
                    isLoading = true;
                    //((TextView) loadingView.findViewById(R.id.dyload_text)).setText(" 加载中...");
                    getMoreSearch();
                }
            }
        });

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    //获取搜索关键词
                    hotWordArray = searchApi.getHotWord();
                    if(hotWordArray != null && hotWordArray.length != 0)
                        handler.post(runnableHotWord);
                    else handler.post(runnableHotWordErr);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    handler.post(runnableHotWordErr);
                }
            }
        }).start();

        seaListView.addFooterView(loadingView);

        return rootLayout;
    }

    private class HotWordAdapter extends ArrayAdapter
    {
        HotWordAdapter(Context context, int resource, Object[] objects)
        {
            super(context, resource, objects);
        }

        MyListener listener = new MyListener();

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            String str = (String) getItem(position);//通过position获取当前要赋值的内容
            if(convertView == null)
            {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            }
            TextView tv = convertView.findViewById(android.R.id.text1);
            tv.setText(str);
            tv.setTextSize(12);
            tv.setOnClickListener(listener);
            return convertView;
        }

        //MyListener类继承OnClickListener，用来监听每个Item的点击事件
        private class MyListener implements View.OnClickListener
        {
            @Override
            public void onClick(View v)
            {
                seaEdittext.setText(((TextView) v).getText());
                getSearch();
            }
        }
    }

    void getSearch()
    {
        InputMethodManager inputMethodManager = (InputMethodManager) ctx.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(seaEdittext.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        isLoading = true;
        searchApi.setSearchWord(seaEdittext.getText().toString());

        seaLoadImg.setImageResource(R.drawable.anim_searching);
        AnimationDrawable loadingImgAnim = (AnimationDrawable) seaLoadImg.getDrawable();
        loadingImgAnim.start();

        rootLayout.findViewById(R.id.sea_noweb).setVisibility(View.GONE);
        rootLayout.findViewById(R.id.sea_searching).setVisibility(View.VISIBLE);
        rootLayout.findViewById(R.id.sea_nonthing).setVisibility(View.GONE);
        rootLayout.findViewById(R.id.sea_listview).setVisibility(View.GONE);
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    searchResult = searchApi.getSearchResult();
                    if(searchResult != null && searchResult.size() != 0)
                        handler.post(runnableUi);
                    else handler.post(runnableNoresult);
                    isLoading = false;
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    handler.post(runnableNoweb);
                }
            }
        }).start();
    }

    void getMoreSearch()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    ArrayList<SearchModel> arrayList = searchApi.getSearchResult();
                    if(arrayList.size() != 0)
                    {
                        searchResult.addAll(arrayList);
                        isLoading = false;
                        handler.post(runnableAddlist);
                    }
                    else
                    {
                        handler.post(runnableNomore);
                    }
                }
                catch (IOException e)
                {
                    handler.post(runnableNoWebH);
                    e.printStackTrace();
                }
            }
        }).start();
    }

    void onViewClick(int id, int position)
    {
        SearchModel searchModel = searchResult.get(position);
        if(searchModel.search_mode == 0)
        {
            Intent intent = new Intent(ctx, BangumiActivity.class);
            intent.putExtra("season_id", ((SearchModel.SearchBangumiModel) searchModel).search_bangumi_season_id);
            startActivity(intent);
        }
        else if(searchModel.search_mode == 1)
        {
            Intent intent = new Intent(ctx, OtherUserActivity.class);
            intent.putExtra("mid", ((SearchModel.SearchUserModel) searchModel).search_user_mid);
            startActivity(intent);
        }
        else if(searchModel.search_mode == 2)
        {
            Intent intent = new Intent(ctx, VideodetailsActivity.class);
            intent.putExtra("aid", ((SearchModel.SearchVideoModel) searchModel).search_video_aid);
            startActivity(intent);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0)
        {
            if(data != null)
            {
                String result = data.getExtras().getString("speech_content");
                if(result.endsWith("。")) result = result.substring(0, result.length() - 1);
                seaEdittext.setText(result);
                getSearch();
            }
            else
            {
                Toast.makeText(ctx, "识别失败，请重试", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
