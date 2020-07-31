package cn.luern0313.wristbilibili.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ViewFlipper;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.FavorBoxApi;
import cn.luern0313.wristbilibili.api.OnlineVideoApi;
import cn.luern0313.wristbilibili.api.VideoApi;
import cn.luern0313.wristbilibili.fragment.ReplyFragment;
import cn.luern0313.wristbilibili.fragment.VideoDetailFragment;
import cn.luern0313.wristbilibili.fragment.VideoRecommendFragment;
import cn.luern0313.wristbilibili.models.FavorBoxModel;
import cn.luern0313.wristbilibili.models.VideoModel;
import cn.luern0313.wristbilibili.service.DownloadService;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;

public class VideoActivity extends BaseActivity implements VideoDetailFragment.VideoDetailFragmentListener
{
    Context ctx;
    Intent intent;
    LayoutInflater inflater;

    VideoApi videoApi;
    VideoModel videoModel;
    OnlineVideoApi onlineVideoApi;
    FragmentPagerAdapter pagerAdapter;
    VideoDetailActivityListener videoDetailActivityListener;

    Handler handler = new Handler();
    Runnable runnableNoWeb, runnableUi, runnableNodata;

    AnimationDrawable loadingImgAnim;

    ViewFlipper uiTitle;
    ViewPager uiViewPager;
    View layoutSendReply, layoutLoading;
    ImageView uiLoadingImg;
    LinearLayout uiLoading;
    LinearLayout uiNoWeb;

    boolean isLogin = false;

    final private static String ARG_AID = "aid";
    final private static String ARG_BVID = "bvid";
    final private int RESULT_VD_FAVOR = 101;
    final private int RESULT_VD_DOWNLOAD = 102;
    final private int RESULT_VD_PART = 103;
    final private int RESULT_VD_SHARE = 104;

    private DownloadService.MyBinder myBinder;
    private VideoDownloadServiceConnection connection = new VideoDownloadServiceConnection();

    public static Intent getActivityIntent(Context ctx, String aid, String bvid)
    {
        Intent intent = new Intent(ctx, VideoActivity.class);
        intent.putExtra(ARG_AID, aid);
        intent.putExtra(ARG_BVID, bvid);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videodetails);

        ctx = this;
        intent = getIntent();

