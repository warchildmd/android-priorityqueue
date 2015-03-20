package ro.mihail.burduja.priorityqueue;

import android.os.Bundle;

import ro.mihail.burduja.priorityqueue.fragments.CreateTaskFragment;

/**
 * Created by Mihail on 1/8/2015.
 */
public class CreateTaskActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            CreateTaskFragment fragment = new CreateTaskFragment();
            fragment.setArguments(savedInstanceState);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_create_task;
    }
}
