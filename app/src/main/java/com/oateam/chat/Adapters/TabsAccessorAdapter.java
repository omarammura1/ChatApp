package com.oateam.chat.Adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.oateam.chat.Fragments.GroupsFragment;
import com.oateam.chat.Fragments.PrivateGroupsFragment;

public class TabsAccessorAdapter extends FragmentPagerAdapter {
    public TabsAccessorAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        if (position == 0)
        {
            return new PrivateGroupsFragment();
        }
        else if (position == 1)
        {
            return new GroupsFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }


    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position)
        {
            case 0:
                return "Private Groups";
            case 1:
                return "Public Groups";
//            case 2:
//                return "Friends";
//            case 3:
//                return "Request";
            default:
                return null;

        }
    }
}
