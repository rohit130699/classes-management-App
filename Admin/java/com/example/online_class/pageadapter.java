package com.example.online_class;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

public class pageadapter extends FragmentPagerAdapter {
    int tabcount;
    ViewPager viewpg;
    TabLayout tblout;
    public pageadapter(@NonNull FragmentManager fm, int behavior, ViewPager viewPager, TabLayout tablayout) {
        super(fm, behavior);
        tabcount=behavior;
        viewpg=viewPager;
        tblout=tablayout;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0: return new ftab1(viewpg,tblout);
            case 1: return new ftab3(viewpg,tblout);
            default: return null;
        }
    }

    @Override
    public int getCount() {
        return tabcount;
    }
}

