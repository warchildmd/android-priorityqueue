package ro.mihail.burduja.priorityqueue.fragments;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;
import ro.mihail.burduja.priorityqueue.R;
import ro.mihail.burduja.priorityqueue.realm.Tag;
import ro.mihail.burduja.priorityqueue.realm.Task;

/**
 * Created by Mihail on 1/10/2015.
 */
public class CreateTaskFragment extends Fragment {

    private static final String ARG_POSITION = "position";

    Button buttonSetDate;
    Button buttonSave;
    EditText editTextDate;
    SeekBar seekBar;
    TextView textViewImportance;
    EditText editTextTitle;
    EditText editTextDetails;
    Calendar selected;

    Task savedTask = null;
    private Realm realm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getInstance(getActivity());

        Bundle extras = getActivity().getIntent().getExtras();
        selected = Calendar.getInstance();

        if (extras != null) {
            if (extras.containsKey("id")) {
                String taskId = extras.getString("id");
                savedTask = realm.where(Task.class).equalTo("id", taskId).findFirst();
                selected.setTime(savedTask.getTargetedAt());
            }
            if (extras.containsKey(ARG_POSITION)) {
                int position = extras.getInt(ARG_POSITION);
                if (position == 2) {
                    selected.add(Calendar.DATE, 1);
                } else if (position == 3) {
                    selected.add(Calendar.DATE, 2);
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_new_task, container, false);

        buttonSave = (Button) rootView.findViewById(R.id.buttonSave);
        buttonSetDate = (Button) rootView.findViewById(R.id.buttonSetDate);
        editTextDate = (EditText) rootView.findViewById(R.id.editTextDate);
        seekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
        editTextTitle = (EditText) rootView.findViewById(R.id.editTextTitle);
        editTextDetails = (EditText) rootView.findViewById(R.id.editTextDetails);
        textViewImportance = (TextView) rootView.findViewById(R.id.textViewImportance);
        final DateFormat df = new SimpleDateFormat("MMMM dd, yyyy");
        editTextDate.setText(df.format(selected.getTime()));

        editTextTitle.requestFocus();

        buttonSetDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        selected = Calendar.getInstance();
                        selected.set(Calendar.YEAR, year);
                        selected.set(Calendar.MONTH, monthOfYear);
                        selected.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        editTextDate.setText(df.format(selected.getTime()));
                    }
                };
                DatePickerDialog dpd = new DatePickerDialog(getActivity(), dateSetListener, selected.get(Calendar.YEAR), selected.get(Calendar.MONTH), selected.get(Calendar.DAY_OF_MONTH));
                dpd.show();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editTextTitle.clearFocus();
                editTextDetails.clearFocus();
                if (progress <= 100) {
                    textViewImportance.setText(progress + ": CRUCIAL");
                    textViewImportance.setTextColor(Color.parseColor("#D50000"));
                }
                if (progress <= 80) {
                    textViewImportance.setText(progress + ": IMPORTANT");
                    textViewImportance.setTextColor(Color.parseColor("#E65100"));
                }
                if (progress <= 30) {
                    textViewImportance.setText(progress + ": IMPORTANT");
                    textViewImportance.setTextColor(Color.parseColor("#827717"));
                }
                if (progress == 0) {
                    textViewImportance.setText(progress + ": NOT IMPORTANT");
                    textViewImportance.setTextColor(Color.parseColor("#8BC34A"));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = editTextTitle.getText().toString();
                String details = editTextDetails.getText().toString();
                Integer priority = seekBar.getProgress();

                if (title.isEmpty()) {
                    Toast.makeText(getActivity(), "Title is necessary!", Toast.LENGTH_SHORT);
                } else {
                    Pattern pattern = Pattern.compile("#(\\w+)");
                    Matcher matcher = pattern.matcher(details);
                    List<Tag> tags = new ArrayList<Tag>();
                    while (matcher.find()) {
                        String match = matcher.group().substring(1).toLowerCase();
                        if (match.length() == 0) continue;
                        Tag tag = realm.where(Tag.class).equalTo("content", match).findFirst();
                        if (tag == null) {
                            tag = new Tag();
                            tag.setId(UUID.randomUUID().toString());
                            tag.setContent(match);
                            realm.beginTransaction();
                            tag = realm.copyToRealm(tag);
                            realm.commitTransaction();
                        }
                        tags.add(tag);
                    }

                    if (savedTask != null) {
                        realm.beginTransaction();
                        savedTask.setTitle(title);
                        savedTask.setDescription(details);
                        savedTask.setPriority(priority);
                        savedTask.setTargetedAt(selected.getTime());
                        savedTask.getTags().clear();
                        savedTask.getTags().addAll(tags);
                        realm.commitTransaction();
                    } else {
                        Task task = new Task();
                        task.setId(UUID.randomUUID().toString());
                        task.setTitle(title);
                        task.setDescription(details);
                        task.setPriority(priority);
                        task.setTargetedAt(selected.getTime());

                        task.setCompleted(false);
                        task.setCreatedAt(new Date());
                        task.setCompletedAt(new Date());

                        Log.d("TASK ID", task.getId());

                        realm.beginTransaction();
                        task = realm.copyToRealm(task);
                        task.getTags().addAll(tags);
                        realm.commitTransaction();
                    }
                    getActivity().finish();
                }
            }
        });

        // Populate if edit or view
        if (savedTask != null) {
            editTextTitle.setText(savedTask.getTitle());
            editTextDetails.setText(savedTask.getDescription());
            seekBar.setProgress(savedTask.getPriority());

            DateFormat outputDateFormat = new SimpleDateFormat("MMMM dd, yyyy");
            editTextDate.setText(outputDateFormat.format(selected.getTime()));

            Integer progress = savedTask.getPriority();
            if (progress <= 100) {
                textViewImportance.setText(progress + ": CRUCIAL");
                textViewImportance.setTextColor(Color.parseColor("#D50000"));
            }
            if (progress <= 80) {
                textViewImportance.setText(progress + ": IMPORTANT");
                textViewImportance.setTextColor(Color.parseColor("#E65100"));
            }
            if (progress <= 30) {
                textViewImportance.setText(progress + ": IMPORTANT");
                textViewImportance.setTextColor(Color.parseColor("#827717"));
            }
            if (progress == 0) {
                textViewImportance.setText(progress + ": NOT IMPORTANT");
                textViewImportance.setTextColor(Color.parseColor("#8BC34A"));
            }
        }

        return rootView;
    }

    @Override
    public void onDestroy() {
        realm.close();
        super.onDestroy();
    }
}
