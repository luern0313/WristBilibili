package cn.luern0313.wristbilibili.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.ui.MenuActivity;

public class Download extends Fragment
{
    Context ctx;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ctx = this.getActivity();
        return inflater.inflate(R.layout.fragment_download, container, false);
    }
}
