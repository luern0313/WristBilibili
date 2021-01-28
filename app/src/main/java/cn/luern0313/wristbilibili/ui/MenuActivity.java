package cn.luern0313.wristbilibili.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.luern0313.lson.LsonUtil;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.MenuAdapter;
import cn.luern0313.wristbilibili.api.UserInfoApi;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;

public class MenuActivity extends BaseActivity
{
    Context ctx;
    LayoutInflater inflater;

    ListView uiListView;
    MenuAdapter menuAdapter;
    MenuAdapter.MenuAdapterListener menuAdapterListener;
    View layoutMenuHeader;

    TextView uiUserName;
    TextView uiUserCoin;
    TextView uiUserLV;
    ImageView uiUserHead;
    ImageView uiUserVip;

    Handler handler = new Handler();
    Runnable runnableUi;

    Bitmap head;
    private Intent resultIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        ctx = this;
        inflater = getLayoutInflater();

        uiListView = findViewById(R.id.menu_listview);
        layoutMenuHeader = inflater.inflate(R.layout.widget_menu_header, null);
        uiListView.addHeaderView(layoutMenuHeader);

        menuAdapterListener = this::onClick;

        uiUserName = layoutMenuHeader.findViewById(R.id.menu_username);
        uiUserCoin = layoutMenuHeader.findViewById(R.id.menu_user_coin);
        uiUserLV = layoutMenuHeader.findViewById(R.id.menu_user_lv);
        uiUserHead = layoutMenuHeader.findViewById(R.id.menu_user_img);
        uiUserVip = layoutMenuHeader.findViewById(R.id.menu_user_vip);

