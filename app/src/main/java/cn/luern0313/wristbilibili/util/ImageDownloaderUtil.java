package cn.luern0313.wristbilibili.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 被 luern0313 创建于 2019/8/31.
 */

public class ImageDownloaderUtil
{
    /**
     * 根据url从网络上下载图片
     *
     * @return 图片
     */
    public static Bitmap downloadImage(String imageUrl) throws IOException
    {
        return downloadImage(imageUrl, 170);
    }

    public static Bitmap downloadImage(String imageUrl, int width) throws IOException
    {
        URL url = new URL(imageUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setConnectTimeout(4 * 1000);
        con.setReadTimeout(4 * 1000);
        Bitmap bitmap = getCompressBitmap(con.getInputStream(), width);
        con.disconnect();
        if(LruCacheUtil.getLruCache().get(imageUrl) == null && bitmap != null)
        {
            LruCacheUtil.getLruCache().put(imageUrl, new BitmapDrawable(MyApplication.getContext().getResources(), bitmap));
        }
        return bitmap;
    }

    /**
     * 获得需要压缩的比率
     *
     * @param options 需要传入已经BitmapFactory.decodeStream(is, null, options);
     * @return 返回压缩的比率，最小为1
     */
    public static int getInSampleSize(BitmapFactory.Options options, int width) {
        int inSampleSize = 1;
        float realHeight = options.outHeight * 1.0f / options.outWidth * width;

        int outWidth = options.outWidth;
        int outHeight = options.outHeight;

        //获取比率最大的那个
        if (outWidth > width || outHeight > realHeight) {
            int withRadio = Math.round((float) outWidth / width);
            int heightRadio = Math.round((float)outHeight / realHeight);
            inSampleSize = Math.max(withRadio, heightRadio);
        }
        return inSampleSize;
    }

    /**
     * 根据输入流返回一个压缩的图片
     * @param input 图片的输入流
     * @return 压缩的图片
     */
    public static Bitmap getCompressBitmap(InputStream input, int width)
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
        options.inSampleSize = getInSampleSize(options, width);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(is2, null, options);
    }
}
