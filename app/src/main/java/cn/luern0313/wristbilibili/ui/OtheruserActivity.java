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
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import cn.carbs.android.expandabletextview.library.ExpandableTextView;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.OthersUserApi;
import cn.luern0313.wristbilibili.api.UserDynamicApi;
import cn.luern0313.wristbilibili.widget.ImageDownloader;
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
public class OtheruserActivity extends Activity
{
    Context ctx;
    Intent intent;
    LayoutInflater inflater;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    UserDynamicApi userDynamicApi;

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

    TextView uiTitleTextView;
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
    ArrayList<OthersUserApi.People> followArrayList = new ArrayList<>();
    ArrayList<OthersUserApi.People> fansArrayList = new ArrayList<>();
    mAdapter mAdapter;
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

        othersUserApi = new OthersUserApi(sharedPreferences.getString("cookies", ""), sharedPreferences.getString("csrf", ""), intent.getStringExtra("mid"));
        uiTitleTextView = findViewById(R.id.ou_title_title);
        uiViewpager = findViewById(R.id.ou_viewpager);
        uiLoading = findViewById(R.id.ou_loading_img);
        dynamicLayoutLoading = inflater.inflate(R.layout.widget_dy_loading, null);
        followLayoutLoading = inflater.inflate(R.layout.widget_dy_loading, null);
        fansLayoutLoading = inflater.inflate(R.layout.widget_dy_loading, null);

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
                    mAdapter = new mAdapter(inflater, dynamicArrayList);
                    uiDynamicListView.setAdapter(mAdapter);
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
                    mAdapter.notifyDataSetChanged();
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
                    Log.i("bilibili", "123");
                    ((TextView) dynamicLayoutLoading.findViewById(R.id.wid_dy_load_text)).setText("  没有更多了...");
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
                    ((TextView) followLayoutLoading.findViewById(R.id.wid_dy_load_text)).setText("无法查看更多了");
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
                    ((TextView) fansLayoutLoading.findViewById(R.id.wid_dy_load_text)).setText("无法查看更多了");
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
                    ((TextView) dynamicLayoutLoading.findViewById(R.id.wid_dy_load_button)).setText("好像没有网络...\n检查下网络？");
                    dynamicLayoutLoading.findViewById(R.id.wid_dy_load_button).setVisibility(View.VISIBLE);
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
                    ((TextView) followLayoutLoading.findViewById(R.id.wid_dy_load_button)).setText("好像没有网络...\n检查下网络？");
                    followLayoutLoading.findViewById(R.id.wid_dy_load_button).setVisibility(View.VISIBLE);
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
                    ((TextView) fansLayoutLoading.findViewById(R.id.wid_dy_load_button)).setText("好像没有网络...\n检查下网络？");
                    fansLayoutLoading.findViewById(R.id.wid_dy_load_button).setVisibility(View.VISIBLE);
                    isFansLoading = false;
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
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
                if(position == 0) titleAnim("用户");
                else if(position == 1) titleAnim("动态");
                else if(position == 2) titleAnim("关注");
                else if(position == 3) titleAnim("粉丝");
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
                    ArrayList<OthersUserApi.People> followList = othersUserApi.getUserFollow();
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
                    ArrayList<OthersUserApi.People> fansList = othersUserApi.getUserFans();
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