        uiUserName.setText(SharedPreferencesUtil.getString(SharedPreferencesUtil.userName, getString(R.string.menu_default_login)));
        uiUserCoin.setText(String.format(getString(R.string.menu_coin), SharedPreferencesUtil.getString(SharedPreferencesUtil.userCoin, "0")));
        uiUserLV.setText(String.format(getString(R.string.menu_lv), SharedPreferencesUtil.getInt(SharedPreferencesUtil.userLV, 0)));
        uiUserVip.setVisibility(SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.userVip, false) ? View.VISIBLE : View.GONE);
        try
        {
            uiUserHead.setImageBitmap(BitmapFactory.decodeStream(new FileInputStream(new File(getFilesDir(), "head.png"))));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        menuAdapter = new MenuAdapter(inflater, getMenuSort(), 0, uiListView, menuAdapterListener);
        uiListView.setAdapter(menuAdapter);

        resultIntent = new Intent();
        setResult(-1, resultIntent);

        setUserInfo();

    }

    public void setUserInfo()
    {
        if(SharedPreferencesUtil.contains(SharedPreferencesUtil.cookies)) //是否登录（←错错错错错错错错！！cookie有时限！！！！）
        {
            final UserInfoApi userInfoApi = new UserInfoApi();
            runnableUi = () -> {
                uiUserName.setText(userInfoApi.getUserName());
                uiUserCoin.setText(String.format(getString(R.string.menu_coin), userInfoApi.getUserCoin()));
                uiUserLV.setText(String.format(getString(R.string.menu_lv), userInfoApi.getUserLV()));
                uiUserHead.setImageBitmap(head);
                uiUserVip.setVisibility(userInfoApi.isVip() ? View.VISIBLE : View.GONE);

                SharedPreferencesUtil.putString(SharedPreferencesUtil.userName, userInfoApi.getUserName());
                SharedPreferencesUtil.putString(SharedPreferencesUtil.userCoin, userInfoApi.getUserCoin());
                SharedPreferencesUtil.putInt(SharedPreferencesUtil.userLV, userInfoApi.getUserLV());
                SharedPreferencesUtil.putBoolean(SharedPreferencesUtil.userVip, userInfoApi.isVip());
            };
            new Thread(() -> {
                try
                {
                    int stat = userInfoApi.getUserInfo();
                    //0正常，-1其他问题，-2登录过期
                    if(stat == 0)
                    {
                        head = userInfoApi.getUserHead();
                        saveBitmap(head);
                        handler.post(runnableUi);
                    }
                    else if(stat == -2)
                    {
                        Looper.prepare();
                        Toast.makeText(getApplicationContext(), "您的登录信息已过期，请注销后重新登录", Toast.LENGTH_LONG).show();
                        Intent i = new Intent(ctx, LogsoffActivity.class);
                        startActivity(i);
                        Looper.loop();
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    protected void saveBitmap(Bitmap bt)
    {
        File file = new File(this.getFilesDir(), "head.png");
        FileOutputStream out = null;
        try
        {
            out = new FileOutputStream(file);
            bt.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0 && resultCode == 0)
        {
            if(data.getBooleanExtra("isLogin", false))
            {
                uiUserName.setText("登录中...");
                setUserInfo();
            }
        }
    }

    public void buttonUser(View view)  //个人信息/登录
    {
        if(!SharedPreferencesUtil.contains(SharedPreferencesUtil.cookies))//是否登录的验证
        {
            Intent intent = new Intent(ctx, LoginActivity.class);
            startActivityForResult(intent, 0);
            overridePendingTransition(R.anim.anim_activity_in_left, 0);
        }
        else
        {
            Intent intent = new Intent(ctx, UserActivity.class);
            intent.putExtra("mid", SharedPreferencesUtil.getString(SharedPreferencesUtil.mid, ""));
            startActivity(intent);
        }
    }

    public void onClick(int position)
    {
        resultIntent.putExtra("activity", position);
        setResult(0, resultIntent);
        finish();
        overridePendingTransition(0, R.anim.anim_activity_out_up);
    }

    public int[] getMenuSort()
    {
        return LsonUtil.fromJson(LsonUtil.parse(SharedPreferencesUtil.getString(SharedPreferencesUtil.menuSort, "[0,1,2,3,4,5,6,7,8,9,10]")), int[].class);
    }

    public void buttonDynamic(View view)
    {
        resultIntent.putExtra("activity", 1);
        setResult(0, resultIntent);
        finish();
        overridePendingTransition(0, R.anim.anim_activity_out_up);
    }

    public void buttonRecommend(View view)
    {
        resultIntent.putExtra("activity", 2);
        setResult(0, resultIntent);
        finish();
        overridePendingTransition(0, R.anim.anim_activity_out_up);
    }

    public void buttonRanking(View view)
    {
        resultIntent.putExtra("activity", 3);
        setResult(0, resultIntent);
        finish();
        overridePendingTransition(0, R.anim.anim_activity_out_up);
    }

    public void buttonRemind(View view)
    {
        resultIntent.putExtra("activity", 4);
        setResult(0, resultIntent);
        finish();
        overridePendingTransition(0, R.anim.anim_activity_out_up);
    }

    public void buttonRegion(View view)
    {
        resultIntent.putExtra("activity", 5);
        setResult(0, resultIntent);
        finish();
        overridePendingTransition(0, R.anim.anim_activity_out_up);
    }

    public void buttonDL(View view)
    {
        resultIntent.putExtra("activity", 6);
        setResult(0, resultIntent);
        finish();
        overridePendingTransition(0, R.anim.anim_activity_out_up);
    }

    public void buttonSearch(View view)
    {
        resultIntent.putExtra("activity", 7);
        setResult(0, resultIntent);
        finish();
        overridePendingTransition(0, R.anim.anim_activity_out_up);
    }

    public void buttonCollect(View view)
    {
        resultIntent.putExtra("activity", 8);
        setResult(0, resultIntent);
        finish();
        overridePendingTransition(0, R.anim.anim_activity_out_up);
    }

    public void buttonWatchlater(View view)
    {
        resultIntent.putExtra("activity", 9);
        setResult(0, resultIntent);
        finish();
        overridePendingTransition(0, R.anim.anim_activity_out_up);
    }

    public void buttonHistory(View view)
    {
        resultIntent.putExtra("activity", 10);
        setResult(0, resultIntent);
        finish();
        overridePendingTransition(0, R.anim.anim_activity_out_up);
    }

    public void buttonSetting(View view)
    {
        resultIntent.putExtra("activity", 11);
        setResult(0, resultIntent);
        finish();
        overridePendingTransition(0, R.anim.anim_activity_out_up);
    }
}
