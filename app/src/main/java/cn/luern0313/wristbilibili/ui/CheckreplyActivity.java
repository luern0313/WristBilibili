package cn.luern0313.wristbilibili.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.fragment.ReplyFragment;
import cn.luern0313.wristbilibili.models.ReplyModel;
import cn.luern0313.wristbilibili.widget.TitleView;

public class CheckreplyActivity extends BaseActivity implements TitleView.TitleViewListener
{
    Context ctx;
    LayoutInflater inflater;
    Intent intent;
    ReplyModel root;
    int position;

    TitleView titleView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkreply);
        ctx = this;
        inflater = getLayoutInflater();
        intent = getIntent();
        root = intent.hasExtra("root") ? (ReplyModel) intent.getSerializableExtra("root") : null;
        position = intent.getIntExtra("position", -1);

        titleView = findViewById(R.id.cr_title);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.cr_frame, ReplyFragment.newInstance(intent.getStringExtra("oid"), intent.getStringExtra("type"), root, position));
        transaction.commit();
    }

    @Override
    public boolean hideTitle()
    {
        return titleView.hide();
    }

    @Override
    public boolean showTitle()
    {
        return titleView.show();
    }
}
