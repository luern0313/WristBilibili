package cn.luern0313.wristbilibili.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.SearchAdapter;
import cn.luern0313.wristbilibili.api.SearchApi;
import cn.luern0313.wristbilibili.models.SearchModel;
import cn.luern0313.wristbilibili.ui.BangumiActivity;
import cn.luern0313.wristbilibili.ui.UserActivity;
import cn.luern0313.wristbilibili.ui.VideoActivity;
import cn.luern0313.wristbilibili.util.ListViewTouchListener;
import cn.luern0313.wristbilibili.util.SearchHtmlTagHandlerUtil;
import cn.luern0313.wristbilibili.util.ViewScrollListener;
import cn.luern0313.wristbilibili.util.ViewTouchListener;
import cn.luern0313.wristbilibili.widget.TitleView;

/**
 * Created by liupe on 2018/11/16.
 * 搜索。。
 */

public class SearchFragment extends Fragment
{
    private Context ctx;
    private SearchApi searchApi;
    private SearchAdapter searchAdapter;
    private SearchAdapter.SearchAdapterListener searchAdapterListener;
    private SearchHtmlTagHandlerUtil htmlTagHandlerUtil;
    private TitleView.TitleViewListener titleViewListener;
    private ListViewTouchListener.ListViewScrollListener listViewScrollListener;

    private View rootLayout;
    private TextView seaHotWordText;
    private GridView seaHotWordList;
    private ListView seaListView;
    private View loadingView;
    private LinearLayout searchBox;
    private EditText seaEdittext;
    private TextView inButton;
    private TextView seaButton;
    private ImageView seaLoadImg;

    private final Handler handler = new Handler();
    private Runnable runnableHotWord, runnableHotWordErr, runnableNoweb, runnableUi;
    private Runnable runnableAddlist, runnableNoWebH, runnableNoresult, runnableNomore;

    private ObjectAnimator searchBoxAnimator;

    private String[] hotWordArray;
    private ArrayList<SearchModel.SearchBaseModel> searchResult;
    private HotWordAdapter hotwordAdapter;

