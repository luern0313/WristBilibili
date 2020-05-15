package cn.luern0313.wristbilibili.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.fragment.ReplyFragment;
import cn.luern0313.wristbilibili.models.ReplyModel;

public class CheckreplyActivity extends AppCompatActivity
{
    Context ctx;
    LayoutInflater inflater;
    Intent intent;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ReplyModel root;
    int position;

    private FragmentManager fm;
    private FragmentTransaction transaction;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkreply);
        ctx = this;
        inflater = getLayoutInflater();
        intent = getIntent();
        sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        root = intent.hasExtra("root") ? (ReplyModel) intent.getSerializableExtra("root") : null;
        position = intent.getIntExtra("position", -1);

        fm = getSupportFragmentManager();
        transaction = fm.beginTransaction();
        transaction.replace(R.id.cr_frame, ReplyFragment.newInstance(intent.getStringExtra("oid"), intent.getStringExtra("type"), root, position));
        transaction.commit();
    }
}
