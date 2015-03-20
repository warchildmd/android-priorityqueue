package ro.mihail.burduja.priorityqueue;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;

import com.astuetz.PagerSlidingTabStrip;

import ro.mihail.burduja.priorityqueue.adapters.CustomPagerAdapter;
import ro.mihail.burduja.priorityqueue.fragments.ListTasksFragment;


public class ListTasksActivity extends BaseActivity {

    private final Handler handler = new Handler();

    private PagerSlidingTabStrip tabs;
    private ViewPager pager;
    private CustomPagerAdapter adapter;

    private int currentColor = R.color.primary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        pager = (ViewPager) findViewById(R.id.pager);
        adapter = new CustomPagerAdapter(getSupportFragmentManager());

        pager.setAdapter(adapter);
        tabs.setIndicatorColor(Color.parseColor("#3F51B5"));
        tabs.setIndicatorHeight(4);
        tabs.setUnderlineHeight(4);

        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        pager.setPageMargin(pageMargin);

        tabs.setViewPager(pager);

        pager.setCurrentItem(1);

        tabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                ListTasksFragment fragment = (ListTasksFragment) adapter.instantiateItem(pager, position);
                if (fragment != null) {
                    fragment.notifyDataSetChanged("");
                }
            }

            @Override
            public void onPageSelected(int position) {
                ListTasksFragment fragment = (ListTasksFragment) adapter.instantiateItem(pager, position);
                if (fragment != null) {
                    fragment.notifyDataSetChanged("");
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_list_tasks;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_main, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void changeColor(int newColor) {
        tabs.setIndicatorColor(newColor);

        currentColor = newColor;
    }

    private Drawable.Callback drawableCallback = new Drawable.Callback() {
        @Override
        public void invalidateDrawable(Drawable who) {
            getActionBar().setBackgroundDrawable(who);
        }

        @Override
        public void scheduleDrawable(Drawable who, Runnable what, long when) {
            handler.postAtTime(what, when);
        }

        @Override
        public void unscheduleDrawable(Drawable who, Runnable what) {
            handler.removeCallbacks(what);
        }
    };
}
