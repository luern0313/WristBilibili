package cn.luern0313.wristbilibili.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.R;
import me.panpf.sketch.SketchImageView;

public class ImgActivity extends Activity
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
                    ImageTask it = new ImageTask();
                    it.execute(url);
                    return null;
                }
            }

            class ImageTask extends AsyncTask<String, Void, BitmapDrawable>
            {
                private String imageUrl;

                @Override
                protected BitmapDrawable doInBackground(String... params)
                {
                    try
                    {
                        imageUrl = params[0];
                        Bitmap bitmap = null;
                        bitmap = downloadImage();
                        BitmapDrawable db = new BitmapDrawable(viewPager.getResources(), bitmap);
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

                /**
                 * 获得需要压缩的比率
                 *
                 * @param options 需要传入已经BitmapFactory.decodeStream(is, null, options);
                 * @return 返回压缩的比率，最小为1
                 */
                public int getInSampleSize(BitmapFactory.Options options) {
                    int inSampleSize = 1;
                    int realWith = 800;
                    int realHeight = 800;

                    int outWidth = options.outWidth;
                    int outHeight = options.outHeight;

                    //获取比率最大的那个
                    if (outWidth > realWith || outHeight > realHeight) {
                        int withRadio = Math.round(outWidth / realWith);
                        int heightRadio = Math.round(outHeight / realHeight);
                        inSampleSize = withRadio > heightRadio ? withRadio : heightRadio;
                    }
                    return inSampleSize;
                }

                /**
                 * 根据输入流返回一个压缩的图片
                 * @param input 图片的输入流
                 * @return 压缩的图片
                 */
                public Bitmap getCompressBitmap(InputStream input)
                {
                    //因为InputStream要使用两次，但是使用一次就无效了，所以需要复制两个
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try
                    {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = input.read(buffer)) > -1)
                        {
                            baos.write(buffer, 0, len);
                        }
                        baos.flush();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                    //复制新的输入流
                    InputStream is = new ByteArrayInputStream(baos.toByteArray());
                    InputStream is2 = new ByteArrayInputStream(baos.toByteArray());

                    //只是获取网络图片的大小，并没有真正获取图片
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(is, null, options);
                    //获取图片并进行压缩
                    options.inSampleSize = getInSampleSize(options);
                    options.inJustDecodeBounds = false;
                    return BitmapFactory.decodeStream(is2, null, options);
                }

                /**
                 * 根据url从网络上下载图片
                 *
                 * @return 图片
                 */
                private Bitmap downloadImage() throws IOException
                {
                    HttpURLConnection con = null;
                    Bitmap bitmap = null;
                    URL url = new URL(imageUrl);
                    con = (HttpURLConnection) url.openConnection();
                    con.setConnectTimeout(5 * 1000);
                    con.setReadTimeout(10 * 1000);
                    bitmap = getCompressBitmap(con.getInputStream());
                    if(con != null)
                    {
                        con.disconnect();
                    }
                    return bitmap;
                }
            }
        };

        viewPager.setAdapter(pagerAdapter);
    }
}
