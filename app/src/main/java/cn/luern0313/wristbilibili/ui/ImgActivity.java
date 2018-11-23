package cn.luern0313.wristbilibili.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import cn.luern0313.wristbilibili.R;

public class ImgActivity extends Activity
{
    Context ctx;
    Intent intent;

    String[] imgUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img);

        ctx = this;
        intent = getIntent();
        imgUrl = intent.getStringArrayExtra("imgUrl");

        PagerAdapter pagerAdapter = new PagerAdapter()
        {
            @Override
            public int getCount()
            {
                return imgUrl.length;
            }

            @Override
            public boolean isViewFromObject(View view, Object object)
            {
                return false;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object)
            {
                container.removeView((View) object);
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position)
            {
                return super.instantiateItem(container, position);
            }

        };
    }
}
