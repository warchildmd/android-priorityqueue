package ro.mihail.burduja.priorityqueue;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import io.realm.Realm;
import ro.mihail.burduja.priorityqueue.realm.Task;

public class ShareActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();

        Task task = new Task();
        task.setId(UUID.randomUUID().toString());
        if (extras.containsKey(Intent.EXTRA_TITLE)) {
            task.setTitle(extras.getString(Intent.EXTRA_TITLE));
        } else {
            task.setTitle(extras.getString(Intent.EXTRA_SUBJECT));
        }

        task.setDescription(extras.getString(Intent.EXTRA_TEXT, ""));
        task.setPriority(50);
        task.setTargetedAt(Calendar.getInstance().getTime());

        task.setCompleted(false);
        task.setCreatedAt(new Date());
        task.setCompletedAt(new Date());

        Log.d("TASK ID", task.getId());

        Realm realm = Realm.getInstance(this);
        realm.beginTransaction();
        realm.copyToRealm(task);
        realm.commitTransaction();

        Toast.makeText(this, "Task added!", Toast.LENGTH_SHORT).show();
        finish();
    }
}