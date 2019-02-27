package cn.luern0313.wristbilibili.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.util.LruCache;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.zip.Inflater;

import cn.carbs.android.expandabletextview.library.ExpandableTextView;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.OthersUser;
import cn.luern0313.wristbilibili.api.UserDynamic;
import cn.luern0313.wristbilibili.fragment.Dynamic;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 被 luern0313 创建于 不知道什么时候.
 * 这个部分做的很！不！优！雅！！
 * 该api去获取去解析的直接在activity文件里完成了
 * 现在要拓展功能才知道当初不该偷懒
 */
public class OtheruserActivity extends Activity
{
    Context ctx;
    Intent intent;
    LayoutInflater inflater;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    UserDynamic userDynamic;

    OthersUser othersUser;
    JSONObject otherUserJson;
    JSONObject otherUserCardJson;
    Bitmap userHead;

    Handler handler = new Handler();
    Runnable runnableUi;
    Runnable runnableHead;
    Runnable runnableNoweb;
    Runnable runnableFollow;
    Runnable runnableUnfollow;
    Runnable runnableDynamic;
    Runnable runnableDynamicAddlist;
    Runnable runnableMoreNomore;
    Runnable runnableMoreErr;

    TextView uiTitleTextView;
    ViewPager uiViewpager;
    ImageView uiLoading;
    View layoutLoading;

    ImageView uiImg;
    TextView uiName;
    TextView uiFollow;
    TextView uiLv;
    TextView uiAnth;
    TextView uiSign;
    TextView uiOther;
    ListView uiDynamicListView;

    ArrayList<Object> dynamicArrayList;
    mAdapter mAdapter;