    private boolean isLoading = true;
    private boolean isHide;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_search, container, false);
        searchApi = new SearchApi();
        htmlTagHandlerUtil = new SearchHtmlTagHandlerUtil(ctx);
        searchAdapterListener = this::onViewClick;

        listViewScrollListener = new ListViewTouchListener.ListViewScrollListener()
        {
            @Override
            public void hide()
            {
                hideSearchBox();
            }

            @Override
            public void show()
            {
                showSearchBox();
            }
        };

        seaHotWordText = rootLayout.findViewById(R.id.sea_hotword_text);
        seaHotWordList = rootLayout.findViewById(R.id.sea_hotword);
        seaListView = rootLayout.findViewById(R.id.sea_listview);
        loadingView = inflater.inflate(R.layout.widget_loading, null);
        searchBox = rootLayout.findViewById(R.id.sea_box);
        seaEdittext = rootLayout.findViewById(R.id.sea_edittext);
        inButton = rootLayout.findViewById(R.id.sea_inbutton);
        seaButton = rootLayout.findViewById(R.id.sea_seabutton);
        seaLoadImg = rootLayout.findViewById(R.id.sea_searching_img);

        runnableHotWord = () -> {
            hotwordAdapter = new HotWordAdapter(ctx, android.R.layout.simple_list_item_1, hotWordArray);
            seaHotWordText.setText("大家都在搜");
            seaHotWordList.setAdapter(hotwordAdapter);
        };

        runnableHotWordErr = () -> seaHotWordText.setText("搜索热词加载失败");

        runnableNoweb = () -> {
            rootLayout.findViewById(R.id.sea_noweb).setVisibility(View.VISIBLE);
            rootLayout.findViewById(R.id.sea_searching).setVisibility(View.GONE);
            rootLayout.findViewById(R.id.sea_nonthing).setVisibility(View.GONE);
            rootLayout.findViewById(R.id.sea_listview).setVisibility(View.GONE);
        };

        runnableNoWebH = () -> {
            ((TextView) loadingView.findViewById(R.id.wid_load_text)).setText(getString(R.string.main_tip_no_more_web));
            loadingView.findViewById(R.id.wid_load_button).setVisibility(View.VISIBLE);
        };

        runnableUi = () -> {
            searchAdapter = new SearchAdapter(inflater, searchResult, seaListView, searchAdapterListener, htmlTagHandlerUtil);
            seaListView.setAdapter(searchAdapter);
            rootLayout.findViewById(R.id.sea_noweb).setVisibility(View.GONE);
            rootLayout.findViewById(R.id.sea_searching).setVisibility(View.GONE);
            rootLayout.findViewById(R.id.sea_nonthing).setVisibility(View.GONE);
            rootLayout.findViewById(R.id.sea_listview).setVisibility(View.VISIBLE);
        };

        runnableNoresult = () -> {
            rootLayout.findViewById(R.id.sea_noweb).setVisibility(View.GONE);
            rootLayout.findViewById(R.id.sea_searching).setVisibility(View.GONE);
            rootLayout.findViewById(R.id.sea_nonthing).setVisibility(View.VISIBLE);
            rootLayout.findViewById(R.id.sea_listview).setVisibility(View.GONE);
        };

        runnableNomore = () -> ((TextView) loadingView.findViewById(R.id.wid_load_text)).setText(getString(R.string.main_tip_no_more_data));

        runnableAddlist = () -> searchAdapter.notifyDataSetChanged();

        loadingView.findViewById(R.id.wid_load_button).setOnClickListener(v -> {
            ((TextView) loadingView.findViewById(R.id.wid_load_text)).setText(getString(R.string.main_tip_no_more_data_loading));
            loadingView.findViewById(R.id.wid_load_button).setVisibility(View.GONE);
            getMoreSearch();
        });

        inButton.setOnClickListener(v -> {
            try
            {
                Intent voiceInputIntent = new Intent("com.mobvoi.ticwear.action.SPEECH");
                voiceInputIntent.putExtra("start_mode", "start_mode_with_voice_input");
                startActivityForResult(voiceInputIntent, 0);
            }
            catch (Exception e)
            {
                Toast.makeText(ctx, getString(R.string.main_tip_voice_input), Toast.LENGTH_SHORT).show();
            }
        });

        seaButton.setOnClickListener(v -> getSearch());

        seaListView.setOnScrollListener(new ViewScrollListener(this));
        seaListView.setOnTouchListener(new ViewTouchListener(seaListView, titleViewListener, customViewListener));

        new Thread(() -> {
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
        }).start();

        seaListView.addFooterView(loadingView);

        return rootLayout;
    }

    private class HotWordAdapter extends ArrayAdapter<String>
    {
        HotWordAdapter(Context context, int resource, String[] objects)
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

    private void getSearch()
    {
        InputMethodManager inputMethodManager = (InputMethodManager) ctx.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE);
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
        new Thread(() -> {
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
        }).start();
    }

    private void getMoreSearch()
    {
        new Thread(() -> {
            try
            {
                ArrayList<SearchModel.SearchBaseModel> arrayList = searchApi.getSearchResult();
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
        }).start();
    }

    private void hideSearchBox()
    {
        if(!isHide)
        {
            searchBoxAnimator = ObjectAnimator.ofFloat(searchBox, "alpha", 0);
            searchBoxAnimator.setDuration(200);
            searchBoxAnimator.setInterpolator(new AccelerateInterpolator());
            searchBoxAnimator.addListener(new AnimatorListenerAdapter()
            {
                boolean isCancel;

                @Override
                public void onAnimationCancel(Animator animation)
                {
                    isCancel = true;
                }

                @Override
                public void onAnimationEnd(Animator animation)
                {
                    if(isCancel)
                        isCancel = false;
                    else
                        searchBox.setVisibility(View.GONE);
                }
            });
            searchBoxAnimator.start();
            isHide = true;
        }
    }

    private void showSearchBox()
    {
        if(isHide)
        {
            searchBox.setVisibility(View.VISIBLE);
            if(searchBoxAnimator.isRunning())
                searchBoxAnimator.cancel();
            searchBoxAnimator = ObjectAnimator.ofFloat(searchBox, "alpha", 1);
            searchBoxAnimator.setDuration(200);
            searchBoxAnimator.setInterpolator(new DecelerateInterpolator());
            searchBoxAnimator.start();
            isHide = false;
        }
    }

    private void onViewClick(int id, int position)
    {
        SearchModel.SearchBaseModel searchModel = searchResult.get(position);
        if(searchModel.getSearchMode() == 0)
        {
            Intent intent = new Intent(ctx, BangumiActivity.class);
            intent.putExtra("season_id", ((SearchModel.SearchBangumiModel) searchModel).search_bangumi_season_id);
            startActivity(intent);
        }
        else if(searchModel.getSearchMode() == 1)
        {
            Intent intent = new Intent(ctx, UserActivity.class);
            intent.putExtra("mid", ((SearchModel.SearchUserModel) searchModel).search_user_mid);
            startActivity(intent);
        }
        else if(searchModel.getSearchMode() == 2)
            startActivity(VideoActivity.getActivityIntent(ctx, ((SearchModel.SearchVideoModel) searchModel).search_video_aid, ""));
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

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        if(context instanceof TitleView.TitleViewListener)
            titleViewListener = (TitleView.TitleViewListener) context;
    }
}
