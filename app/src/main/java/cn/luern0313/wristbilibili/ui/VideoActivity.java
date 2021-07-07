package cn.luern0313.wristbilibili.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.NonNull;
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
import cn.luern0313.wristbilibili.util.TitleViewPagerChangeListener;
import cn.luern0313.wristbilibili.widget.ExceptionHandlerView;
import cn.luern0313.wristbilibili.widget.TitleView;

public class VideoActivity extends BaseActivity implements VideoDetailFragment.VideoDetailFragmentListener, TitleView.TitleViewListener
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
    Runnable runnableUi;

    TitleView uiTitleView;
    ExceptionHandlerView uiExceptionHandlerView;
    ViewPager uiViewPager;
    View layoutSendReply, layoutLoading;

    boolean isLogin = false;

    public final static String ARG_AID = "aid";
    public final static String ARG_BVID = "bvid";
    private final int RESULT_VD_FAVOR = 101;
    private final int RESULT_VD_DOWNLOAD = 102;
    private final int RESULT_VD_PART = 103;
    private final int RESULT_VD_SHARE = 104;

    private DownloadService.MyBinder myBinder;
    private final VideoDownloadServiceConnection connection = new VideoDownloadServiceConnection();

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

        uiTitleView = findViewById(R.id.vd_title);
        uiExceptionHandlerView = findViewById(R.id.vd_exception);
        uiViewPager = findViewById(R.id.vd_viewpager);
        uiViewPager.setOffscreenPageLimit(2);

        isLogin = !SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, "").equals("");
        videoApi = new VideoApi(intent.getStringExtra(ARG_AID), intent.getStringExtra(ARG_BVID));

        if(SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.tipVd, true))
            findViewById(R.id.vd_tip).setVisibility(View.VISIBLE);

        runnableUi = () -> {
            try
            {
                uiExceptionHandlerView.hideAllView();
                uiViewPager.setAdapter(pagerAdapter);
            }
            catch (Exception e)
            {
                e.printStackTrace();
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
                    return ReplyFragment.newInstance(videoModel.getAid(), "1", null, -1);
                else
                    return VideoRecommendFragment.newInstance(videoModel);
            }
        };

        uiViewPager.addOnPageChangeListener(new TitleViewPagerChangeListener(ctx, uiTitleView));

        new Thread(() -> {
            try
            {
                videoModel = videoApi.getVideoDetails();
                if(videoModel != null)
                    handler.post(runnableUi);
                else
                    uiExceptionHandlerView.noData();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                uiExceptionHandlerView.noWeb();
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
            String[] videoPartNames = new String[videoModel.getPartList().size()];
            String[] videoPartCids = new String[videoModel.getPartList().size()];
            for(int i = 0; i < videoModel.getPartList().size(); i++)
                videoPartNames[i] = videoModel.getPartList().get(i).getPartName();
            for(int i = 0; i < videoModel.getPartList().size(); i++)
                videoPartCids[i] = String.valueOf(videoModel.getPartList().get(i).getPartCid());
            Intent intent = new Intent(ctx, SelectPartActivity.class);
            intent.putExtra("title", "分P");
            intent.putExtra("options_name", videoPartNames);
            intent.putExtra("options_id", videoPartCids);
            startActivityForResult(intent, RESULT_VD_PART);
        }
        else if(viewId == R.id.vd_bt_cover)
        {
            Intent intent = new Intent(ctx, ImgActivity.class);
            intent.putExtra("imgUrl", new String[]{videoModel.getCover()});
            startActivity(intent);
        }
        else if(viewId == R.id.vd_bt_play)
        {
            Intent intent = new Intent(ctx, PlayerActivity.class);
            intent.putExtra("title", videoModel.getTitle());
            intent.putExtra("aid", videoModel.getAid());
            intent.putExtra("cid", videoModel.getCid());
            startActivity(intent);
        }
        else if(viewId == R.id.vd_bt_watchlater)
        {
            new Thread(() -> {
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
            }).start();
        }
        else if(viewId == R.id.vd_bt_download)
        {
            String[] videoPartNames = new String[videoModel.getPartList().size()];
            String[] videoPartCids = new String[videoModel.getPartList().size()];
            for (int i = 0; i < videoModel.getPartList().size(); i++)
                videoPartNames[i] = videoModel.getPartList().get(i).getPartName();
            for (int i = 0; i < videoModel.getPartList().size(); i++)
                videoPartCids[i] = String.valueOf(videoModel.getPartList().get(i).getPartCid());
            Intent intent = new Intent(ctx, SelectPartActivity.class);
            intent.putExtra("title", "分P下载");
            intent.putExtra("tip", "选择要下载的分P");
            intent.putExtra("options_name", videoPartNames);
            intent.putExtra("options_id", videoPartCids);
            startActivityForResult(intent, RESULT_VD_DOWNLOAD);
        }
        else if(viewId == R.id.vd_bt_share)
        {
            Intent intent = new Intent(ctx, SendDynamicActivity.class);
            intent.putExtra("is_share", true);
            intent.putExtra("share_dyid", videoModel.getAid());
            intent.putExtra("share_up", videoModel.getUpName());
            intent.putExtra("share_img", videoModel.getCover());
            intent.putExtra("share_title", videoModel.getTitle());
            startActivityForResult(intent, RESULT_VD_SHARE);
        }
        else if(viewId == R.id.vd_like)
        {
            new Thread(() -> {
                try
                {
                    if(videoModel.isUserLike())
                    {
                        String result = videoApi.likeVideo(2);
                        if(result.equals(""))
                        {
                            videoModel.setDetailLike(videoModel.getDetailLike() - 1);
                            videoModel.setUserLike(false);
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
                            videoModel.setDetailLike(videoModel.getDetailLike() + 1);
                            videoModel.setUserLike(true);
                            videoModel.setUserDislike(false);
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
            }).start();
        }
        else if(viewId == R.id.vd_coin)
        {
            new Thread(() -> {
                try
                {
                    if(videoModel.getDetailCopyright() == 1)  //1原创
                    {
                        if(videoModel.getUserCoin() < 2)
                        {
                            String result = videoApi.coinVideo(1);
                            if(result.equals(""))
                            {
                                videoModel.setDetailCoin(videoModel.getDetailCoin() + 1);
                                videoModel.setUserCoin(videoModel.getUserCoin() + 1);
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
                        if(videoModel.getUserCoin() < 1)
                        {
                            String result = videoApi.coinVideo(1);
                            if(result.equals(""))
                            {
                                videoModel.setDetailLike(videoModel.getDetailLike() + 1);
                                videoModel.setUserCoin(videoModel.getUserCoin() + 1);
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
            }).start();
        }
        else if(viewId == R.id.vd_fav)
        {
            new Thread(() -> {
                try
                {
                    FavorBoxApi favorBoxApi = new FavorBoxApi(SharedPreferencesUtil.getString(SharedPreferencesUtil.mid, ""), false);
                    ArrayList<FavorBoxModel.BoxModel> favorBoxArrayList = favorBoxApi.getFavorBox().getBoxModelArrayList();
                    String[] favorBoxNames = new String[favorBoxArrayList.size()];
                    for (int i = 0; i < favorBoxArrayList.size(); i++)
                        favorBoxNames[i] = favorBoxArrayList.get(i).getTitle();
                    String[] favorBoxIds = new String[favorBoxArrayList.size()];
                    for (int i = 0; i < favorBoxArrayList.size(); i++)
                        favorBoxIds[i] = favorBoxArrayList.get(i).getId();
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
            }).start();
        }
        else if(viewId == R.id.vd_dislike)
        {
            new Thread(() -> {
                try
                {
                    if(videoModel.isUserDislike())
                    {
                        String result = videoApi.likeVideo(4);
                        if(result.equals(""))
                        {
                            videoModel.setUserDislike(false);
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
                            videoModel.setDetailLike(videoModel.getDetailLike() - (videoModel.isUserLike() ? 1 : 0));
                            videoModel.setUserDislike(true);
                            videoModel.setUserLike(false);
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
            }).start();
        }
    }

    @Override
    public void onVideoDetailFragmentPartClick(int position)
    {
        VideoModel.VideoPartModel videoPartModel = videoModel.getPartList().get(position);
        Intent intent = new Intent(ctx, PlayerActivity.class);
        intent.putExtra("title", videoPartModel.getPartName());
        intent.putExtra("aid", videoModel.getAid());
        intent.putExtra("cid", videoPartModel.getPartCid());
        if(videoPartModel.getPartCid().equals(videoModel.getUserProgressCid()))
            intent.putExtra("time", videoModel.getUserProgressTime());
        startActivity(intent);
        videoModel.setUserProgressCid(videoPartModel.getPartCid());
        videoModel.setUserProgressPosition(position);
        videoModel.setUserProgressTime(0);
        EventBus.getDefault().post(videoModel);
    }

    @Override
    public void onVideoDetailFragmentTriple()
    {
        new Thread(() -> {
            try
            {
                VideoModel.VideoTripleModel videoTripleModel = videoApi.tripleVideo();
                if(videoTripleModel != null)
                {
                    videoModel.setUserLike(videoTripleModel.isLike());
                    videoModel.setUserCoin(videoModel.getUserCoin() + videoTripleModel.getMultiply());
                    videoModel.setUserFav(videoTripleModel.isFav());
                    videoModel.setDetailLike(videoModel.getDetailLike() + (videoTripleModel.isLike() ? 1 : 0));
                    videoModel.setDetailCoin(videoModel.getDetailCoin() + videoTripleModel.getMultiply());
                    videoModel.setDetailFav(videoModel.getDetailFav() + (videoTripleModel.isFav() ? 1 : 0));
                    Looper.prepare();
                    Toast.makeText(ctx, "三连成功！", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Looper.prepare();
                    Toast.makeText(ctx, getString(R.string.main_error_unknown), Toast.LENGTH_SHORT).show();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
                Looper.prepare();
                Toast.makeText(ctx, getString(R.string.main_error_web), Toast.LENGTH_SHORT).show();
            }
            finally
            {
                EventBus.getDefault().post(videoModel);
                Looper.loop();
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
                new Thread(() -> {
                    try
                    {
                        String result = videoApi.favVideo(data.getStringExtra("option_id"));
                        if(result.equals(""))
                        {
                            videoModel.setDetailFav(videoModel.getDetailFav() + (videoModel.isUserFav() ? 0 : 1));
                            videoModel.setUserFav(true);
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
                }).start();
                break;

            case RESULT_VD_DOWNLOAD:
                new Thread(() -> {
                    try
                    {
                        onlineVideoApi = new OnlineVideoApi(videoModel.getAid(), data.getStringExtra("option_id"));
                        onlineVideoApi.connectionVideoUrl();
                        connection.downloadVideo(data.getStringExtra("option_name") + " - " + videoModel.getTitle(), data.getStringExtra("option_id"));
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        Looper.prepare();
                        Toast.makeText(ctx, "网络连接失败，请检查网络", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }).start();
                break;

            case RESULT_VD_PART:
                Intent intent = new Intent(ctx, PlayerActivity.class);
                intent.putExtra("title", data.getStringExtra("option_name") + " - " + videoModel.getTitle());
                intent.putExtra("aid", videoModel.getAid());
                intent.putExtra("cid", data.getStringExtra("option_id"));
                startActivity(intent);
                break;

            case RESULT_VD_SHARE:
                new Thread(() -> {
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
                }).start();
                break;
        }
    }

    public void setVideoDetailActivityListener(VideoDetailActivityListener videoDetailActivityListener)
    {
        this.videoDetailActivityListener = videoDetailActivityListener;
    }

    @Override
    public boolean hideTitle()
    {
        return uiTitleView.hide();
    }

    @Override
    public boolean showTitle()
    {
        return uiTitleView.show();
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
            String result = myBinder.startDownload(videoModel.getAid(), cid, title,
                                                   videoModel.getCover(), onlineVideoApi.getVideoUrl(),
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
