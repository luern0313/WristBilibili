package cn.luern0313.wristbilibili.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.ui.AboutActivity;
import cn.luern0313.wristbilibili.ui.FollowmeActivity;
import cn.luern0313.wristbilibili.ui.JoinqqActivity;
import cn.luern0313.wristbilibili.ui.LogsoffActivity;
import cn.luern0313.wristbilibili.ui.OpensourceActivity;
import cn.luern0313.wristbilibili.ui.SueActivity;
import cn.luern0313.wristbilibili.ui.SupportActivity;

/**
 * Created by liupe on 2018/11/15.
 * 说是设置。。好像没有真的可以设置的东西。。
 */

public class Setting extends Fragment
{
    Context ctx;

    View rootLayout;
    ListView setList;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = this.getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_setting, container, false);

        setList = rootLayout.findViewById(R.id.set_listview);

        final ArrayList<String> list = new ArrayList<String>(Arrays.asList("注销", "关注作者b站账号", "发动态炫耀一下", "支持作者", "关于开源", "联系作者", "关于"));
        mAdapter mAdapter = new mAdapter(inflater, list);
        setList.setAdapter(mAdapter);

        setList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if(list.get(position).equals("注销"))
                {
                    Intent intent = new Intent(ctx, LogsoffActivity.class);
                    startActivity(intent);
                }
                else if(list.get(position).equals("关注作者b站账号"))
                {
                    Intent intent = new Intent(ctx, FollowmeActivity.class);
                    startActivity(intent);
                }
                else if(list.get(position).equals("发动态炫耀一下"))
                {
                    Intent intent = new Intent(ctx, SueActivity.class);
                    startActivity(intent);
                }
                else if(list.get(position).equals("支持作者"))
                {
                    Intent intent = new Intent(ctx, SupportActivity.class);
                    startActivity(intent);
                }
                else if(list.get(position).equals("关于开源"))
                {
                    Intent intent = new Intent(ctx, OpensourceActivity.class);
                    startActivity(intent);
                }
                else if(list.get(position).equals("联系作者"))
                {
                    Intent intent = new Intent(ctx, JoinqqActivity.class);
                    startActivity(intent);
                }
                else if(list.get(position).equals("关于"))
                {
                    Intent intent = new Intent(ctx, AboutActivity.class);
                    startActivity(intent);
                }
            }
        });

        return rootLayout;
    }

    class mAdapter extends BaseAdapter
    {
        private LayoutInflater mInflater;
        private ArrayList<String> setList;
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
