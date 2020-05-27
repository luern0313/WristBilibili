package cn.luern0313.wristbilibili.ui;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.LruCache;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.ImageDownloaderUtil;
import me.panpf.sketch.SketchImageView;

public class ImgActivity extends AppCompatActivity
{
    Context ctx;
    Intent intent;

    String[] imgUrlList;
    int imgPosition;
    ArrayList<View> viewList;

    LinearLayout uiTab;
    ViewPager uiViewPager;
    ViewFlipper uiViewFlipper;
    TextView uiImgCount;

    LruCache<String, BitmapDrawable> mImageCache;
    Handler handler = new Handler();
    Runnable runnableTimer;

    boolean isShowTab = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img);

        ctx = this;
        intent = getIntent();
        imgUrlList = intent.getStringArrayExtra("imgUrl");
        imgPosition = intent.getIntExtra("position", 0);

        LayoutInflater inflater = getLayoutInflater();

        uiTab = findViewById(R.id.img_tab);
        uiViewPager = findViewById(R.id.img_viewpager);
        uiViewFlipper = findViewById(R.id.img_viewflipper);
        uiImgCount = findViewById(R.id.img_imgcount);

        for(int i = 1; i <= imgUrlList.length; i++)
            uiViewFlipper.addView(getTitleTextView(String.valueOf(i)));
        uiImgCount.setText(" / " + imgUrlList.length);

        viewList = new ArrayList<>();
        for(String s : imgUrlList)
        {
            View view = inflater.inflate(R.layout.viewpager_img_img, null);
            view.setTag(s);
            viewList.add(view);
        }

        int maxCache = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxCache / 8;
        mImageCache = new LruCache<String, BitmapDrawable>(cacheSize)
        {
            @Override
            protected int sizeOf(String key, BitmapDrawable value)
            {
                try
                {
                    return value.getBitmap().getByteCount();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                return 0;
            }
        };

        PagerAdapter pagerAdapter = new ImgPagerAdapter();

        uiViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {
            }

            @Override
            public void onPageSelected(int position)
            {
                showTab(false);
                while(uiViewFlipper.getDisplayedChild() != position)
                {
                    if(uiViewFlipper.getDisplayedChild() < position)
                    {
                        uiViewFlipper.setInAnimation(ctx, R.anim.slide_in_right);
                        uiViewFlipper.setOutAnimation(ctx, R.anim.slide_out_left);
                        uiViewFlipper.showNext();
                    }
                    else
                    {
                        uiViewFlipper.setInAnimation(ctx, android.R.anim.slide_in_left);
                        uiViewFlipper.setOutAnimation(ctx, android.R.anim.slide_out_right);
                        uiViewFlipper.showPrevious();
                    }
                }
            }
        });

        runnableTimer = new Runnable()
        {
            @Override
            public void run()
            {
                showTab(true);
            }
        };

        handler.postDelayed(runnableTimer, 5000);

        uiViewPager.setAdapter(pagerAdapter);
        uiViewPager.setCurrentItem(imgPosition, true);
    }

    private void showTab(boolean show)
    {
        handler.removeCallbacks(runnableTimer);
        handler.postDelayed(runnableTimer, 5000);
        if(show && isShowTab)
        {
            ObjectAnimator anim = ObjectAnimator.ofFloat(findViewById(R.id.img_tab), "translationY", 0, -DataProcessUtil.dip2px(30));
            anim.setDuration(250);
            anim.start();
            isShowTab = false;
        }
        else if(!show && !isShowTab)
        {
            ObjectAnimator anim = ObjectAnimator.ofFloat(findViewById(R.id.img_tab), "translationY", -DataProcessUtil.dip2px(30), 0);
            anim.setDuration(250);
            anim.start();
            isShowTab = true;
        }
    }

    private TextView getTitleTextView(String title)
    {
        TextView t = new TextView(ctx);
        t.setText(title);
        t.setTextColor(getResources().getColor(R.color.white));
        t.setGravity(Gravity.END);
        return t;
    }

    class ImgPagerAdapter extends PagerAdapter
    {
        @Override
        public int getCount()
        {
            return viewList.size();
        }

        @Override
        public boolean isViewFromObject(View view, @NonNull Object object)
        {
            return view.getTag().equals(object);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, @NonNull Object object)
        {
            container.removeView(container.findViewWithTag(object));
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position)
        {
            View v = viewList.get(position);
            ((SketchImageView) v.findViewById(R.id.vp_imageView).findViewById(R.id.vp_imageView)).setZoomEnabled(true);
            v.findViewById(R.id.vp_imageView).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    showTab(isShowTab);
                }
            });
            BitmapDrawable b = setImageFormWeb((String) v.getTag());
            if(b != null)
                ((SketchImageView) v.findViewById(R.id.vp_imageView).findViewById(R.id.vp_imageView)).setImageDrawable(b);
            container.addView(v);
            return v.getTag();
        }

        BitmapDrawable setImageFormWeb(String url)
        {
            if(mImageCache.get(url) != null)
            {
                return mImageCache.get(url);
            }
            else
            {
                ImageTask it = new ImageTask(uiViewPager);
                it.execute(url);
                return null;
            }
        }

        class ImageTask extends AsyncTask<String, Void, BitmapDrawable>
        {
            private String imageUrl;
            private Resources viewpagerResources;

            ImageTask(ViewPager viewPager)
            {
                this.viewpagerResources = viewPager.getResources();
            }

            @Override
            protected BitmapDrawable doInBackground(String... params)
            {
                try
                {
                    imageUrl = params[0];
                    Bitmap bitmap = null;
                    bitmap = ImageDownloaderUtil.downloadImage(imageUrl, 500);
                    BitmapDrawable db = new BitmapDrawable(viewpagerResources, bitmap);
                    // 如果本地还没缓存该图片，就缓存
                    if(mImageCache.get(imageUrl) == null && bitmap != null)
                    {
                        mImageCache.put(imageUrl, db);
                    }
                    return db;
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(BitmapDrawable result)
            {
                try
                {
                    // 通过Tag找到我们需要的ImageView，如果该ImageView所在的item已被移出页面，就会直接返回null
                    View iv = uiViewPager.findViewWithTag(imageUrl);
                    if(iv != null && result != null)
                    {
                        ((SketchImageView) iv.findViewById(R.id.vp_imageView)).setImageDrawable(
                                result);
                    }
                }
                catch (NullPointerException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
