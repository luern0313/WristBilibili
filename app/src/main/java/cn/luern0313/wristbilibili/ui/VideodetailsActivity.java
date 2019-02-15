package cn.luern0313.wristbilibili.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.w3c.dom.Text;

import java.io.IOException;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.VideoDetails;

public class VideodetailsActivity extends Activity
{
    Context ctx;
    Intent intent;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    VideoDetails videoDetail;

    Handler handler = new Handler();
    Runnable runnableNoWeb;
    Runnable runnableUi;
    Runnable runnableImg;
    Runnable runnableSetface;
    Runnable runnableNodata;
    Thread threadReply;
    Runnable runnableReply;
    Runnable runnableReplyerr;

    AnimationDrawable loadingImgAnim;
    Bitmap videoUpFace;
    String reply;

    ImageView uiLoadingImg;
    LinearLayout uiLoading;
    LinearLayout uiNoWeb;
    TextView uiLikeText;
    TextView uiCoinText;
    TextView uiFavText;
    ImageView uiLikeImg;
    ImageView uiCoinImg;
    ImageView uiFavImg;
    ImageView uiDislikeImg;
    LinearLayout uiLikeLay;
    LinearLayout uiCoinLay;
    LinearLayout uiFavLay;
    LinearLayout uiDislikeLay;
    TextView uiReply;

