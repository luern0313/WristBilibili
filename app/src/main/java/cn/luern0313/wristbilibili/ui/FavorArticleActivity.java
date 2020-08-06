package cn.luern0313.wristbilibili.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.fragment.FavorArticleFragment;

public class FavorArticleActivity extends AppCompatActivity
{
    Context ctx;
    LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favor_article);

        ctx = this;
        inflater = getLayoutInflater();

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.favor_article_frame, FavorArticleFragment.newInstance());
        transaction.commit();
    }
}