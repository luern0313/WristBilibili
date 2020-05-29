package cn.luern0313.wristbilibili.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import cn.luern0313.wristbilibili.R;

public class SelectPartActivity extends AppCompatActivity
{
    Context ctx;
    Intent inIntent;
    LayoutInflater inflater;

    String title;
    String tip;
    String[] options;
    String[] optionsId;

    ListView spListView;
    View selectPartTipView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_part);
        ctx = this;
        inIntent = getIntent();
        inflater = getLayoutInflater();

        setResult(-1, inIntent);

        title = inIntent.hasExtra("title") ? inIntent.getStringExtra("title") : "请选择";
        tip = inIntent.hasExtra("tip") ? inIntent.getStringExtra("tip") : "";
        options = inIntent.hasExtra("options_name") ? inIntent.getStringArrayExtra("options_name") : new String[0];
        optionsId = inIntent.hasExtra("options_id") ? inIntent.getStringArrayExtra("options_id") : new String[0];

        spListView = findViewById(R.id.sp_list);
        ((TextView) findViewById(R.id.sp_title_title)).setText(title);
        if(!"".equals(tip))
        {
            selectPartTipView = inflater.inflate(R.layout.widget_select_part_tip, null);
            ((TextView) selectPartTipView.findViewById(R.id.sp_tip_text)).setText(tip);
            spListView.addHeaderView(selectPartTipView, null, false);
            spListView.setHeaderDividersEnabled(false);
        }

        mAdapter mAdapter = new mAdapter(getLayoutInflater(), options);

        spListView.setAdapter(mAdapter);
    }

    class mAdapter extends BaseAdapter
    {
        private LayoutInflater mInflater;

        private String[] spList;

        public mAdapter(LayoutInflater inflater, String[] spList)
        {
            mInflater = inflater;
            this.spList = spList;
        }

        @Override
        public int getCount()
        {
            return spList.length;
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
        public View getView(final int position, View convertView, ViewGroup viewGroup)
        {
            ViewHolder viewHolder;
            if(convertView == null)
            {
                convertView = mInflater.inflate(R.layout.item_select_part, null);
                viewHolder = new ViewHolder();
                convertView.setTag(viewHolder);
                viewHolder.text = convertView.findViewById(R.id.sp_item_text);
            }
            else
            {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.text.setText(spList[position]);

            viewHolder.text.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if(optionsId.length != 0) inIntent.putExtra("option_id", optionsId[position]);
                    inIntent.putExtra("option_position", position);
                    inIntent.putExtra("option_name", options[position]);
                    setResult(0, inIntent);
                    finish();
                }
            });
            return convertView;
        }

        class ViewHolder
        {
            TextView text;
        }
    }
}
