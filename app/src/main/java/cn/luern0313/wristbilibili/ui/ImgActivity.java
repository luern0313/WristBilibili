package cn.luern0313.wristbilibili.ui;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.util.ImageTaskUtil;
import cn.luern0313.wristbilibili.util.LruCacheUtil;
import me.panpf.sketch.SketchImageView;

public class ImgActivity extends BaseActivity implements View.OnClickListener
{
    private Context ctx;

    private String[] imgUrlList;
    private int imgPosition;
    private ArrayList<View> viewList;

    private RelativeLayout uiTab;
    private ViewPager uiViewPager;
    private ViewFlipper uiViewFlipper;

    private ImageTaskUtil.ImageTaskDownloadedListener imageTaskDownloadedListener;

    private final Handler handler = new Handler();
    private Runnable runnableTimer;

    private boolean isShowTab = true;
    private float screenWidth, screenHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img);

        ctx = this;
        Intent intent = getIntent();
        imgUrlList = intent.getStringArrayExtra("imgUrl");
        imgPosition = intent.getIntExtra("position", 0);
        LayoutInflater inflater = getLayoutInflater();

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

        imageTaskDownloadedListener = this::onDownloaded;

        uiTab = findViewById(R.id.img_tab);
        uiViewPager = findViewById(R.id.img_viewpager);
        uiViewFlipper = findViewById(R.id.img_viewflipper);
        TextView uiImgCount = findViewById(R.id.img_imgcount);

        for(int i = 1; i <= imgUrlList.length; i++)
            uiViewFlipper.addView(getTitleTextView(String.valueOf(i)));
        uiImgCount.setText(String.format(getString(R.string.img_title), imgUrlList.length));

        viewList = new ArrayList<>();
        for(int i = 0; i < imgUrlList.length; i++)
        {
            imgUrlList[i] = LruCacheUtil.getImageUrl(imgUrlList[i], 500);
            View view = inflater.inflate(R.layout.viewpager_img_img, null);
            view.setTag(imgUrlList[i]);
            viewList.add(view);
        }

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

        findViewById(R.id.img_tab_exit).setOnClickListener(this);
        findViewById(R.id.img_tab_download).setOnClickListener(this);

        runnableTimer = () -> showTab(true);

        handler.postDelayed(runnableTimer, 3000);

        uiViewPager.setAdapter(pagerAdapter);
        uiViewPager.setCurrentItem(imgPosition, true);
    }

    private void showTab(boolean show)
    {
        handler.removeCallbacks(runnableTimer);
        handler.postDelayed(runnableTimer, 3000);
        if(show && isShowTab)
        {
            ObjectAnimator anim = ObjectAnimator.ofFloat(findViewById(R.id.img_tab), "translationY", -uiTab.getMeasuredHeight());
            anim.setDuration(250);
            anim.start();
            isShowTab = false;
        }
        else if(!show && !isShowTab)
        {
            ObjectAnimator anim = ObjectAnimator.ofFloat(findViewById(R.id.img_tab), "translationY", 0);
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

    private void onDownloaded(String url, BitmapDrawable bitmapDrawable)
    {
        SketchImageView imageView = null;
        LinearLayout linearLayout = uiViewPager.findViewWithTag(url);
        if(linearLayout != null)
            imageView = linearLayout.findViewById(R.id.vp_imageView);
        if(imageView != null && bitmapDrawable != null)
        {
            Bitmap bitmap = bitmapDrawable.getBitmap();
            setPadding(bitmap.getWidth(), bitmap.getHeight(), imageView);
            imageView.setImageBitmap(bitmap);
        }
    }

    private void setPadding(float width, float height, SketchImageView imageView)
    {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M || ctx.getResources().getConfiguration().isScreenRound())
        {
            if((width / height) > (screenWidth / screenHeight))
            {
                float vw = screenWidth;
                float vh = screenWidth * height / width;
                double l = Math.pow(Math.pow(vw, 2) + Math.pow(vh, 2), 0.5) / 2;
                double s = l - screenWidth / 2;
                int wp = (int) (s / l * vw / 2);
                int hp = (int) (s / l * vh / 2);
                int margin = Math.round((screenHeight - vh) / 2);
                imageView.setPadding(wp, margin + hp, wp, margin + hp);
            }
            else
            {
                float vw = screenHeight * width / height;
                float vh = screenHeight;
                double l = Math.pow(Math.pow(vw, 2) + Math.pow(vh, 2), 0.5) / 2;
                double s = l - screenWidth / 2;
                int wp = (int) (s / l * vw / 2);
                int hp = (int) (s / l * vh / 2);
                int margin = Math.round((screenWidth - screenHeight / height * width) / 2);
                imageView.setPadding(margin + wp, hp, margin + wp, hp);
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void onClick(View v)
    {
        handler.removeCallbacks(runnableTimer);
        handler.postDelayed(runnableTimer, 3000);
        if(v.getId() == R.id.img_tab_exit)
            finish();
        else if(v.getId() == R.id.img_tab_download)
        {
            BitmapDrawable bitmapDrawable = LruCacheUtil.getLruCache().get(imgUrlList[uiViewPager.getCurrentItem()]);
            if(bitmapDrawable != null)
            {
                try
                {
                    String path = getExternalFilesDir(null) + "/image/";
                    new File(path).mkdirs();
                    File imgFile = new File(path + getImageName(imgUrlList[uiViewPager.getCurrentItem()]) + ".png");
                    imgFile.createNewFile();

                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(imgFile));
                    bitmapDrawable.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, bos);
                    bos.flush();
                    bos.close();
                    Toast.makeText(ctx, String.format(getString(R.string.img_tab_download_done), path), Toast.LENGTH_LONG).show();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Toast.makeText(ctx, getString(R.string.img_tab_download_error), Toast.LENGTH_SHORT).show();
                }
            }
            else
                Toast.makeText(ctx, getString(R.string.img_tab_download_loading), Toast.LENGTH_SHORT).show();
        }
    }

    public static String getImageName(String url)
    {
        String pattern = "/([^/@]+)\\.[\\w]+(@.+)?$";

        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(url);
        if(m.find())
            return m.toMatchResult().group(1);
        return url;
    }

    private class ImgPagerAdapter extends PagerAdapter
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
            //setScrollBarAlpha(v.findViewById(R.id.vp_imageView).findViewById(R.id.vp_imageView));
            v.findViewById(R.id.vp_imageView).setOnClickListener(v1 -> showTab(isShowTab));

            BitmapDrawable b = setImageFormWeb((String) v.getTag());
            if(b != null)
            {
                ((SketchImageView) v.findViewById(R.id.vp_imageView).findViewById(R.id.vp_imageView)).setImageDrawable(b);
                setPadding(b.getBitmap().getWidth(), b.getBitmap().getHeight(), v.findViewById(R.id.vp_imageView).findViewById(R.id.vp_imageView));
            }
            container.addView(v);
            return v.getTag();
        }

        /*private void setScrollBarAlpha(SketchImageView sketchImageView)
        {
            ImageZoomer imageZoomer = sketchImageView.getZoomer();
            if(imageZoomer == null) return;

            try
            {
                Field scrollBarHelperField = imageZoomer.getClass().getDeclaredField("scrollBarHelper");
                scrollBarHelperField.setAccessible(true);
                Object scrollBarHelper = scrollBarHelperField.get(imageZoomer);
                Field scrollBarPaintField = scrollBarHelper.getClass().getDeclaredField("scrollBarPaint");
                scrollBarPaintField.setAccessible(true);
                Paint scrollBarPaint = (Paint) scrollBarPaintField.get(scrollBarHelper);
                scrollBarPaint.setAlpha(0);
                scrollBarPaintField.set(scrollBarHelper, scrollBarPaint);
                scrollBarHelperField.set(imageZoomer, scrollBarHelper);

                Field functionsField = sketchImageView.getClass().getSuperclass().getSuperclass().getDeclaredField("functions");
                functionsField.setAccessible(true);
                Object viewFunctions = functionsField.get(sketchImageView);
                Field zoomFunctionField = viewFunctions.getClass().getDeclaredField("zoomFunction");
                zoomFunctionField.setAccessible(true);
                ImageZoomFunction zoomFunction = (ImageZoomFunction) zoomFunctionField.get(viewFunctions);
                Field zoomerField = zoomFunction.getClass().getDeclaredField("zoomer");
                zoomerField.setAccessible(true);
                zoomerField.set(zoomFunction, imageZoomer);
                zoomFunctionField.set(viewFunctions, zoomFunction);
                functionsField.set(sketchImageView, viewFunctions);
            }
            catch (NoSuchFieldException | IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }*/

        private BitmapDrawable setImageFormWeb(String url)
        {
            if(url != null && LruCacheUtil.getLruCache().get(url) != null)
                return LruCacheUtil.getLruCache().get(url);
            else
            {
                ImageTaskUtil it = new ImageTaskUtil(uiViewPager, 500, imageTaskDownloadedListener);
                it.execute(url);
                return null;
            }
        }
    }
}
