package cn.luern0313.wristbilibili.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.SettingAdapter;
import cn.luern0313.wristbilibili.api.SettingApi;
import cn.luern0313.wristbilibili.models.SettingModel;
import cn.luern0313.wristbilibili.ui.BaseActivity;
import cn.luern0313.wristbilibili.ui.SettingActivity;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;
import cn.luern0313.wristbilibili.util.ViewTouchListener;
import cn.luern0313.wristbilibili.widget.TitleView;

/**
 * Created by liupe on 2018/11/15.
 * 说是设置。。好像没有真的可以设置的东西。。
 * 有了主题
 * -- by fifth_light
 */

public class SettingFragment extends Fragment
{
    Context ctx;

    View rootLayout;
    private ListView uiListView;
    private TitleView.TitleViewListener titleViewListener;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = this.getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_setting, container, false);

        uiListView = rootLayout.findViewById(R.id.set_listview);

        final ArrayList<String> list = new ArrayList<String>(Arrays.asList("注销", "关注作者b站账号", "主题设置", "小尾巴设置", "发动态炫耀一下", "查看介绍视频", "支持作者", "关于开源", "更新日志", "联系作者", "关于"));
        mAdapter mAdapter = new mAdapter(inflater, list);
        uiListView.setAdapter(mAdapter);

        uiListView.setOnItemClickListener((parent, view, position, id) -> {
            switch (list.get(position))
            {
                case "注销":
                {
                    Intent intent = new Intent(ctx, LogsoffActivity.class);
                    startActivity(intent);
                    break;
                }
                case "关注作者b站账号":
                {
                    Intent intent = new Intent(ctx, FollowmeActivity.class);
                    startActivity(intent);
                    break;
                }
                case "主题设置":
                {
                    Intent intent = new Intent(ctx, ThemeActivity.class);
                    startActivity(intent);
                    break;
                }
                case "小尾巴设置":
                {
                    Intent intent = new Intent(ctx, TailActivity.class);
                    startActivity(intent);
                    break;
                }
                case "发动态炫耀一下":
                {
                    Intent intent = new Intent(ctx, SueActivity.class);
                    startActivity(intent);
                    break;
                }
                case "查看介绍视频":
                    startActivity(VideoActivity.getActivityIntent(ctx, "37132444", ""));
                    break;
                case "支持作者":
                {
                    Intent intent = new Intent(ctx, SupportActivity.class);
                    startActivity(intent);
                    break;
                }
                case "关于开源":
                {
                    Intent intent = new Intent(ctx, OpensourceActivity.class);
                    startActivity(intent);
                    break;
                }
                case "更新日志":
                {
                    Intent intent = new Intent(ctx, TextActivity.class);
                    intent.putExtra("title", "更新日志");
                    intent.putExtra("text", getString(R.string.update));
                    startActivity(intent);
                    break;
                }
                case "联系作者":
                {
                    Intent intent = new Intent(ctx, JoinqqActivity.class);
                    startActivity(intent);
                    break;
                }
                case "关于":
                {
                    Intent intent = new Intent(ctx, AboutActivity.class);
                    startActivity(intent);
                    break;
                }
                default:
                    throw new AssertionError();
            }
        });

        uiListView.setOnTouchListener(new ListViewTouchListener(uiListView, titleViewListener));

        return rootLayout;
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        if(context instanceof TitleView.TitleViewListener)
            titleViewListener = (TitleView.TitleViewListener) context;
    }

    private static class mAdapter extends BaseAdapter
    {
        private final LayoutInflater mInflater;
        private final ArrayList<String> setList;
        TextView text;

        public mAdapter(LayoutInflater inflater, ArrayList<String> setList)
        {
            mInflater = inflater;
            this.setList = setList;
        }

        @Override
        public int getCount()
        {
            return setList.size();
        }

        @Override
        public Object getItem(int position)
        {
            return position;
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup)
        {
            convertView = mInflater.inflate(R.layout.item_setting_item, null);
            text = convertView.findViewById(R.id.si_text);

            text.setText(setList.get(position));
            return convertView;
        }

    }
}