    boolean isDynamicLoading = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otheruser);

        ctx = this;
        intent = getIntent();
        inflater = getLayoutInflater();
        sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        othersUser = new OthersUser(sharedPreferences.getString("cookies", ""), sharedPreferences.getString("csrf", ""), intent.getStringExtra("mid"));
        uiTitleTextView = findViewById(R.id.ou_title_title);
        uiViewpager = findViewById(R.id.ou_viewpager);
        uiLoading = findViewById(R.id.ou_loading_img);
        layoutLoading = inflater.inflate(R.layout.widget_dyloading, null);

        runnableUi = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    findViewById(R.id.ou_loading).setVisibility(View.GONE);
                    findViewById(R.id.ou_noweb).setVisibility(View.GONE);

                    if(otherUserCardJson.optJSONObject("vip").optInt("vipStatus") == 0)
                        uiName.setTextColor(getResources().getColor(R.color.textColor3));
                    uiName.setText(otherUserCardJson.optString("name"));
                    uiLv.setText("LV" + otherUserCardJson.optJSONObject("level_info").optInt("current_level"));
                    if(otherUserJson.optBoolean("following"))
                    {
                        uiFollow.setText("已关注");
                        uiFollow.setBackgroundResource(R.drawable.shape_anre_followbgyes);
                    }
                    if(!otherUserCardJson.optJSONObject("Official").optString("title").equals(""))
                    {
                        uiAnth.setVisibility(View.VISIBLE);
                        uiAnth.setText(otherUserCardJson.optJSONObject("Official").optString("title"));
                    }
                    if(!otherUserCardJson.optString("sign").equals(""))
                    {
                        uiSign.setVisibility(View.VISIBLE);
                        uiSign.setText(otherUserCardJson.optString("sign"));
                    }
                    uiOther.setText("关注 : " + otherUserCardJson.optString("friend") + "  粉丝 : " + otherUserCardJson.optString("fans") + "\n投稿 : " + otherUserJson.optString("archive_count"));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableHead = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    uiImg.setImageBitmap(userHead);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableNoweb = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    findViewById(R.id.ou_loading).setVisibility(View.GONE);
                    findViewById(R.id.ou_noweb).setVisibility(View.VISIBLE);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableFollow = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    uiFollow.setText("已关注");
                    uiFollow.setBackgroundResource(R.drawable.shape_anre_followbgyes);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableUnfollow = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    uiFollow.setText("+关注");
                    uiFollow.setBackgroundResource(R.drawable.shape_anre_followbg);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableDynamic = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    mAdapter = new mAdapter(inflater, dynamicArrayList);
                    uiDynamicListView.setAdapter(mAdapter);
                }
                catch (Exception e)
                {

                }
            }
        };

        runnableDynamicAddlist = new Runnable()
        {
            @Override
            public void run()
            {
                mAdapter.notifyDataSetChanged();
            }
        };

        runnableMoreNomore = new Runnable()
        {
            @Override
            public void run()
            {
                ((TextView) layoutLoading.findViewById(R.id.dyload_text)).setText("  没有更多了...");
            }
        };

        runnableMoreErr = new Runnable()
        {
            @Override
            public void run()
            {
                ((TextView) layoutLoading.findViewById(R.id.dyload_text)).setText("好像没有网络...\n检查下网络？");
                layoutLoading.findViewById(R.id.dyload_button).setVisibility(View.VISIBLE);
                isDynamicLoading = false;
            }
        };

        uiLoading.setImageResource(R.drawable.anim_loading);
        AnimationDrawable loadingImgAnim = (AnimationDrawable) uiLoading.getDrawable();
        loadingImgAnim.start();

        final PagerAdapter pagerAdapter = new PagerAdapter()
        {
            @Override
            public int getCount()
            {
                return 2;
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
                if(position == 0)
                {
                    View v = inflater.inflate(R.layout.viewpager_ou_userinfo, null);
                    v.setTag(0);

                    uiImg = v.findViewById(R.id.ou_head);
                    uiName = v.findViewById(R.id.ou_name);
                    uiFollow = v.findViewById(R.id.ou_follow);
                    uiLv = v.findViewById(R.id.ou_lv);
                    uiAnth = v.findViewById(R.id.ou_anth);
                    uiSign = v.findViewById(R.id.ou_sign);
                    uiOther = v.findViewById(R.id.ou_other);

                    if(sharedPreferences.getString("mid", "").equals(intent.getStringExtra("mid")))
                        uiFollow.setVisibility(View.INVISIBLE);

                    uiImg.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Intent i = new Intent(ctx, ImgActivity.class);
                            i.putExtra("imgUrl", new String[]{otherUserCardJson.optString("face")});
                            startActivity(i);
                        }
                    });

                    uiFollow.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            new Thread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    try
                                    {
                                        if(uiFollow.getText().toString().equals("已关注"))
                                        {
                                            othersUser.unfollow();
                                            handler.post(runnableUnfollow);
                                        }
                                        else
                                        {
                                            othersUser.follow();
                                            handler.post(runnableFollow);
                                        }
                                    }
                                    catch (IOException e)
                                    {
                                        Looper.prepare();
                                        Toast.makeText(ctx, "操作失败...", Toast.LENGTH_SHORT).show();
                                        Looper.loop();
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                        }
                    });

                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                otherUserJson = new JSONObject(othersUser.getOtheruserInfo()).getJSONObject("data");
                                otherUserCardJson = otherUserJson.getJSONObject("card");
                                handler.post(runnableUi);
                                userHead = (Bitmap) get(otherUserCardJson.optString("face"), 2);
                                handler.post(runnableHead);
                            }
                            catch (JSONException e)
                            {
                                Looper.prepare();
                                Toast.makeText(ctx, "查无此人. . .", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                                e.printStackTrace();
                            }
                            catch (IOException e)
                            {
                                handler.post(runnableNoweb);
                                e.printStackTrace();
                            }
                        }
                    }).start();

                    container.addView(v);
                    return 0;
                }
                else
                {
                    View v = inflater.inflate(R.layout.viewpager_ou_dynamic, null);
                    v.setTag(1);
                    uiDynamicListView = v.findViewById(R.id.ou_dy_listview);
                    uiDynamicListView.setEmptyView(v.findViewById(R.id.ou_dy_nonthing));
                    uiDynamicListView.addFooterView(layoutLoading);

                    uiDynamicListView.setOnScrollListener(new AbsListView.OnScrollListener()
                    {
                        @Override
                        public void onScrollStateChanged(AbsListView view, int scrollState)
                        {
                        }

                        @Override
                        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
                        {
                            if(visibleItemCount + firstVisibleItem == totalItemCount && !isDynamicLoading)
                            {
                                getMoreDynamic();
                            }
                        }
                    });

                    userDynamic = new UserDynamic(MainActivity.sharedPreferences.getString("cookies", ""), MainActivity.sharedPreferences.getString("csrf", ""), MainActivity.sharedPreferences.getString("mid", ""), intent.getStringExtra("mid"), false);
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                userDynamic.getDynamic();
                                dynamicArrayList = userDynamic.getDynamicList();
                                isDynamicLoading = false;
                                handler.post(runnableDynamic);
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                                isDynamicLoading = false;
                                dynamicArrayList = new ArrayList<>();
                                handler.post(runnableDynamic);
                            }

                        }
                    }).start();

                    container.addView(v);
                    return 1;
                }
            }
        };

        uiViewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
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
                if(position == 0) titleAnim("用户");
                else if(position == 1) titleAnim("动态");
            }
        });
        uiViewpager.setAdapter(pagerAdapter);
    }

    void getMoreDynamic()
    {
        isDynamicLoading = true;
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    userDynamic.getHistoryDynamic();
                    ArrayList<Object> d = userDynamic.getDynamicList();
                    isDynamicLoading = false;
                    if(d != null && d.size() != 0)
                    {
                        dynamicArrayList.addAll(d);
                        handler.post(runnableDynamicAddlist);
                    }
                    else
                    {
                        handler.post(runnableMoreNomore);
                    }
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    void titleAnim(final String title)
    {
        ObjectAnimator alpha = ObjectAnimator.ofFloat(uiTitleTextView, "alpha", 1f, 0f);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(alpha);
        animatorSet.setDuration(500);
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {
            }

            @Override
            public void onAnimationCancel(Animator animation)
            {
            }

            @Override
            public void onAnimationRepeat(Animator animation)
            {
            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                uiTitleTextView.setText(title);
                uiTitleTextView.setAlpha(1);
            }
        });
    }

    class mAdapter extends BaseAdapter
    {
        private LayoutInflater mInflater;

        private LruCache<String, BitmapDrawable> mImageCache;

        private ArrayList<Object> dyList;

        public mAdapter(LayoutInflater inflater, ArrayList<Object> dyList)
        {
            mInflater = inflater;
            this.dyList = dyList;

            int maxCache = (int) Runtime.getRuntime().maxMemory();
            int cacheSize = maxCache / 6;
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
        }

        @Override
        public int getCount()
        {
            return dyList.size();
        }

        @Override
        public Object getItem(int position)
        {
            return position;
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public int getViewTypeCount()
        {
            return 5;
        }

        @Override
        public int getItemViewType(int position)
        {
            if(dyList.get(position) instanceof UserDynamic.cardOriginalVideo) return 4;
            else if(dyList.get(position) instanceof UserDynamic.cardOriginalText) return 3;
            else if(dyList.get(position) instanceof UserDynamic.cardUnknow) return 2;
            else if(dyList.get(position) instanceof UserDynamic.cardShareVideo) return 1;
            else if(dyList.get(position) instanceof UserDynamic.cardShareText) return 0;
            return 2;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup)
        {
            ViewHolderOriText viewHolderOriText = null;
            ViewHolderOriVid viewHolderOriVid = null;
            ViewHolderShaText viewHolderShaText = null;
            ViewHolderShaVid viewHolderShaVid = null;
            ViewHolderUnktyp viewHolderUnktyp = null;
            int type = getItemViewType(position);

            // 若无可重用的 view 则进行加载
            if(convertView == null)
            {
                switch (type)
                {
                    case 4:
                        //原创视频
                        convertView = mInflater.inflate(R.layout.item_news_original_video, null);
                        viewHolderOriVid = new ViewHolderOriVid();
                        convertView.setTag(viewHolderOriVid);

                        viewHolderOriVid.lay = convertView.findViewById(R.id.liov_lay);
                        viewHolderOriVid.head = convertView.findViewById(R.id.liov_head);
                        viewHolderOriVid.name = convertView.findViewById(R.id.liov_name);
                        viewHolderOriVid.time = convertView.findViewById(R.id.liov_time);
                        viewHolderOriVid.text = convertView.findViewById(R.id.liov_text);
                        viewHolderOriVid.img = convertView.findViewById(R.id.liov_share_img);
                        viewHolderOriVid.imgtext = convertView.findViewById(R.id.liov_textimg);
                        viewHolderOriVid.title = convertView.findViewById(R.id.liov_title);
                        viewHolderOriVid.likebu = convertView.findViewById(R.id.liov_likebu);
                        viewHolderOriVid.likei = convertView.findViewById(R.id.liov_likei);
                        viewHolderOriVid.like = convertView.findViewById(R.id.liov_like);
                        break;

                    case 3:
                        //原创文字
                        convertView = mInflater.inflate(R.layout.item_news_original_text, null);
                        viewHolderOriText = new ViewHolderOriText();
                        convertView.setTag(viewHolderOriText);

                        viewHolderOriText.head = convertView.findViewById(R.id.liot_head);
                        viewHolderOriText.name = convertView.findViewById(R.id.liot_name);
                        viewHolderOriText.time = convertView.findViewById(R.id.liot_time);
                        viewHolderOriText.text = convertView.findViewById(R.id.liot_text);
                        viewHolderOriText.textimg = convertView.findViewById(R.id.liot_textimg);
                        viewHolderOriText.replybu = convertView.findViewById(R.id.liot_replybu);
                        viewHolderOriText.reply = convertView.findViewById(R.id.liot_reply);
                        viewHolderOriText.likebu = convertView.findViewById(R.id.liot_likebu);
                        viewHolderOriText.likei = convertView.findViewById(R.id.liot_likei);
                        viewHolderOriText.like = convertView.findViewById(R.id.liot_like);
                        break;

                    case 2:
                        //未知类型
                        convertView = mInflater.inflate(R.layout.item_news_unknowtype, null);
                        viewHolderUnktyp = new ViewHolderUnktyp();
                        convertView.setTag(viewHolderUnktyp);

                        viewHolderUnktyp.head = convertView.findViewById(R.id.liuk_head);
                        viewHolderUnktyp.name = convertView.findViewById(R.id.liuk_name);
                        viewHolderUnktyp.time = convertView.findViewById(R.id.liuk_time);
                        break;

                    case 1:
                        //转发视频
                        convertView = mInflater.inflate(R.layout.item_news_share_video, null);
                        viewHolderShaVid = new ViewHolderShaVid();
                        convertView.setTag(viewHolderShaVid);

                        viewHolderShaVid.head = convertView.findViewById(R.id.lisv_head);
                        viewHolderShaVid.name = convertView.findViewById(R.id.lisv_name);
                        viewHolderShaVid.time = convertView.findViewById(R.id.lisv_time);
                        viewHolderShaVid.text = convertView.findViewById(R.id.lisv_text);
                        viewHolderShaVid.slay = convertView.findViewById(R.id.lisv_share_lay);
                        viewHolderShaVid.shead = convertView.findViewById(R.id.lisv_share_head);
                        viewHolderShaVid.sname = convertView.findViewById(R.id.lisv_share_name);
                        viewHolderShaVid.simg = convertView.findViewById(R.id.lisv_share_img);
                        viewHolderShaVid.simgtext = convertView.findViewById(R.id.lisv_share_imgtext);
                        viewHolderShaVid.stitle = convertView.findViewById(R.id.lisv_share_text);
                        viewHolderShaVid.replybu = convertView.findViewById(R.id.lisv_replybu);
                        viewHolderShaVid.reply = convertView.findViewById(R.id.lisv_reply);
                        viewHolderShaVid.likebu = convertView.findViewById(R.id.lisv_likebu);
                        viewHolderShaVid.likei = convertView.findViewById(R.id.lisv_likei);
                        viewHolderShaVid.like = convertView.findViewById(R.id.lisv_like);
                        break;

                    case 0:
                        //转发文字
                        convertView = mInflater.inflate(R.layout.item_news_share_text, null);
                        viewHolderShaText = new ViewHolderShaText();
                        convertView.setTag(viewHolderShaText);

                        viewHolderShaText.head = convertView.findViewById(R.id.list_head);
                        viewHolderShaText.name = convertView.findViewById(R.id.list_name);
                        viewHolderShaText.time = convertView.findViewById(R.id.list_time);
                        viewHolderShaText.text = convertView.findViewById(R.id.list_text);
                        viewHolderShaText.shead = convertView.findViewById(R.id.list_share_head);
                        viewHolderShaText.sname = convertView.findViewById(R.id.list_share_name);
                        viewHolderShaText.stext = convertView.findViewById(R.id.list_share_text);
                        viewHolderShaText.stextimg = convertView.findViewById(R.id.list_share_textimg);
                        viewHolderShaText.replybu = convertView.findViewById(R.id.list_replybu);
                        viewHolderShaText.reply = convertView.findViewById(R.id.list_reply);
                        viewHolderShaText.likebu = convertView.findViewById(R.id.list_likebu);
                        viewHolderShaText.likei = convertView.findViewById(R.id.list_likei);
                        viewHolderShaText.like = convertView.findViewById(R.id.list_like);
                        break;
                }
            }
            else
            {
                switch (type)
                {
                    case 4:
                        viewHolderOriVid = (ViewHolderOriVid) convertView.getTag();
                        break;
                    case 3:
                        viewHolderOriText = (ViewHolderOriText) convertView.getTag();
                        break;
                    case 2:
                        viewHolderUnktyp = (ViewHolderUnktyp) convertView.getTag();
                        break;
                    case 1:
                        viewHolderShaVid = (ViewHolderShaVid) convertView.getTag();
                        break;
                    case 0:
                        viewHolderShaText = (ViewHolderShaText) convertView.getTag();
                        break;
                }
            }

            if(type == 4) //原创视频
            {
                final UserDynamic.cardOriginalVideo dy = (UserDynamic.cardOriginalVideo) dyList.get(position);
                viewHolderOriVid.name.setText(Html.fromHtml("<b>" + dy.getOwnerName() + "</b>投稿了视频"));
                viewHolderOriVid.time.setText(dy.getDynamicTime());
                if(!dy.getDynamic().equals(""))
                {
                    viewHolderOriVid.text.setVisibility(View.VISIBLE);
                    viewHolderOriVid.text.setText(dy.getDynamic());
                }
                else viewHolderOriVid.text.setVisibility(View.GONE);
                viewHolderOriVid.imgtext.setText(dy.getVideoDuration() + "  " + dy.getVideoView() + "观看");
                viewHolderOriVid.title.setText(dy.getVideoTitle());
                if(dy.isLike) viewHolderOriVid.likei.setImageResource(R.drawable.icon_liked);
                else viewHolderOriVid.likei.setImageResource(R.drawable.icon_like);
                viewHolderOriVid.like.setText(String.valueOf(dy.getBeLiked()));
                viewHolderOriVid.head.setImageResource(R.drawable.img_default_head);
                viewHolderOriVid.img.setImageResource(R.drawable.img_default_vid);

                viewHolderOriVid.head.setTag(dy.getOwnerHead());
                viewHolderOriVid.img.setTag(dy.getVideoImg());
                final BitmapDrawable h = setImageFormWeb(dy.getOwnerHead());
                BitmapDrawable i = setImageFormWeb(dy.getVideoImg());
                if(h != null) viewHolderOriVid.head.setImageDrawable(h);
                if(i != null) viewHolderOriVid.img.setImageDrawable(i);

                viewHolderOriVid.lay.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(ctx, VideodetailsActivity.class);
                        intent.putExtra("aid", dy.getVideoAid());
                        startActivity(intent);
                    }
                });

                viewHolderOriVid.head.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(ctx, OtheruserActivity.class);
                        intent.putExtra("mid", dy.getOwnerUid());
                        startActivity(intent);
                    }
                });

                viewHolderOriVid.likebu.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        new Thread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                String s = userDynamic.likeDynamic(dy.getDynamicId(), dy.isLike ? "2" : "1");
                                if(s.equals(""))
                                {
                                    dy.isLike = !dy.isLike;
                                    dy.likeDynamic(dy.isLike ? 1 : -1);
                                    handler.post(runnableDynamicAddlist);
                                }
                                else
                                {
                                    Looper.prepare();
                                    Toast.makeText(ctx, (dy.isLike ? "取消" : "点赞") + "失败：\n" + s, Toast.LENGTH_SHORT).show();
                                    Looper.loop();
                                }
                            }
                        }).start();
                    }
                });

            }
            else if(type == 3)// 原创文字
            {
                final UserDynamic.cardOriginalText dy = (UserDynamic.cardOriginalText) dyList.get(position);
                viewHolderOriText.name.setText(dy.getUserName());
                viewHolderOriText.time.setText(dy.getDynamicTime());
                viewHolderOriText.text.setText(dy.getDynamicText());
                if(!dy.getTextImgCount().equals("0"))
                {
                    viewHolderOriText.textimg.setVisibility(View.VISIBLE);
                    viewHolderOriText.textimg.setText("查看共" + dy.getTextImgCount() + "张图片");
                }
                else viewHolderOriText.textimg.setVisibility(View.GONE);
                viewHolderOriText.reply.setText(String.valueOf(dy.getBeReply()));
                if(dy.isLike) viewHolderOriText.likei.setImageResource(R.drawable.icon_liked);
                else viewHolderOriText.likei.setImageResource(R.drawable.icon_like);
                viewHolderOriText.like.setText(String.valueOf(dy.getBeLiked()));
                viewHolderOriText.head.setImageResource(R.drawable.img_default_head);

                viewHolderOriText.head.setTag(dy.getUserHead());
                BitmapDrawable h = setImageFormWeb(dy.getUserHead());
                if(h != null) viewHolderOriText.head.setImageDrawable(h);

                viewHolderOriText.textimg.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(ctx, ImgActivity.class);
                        intent.putExtra("imgUrl", dy.getImgsSrc());
                        startActivity(intent);
                    }
                });

                viewHolderOriText.head.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(ctx, OtheruserActivity.class);
                        intent.putExtra("mid", dy.getUserUid());
                        startActivity(intent);
                    }
                });

                viewHolderOriText.replybu.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(ctx, ReplyActivity.class);
                        intent.putExtra("oid", dy.getDynamicId(1));
                        intent.putExtra("type", dy.getReplyType());
                        intent.putExtra("root", "");
                        startActivityForResult(intent, 0);
                    }
                });

                viewHolderOriText.likebu.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        new Thread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                String s = userDynamic.likeDynamic(dy.getDynamicId(2), dy.isLike ? "2" : "1");
                                if(s.equals(""))
                                {
                                    dy.isLike = !dy.isLike;
                                    dy.likeDynamic(dy.isLike ? 1 : -1);
                                    handler.post(runnableDynamicAddlist);
                                }
                                else
                                {
                                    Looper.prepare();
                                    Toast.makeText(ctx, (dy.isLike ? "取消" : "点赞") + "失败：\n" + s, Toast.LENGTH_SHORT).show();
                                    Looper.loop();
                                }
                            }
                        }).start();
                    }
                });
            }
            else if(type == 2) //未知类型
            {
                final UserDynamic.cardUnknow dy = (UserDynamic.cardUnknow) dyList.get(position);
                viewHolderUnktyp.name.setText(dy.getOwnerName());
                viewHolderUnktyp.time.setText(dy.getDynamicTime());
                viewHolderUnktyp.head.setImageResource(R.drawable.img_default_head);

                if(dy.getOwnerHead() != null)
                {
                    viewHolderUnktyp.head.setTag(dy.getOwnerHead());
                    BitmapDrawable h = setImageFormWeb(dy.getOwnerHead());
                    if(h != null) viewHolderUnktyp.head.setImageDrawable(h);
                }

                viewHolderUnktyp.head.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(ctx, OtheruserActivity.class);
                        intent.putExtra("mid", dy.getOwnerUid());
                        startActivity(intent);
                    }
                });
            }
            else if(type == 1) //转发视频
            {
                final UserDynamic.cardShareVideo dy = (UserDynamic.cardShareVideo) dyList.get(position);
                final UserDynamic.cardOriginalVideo sdy = (UserDynamic.cardOriginalVideo) userDynamic.getDynamicClass(dy.getOriginalVideo(), 1);
                viewHolderShaVid.name.setText(dy.getUserName());
                viewHolderShaVid.time.setText(dy.getDynamicTime());
                viewHolderShaVid.text.setText(dy.getDynamicText());
                viewHolderShaVid.sname.setText(sdy.getOwnerName());
                viewHolderShaVid.simgtext.setText(sdy.getVideoDuration() + "  " + sdy.getVideoView() + "观看");
                viewHolderShaVid.stitle.setText(sdy.getVideoTitle());
                viewHolderShaVid.reply.setText(String.valueOf(dy.getBeReply()));
                if(dy.isLike) viewHolderShaVid.likei.setImageResource(R.drawable.icon_liked);
                else viewHolderShaVid.likei.setImageResource(R.drawable.icon_like);
                viewHolderShaVid.like.setText(String.valueOf(dy.getBeLiked()));
                viewHolderShaVid.head.setImageResource(R.drawable.img_default_head);
                viewHolderShaVid.shead.setImageResource(R.drawable.img_default_head);
                viewHolderShaVid.simg.setImageResource(R.drawable.img_default_vid);

                viewHolderShaVid.head.setTag(dy.getUserHead());
                viewHolderShaVid.shead.setTag(sdy.getOwnerHead());
                viewHolderShaVid.simg.setTag(sdy.getVideoImg());
                BitmapDrawable h = setImageFormWeb(dy.getUserHead());
                BitmapDrawable o = setImageFormWeb(sdy.getOwnerHead());
                BitmapDrawable i = setImageFormWeb(sdy.getVideoImg());
                if(h != null) viewHolderShaVid.head.setImageDrawable(h);
                if(o != null) viewHolderShaVid.shead.setImageDrawable(o);
                if(i != null) viewHolderShaVid.simg.setImageDrawable(i);

                viewHolderShaVid.slay.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(ctx, VideodetailsActivity.class);
                        intent.putExtra("aid", sdy.getVideoAid());
                        startActivity(intent);
                    }
                });

                viewHolderShaVid.head.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(ctx, OtheruserActivity.class);
                        intent.putExtra("mid", dy.getUserUid());
                        startActivity(intent);
                    }
                });

                convertView.findViewById(R.id.lisv_share_user).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(ctx, OtheruserActivity.class);
                        intent.putExtra("mid", sdy.getOwnerUid());
                        startActivity(intent);
                    }
                });

                viewHolderShaVid.replybu.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(ctx, ReplyActivity.class);
                        intent.putExtra("oid", dy.getDynamicId());
                        intent.putExtra("type", dy.getReplyType());
                        intent.putExtra("root", "");
                        startActivityForResult(intent, 0);
                    }
                });

                viewHolderShaVid.likebu.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        new Thread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                String s = userDynamic.likeDynamic(dy.getDynamicId(), dy.isLike ? "2" : "1");
                                if(s.equals(""))
                                {
                                    dy.isLike = !dy.isLike;
                                    dy.likeDynamic(dy.isLike ? 1 : -1);
                                    handler.post(runnableDynamicAddlist);
                                }
                                else
                                {
                                    Looper.prepare();
                                    Toast.makeText(ctx, (dy.isLike ? "取消" : "点赞") + "失败：\n" + s, Toast.LENGTH_SHORT).show();
                                    Looper.loop();
                                }
                            }
                        }).start();
                    }
                });
            }
            else if(type == 0) //转发文字
            {
                final UserDynamic.cardShareText dy = (UserDynamic.cardShareText) dyList.get(position);
                final UserDynamic.cardOriginalText sdy = (UserDynamic.cardOriginalText) userDynamic.getDynamicClass(dy.getOriginalText(), 2);
                viewHolderShaText.name.setText(dy.getUserName());
                viewHolderShaText.time.setText(dy.getDynamicTime());
                viewHolderShaText.text.setText(dy.getDynamicText());
                viewHolderShaText.sname.setText(sdy.getUserName());
                viewHolderShaText.stext.setText(sdy.getDynamicText());
                if(!sdy.getTextImgCount().equals("0"))
                {
                    viewHolderShaText.stextimg.setVisibility(View.VISIBLE);
                    viewHolderShaText.stextimg.setText("查看共" + sdy.getTextImgCount() + "张图片");
                }
                else viewHolderShaText.stextimg.setVisibility(View.GONE);
                viewHolderShaText.reply.setText(String.valueOf(dy.getBeReply()));
                if(dy.isLike) viewHolderShaText.likei.setImageResource(R.drawable.icon_liked);
                else viewHolderShaText.likei.setImageResource(R.drawable.icon_like);
                viewHolderShaText.like.setText(String.valueOf(dy.getBeLiked()));
                viewHolderShaText.head.setImageResource(R.drawable.img_default_head);
                viewHolderShaText.shead.setImageResource(R.drawable.img_default_head);

                viewHolderShaText.head.setTag(dy.getUserHead());
                viewHolderShaText.shead.setTag(sdy.getUserHead());
                BitmapDrawable h = setImageFormWeb(dy.getUserHead());
                BitmapDrawable o = setImageFormWeb(sdy.getUserHead());
                if(h != null) viewHolderShaText.head.setImageDrawable(h);
                if(o != null) viewHolderShaText.shead.setImageDrawable(o);

                viewHolderShaText.stextimg.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(ctx, ImgActivity.class);
                        intent.putExtra("imgUrl", sdy.getImgsSrc());
                        startActivity(intent);
                    }
                });

                viewHolderShaText.head.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(ctx, OtheruserActivity.class);
                        intent.putExtra("mid", dy.getUserUid());
                        startActivity(intent);
                    }
                });

                convertView.findViewById(R.id.list_share_user).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(ctx, OtheruserActivity.class);
                        intent.putExtra("mid", sdy.getUserUid());
                        startActivity(intent);
                    }
                });

                viewHolderShaText.replybu.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(ctx, ReplyActivity.class);
                        intent.putExtra("oid", dy.getDynamicId());
                        intent.putExtra("type", dy.getReplyType());
                        intent.putExtra("root", "");
                        startActivityForResult(intent, 0);
                    }
                });

                viewHolderShaText.likebu.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        new Thread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                String s = userDynamic.likeDynamic(dy.getDynamicId(), dy.isLike ? "2" : "1");
                                if(s.equals(""))
                                {
                                    dy.isLike = !dy.isLike;
                                    dy.likeDynamic(dy.isLike ? 1 : -1);
                                    handler.post(runnableDynamicAddlist);
                                }
                                else
                                {
                                    Looper.prepare();
                                    Toast.makeText(ctx, (dy.isLike ? "取消" : "点赞") + "失败：\n" + s, Toast.LENGTH_SHORT).show();
                                    Looper.loop();
                                }
                            }
                        }).start();
                    }
                });
            }
            return convertView;
        }

        BitmapDrawable setImageFormWeb(String url)
        {
            if(mImageCache.get(url) != null)
            {
                return mImageCache.get(url);
            }
            else
            {
                mAdapter.ImageTask it = new mAdapter.ImageTask();
                it.execute(url);
                return null;
            }
        }

        class ViewHolderOriVid
        {
            RelativeLayout lay;
            ImageView head;
            TextView name;
            TextView time;
            ExpandableTextView text;
            ImageView img;
            TextView imgtext;
            TextView title;
            LinearLayout likebu;
            ImageView likei;
            TextView like;
        }

        class ViewHolderOriText
        {
            ImageView head;
            TextView name;
            TextView time;
            ExpandableTextView text;
            TextView textimg;
            LinearLayout replybu;
            TextView reply;
            LinearLayout likebu;
            ImageView likei;
            TextView like;
        }

        class ViewHolderUnktyp
        {
            ImageView head;
            TextView name;
            TextView time;
        }

        class ViewHolderShaVid
        {
            ImageView head;
            TextView name;
            TextView time;
            ExpandableTextView text;
            RelativeLayout slay;
            ImageView shead;
            TextView sname;
            ImageView simg;
            TextView simgtext;
            TextView stitle;
            LinearLayout replybu;
            TextView reply;
            LinearLayout likebu;
            ImageView likei;
            TextView like;
        }

        class ViewHolderShaText
        {
            ImageView head;
            TextView name;
            TextView time;
            ExpandableTextView text;
            ImageView shead;
            TextView sname;
            ExpandableTextView stext;
            TextView stextimg;
            LinearLayout replybu;
            TextView reply;
            LinearLayout likebu;
            ImageView likei;
            TextView like;
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
                    BitmapDrawable db = new BitmapDrawable(uiDynamicListView.getResources(), bitmap);
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
                // 通过Tag找到我们需要的ImageView，如果该ImageView所在的item已被移出页面，就会直接返回null
                ImageView iv = uiDynamicListView.findViewWithTag(imageUrl);
                if(iv != null && result != null)
                {
                    iv.setImageDrawable(result);
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
                int realWith = 170;
                int realHeight = 170;

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

    }

    private Object get(String url, int mode) throws IOException
    {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).build();
        Request.Builder requestb = new Request.Builder().url(url).header("Referer", "https://www.bilibili.com/anime/timeline").addHeader("Accept", "*/*").addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        Request request = requestb.build();
        Response response = client.newCall(request).execute();

        if(response.isSuccessful())
        {
            if(mode == 1) return response.body().string();
            else if(mode == 2)
            {
                byte[] buffer = readStream(response.body().byteStream());
                return BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
            }
        }
        return null;
    }

    private byte[] readStream(InputStream inStream) throws IOException
    {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1)
        {
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        inStream.close();
        return outStream.toByteArray();
    }
}
