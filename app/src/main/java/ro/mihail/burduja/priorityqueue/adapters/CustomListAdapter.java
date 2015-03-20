package ro.mihail.burduja.priorityqueue.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import ro.mihail.burduja.priorityqueue.R;
import ro.mihail.burduja.priorityqueue.realm.Tag;
import ro.mihail.burduja.priorityqueue.realm.Task;

/**
 * Created by Mihail on 1/10/2015.
 */
public class CustomListAdapter extends BaseAdapter {

    private final Context context;
    private LayoutInflater mInflater;
    private RealmResults<Task> tasks;
    private DateFormat outputDateFormat;

    public CustomListAdapter(Context context) {
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;

        outputDateFormat = new SimpleDateFormat("MMMM dd, yyyy");
    }

    public List<Task> getItems() {
        return tasks;
    }

    public void removeItem(int position) {
        tasks.remove(position);
    }

    @Override
    public int getCount() {
        return tasks.size();
    }

    @Override
    public Task getItem(int position) {
        return tasks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        EntryViewHolder holder = null;

        if (convertView == null) {
            holder = new EntryViewHolder();
            convertView = mInflater.inflate(R.layout.list_item_separator, null);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBoxCompleted);
            holder.textViewPriority = (TextView) convertView.findViewById(R.id.textViewPriority);
            holder.textViewDetails = (TextView) convertView.findViewById(R.id.textViewDetails);
            holder.textViewDate = (TextView) convertView.findViewById(R.id.textViewDate);
            holder.textViewTags = (TextView) convertView.findViewById(R.id.textViewTags);
            convertView.setTag(holder);
        } else {
            holder = (EntryViewHolder) convertView.getTag();
        }
        holder.checkBox.setOnCheckedChangeListener(null);

        holder.checkBox.setChecked(getItem(position).isCompleted());
        holder.checkBox.setText(getItem(position).getTitle());
        holder.textViewPriority.setText("" + getItem(position).getPriority());
        if (getItem(position).getPriority() <= 100) {
            holder.textViewPriority.setTextColor(Color.parseColor("#D50000"));
        }
        if (getItem(position).getPriority() < 80) {
            holder.textViewPriority.setTextColor(Color.parseColor("#E65100"));
        }
        if (getItem(position).getPriority() < 30) {
            holder.textViewPriority.setTextColor(Color.parseColor("#827717"));
        }
        if (getItem(position).getPriority() == 0) {
            holder.textViewPriority.setTextColor(Color.parseColor("#8BC34A"));
        }
        holder.textViewDetails.setText(getItem(position).getDescription());
        if (!getItem(position).isCompleted()) {
            holder.textViewDate.setText("Targeted: " + outputDateFormat.format(getItem(position).getTargetedAt()));
            if (getItem(position).getTargetedAt().before(Calendar.getInstance().getTime())) {
                holder.textViewDate.setTextColor(Color.parseColor("#CC0000"));
            }
        } else {
            holder.textViewDate.setText("Completed: " + outputDateFormat.format(getItem(position).getCompletedAt()));
            holder.textViewDate.setTextColor(Color.parseColor("#3333FF"));
        }

        List<Tag> matches = getItem(position).getTags();
        if (matches.size() > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append("Tags: ");
            for (int k = 0; k < matches.size() - 1; k++) {
                builder.append(matches.get(k).getContent()).append(", ");
            }
            builder.append(matches.get(matches.size() - 1).getContent());
            holder.textViewTags.setText(builder.toString());
            holder.textViewTags.setTextColor(Color.rgb(0x3F, 0x51, 0xB5));
            holder.textViewTags.setVisibility(View.VISIBLE);
        } else {
            holder.textViewTags.setVisibility(View.GONE);
        }

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Realm realm = Realm.getInstance(context);
                realm.beginTransaction();
                if (isChecked) {
                    getItem(position).setCompletedAt(new Date());
                    getItem(position).setCompleted(true);
                } else {
                    getItem(position).setCompleted(false);
                }
                realm.commitTransaction();
                notifyDataSetChanged();
            }
        });

        return convertView;
    }

    public void setItems(RealmResults<Task> items) {
        this.tasks = items;
    }

    public static class EntryViewHolder {
        public CheckBox checkBox;
        public TextView textViewPriority;
        public TextView textViewDetails;
        public TextView textViewDate;
        public TextView textViewTags;
    }
}
