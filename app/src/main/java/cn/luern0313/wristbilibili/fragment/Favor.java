package cn.luern0313.wristbilibili.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.luern0313.wristbilibili.R;

/**
 * Created by liupe on 2018/11/16.
 * 收藏的fragment
 * 畜生！你收藏了甚么！
 */

public class Favor extends Fragment
{
    Context ctx;

    View rootLayout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = getActivity();
        rootLayout = inflater.inflate(R.layout.fragment_search, container, false);
        return rootLayout;
    }
}
