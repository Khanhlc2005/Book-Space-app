package com.example.bookspace;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {

    private List<Reminder> reminders;
    private OnReminderChangeListener listener;

    public interface OnReminderChangeListener {
        void onToggle(Reminder reminder, boolean isEnabled);
        void onDelete(Reminder reminder);
    }

    public ReminderAdapter(List<Reminder> reminders, OnReminderChangeListener listener) {
        this.reminders = reminders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_reminder, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reminder reminder = reminders.get(position);
        holder.tvTime.setText(reminder.getTimeString());
        holder.switchEnabled.setChecked(reminder.isEnabled());

        holder.switchEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            reminder.setEnabled(isChecked);
            listener.onToggle(reminder, isChecked);
        });

        holder.btnDelete.setOnClickListener(v -> listener.onDelete(reminder));
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime;
        SwitchMaterial switchEnabled;
        ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvReminderTime);
            switchEnabled = itemView.findViewById(R.id.switchReminder);
            btnDelete = itemView.findViewById(R.id.btnDeleteReminder);
        }
    }
}