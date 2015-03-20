package ro.mihail.burduja.priorityqueue.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import ro.mihail.burduja.priorityqueue.R;
import ro.mihail.burduja.priorityqueue.realm.Tag;
import ro.mihail.burduja.priorityqueue.realm.Task;

/**
 * Created by Mihail on 3/20/2015.
 */
public class WidgetTasksFactory implements RemoteViewsService.RemoteViewsFactory {
    private final Calendar calendar;
    private Realm realm;
    private RealmResults<Task> tasks;
    private List<String> taskTitles;
    private Context context;
    private int appWidgetId;

    public WidgetTasksFactory(Context context, Intent intent) {
        this.context = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        calendar = Calendar.getInstance();

        calendar.add(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        taskTitles = new ArrayList<>();
    }

    @Override
    public void onCreate() {
        Log.d("onCreate", Thread.currentThread().toString());
        realm = Realm.getInstance(context);
        tasks = realm.where(Task.class).equalTo("completed", false).
                lessThan("targetedAt", calendar.getTime()).
                findAllSorted("priority", false);
        taskTitles.clear();
        for (Task task: tasks) {
            taskTitles.add(task.getTitle());
        }
        realm.close();
    }

    @Override
    public void onDataSetChanged() {
        Log.d("onDataSetChanged", Thread.currentThread().toString());
        realm = Realm.getInstance(context);
        tasks = realm.where(Task.class).equalTo("completed", false).
                lessThan("targetedAt", calendar.getTime()).
                findAllSorted("priority", false);
        taskTitles.clear();
        for (Task task: tasks) {
            taskTitles.add(task.getTitle());
        }
        realm.close();
    }

    @Override
    public void onDestroy() {
        // realm.close();
    }

    @Override
    public int getCount() {
        return taskTitles.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    /*
    *Similar to getView of Adapter where instead of View
    *we return RemoteViews
    *
    */
    @Override
    public RemoteViews getViewAt(int position) {
        final RemoteViews remoteView = new RemoteViews(
                context.getPackageName(), android.R.layout.simple_list_item_1);
        String task = taskTitles.get(position);
        remoteView.setTextViewText(android.R.id.text1, task);

        return remoteView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }
}
