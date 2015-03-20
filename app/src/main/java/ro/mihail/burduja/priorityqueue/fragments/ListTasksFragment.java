package ro.mihail.burduja.priorityqueue.fragments;

/**
 * Created by Mihail on 1/10/2015.
 */

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;
import ro.mihail.burduja.priorityqueue.CreateTaskActivity;
import ro.mihail.burduja.priorityqueue.R;
import ro.mihail.burduja.priorityqueue.adapters.CustomListAdapter;
import ro.mihail.burduja.priorityqueue.realm.Tag;
import ro.mihail.burduja.priorityqueue.realm.Task;
import ro.mihail.burduja.priorityqueue.widget.WidgetProvider;

public class ListTasksFragment extends Fragment {

    private static final String ARG_POSITION = "position";
    FloatingActionButton buttonCreateTask;

    private int position;
    private ListView listView;
    private CustomListAdapter adapter;

    private Realm realm;

    public static ListTasksFragment newInstance(int position) {
        ListTasksFragment f = new ListTasksFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        realm = Realm.getInstance(getActivity());

        position = getArguments().getInt(ARG_POSITION);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (position == 0) {
            inflater.inflate(R.menu.menu_done, menu);
        } else {
            inflater.inflate(R.menu.menu_filter, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_clear_done) {
            new AlertDialog.Builder(getActivity())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Are you sure?")
                    .setMessage("Are you sure you want to delete completed tasks?")
                    .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            realm.beginTransaction();
                            realm.where(Task.class).equalTo("completed", true).findAll().clear();
                            realm.commitTransaction();
                            realm.beginTransaction();
                            RealmResults<Tag> tags = realm.where(Tag.class).findAll();
                            for (int i = 0; i < tags.size(); i++) {
                                Tag tag = tags.get(i);
                                long count = realm.where(Task.class).equalTo("tags.id", tag.getId()).count();
                                if (count == 0) {
                                    tag.removeFromRealm();
                                    i--;
                                }
                            }
                            realm.commitTransaction();
                            adapter.notifyDataSetChanged();
                            WidgetProvider.updateWidget(getActivity());
                        }

                    })
                    .setNegativeButton("CANCEL", null)
                    .show();
        }

        if (item.getItemId() == R.id.action_filter) {
            RealmResults<Tag> tags = realm.where(Tag.class).findAll();
            final String [] tagsArray = new String[tags.size() + 1];
            tagsArray[0] = "Clear filters";
            for (int i = 0; i < tags.size(); i++) {
                tagsArray[i + 1] = tags.get(i).getContent();
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Pick a filter")
                    .setItems(tagsArray, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                notifyDataSetChanged("");
                            } else {
                                notifyDataSetChanged(tagsArray[which]);
                            }
                        }
                    });
            builder.show();
        }

        return super.onOptionsItemSelected(item);
    }

    public void notifyDataSetChanged(String tag) {
        if (adapter != null) {
            RealmResults<Task> tasks;

            if (position == 0) {
                tasks = realm.where(Task.class).equalTo("completed", true).
                        findAllSorted("priority", false);
            } else if (position == 1) {
                Calendar calendar = Calendar.getInstance();

                calendar.add(Calendar.DATE, 1);
                calendar.set(Calendar.HOUR, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);

                tasks = realm.where(Task.class).equalTo("completed", false).
                        lessThan("targetedAt", calendar.getTime()).
                        findAllSorted("priority", false);
            } else if (position == 2) {
                Calendar from = Calendar.getInstance();
                Calendar to = Calendar.getInstance();

                from.add(Calendar.DAY_OF_MONTH, 1);
                from.set(Calendar.HOUR, 0);
                from.set(Calendar.MINUTE, 0);
                from.set(Calendar.SECOND, 0);

                to.add(Calendar.DAY_OF_MONTH, 2);
                to.set(Calendar.HOUR, 0);
                to.set(Calendar.MINUTE, 0);
                to.set(Calendar.SECOND, 0);

                tasks = realm.where(Task.class).equalTo("completed", false).
                        lessThan("targetedAt", to.getTime()).
                        greaterThanOrEqualTo("targetedAt", from.getTime()).
                        findAllSorted("priority", false);
            } else {
                Calendar calendar = Calendar.getInstance();

                calendar.add(Calendar.DATE, 2);
                calendar.set(Calendar.HOUR, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);

                tasks = realm.where(Task.class).equalTo("completed", false).
                        greaterThanOrEqualTo("targetedAt", calendar.getTime()).
                        findAllSorted("priority", false);
            }
            if (!tag.equals("")) {
                tasks = tasks.where().equalTo("tags.content", tag).findAll();
                getActivity().setTitle("#" + tag);
            } else {
                getActivity().setTitle(R.string.app_name);
            }

            adapter.setItems(tasks);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_list_tasks, null, false);

        buttonCreateTask = (FloatingActionButton) rootView.findViewById(R.id.button_new_task);
        if (position == 0) {
            buttonCreateTask.setVisibility(View.GONE);
        }
        buttonCreateTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CreateTaskActivity.class);
                intent.putExtra(ARG_POSITION, position);
                startActivity(intent);
            }
        });

        listView = (ListView) rootView.findViewById(R.id.list_tasks);

        adapter = new CustomListAdapter(getActivity());
        notifyDataSetChanged("");

        listView.setAdapter(adapter);
        TextView tvEmptyText = new TextView(getActivity());
        tvEmptyText.setText("You're done!");
        tvEmptyText.setPadding(20, 20, 20, 20);
        tvEmptyText.setGravity(Gravity.CENTER_HORIZONTAL);
        listView.setEmptyView(tvEmptyText);

        buttonCreateTask.attachToListView(listView);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("What do you want?");
                List<String> optionsArray = new ArrayList<String>();
                optionsArray.add("Edit");
                optionsArray.add("Delete");
                ArrayAdapter<String> optionsAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, optionsArray);
                builder.setAdapter(optionsAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            Intent intent = new Intent(getActivity(), CreateTaskActivity.class);
                            intent.putExtra("id", adapter.getItem(position).getId());
                            startActivity(intent);
                        }
                        if (which == 1) {
                            realm.beginTransaction();
                            adapter.getItem(position).removeFromRealm();
                            realm.commitTransaction();

                            realm.beginTransaction();
                            RealmResults<Tag> tags = realm.where(Tag.class).findAll();
                            for (int i = 0; i < tags.size(); i++) {
                                Tag tag = tags.get(i);
                                long count = realm.where(Task.class).equalTo("tags.id", tag.getId()).count();
                                if (count == 0) {
                                    tag.removeFromRealm();
                                    i--;
                                }
                            }
                            realm.commitTransaction();

                            // adapter.removeItem(position);
                            adapter.notifyDataSetChanged();
                            WidgetProvider.updateWidget(getActivity());
                        }
                    }
                });
                builder.create().show();

                return false;
            }
        });

        return rootView;
    }

    @Override
    public void onDestroy() {
        realm.close();
        super.onDestroy();
    }
}
