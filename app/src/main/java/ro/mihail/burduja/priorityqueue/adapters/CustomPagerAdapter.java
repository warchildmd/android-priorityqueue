package ro.mihail.burduja.priorityqueue.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import ro.mihail.burduja.priorityqueue.fragments.ListTasksFragment;

/**
 * Created by Mihail on 1/10/2015.
 */
public class CustomPagerAdapter extends FragmentPagerAdapter {

    private final String[] TITLES = { "Done", "Today", "Tomorrow", "Future" };

    public CustomPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TITLES[position];
    }

    @Override
    public int getCount() {
        return TITLES.length;
    }

    @Override
    public Fragment getItem(int position) {
        return ListTasksFragment.newInstance(position);
    }

}