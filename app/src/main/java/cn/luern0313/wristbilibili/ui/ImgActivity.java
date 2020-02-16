package cn.luern0313.wristbilibili.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.ArrayList;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.util.ImageDownloaderUtil;
import me.panpf.sketch.SketchImageView;

public class ImgActivity extends AppCompatActivity
{
    Context ctx;
    Intent intent;

    String[] imgUrlList;
    ArrayList<View> viewList;

    ViewPager viewPager;

    LruCache<String, BitmapDrawable> mImageCache;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img);

        ctx = this;
        intent = getIntent();
        imgUrlList = intent.getStringArrayExtra("imgUrl");

        LayoutInflater inflater = getLayoutInflater();

        viewPager = findViewById(R.id.img_viewpager);

        viewList = new ArrayList<>();
        for(int i = 0; i < imgUrlList.length; i++)
        {
            View view = inflater.inflate(R.layout.viewpager_img_img, null);
            view.setTag(imgUrlList[i]);
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

        PagerAdapter pagerAdapter = new PagerAdapter()
        {
            @Override
            public int getCount()
            {
                return viewList.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object)
            {
                return view.getTag().equals(object);
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object)
            {
                container.removeView(container.findViewWithTag(object));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position)
            {
                View v = viewList.get(position);
                ((SketchImageView) v.findViewById(R.id.vp_imageView).findViewById(R.id.vp_imageView)).setZoomEnabled(true);
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
                    ImageTask it = new ImageTask(viewPager);
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
                        bitmap = ImageDownloaderUtil.downloadImage(imageUrl);
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
                        View iv = viewPager.findViewWithTag(imageUrl);
                        if(iv != null && result != null)
                        {
                            ((SketchImageView) iv.findViewById(R.id.vp_imageView)).setImageDrawable(result);
                        }
                    }
                    catch (NullPointerException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        };

        viewPager.setAdapter(pagerAdapter);
    }
}