        Intent serviceIntent = new Intent(ctx, DownloadService.class);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);

        inflater = getLayoutInflater();
        layoutSendReply = inflater.inflate(R.layout.widget_reply_sendreply, null);
        layoutLoading = inflater.inflate(R.layout.widget_loading, null);

        uiTitle = findViewById(R.id.vd_title_title);
        uiViewPager = findViewById(R.id.vd_viewpager);
        uiViewPager.setOffscreenPageLimit(2);
        uiLoadingImg = findViewById(R.id.vd_loading_img);
        uiLoading = findViewById(R.id.vd_loading);
        uiNoWeb = findViewById(R.id.vd_noweb);

        isLogin = !SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, "").equals("");
        videoApi = new VideoApi(intent.getStringExtra(ARG_AID), intent.getStringExtra(ARG_BVID));

        uiLoadingImg.setImageResource(R.drawable.anim_loading);
        loadingImgAnim = (AnimationDrawable) uiLoadingImg.getDrawable();
        loadingImgAnim.start();
        uiLoading.setVisibility(View.VISIBLE);

        if(SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.tipVd, true)) findViewById(R.id.vd_tip).setVisibility(View.VISIBLE);

        runnableNoWeb = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    uiLoading.setVisibility(View.GONE);
                    uiNoWeb.setVisibility(View.VISIBLE);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableUi = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    uiLoading.setVisibility(View.GONE);
                    uiNoWeb.setVisibility(View.GONE);
                    uiViewPager.setAdapter(pagerAdapter);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableNodata = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    findViewById(R.id.vd_novideo).setVisibility(View.VISIBLE);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager())
        {
            @Override
            public int getCount()
            {
                return 3;
            }

            @NonNull
            @Override
            public Fragment getItem(int position)
            {
                if(position == 0)
                    return VideoDetailFragment.newInstance(videoModel);
                else if(position == 1)
                    return ReplyFragment.newInstance(videoModel.video_aid, "1", null, -1);
                else
                    return VideoRecommendFragment.newInstance(videoModel);
            }
        };

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
                while(uiTitle.getDisplayedChild() != position)
                {
                    if(uiTitle.getDisplayedChild() < position)
                    {
                        uiTitle.setInAnimation(ctx, R.anim.slide_in_right);
                        uiTitle.setOutAnimation(ctx, R.anim.slide_out_left);
                        uiTitle.showNext();
                    }
                    else
                    {
                        uiTitle.setInAnimation(ctx, android.R.anim.slide_in_left);
                        uiTitle.setOutAnimation(ctx, android.R.anim.slide_out_right);
                        uiTitle.showPrevious();
                    }
                }
            }
        });

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    videoModel = videoApi.getVideoDetails();
                    if(videoModel != null)
                    {
                        handler.post(runnableUi);
                    }
                    else
                    {
                        handler.post(runnableNodata);
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    handler.post(runnableNoWeb);
                }
            }
        }).start();
    }

    public void clickVdTip(View view)
    {
        findViewById(R.id.vd_tip).setVisibility(View.GONE);
        SharedPreferencesUtil.putBoolean(SharedPreferencesUtil.tipVd, false);
    }

    @Override
    public void onVideoDetailFragmentViewClick(int viewId)
    {
        if(viewId == R.id.vd_video_part_layout)
        {
            String[] videoPartNames = new String[videoModel.video_part_array_list.size()];
            String[] videoPartCids = new String[videoModel.video_part_array_list.size()];
            for(int i = 0; i < videoModel.video_part_array_list.size(); i++)
                videoPartNames[i] = videoModel.video_part_array_list.get(i).video_part_name;
            for(int i = 0; i < videoModel.video_part_array_list.size(); i++)
                videoPartCids[i] = String.valueOf(videoModel.video_part_array_list.get(i).video_part_cid);
            Intent intent = new Intent(ctx, SelectPartActivity.class);
            intent.putExtra("title", "分P");
            intent.putExtra("options_name", videoPartNames);
            intent.putExtra("options_id", videoPartCids);
            startActivityForResult(intent, RESULT_VD_PART);
        }
        else if(viewId == R.id.vd_bt_cover)
        {
            Intent intent = new Intent(ctx, ImgActivity.class);
            intent.putExtra("imgUrl", new String[]{videoModel.video_cover});
            startActivity(intent);
        }
        else if(viewId == R.id.vd_bt_play)
        {
            Intent intent = new Intent(ctx, PlayerActivity.class);
            intent.putExtra("title", videoModel.video_title);
            intent.putExtra("aid", videoModel.video_aid);
            intent.putExtra("cid", videoModel.video_cid);
            startActivity(intent);
        }
        else if(viewId == R.id.vd_bt_watchlater)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        String result = videoApi.playLater();
                        if(result.equals(""))
                        {
                            Looper.prepare();
                            Toast.makeText(ctx, "已添加至稍后再看", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                        else
                        {
                            Looper.prepare();
                            Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        Looper.prepare();
                        Toast.makeText(ctx, "未成功添加至稍后观看！请检查网络再试", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }
            }).start();
        }
        else if(viewId == R.id.vd_bt_download)
        {
            String[] videoPartNames = new String[videoModel.video_part_array_list.size()];
            String[] videoPartCids = new String[videoModel.video_part_array_list.size()];
            for (int i = 0; i < videoModel.video_part_array_list.size(); i++)
                videoPartNames[i] = videoModel.video_part_array_list.get(i).video_part_name;
            for (int i = 0; i < videoModel.video_part_array_list.size(); i++)
                videoPartCids[i] = String.valueOf(
                        videoModel.video_part_array_list.get(i).video_part_cid);
            Intent intent = new Intent(ctx, SelectPartActivity.class);
            intent.putExtra("title", "分P下载");
            intent.putExtra("tip", "选择要下载的分P");
            intent.putExtra("options_name", videoPartNames);
            intent.putExtra("options_id", videoPartCids);
            startActivityForResult(intent, RESULT_VD_DOWNLOAD);
        }
        else if(viewId == R.id.vd_bt_history)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        String result = videoApi.playHistory();
                        if(result.equals(""))
                        {
                            Looper.prepare();
                            Toast.makeText(ctx, "已添加至历史记录！你可以在历史记录找到", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                        else
                        {
                            Looper.prepare();
                            Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        Looper.prepare();
                        Toast.makeText(ctx, "未成功添加至历史记录！请检查网络再试", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }
            }).start();
        }
        else if(viewId == R.id.vd_bt_share)
        {
            Intent intent = new Intent(ctx, SendDynamicActivity.class);
            intent.putExtra("is_share", true);
            intent.putExtra("share_dyid", videoModel.video_aid);
            intent.putExtra("share_up", videoModel.video_up_name);
            intent.putExtra("share_img", videoModel.video_cover);
            intent.putExtra("share_title", videoModel.video_title);
            startActivityForResult(intent, RESULT_VD_SHARE);
        }
        else if(viewId == R.id.vd_like)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(videoModel.video_user_like)
                        {
                            String result = videoApi.likeVideo(2);
                            if(result.equals(""))
                            {
                                videoModel.video_detail_like--;
                                videoModel.video_user_like = false;
                                Looper.prepare();
                                Toast.makeText(ctx, "已取消喜欢...", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Looper.prepare();
                                Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
                            }
                        }
                        else
                        {
                            String result = videoApi.likeVideo(1);
                            if(result.equals(""))
                            {
                                videoModel.video_detail_like++;
                                videoModel.video_user_like = true;
                                videoModel.video_user_dislike = false;
                                Looper.prepare();
                                Toast.makeText(ctx, "已喜欢！这个视频会被更多人看到！", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Looper.prepare();
                                Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        Looper.prepare();
                        Toast.makeText(ctx, "喜欢失败...请检查你的网络..", Toast.LENGTH_SHORT).show();
                    }
                    finally
                    {
                        EventBus.getDefault().post(videoModel);
                        Looper.loop();
                    }
                }
            }).start();
        }
        else if(viewId == R.id.vd_coin)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(videoModel.video_detail_copyright == 1)  //1原创
                        {
                            if(videoModel.video_user_coin < 2)
                            {
                                String result = videoApi.coinVideo(1);
                                if(result.equals(""))
                                {
                                    videoModel.video_detail_coin++;
                                    videoModel.video_user_coin++;
                                    Looper.prepare();
                                    Toast.makeText(ctx, "你投了一个硬币！再次点击可以再次投币！", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    Looper.prepare();
                                    Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
                                }
                            }
                            else
                            {
                                Looper.prepare();
                                Toast.makeText(ctx, "最多投两个硬币...", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else  //2转载
                        {
                            if(videoModel.video_user_coin < 1)
                            {
                                String result = videoApi.coinVideo(1);
                                if(result.equals(""))
                                {
                                    videoModel.video_detail_coin++;
                                    videoModel.video_user_coin++;
                                    Looper.prepare();
                                    Toast.makeText(ctx, "你投了一个硬币！本稿件最多投一个硬币", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    Looper.prepare();
                                    Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
                                }
                            }
                            else
                            {
                                Looper.prepare();
                                Toast.makeText(ctx, "本稿件最多投一个硬币...", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        Looper.prepare();
                        Toast.makeText(ctx, "投币失败！请检查你的网络..", Toast.LENGTH_SHORT).show();
                    }
                    finally
                    {
                        EventBus.getDefault().post(videoModel);
                        Looper.loop();
                    }
                }
            }).start();
        }
        else if(viewId == R.id.vd_fav)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        FavorBoxApi favorBoxApi = new FavorBoxApi(SharedPreferencesUtil.getString(SharedPreferencesUtil.mid, ""));
                        ArrayList<FavorBoxModel> favorBoxArrayList = favorBoxApi.getFavorbox();
                        String[] favorBoxNames = new String[favorBoxArrayList.size()];
                        for (int i = 0; i < favorBoxArrayList.size(); i++)
                            favorBoxNames[i] = favorBoxArrayList.get(i).title;
                        String[] favorBoxIds = new String[favorBoxArrayList.size()];
                        for (int i = 0; i < favorBoxArrayList.size(); i++)
                            favorBoxIds[i] = favorBoxArrayList.get(i).id;
                        Intent intent = new Intent(ctx, SelectPartActivity.class);
                        intent.putExtra("title", "收藏");
                        intent.putExtra("tip", "选择收藏夹");
                        intent.putExtra("options_name", favorBoxNames);
                        intent.putExtra("options_id", favorBoxIds);
                        startActivityForResult(intent, RESULT_VD_FAVOR);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        Looper.prepare();
                        Toast.makeText(ctx, "收藏失败！请检查你的网络..", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }
            }).start();
        }
        else if(viewId == R.id.vd_dislike)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(videoModel.video_user_dislike)
                        {
                            String result = videoApi.likeVideo(4);
                            if(result.equals(""))
                            {
                                videoModel.video_user_dislike = false;
                                Looper.prepare();
                                Toast.makeText(ctx, "取消点踩成功！", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Looper.prepare();
                                Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
                            }
                        }
                        else
                        {
                            String result = videoApi.likeVideo(3);
                            if(result.equals(""))
                            {
                                videoModel.video_detail_like -= videoModel.video_user_like ? 1 : 0;
                                videoModel.video_user_dislike = true;
                                videoModel.video_user_like = false;
                                Looper.prepare();
                                Toast.makeText(ctx, "点踩成功！", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Looper.prepare();
                                Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        Looper.prepare();
                        Toast.makeText(ctx, "点踩失败！请检查你的网络..", Toast.LENGTH_SHORT).show();
                    }
                    finally
                    {
                        EventBus.getDefault().post(videoModel);
                        Looper.loop();
                    }
                }
            }).start();
        }
    }

    @Override
    public void onVideoDetailFragmentPartClick(int position)
    {
        VideoModel.VideoPartModel videoPartModel = videoModel.video_part_array_list.get(position);
        Intent intent = new Intent(ctx, PlayerActivity.class);
        intent.putExtra("title", videoPartModel.video_part_name);
        intent.putExtra("aid", videoModel.video_aid);
        intent.putExtra("cid", videoPartModel.video_part_cid);
        if(videoPartModel.video_part_cid.equals(videoModel.video_user_progress_cid))
            intent.putExtra("time", videoModel.video_user_progress_time);
        startActivity(intent);
        videoModel.video_user_progress_cid = videoPartModel.video_part_cid;
        videoModel.video_user_progress_position = position;
        videoModel.video_user_progress_time = 0;
        EventBus.getDefault().post(videoModel);
    }

    @Override
    public void onVideoDetailFragmentTriple()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    JSONObject result = videoApi.tripleVideo();
                    if(result.optInt("code") == 0)
                    {
                        JSONObject data = result.optJSONObject("data");
                        videoModel.video_user_like = data.optBoolean("like");
                        videoModel.video_user_coin += data.optInt("multiply");
                        videoModel.video_user_fav = data.optBoolean("fav");
                        Looper.prepare();
                        Toast.makeText(ctx, "三连成功！", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Looper.prepare();
                        Toast.makeText(ctx, "未知错误", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(ctx, "三连失败！请检查你的网络..", Toast.LENGTH_SHORT).show();
                }
                finally
                {
                    EventBus.getDefault().post(videoModel);
                    Looper.loop();
                }
            }
        }).start();
    }

    @Override
    public void onActivityResult(final int requestCode, int resultCode, final Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != 0) return;
        //videoDetailActivityListener.onVideoDetailActivityLoadingStart();
        switch (requestCode)
        {
            case RESULT_VD_FAVOR:
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            String result = videoApi.favVideo(data.getStringExtra("option_id"));
                            if(result.equals(""))
                            {
                                videoModel.video_detail_fav += videoModel.video_user_fav ? 0 : 1;
                                videoModel.video_user_fav = true;
                                Looper.prepare();
                                Toast.makeText(ctx, "已收藏至 " + data.getStringExtra("option_name") + " 收藏夹！", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Looper.prepare();
                                Toast.makeText(ctx, "错误：" + result, Toast.LENGTH_SHORT).show();
                            }
                            Looper.loop();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                        finally
                        {
                            EventBus.getDefault().post(videoModel);
                        }
                    }
                }).start();
                break;

            case RESULT_VD_DOWNLOAD:
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            onlineVideoApi = new OnlineVideoApi(videoModel.video_aid, data.getStringExtra("option_id"));
                            onlineVideoApi.connectionVideoUrl();
                            connection.downloadVideo(data.getStringExtra("option_name") + " - " + videoModel.video_title,
                                                     data.getStringExtra("option_id"));
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                            Looper.prepare();
                            Toast.makeText(ctx, "网络连接失败，请检查网络", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    }
                }).start();
                break;

            case RESULT_VD_PART:
                Intent intent = new Intent(ctx, PlayerActivity.class);
                intent.putExtra("title", data.getStringExtra("option_name") + " - " + videoModel.video_title);
                intent.putExtra("aid", videoModel.video_aid);
                intent.putExtra("cid", data.getStringExtra("option_id"));
                startActivity(intent);
                break;

            case RESULT_VD_SHARE:
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            String result = videoApi.shareVideo(data.getStringExtra("text"));
                            if(result.equals(""))
                            {
                                Looper.prepare();
                                Toast.makeText(ctx, "发送成功！", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                            else
                            {
                                Looper.prepare();
                                Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                            Looper.prepare();
                            Toast.makeText(ctx, "分享视频失败。。请检查网络？", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    }
                }).start();
                break;
        }
    }

    public void setVideoDetailActivityListener(VideoDetailActivityListener videoDetailActivityListener)
    {
        this.videoDetailActivityListener = videoDetailActivityListener;
    }

    class VideoDownloadServiceConnection implements ServiceConnection
    {
        @Override
        public void onServiceDisconnected(ComponentName name)
        {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            myBinder = (DownloadService.MyBinder) service;
        }

        void downloadVideo(String title, String cid)
        {
            String result = myBinder.startDownload(videoModel.video_aid, cid, title,
                                                   videoModel.video_cover, onlineVideoApi.getVideoUrl(),
                                                   onlineVideoApi.getDanmakuUrl());
            Looper.prepare();
            if(result.equals("")) Toast.makeText(ctx, "已添加至下载列表", Toast.LENGTH_SHORT).show();
            else Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
            Looper.loop();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        try
        {
            unbindService(connection);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public interface VideoDetailActivityListener
    {
        void onVideoDetailActivityLoadingStart();
        void onVideoDetailActivityLoadingFin();
    }
}
