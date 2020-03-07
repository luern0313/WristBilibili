package cn.luern0313.wristbilibili.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.DynamicAdapter;
import cn.luern0313.wristbilibili.api.OthersUserApi;
import cn.luern0313.wristbilibili.api.SendDynamicApi;
import cn.luern0313.wristbilibili.api.UserDynamicApi;
import cn.luern0313.wristbilibili.models.OthersUserModel;
import cn.luern0313.wristbilibili.util.ImageDownloaderUtil;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 被 luern0313 创建于 不知道什么时候.
 * 这个部分做的很！不！优！雅！！
 * 该api去获取去解析的直接在activity文件里完成了
 * 现在要拓展功能才知道当初不该偷懒
 */
public class OtherUserActivity extends AppCompatActivity
{
    Context ctx;
    Intent intent;
    LayoutInflater inflater;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    UserDynamicApi userDynamicApi;
    SendDynamicApi sendDynamicApi;
    DynamicAdapter.DynamicAdapterListener dynamicAdapterListener;

    OthersUserApi othersUserApi;
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
    Runnable runnableFollowUi;
    Runnable runnableFansUi;
    Runnable runnableDynamicAddlist;
    Runnable runnableFollowAddlist;
    Runnable runnableFansAddlist;
    Runnable runnableDynamicMoreNomore;
    Runnable runnableFollowMoreNomore;
    Runnable runnableFansMoreNomore;
    Runnable runnableDynamicMoreErr;
    Runnable runnableFollowMoreErr;
    Runnable runnableFansMoreErr;

    ViewFlipper uiTitle;
    ViewPager uiViewpager;
    ImageView uiLoading;
    View dynamicLayoutLoading;
    View followLayoutLoading;
    View fansLayoutLoading;

    ImageView uiImg;
    TextView uiName;
    TextView uiFollow;
    TextView uiLv;
    TextView uiAnth;
    TextView uiSign;
    TextView uiHowFans;
    TextView uiHowFollow;
    TextView uiOther;
    ListView uiDynamicListView;
    ListView uiPeopleListView;

    ArrayList<Object> dynamicArrayList = new ArrayList<>();
    ArrayList<OthersUserModel> followArrayList = new ArrayList<>();
    ArrayList<OthersUserModel> fansArrayList = new ArrayList<>();
    DynamicAdapter dynamicAdapter;
    pAdapter followAdapter;
    pAdapter fansAdapter;

    boolean isDynamicLoading = true;
    boolean isFollowLoading = true;
    boolean isFansLoading = true;

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

        othersUserApi = new OthersUserApi(sharedPreferences.getString("cookies", ""),
                                          sharedPreferences.getString("csrf", ""),
                                          intent.getStringExtra("mid"));
        sendDynamicApi = new SendDynamicApi(MainActivity.sharedPreferences.getString("cookies", ""),
                                            MainActivity.sharedPreferences.getString("mid", ""),
                                            MainActivity.sharedPreferences.getString("csrf", ""));
        uiTitle = findViewById(R.id.ou_title_title);
        uiViewpager = findViewById(R.id.ou_viewpager);
        uiLoading = findViewById(R.id.ou_loading_img);
        dynamicLayoutLoading = inflater.inflate(R.layout.widget_loading, null);
        followLayoutLoading = inflater.inflate(R.layout.widget_loading, null);
        fansLayoutLoading = inflater.inflate(R.layout.widget_loading, null);

