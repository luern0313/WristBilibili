package cn.luern0313.wristbilibili.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.UserApi;
import cn.luern0313.wristbilibili.api.VideoApi;
import cn.luern0313.wristbilibili.models.ListVideoModel;
import cn.luern0313.wristbilibili.util.NetWorkUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;

/**
 * Created by liupe on 2018/11/11.
 * 关注我~
 */

public class FollowmeActivity extends BaseActivity
{
    Context ctx;

    Handler handler = new Handler();
    Runnable runnVideo;
    Runnable runnImg;

    CardView cardView;
    RelativeLayout cardViewLay;
    TextView cardViewText;
    RelativeLayout uiVideo;
    ImageView uiVideoImg;
    TextView uiVideoTitle;
    LinearLayout uiVoteLin;
    TextView uiVote;
    LinearLayout uiVideoStarLin;
    ImageView uiVideoStar;
    LinearLayout uiVideoLC;
    LinearLayout uiVideoLike;
    ImageView uiVideoLikeImg;
    LinearLayout uiVideoCoin;
    ImageView uiVideoCoinImg;

    UserApi userApi;
    ListVideoModel listVideoModel;
    VideoApi videoDetail;
    Bitmap videoCover;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followme);
        ctx = this;
        userApi = new UserApi("8014831");

        cardView = findViewById(R.id.fme_card);
        cardViewLay = findViewById(R.id.fme_card_lay);
        cardViewText = findViewById(R.id.fme_card_button);
        uiVideo = findViewById(R.id.fme_video);
        uiVideoImg = findViewById(R.id.fme_video_img);
        uiVideoTitle = findViewById(R.id.fme_video_title);
        uiVoteLin = findViewById(R.id.fme_vote);
        uiVote = findViewById(R.id.fme_vote_button);
        uiVideoStarLin = findViewById(R.id.fme_star);
        uiVideoStar = findViewById(R.id.fme_star_rating);
        uiVideoLC = findViewById(R.id.fme_lc);
        uiVideoLike = findViewById(R.id.fme_like);
        uiVideoLikeImg = findViewById(R.id.fme_like_img);
        uiVideoCoin = findViewById(R.id.fme_coin);
        uiVideoCoinImg = findViewById(R.id.fme_coin_img);

        runnVideo = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    uiVideoTitle.setText(listVideoModel.getVideoTitle());
                    if(listVideoModel.getVideoTitle().startsWith("【互动"))
                        uiVideoStarLin.setVisibility(View.VISIBLE);
                    uiVideoLC.setVisibility(View.VISIBLE);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnImg = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    uiVideoImg.setImageBitmap(videoCover);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        if(!SharedPreferencesUtil.contains(SharedPreferencesUtil.cookies))
            findViewById(R.id.fme_nologin).setVisibility(View.VISIBLE);
        else
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        listVideoModel = userApi.getUserVideo(1).get(0);
                        videoDetail = new VideoApi("", listVideoModel.getVideoBvid());
                        handler.post(runnVideo);

                        byte[] picByte = NetWorkUtil.readStream(NetWorkUtil.get(listVideoModel.getVideoCover()).body().byteStream());
                        videoCover = BitmapFactory.decodeByteArray(picByte, 0, picByte.length);
                        handler.post(runnImg);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        cardView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cardViewText.setText("已关注");
                cardViewText.setBackgroundResource(R.drawable.shape_bg_anre_followbgyes);
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            userApi.follow();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            Looper.prepare();
                            Toast.makeText(ctx, "关注失败...", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    }
                }).start();
            }
        });

        uiVideo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(videoDetail != null)
                    startActivity(VideoActivity.getActivityIntent(ctx, "", videoDetail.bvid));
            }
        });

        uiVote.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                uiVote.setText("感谢投票~");
                uiVote.setBackgroundResource(R.drawable.shape_bg_anre_followbgyes);
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            videoDetail.scoreVideo(5);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        uiVideoStarLin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                uiVideoStar.setImageResource(R.drawable.img_fme_star_yes);
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            videoDetail.scoreVideo(5);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        uiVideoLike.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                uiVideoLikeImg.setImageResource(R.drawable.icon_like_yes);
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            videoDetail.likeVideo(1);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        uiVideoCoin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                uiVideoCoinImg.setImageResource(R.drawable.icon_coin_yes);
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            videoDetail.coinVideo(2);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }
}