    private String getView(String v)
    {
        int view = Integer.valueOf(v);
        if(view > 10000) return view / 1000 / 10.0 + "万";
        else return String.valueOf(view);
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
            if(dyList.get(position) instanceof UserDynamicApi.cardOriginalVideo) return 4;
            else if(dyList.get(position) instanceof UserDynamicApi.cardOriginalText) return 3;
            else if(dyList.get(position) instanceof UserDynamicApi.cardUnknow) return 2;
            else if(dyList.get(position) instanceof UserDynamicApi.cardShareVideo) return 1;
            else if(dyList.get(position) instanceof UserDynamicApi.cardShareText) return 0;
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
                        viewHolderOriText.sharei = convertView.findViewById(R.id.liot_sharei);
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
                        viewHolderShaVid.sharei = convertView.findViewById(R.id.lisv_sharei);
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
                        viewHolderShaText.sharei = convertView.findViewById(R.id.list_sharei);
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
                final UserDynamicApi.cardOriginalVideo dy = (UserDynamicApi.cardOriginalVideo) dyList.get(position);
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
                });

            }
            else if(type == 3)// 原创文字
            {
                final UserDynamicApi.cardOriginalText dy = (UserDynamicApi.cardOriginalText) dyList.get(position);
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

                viewHolderOriText.sharei.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(ctx, SendDynamicActivity.class);
                        intent.putExtra("is_share", true);
                        intent.putExtra("share_up", dy.getUserName());
                        intent.putExtra("share_img", Integer.valueOf(dy.getTextImgCount()) == 0 ? "" : dy.getImgsSrc()[0]);
                        intent.putExtra("share_title", dy.getDynamicText());
                        intent.putExtra("share_dyid", dy.getDynamicId(2));
                        startActivityForResult(intent, 0);
                    }
                });

                viewHolderOriText.replybu.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(ctx, CheckreplyActivity.class);
                        intent.putExtra("oid", dy.getDynamicId(1));
                        intent.putExtra("type", dy.getReplyType());
                        intent.putExtra("root", "");
                        startActivity(intent);
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
                });
            }
            else if(type == 2) //未知类型
            {
                final UserDynamicApi.cardUnknow dy = (UserDynamicApi.cardUnknow) dyList.get(position);
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
                final UserDynamicApi.cardShareVideo dy = (UserDynamicApi.cardShareVideo) dyList.get(position);
                final UserDynamicApi.cardOriginalVideo sdy = (UserDynamicApi.cardOriginalVideo) userDynamicApi.getDynamicClass(dy.getOriginalVideo(), 1);
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

                viewHolderShaVid.sharei.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
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
                });

                viewHolderShaVid.replybu.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(ctx, CheckreplyActivity.class);
                        intent.putExtra("oid", dy.getDynamicId());
                        intent.putExtra("type", dy.getReplyType());
                        intent.putExtra("root", "");
                        startActivity(intent);
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
                });
            }
            else if(type == 0) //转发文字
            {
                final UserDynamicApi.cardShareText dy = (UserDynamicApi.cardShareText) dyList.get(position);
                final UserDynamicApi.cardOriginalText sdy = (UserDynamicApi.cardOriginalText) userDynamicApi.getDynamicClass(dy.getOriginalText(), 2);
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

                viewHolderShaText.sharei.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
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
                });

                viewHolderShaText.replybu.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(ctx, CheckreplyActivity.class);
                        intent.putExtra("oid", dy.getDynamicId());
                        intent.putExtra("type", dy.getReplyType());
                        intent.putExtra("root", "");
                        startActivity(intent);
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
                });
            }
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
                ImageTask it = new ImageTask();
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
            ImageView sharei;
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
            ImageView sharei;
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
            ImageView sharei;
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
                    bitmap = ImageDownloader.downloadImage(imageUrl);
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
        }
    }

    class pAdapter extends BaseAdapter
    {
        private LayoutInflater mInflater;

        private LruCache<String, BitmapDrawable> mImageCache;

        private ArrayList<OthersUserApi.People> ppList;
        private int mode;

        public pAdapter(LayoutInflater inflater, ArrayList<OthersUserApi.People> ppList, int mode)
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

            final OthersUserApi.People pp = ppList.get(position);
            viewHolder.img.setImageResource(R.drawable.img_default_head);
            viewHolder.name.setText(pp.nmae);
            if(pp.vip == 2)
                viewHolder.name.setTextColor(getResources().getColor(R.color.textColor3));
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
                    Intent intent = new Intent(ctx, OtheruserActivity.class);
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
                ImageTask it = new ImageTask();
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

            @Override
            protected BitmapDrawable doInBackground(String... params)
            {
                try
                {
                    imageUrl = params[0];
                    Bitmap bitmap = null;
                    bitmap = ImageDownloader.downloadImage(imageUrl);
                    BitmapDrawable db = new BitmapDrawable(uiViewpager.findViewWithTag(mode).findViewById(R.id.ou_pp_listview).getResources(), bitmap);
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