        uiViewpager.setOffscreenPageLimit(3);

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
                        uiName.setTextColor(getResources().getColor(R.color.gray_44));
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
                    uiHowFans.setText("粉丝：" + getView(otherUserCardJson.optString("fans")));
                    uiHowFollow.setText("关注：" + getView(otherUserCardJson.optString("friend")));
                    uiOther.setText("投稿：" + otherUserJson.optString("archive_count"));
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
                catch (Exception e)
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
                    dynamicAdapter = new DynamicAdapter(inflater, dynamicArrayList, uiDynamicListView, dynamicAdapterListener);
                    uiDynamicListView.setAdapter(dynamicAdapter);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableFollowUi = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    ((ListView) uiViewpager.findViewWithTag(2).findViewById(R.id.ou_pp_listview)).setAdapter(followAdapter);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableFansUi = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    ((ListView) uiViewpager.findViewWithTag(3).findViewById(R.id.ou_pp_listview)).setAdapter(fansAdapter);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableDynamicAddlist = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    dynamicAdapter.notifyDataSetChanged();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableFollowAddlist = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    followAdapter.notifyDataSetChanged();
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableFansAddlist = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    fansAdapter.notifyDataSetChanged();
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableDynamicMoreNomore = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    ((TextView) dynamicLayoutLoading.findViewById(R.id.wid_load_text)).setText("  没有更多了...");
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableFollowMoreNomore = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    ((TextView) followLayoutLoading.findViewById(R.id.wid_load_text)).setText("无法查看更多了");
                    isFollowLoading = true;
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableFansMoreNomore = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    ((TextView) fansLayoutLoading.findViewById(R.id.wid_load_text)).setText("无法查看更多了");
                    isFansLoading = true;
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableDynamicMoreErr = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    ((TextView) dynamicLayoutLoading.findViewById(R.id.wid_load_button)).setText("好像没有网络...\n检查下网络？");
                    dynamicLayoutLoading.findViewById(R.id.wid_load_button).setVisibility(View.VISIBLE);
                    isDynamicLoading = false;
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableFollowMoreErr = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    ((TextView) followLayoutLoading.findViewById(R.id.wid_load_button)).setText("好像没有网络...\n检查下网络？");
                    followLayoutLoading.findViewById(R.id.wid_load_button).setVisibility(View.VISIBLE);
                    isFollowLoading = false;
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnableFansMoreErr = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    ((TextView) fansLayoutLoading.findViewById(R.id.wid_load_button)).setText("好像没有网络...\n检查下网络？");
                    fansLayoutLoading.findViewById(R.id.wid_load_button).setVisibility(View.VISIBLE);
                    isFansLoading = false;
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        dynamicAdapterListener = new DynamicAdapter.DynamicAdapterListener()
        {
            @Override
            public void onClick(int viewId, int position, int mode)
            {
                onViewClick(viewId, position, mode);
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
                return 4;
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
            public Object instantiateItem(ViewGroup container, final int position)
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
                    uiHowFans = v.findViewById(R.id.ou_howfans);
                    uiHowFollow = v.findViewById(R.id.ou_howfollow);
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

                    uiHowFollow.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            uiViewpager.setCurrentItem(2, true);
                        }
                    });

                    uiHowFans.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            uiViewpager.setCurrentItem(3, true);
                        }
                    });

                    uiOther.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            uiViewpager.setCurrentItem(1, true);
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
                                            othersUserApi.unfollow();
                                            handler.post(runnableUnfollow);
                                        }
                                        else
                                        {
                                            othersUserApi.follow();
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
                                otherUserJson = new JSONObject(othersUserApi.getOtheruserInfo()).getJSONObject("data");
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
                else if(position == 1)
                {
                    View v = inflater.inflate(R.layout.viewpager_ou_dynamic, null);
                    v.setTag(1);
                    uiDynamicListView = v.findViewById(R.id.ou_dy_listview);
                    uiDynamicListView.setEmptyView(v.findViewById(R.id.ou_dy_nonthing));
                    uiDynamicListView.addFooterView(dynamicLayoutLoading);

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
                                getMoreDynamic();
                        }
                    });

                    userDynamicApi = new UserDynamicApi(MainActivity.sharedPreferences.getString("cookies", ""), MainActivity.sharedPreferences.getString("csrf", ""), MainActivity.sharedPreferences.getString("mid", ""), intent.getStringExtra("mid"), false);
                    isDynamicLoading = true;
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                userDynamicApi.getDynamic();
                                dynamicArrayList = userDynamicApi.getDynamicList();
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
                else
                {
                    View v = inflater.inflate(R.layout.viewpager_ou_people, null);
                    v.setTag(position);
                    uiPeopleListView = v.findViewById(R.id.ou_pp_listview);
                    uiPeopleListView.setEmptyView(v.findViewById(R.id.ou_pp_nonthing));
                    uiPeopleListView.addFooterView(position == 2 ? followLayoutLoading : fansLayoutLoading);

                    uiPeopleListView.setOnScrollListener(new AbsListView.OnScrollListener()
                    {
                        @Override
                        public void onScrollStateChanged(AbsListView view, int scrollState)
                        {
                        }

                        @Override
                        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
                        {
                            if(visibleItemCount + firstVisibleItem == totalItemCount && (uiViewpager.getCurrentItem() == 2 ? !isFollowLoading : !isFansLoading))
                                if(uiViewpager.getCurrentItem() == 2)
                                    getMoreFollow();
                                else
                                    getMoreFans();
                        }
                    });

                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                if(position == 2)
                                {
                                    isFollowLoading = true;
                                    followArrayList.addAll(othersUserApi.getUserFollow());
                                    followAdapter = new pAdapter(inflater, followArrayList, 2);
                                    isFollowLoading = false;
                                    handler.post(runnableFollowUi);
                                }
                                else
                                {
                                    isFansLoading = true;
                                    fansArrayList.addAll(othersUserApi.getUserFans());
                                    fansAdapter = new pAdapter(inflater, fansArrayList, 3);
                                    isFansLoading = false;
                                    handler.post(runnableFansUi);
                                }
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                                if(position == 2)
                                {
                                    isFollowLoading = false;
                                    followArrayList = new ArrayList<>();
                                }
                                else
                                {
                                    isFansLoading = false;
                                }
                            }
                        }
                    }).start();

                    container.addView(v);
                    return position;
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
                if(uiTitle.getDisplayedChild() != position)
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
                    userDynamicApi.getHistoryDynamic();
                    ArrayList<Object> d = userDynamicApi.getDynamicList();
                    isDynamicLoading = false;
                    if(d != null && d.size() != 0)
                    {
                        dynamicArrayList.addAll(d);
                        handler.post(runnableDynamicAddlist);
                    }
                    else
                    {
                        handler.post(runnableDynamicMoreNomore);
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    handler.post(runnableDynamicMoreErr);
                }
            }
        }).start();
    }

    void getMoreFollow()
    {
        isFollowLoading = true;
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    ArrayList<OthersUserModel> followList = othersUserApi.getUserFollow();
                    isFollowLoading = false;
                    if(followList != null && followList.size() != 0)
                    {
                        followArrayList.addAll(followList);
                        handler.post(runnableFollowAddlist);
                    }
                    else
                        handler.post(runnableFollowMoreNomore);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    handler.post(runnableFollowMoreErr);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    handler.post(runnableFollowMoreNomore);
                }
            }
        }).start();
    }

    void getMoreFans()
    {
        isFansLoading = true;
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    ArrayList<OthersUserModel> fansList = othersUserApi.getUserFans();
                    isFansLoading = false;
                    if(fansList != null && fansList.size() != 0)
                    {
                        fansArrayList.addAll(fansList);
                        handler.post(runnableFansAddlist);
                    }
                    else
                        handler.post(runnableFansMoreNomore);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    handler.post(runnableFansMoreErr);
                }
                catch (NullPointerException e)
                {
                    e.printStackTrace();
                    handler.post(runnableFansMoreNomore);
                }
            }
        }).start();
    }

    private String getView(String v)
    {
        int view = Integer.valueOf(v);
        if(view > 10000) return view / 1000 / 10.0 + "万";
        else return String.valueOf(view);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        //dyid都是传过去再传回来
        //我王境泽传数据就是乱死！也不建多余的变量！（没有真香）
        if(requestCode == 0 && resultCode == 0 && data != null)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(!data.getStringExtra("text").equals(""))
                        {
                            String result;
                            if(!data.getBooleanExtra("is_share", false))
                                result = sendDynamicApi.sendDynamic(data.getStringExtra("text"));
                            else
                                result = sendDynamicApi.sendDynamicWithDynamic(data.getStringExtra("share_dyid"), data.getStringExtra("text"));
                            if(result.equals(""))
                            {
                                Looper.prepare();
                                Toast.makeText(ctx, "发送成功！", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                            else
                            {
                                Looper.prepare();
                                Toast.makeText(ctx, "发送失败，" + result, Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        Looper.prepare();
                        Toast.makeText(ctx, "发送失败，请检查网络？", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }
            }).start();
        }
    }

    void onViewClick(int id, int position, int mode)
    {
        if(mode == 4)
        {
            final UserDynamicApi.cardOriginalVideo dy = (UserDynamicApi.cardOriginalVideo) dynamicArrayList.get(position);
            if(id == R.id.liov_lay)
            {
                Intent intent = new Intent(ctx, VideodetailsActivity.class);
                intent.putExtra("aid", dy.getVideoAid());
                startActivity(intent);
            }
            else if(id == R.id.liov_head)
            {
                Intent intent = new Intent(ctx, OtherUserActivity.class);
                intent.putExtra("mid", dy.getOwnerUid());
                startActivity(intent);
            }
            else if(id == R.id.liov_likebu)
            {
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        String s = userDynamicApi.likeDynamic(dy.getDynamicId(), dy.isLike ? "2" : "1");
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
        }
        else if(mode == 3)
        {
            final UserDynamicApi.cardOriginalText dy = (UserDynamicApi.cardOriginalText) dynamicArrayList.get(position);
            if(id == R.id.liot_textimg)
            {
                Intent intent = new Intent(ctx, ImgActivity.class);
                intent.putExtra("imgUrl", dy.getImgsSrc());
                startActivity(intent);
            }
            else if(id == R.id.liot_head)
            {
                Intent intent = new Intent(ctx, OtherUserActivity.class);
                intent.putExtra("mid", dy.getUserUid());
                startActivity(intent);
            }
            else if(id == R.id.liot_sharei)
            {
                Intent intent = new Intent(ctx, SendDynamicActivity.class);
                intent.putExtra("is_share", true);
                intent.putExtra("share_up", dy.getUserName());
                intent.putExtra("share_img", Integer.valueOf(dy.getTextImgCount()) == 0 ? "" : dy.getImgsSrc()[0]);
                intent.putExtra("share_title", dy.getDynamicText());
                intent.putExtra("share_dyid", dy.getDynamicId(2));
                startActivityForResult(intent, 0);
            }
            else if(id == R.id.liot_replybu)
            {
                Intent intent = new Intent(ctx, CheckreplyActivity.class);
                intent.putExtra("oid", dy.getDynamicId(1));
                intent.putExtra("type", dy.getReplyType());
                intent.putExtra("root", "");
                startActivity(intent);
            }
            else if(id == R.id.liot_likebu)
            {
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        String s = userDynamicApi.likeDynamic(dy.getDynamicId(2), dy.isLike ? "2" : "1");
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
        }
        else if(mode == 2)
        {
            final UserDynamicApi.cardUnknow dy = (UserDynamicApi.cardUnknow) dynamicArrayList.get(position);
            if(id == R.id.liuk_head)
            {
                Intent intent = new Intent(ctx, OtherUserActivity.class);
                intent.putExtra("mid", dy.getOwnerUid());
                startActivity(intent);
            }
        }
        else if(mode == 1)
        {
            final UserDynamicApi.cardShareVideo dy = (UserDynamicApi.cardShareVideo) dynamicArrayList.get(position);
            final UserDynamicApi.cardOriginalVideo sdy = dy.getOriginalVideo();
            if(id == R.id.lisv_share_lay)
            {
                Intent intent = new Intent(ctx, VideodetailsActivity.class);
                intent.putExtra("aid", sdy.getVideoAid());
                startActivity(intent);
            }
            else if(id == R.id.lisv_head)
            {
                Intent intent = new Intent(ctx, OtherUserActivity.class);
                intent.putExtra("mid", dy.getUserUid());
                startActivity(intent);
            }
            else if(id == R.id.lisv_share_user)
            {
                Intent intent = new Intent(ctx, OtherUserActivity.class);
                intent.putExtra("mid", sdy.getOwnerUid());
                startActivity(intent);
            }
            else if(id == R.id.lisv_sharei)
            {
                Intent intent = new Intent(ctx, SendDynamicActivity.class);
                intent.putExtra("is_share", true);
                intent.putExtra("share_text", "//@" + dy.getUserName() + ":" + dy.getDynamicText());
                intent.putExtra("share_up", sdy.getOwnerName());
                intent.putExtra("share_img", sdy.getVideoImg());
                intent.putExtra("share_title", sdy.getVideoTitle());
                intent.putExtra("share_dyid", dy.getDynamicId());
                startActivityForResult(intent, 0);
            }
            else if(id == R.id.lisv_replybu)
            {
                Intent intent = new Intent(ctx, CheckreplyActivity.class);
                intent.putExtra("oid", dy.getDynamicId());
                intent.putExtra("type", dy.getReplyType());
                intent.putExtra("root", "");
                startActivity(intent);
            }
            else if(id == R.id.lisv_likebu)
            {
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        String s = userDynamicApi.likeDynamic(dy.getDynamicId(), dy.isLike ? "2" : "1");
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
        }
        else if(mode == 0)
        {
            final UserDynamicApi.cardShareText dy = (UserDynamicApi.cardShareText) dynamicArrayList.get(position);
            final UserDynamicApi.cardOriginalText sdy = dy.getOriginalText();
            if(id == R.id.list_share_textimg)
            {
                Intent intent = new Intent(ctx, ImgActivity.class);
                intent.putExtra("imgUrl", sdy.getImgsSrc());
                startActivity(intent);
            }
            else if(id == R.id.list_head)
            {
                Intent intent = new Intent(ctx, OtherUserActivity.class);
                intent.putExtra("mid", dy.getUserUid());
                startActivity(intent);
            }
            else if(id == R.id.list_share_user)
            {
                Intent intent = new Intent(ctx, OtherUserActivity.class);
                intent.putExtra("mid", sdy.getUserUid());
                startActivity(intent);
            }
            else if(id == R.id.list_sharei)
            {
                Intent intent = new Intent(ctx, SendDynamicActivity.class);
                intent.putExtra("is_share", true);
                intent.putExtra("share_text", "//@" + dy.getUserName() + ":" + dy.getDynamicText());
                intent.putExtra("share_up", sdy.getUserName());
                intent.putExtra("share_img", Integer.valueOf(sdy.getTextImgCount()) == 0 ? "" : sdy.getImgsSrc()[0]);
                intent.putExtra("share_title", sdy.getDynamicText());
                intent.putExtra("share_dyid", dy.getDynamicId());
                startActivityForResult(intent, 0);
            }
            else if(id == R.id.list_replybu)
            {
                Intent intent = new Intent(ctx, CheckreplyActivity.class);
                intent.putExtra("oid", dy.getDynamicId());
                intent.putExtra("type", dy.getReplyType());
                intent.putExtra("root", "");
                startActivity(intent);
            }
            else if(id == R.id.list_likebu)
            {
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        String s = userDynamicApi.likeDynamic(dy.getDynamicId(), dy.isLike ? "2" : "1");
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
        }
    }

    class pAdapter extends BaseAdapter
    {
        private LayoutInflater mInflater;

        private LruCache<String, BitmapDrawable> mImageCache;

        private ArrayList<OthersUserModel> ppList;
        private int mode;

        public pAdapter(LayoutInflater inflater, ArrayList<OthersUserModel> ppList, int mode)
        {
            mInflater = inflater;
            this.ppList = ppList;
            this.mode = mode;

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
            return ppList.size();
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
        public View getView(int position, View convertView, ViewGroup viewGroup)
        {
            ViewHolder viewHolder = null;
            if(convertView == null)
            {
                convertView = mInflater.inflate(R.layout.item_ou_people, null);
                viewHolder = new ViewHolder();
                convertView.setTag(viewHolder);

                viewHolder.lay = convertView.findViewById(R.id.oupp_lay);
                viewHolder.img = convertView.findViewById(R.id.oupp_face);
                viewHolder.name = convertView.findViewById(R.id.oupp_name);
                viewHolder.sign = convertView.findViewById(R.id.oupp_sign);
                viewHolder.time = convertView.findViewById(R.id.oupp_time);
            }
            else
            {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final OthersUserModel pp = ppList.get(position);
            viewHolder.img.setImageResource(R.drawable.img_default_head);
            viewHolder.name.setText(pp.nmae);
            if(pp.vip == 2)
                viewHolder.name.setTextColor(getResources().getColor(R.color.gray_44));
            else
                viewHolder.name.setTextColor(getResources().getColor(R.color.colorAccent));
            String s = pp.verifyName.equals("") ? pp.sign : pp.verifyName;
            if(!s.equals(""))
            {
                viewHolder.sign.setVisibility(View.VISIBLE);
                viewHolder.sign.setText(s);
            }
            else
                viewHolder.sign.setVisibility(View.GONE);
            viewHolder.time.setText((mode == 2 ? "关注于：" : "粉于：") + pp.mtime);
            switch (pp.verifyType)
            {
                case "0":
                    convertView.findViewById(R.id.oupp_ver_1).setVisibility(View.VISIBLE);
                    convertView.findViewById(R.id.oupp_ver_2).setVisibility(View.GONE);
                    break;
                case "1":
                    convertView.findViewById(R.id.oupp_ver_1).setVisibility(View.GONE);
                    convertView.findViewById(R.id.oupp_ver_2).setVisibility(View.VISIBLE);
                    break;
                default:
                    convertView.findViewById(R.id.oupp_ver_1).setVisibility(View.GONE);
                    convertView.findViewById(R.id.oupp_ver_2).setVisibility(View.GONE);
                    break;
            }
            viewHolder.img.setTag(pp.face);
            BitmapDrawable f = setImageFormWeb(pp.face);
            if(f != null) viewHolder.img.setImageDrawable(f);

            viewHolder.lay.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(ctx, OtherUserActivity.class);
                    intent.putExtra("mid", pp.uid);
                    startActivity(intent);
                }
            });

            return convertView;
        }

        BitmapDrawable setImageFormWeb(String url)
        {
            if(url != null && mImageCache.get(url) != null)
            {
                return mImageCache.get(url);
            }
            else
            {
                ImageTask it = new ImageTask((ListView) uiViewpager.findViewWithTag(mode).findViewById(R.id.ou_pp_listview));
                it.execute(url);
                return null;
            }
        }

        class ViewHolder
        {
            RelativeLayout lay;
            CircleImageView img;
            TextView name;
            TextView sign;
            TextView time;
        }

        class ImageTask extends AsyncTask<String, Void, BitmapDrawable>
        {
            private String imageUrl;
            private Resources listViewResources;

            ImageTask(ListView listView)
            {
                this.listViewResources = listView.getResources();
            }

            @Override
            protected BitmapDrawable doInBackground(String... params)
            {
                try
                {
                    imageUrl = params[0];
                    Bitmap bitmap = null;
                    bitmap = ImageDownloaderUtil.downloadImage(imageUrl);
                    BitmapDrawable db = new BitmapDrawable(listViewResources, bitmap);
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
                ImageView iv = (uiViewpager.findViewWithTag(mode).findViewById(R.id.ou_pp_listview).findViewWithTag(imageUrl));
                if(iv != null && result != null)
                {
                    iv.setImageDrawable(result);
                }
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