    boolean isLogin = false;
    int isLiked = 0;//012
    int isCoined = 0;
    boolean isFaved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videodetails);

        ctx = this;
        intent = getIntent();
        sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        uiLoadingImg = findViewById(R.id.vd_loading_img);
        uiLoading = findViewById(R.id.vd_loading);
        uiNoWeb = findViewById(R.id.vd_noweb);
        uiLikeText = findViewById(R.id.vd_like_text);
        uiCoinText = findViewById(R.id.vd_coin_text);
        uiFavText = findViewById(R.id.vd_fav_text);
        uiLikeImg = findViewById(R.id.vd_like_img);
        uiCoinImg = findViewById(R.id.vd_coin_img);
        uiFavImg = findViewById(R.id.vd_fav_img);
        uiDislikeImg = findViewById(R.id.vd_dislike_img);
        uiLikeLay = findViewById(R.id.vd_like);
        uiCoinLay = findViewById(R.id.vd_coin);
        uiFavLay = findViewById(R.id.vd_fav);
        uiDislikeLay = findViewById(R.id.vd_dislike);
        uiReply = findViewById(R.id.vd_reply);

        isLogin = !MainActivity.sharedPreferences.getString("cookies", "").equals("");
        videoDetail = new VideoDetails(sharedPreferences.getString("cookies", ""), sharedPreferences.getString("csrf", ""), sharedPreferences.getString("mid", ""), intent.getStringExtra("aid"));

        uiLoadingImg.setImageResource(R.drawable.anim_loading);
        loadingImgAnim = (AnimationDrawable) uiLoadingImg.getDrawable();
        loadingImgAnim.start();
        uiLoading.setVisibility(View.VISIBLE);

        runnableNoWeb = new Runnable()
        {
            @Override
            public void run()
            {
                uiLoading.setVisibility(View.GONE);
                uiNoWeb.setVisibility(View.VISIBLE);
            }
        };

        runnableUi = new Runnable()
        {
            @Override
            public void run()
            {
                ((TextView) findViewById(R.id.vd_video_title)).setText(videoDetail.getVideoTitle());
                ((TextView) findViewById(R.id.vd_video_play)).setText("播放:" + videoDetail.getVideoPlay() + "  弹幕:" + videoDetail.getVideoDanmaku());
                ((TextView) findViewById(R.id.vd_video_time)).setText(videoDetail.getVideoupTime() + "  AV" + videoDetail.getVideoAid());
                ((TextView) findViewById(R.id.vd_video_details)).setText(videoDetail.getVideoDetail());
                ((TextView) findViewById(R.id.vd_card_name)).setText(videoDetail.getVideoUpName());
                ((TextView) findViewById(R.id.vd_card_sen)).setText(videoDetail.getVideoUpSign());
                uiLikeText.setText(videoDetail.getVideoLike());
                uiCoinText.setText(videoDetail.getVideoCoin());
                uiFavText.setText(videoDetail.getVideoFav());
                isLiked = videoDetail.getSelfLiked();
                isCoined = videoDetail.getSelfCoined();
                isFaved = videoDetail.getSelfFaved();

                setIcon();

                uiLoading.setVisibility(View.GONE);
                uiNoWeb.setVisibility(View.GONE);
            }
        };

        runnableImg = new Runnable()
        {
            @Override
            public void run()
            {
                setIcon();
            }
        };

        runnableSetface = new Runnable()
        {
            @Override
            public void run()
            {
                ((ImageView) findViewById(R.id.vd_card_head)).setImageBitmap(videoUpFace);
            }
        };

        runnableNodata = new Runnable()
        {
            @Override
            public void run()
            {
                findViewById(R.id.vd_novideo).setVisibility(View.VISIBLE);
            }
        };

        threadReply = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    reply = videoDetail.getVideoReply();
                    handler.post(runnableReply);
                }
                catch (IOException e)
                {
                    handler.post(runnableReplyerr);
                }
            }
        });

        runnableReply = new Runnable()
        {
            @Override
            public void run()
            {
                ((TextView) findViewById(R.id.vd_reply)).setText(Html.fromHtml(reply));
            }
        };

        runnableReplyerr = new Runnable()
        {
            @Override
            public void run()
            {
                ((TextView) findViewById(R.id.vd_reply)).setText("评论加载失败。。。");
            }
        };

        findViewById(R.id.vd_card).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(ctx, OtheruserActivity.class);
                intent.putExtra("mid", videoDetail.getVideoUpAid());
                startActivity(intent);
            }
        });

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if(videoDetail.getVideoDetails())
                    {
                        handler.post(runnableUi);
                        videoUpFace = videoDetail.getVideoUpFace();
                        handler.post(runnableSetface);
                        threadReply.start();
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

    void setIcon()
    {
        uiLikeText.setText(videoDetail.getVideoLike());
        uiCoinText.setText(videoDetail.getVideoCoin());
        uiFavText.setText(videoDetail.getVideoFav());
        uiLikeImg.setImageResource(R.drawable.icon_vdd_do_like_no);
        uiCoinImg.setImageResource(R.drawable.icon_vdd_do_coin_no);
        uiFavImg.setImageResource(R.drawable.icon_vdd_do_fav_no);
        uiDislikeImg.setImageResource(R.drawable.icon_vdd_do_dislike_no);
        if(isLiked == 1)//赞
        {
            uiLikeImg.setImageResource(R.drawable.icon_vdd_do_like_yes);
            uiDislikeImg.setImageResource(R.drawable.icon_vdd_do_dislike_no);
        }
        else if(isLiked == 2)
        {
            uiLikeImg.setImageResource(R.drawable.icon_vdd_do_like_no);
            uiDislikeImg.setImageResource(R.drawable.icon_vdd_do_dislike_yes);
        }
        else
        {
            uiLikeImg.setImageResource(R.drawable.icon_vdd_do_like_no);
            uiDislikeImg.setImageResource(R.drawable.icon_vdd_do_dislike_no);
        }
        if(isCoined > 0)
            uiCoinImg.setImageResource(R.drawable.icon_vdd_do_coin_yes);
        else
            uiCoinImg.setImageResource(R.drawable.icon_vdd_do_coin_no);
        if(isFaved)
            uiFavImg.setImageResource(R.drawable.icon_vdd_do_fav_yes);
        else
            uiFavImg.setImageResource(R.drawable.icon_vdd_do_fav_no);
    }

    public void clickCover(View view)
    {
        Intent intent = new Intent(ctx, ImgActivity.class);
        intent.putExtra("imgUrl", new String[]{videoDetail.getVideoFace()});
        startActivity(intent);
    }

    public void clickPlay(View view)
    {
        Intent intent = new Intent(ctx, QRActivity.class);
        intent.putExtra("url", "https://www.bilibili.com/video/av" + videoDetail.getVideoAid());
        startActivity(intent);
    }

    public void clickCoverLater(View view)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if(videoDetail.playLater())
                    {
                        Looper.prepare();
                        Toast.makeText(ctx, "已添加至稍后再看", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                    else
                    {
                        Looper.prepare();
                        Toast.makeText(ctx, "未成功添加至稍后观看！请检查网络再试", Toast.LENGTH_SHORT).show();
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

    public void clickHistory(View view)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if(videoDetail.playHistory())
                    {
                        Looper.prepare();
                        Toast.makeText(ctx, "已添加至历史记录！你可以在历史记录找到", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                    else
                    {
                        Looper.prepare();
                        Toast.makeText(ctx, "未成功添加至历史记录！请检查网络再试", Toast.LENGTH_SHORT).show();
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

    public void clickLike(View view)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if(isLiked == 1)
                    {
                        videoDetail.likeVideo(2);
                        isLiked = 0;
                        videoDetail.setSelfLiked(-1);
                        Looper.prepare();
                        Toast.makeText(ctx, "已取消喜欢...", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        videoDetail.likeVideo(1);
                        isLiked = 1;
                        videoDetail.setSelfLiked(1);
                        Looper.prepare();
                        Toast.makeText(ctx, "已喜欢！这个视频会被更多人看到！", Toast.LENGTH_SHORT).show();
                    }
                    handler.post(runnableImg);
                    Looper.loop();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(ctx, "喜欢失败...请检查你的网络..", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }
        }).start();
    }

    public void clickCoin(View view)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if(videoDetail.getVideoCopyright() == 1)  //1原创
                    {
                        if(isCoined < 2)
                        {
                            isCoined++;
                            videoDetail.coinVideo(1);
                            videoDetail.setSelfCoined(1);
                            Looper.prepare();
                            Toast.makeText(ctx, "你投了一个硬币！再次点击可以再次投币！", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Looper.prepare();
                            Toast.makeText(ctx, "最多投两个硬币...", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else  //2转载
                    {
                        if(isCoined < 1)
                        {
                            isCoined++;
                            videoDetail.coinVideo(1);
                            videoDetail.setSelfCoined(1);
                            Looper.prepare();
                            Toast.makeText(ctx, "你投了一个硬币！本稿件最多投一个硬币", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Looper.prepare();
                            Toast.makeText(ctx, "本稿件最多投一个硬币...", Toast.LENGTH_SHORT).show();
                        }
                    }
                    handler.post(runnableImg);
                    Looper.loop();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(ctx, "投币失败！请检查你的网络..", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }
        }).start();
    }

    public void clickFav(final View view)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if(isFaved)
                    {
                        isFaved = false;
                        videoDetail.favCancalVideo();
                        videoDetail.setSelfFaved(-1);
                        Looper.prepare();
                        Toast.makeText(ctx, "已取消收藏！", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        isFaved = true;
                        videoDetail.favVideo();
                        videoDetail.setSelfFaved(1);
                        Looper.prepare();
                        Toast.makeText(ctx, "已收藏至默认收藏夹！\n(别问我为什么不能选择别的..懒...)", Toast.LENGTH_SHORT).show();
                    }
                    handler.post(runnableImg);
                    Looper.loop();
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

    public void clickDislike(View view)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if(isLiked == 2)
                    {
                        videoDetail.likeVideo(4);
                        isLiked = 0;
                        Looper.prepare();
                        Toast.makeText(ctx, "取消点踩成功！", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        videoDetail.likeVideo(3);
                        isLiked = 2;
                        Looper.prepare();
                        Toast.makeText(ctx, "点踩成功！", Toast.LENGTH_SHORT).show();
                    }
                    handler.post(runnableImg);
                    Looper.loop();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(ctx, "点踩失败！请检查你的网络..", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }
        }).start();
    }

    public void clickSendReply(View view)
    {
        Intent replyIntent = new Intent(ctx, ReplyActivity.class);
        replyIntent.putExtra("oid", intent.getStringExtra("aid"));
        replyIntent.putExtra("type", "1");
        startActivityForResult(replyIntent, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0 && resultCode == 0 && (!data.getStringExtra("text").equals("")))
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(videoDetail.sendReply(data.getStringExtra("text")))
                        {
                            Looper.prepare();
                            Toast.makeText(ctx, "发送成功！", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                        else
                        {
                            Looper.prepare();
                            Toast.makeText(ctx, "发送失败，可能是短时间发送过多？", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    }
                    catch(IOException e)
                    {
                        e.printStackTrace();
                        Looper.prepare();
                        Toast.makeText(ctx, "评论发送失败。。请检查网络？", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }
            }).start();
        }
    }
}
