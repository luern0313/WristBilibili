package cn.luern0313.wristbilibili.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.adapter.SettingAdapter;
import cn.luern0313.wristbilibili.api.SettingApi;
import cn.luern0313.wristbilibili.models.SettingModel;
import cn.luern0313.wristbilibili.ui.BaseActivity;
import cn.luern0313.wristbilibili.ui.SettingActivity;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;
import cn.luern0313.wristbilibili.util.ViewTouchListener;
import cn.luern0313.wristbilibili.widget.TitleView;

/**
 * Created by liupe on 2018/11/15.
 * 说是设置。。好像没有真的可以设置的东西。。
 * 有了主题
 * -- by fifth_light
 */

public class SettingFragment extends Fragment
{
    private static final String ARG_SETTING_MODEL_LIST = "settingModelListArg";
    private Context ctx;
    private SettingApi settingApi;
    private ArrayList<SettingModel> settingModelArrayList;

    private View rootLayout;
    private ListView listView;
    private SettingAdapter settingAdapter;
    private SettingAdapter.SettingAdapterListener settingAdapterListener;
    private TitleView.TitleViewListener titleViewListener;

    public static Fragment newInstance(ArrayList<SettingModel> settingModelArrayList)
    {
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SETTING_MODEL_LIST, settingModelArrayList);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
        {
            settingModelArrayList = (ArrayList<SettingModel>) getArguments().getSerializable(ARG_SETTING_MODEL_LIST);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = this.getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_setting, container, false);
        settingApi = new SettingApi();

        settingAdapterListener = new SettingAdapter.SettingAdapterListener()
        {
            @Override
            public void onItemClick(int position)
            {
                itemClick(position);
            }

            @Override
            public boolean onSwitchChange(int position, boolean s)
            {
                return switchChange(position, s);
            }
        };
        if(settingModelArrayList == null)
            settingModelArrayList = settingApi.getSettingModelArrayList();
        listView = rootLayout.findViewById(R.id.set_listview);
        settingAdapter = new SettingAdapter(inflater, settingModelArrayList, listView, settingAdapterListener);
        listView.setAdapter(settingAdapter);

        listView.setOnTouchListener(new ViewTouchListener(listView, titleViewListener));

        return rootLayout;
    }

    private void itemClick(int position)
    {
        SettingModel settingModel = settingModelArrayList.get(position);
        if(settingModel.getMode() == 0)
        {
            try
            {
                Intent intent = new Intent(ctx, Class.forName("cn.luern0313.wristbilibili.ui." + settingModel.getGotoClass()));
                if(settingModel.getParameter() != null)
                {
                    for (String key : settingModel.getParameter().keySet().toArray(new String[0]))
                    {
                        String value = settingModel.getParameter().get(key);
                        if(value.startsWith("@"))
                            intent.putExtra(key, ctx.getString(R.string.class.getField(value.substring(1)).getInt(null)));
                        else
                            intent.putExtra(key, settingModel.getParameter().get(key));
                    }
                }
                startActivity(intent);
            }
            catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException ignored)
            {
            }
        }
        else if(settingModel.getMode() == 1)
        {
            Intent intent = new Intent(ctx, SettingActivity.class);
            intent.putExtra(SettingActivity.ARG_SETTING_MODEL_LIST, settingModel.getSub());
            startActivity(intent);
        }
    }

    private boolean switchChange(int position, boolean s)
    {
        try
        {
            SettingModel settingModel = settingModelArrayList.get(position);
            SharedPreferencesUtil.putBoolean((String) SharedPreferencesUtil.class.getField(settingModel.getSpName()).get(null), s);

            if(settingModel.getSpName().equals("screenRound"))
                BaseActivity.restartAllActivity();

            return true;
        }
        catch (IllegalAccessException | NoSuchFieldException ignored)
        {
        }
        return false;
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        if(context instanceof TitleView.TitleViewListener)
            titleViewListener = (TitleView.TitleViewListener) context;
    }
}
