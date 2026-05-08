package com.example.bookspace;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookspace.database.entity.ReminderEntity;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    private List<ReminderEntity> reminders = new ArrayList<>();
    private OnReminderClickListener listener;
    private boolean isEditMode = false;

    public interface OnReminderClickListener {
        void onReminderClick(ReminderEntity reminder);
        void onReminderToggle(ReminderEntity reminder, boolean isActive);
        void onReminderDelete(ReminderEntity reminder);
    }

    public void setOnReminderClickListener(OnReminderClickListener listener) {
        this.listener = listener;
    }

    public void setReminders(List<ReminderEntity> reminders) {
        this.reminders = reminders;
        notifyDataSetChanged();
    }

    public void setEditMode(boolean editMode) {
        this.isEditMode = editMode;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        ReminderEntity reminder = reminders.get(position);
        holder.tvTime.setText(String.format(Locale.getDefault(), "%02d:%02d", reminder.hour, reminder.minute));
        
        holder.switchActive.setOnCheckedChangeListener(null);
        holder.switchActive.setChecked(reminder.isActive);
        
        // Cảm giác giống iPhone: Nếu tắt thì mờ đi
        float alpha = reminder.isActive ? 1.0f : 0.5f;
        holder.tvTime.setAlpha(alpha);
        holder.tvLabel.setAlpha(alpha);

        if (isEditMode) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.switchActive.setVisibility(View.GONE);
        } else {
            holder.btnDelete.setVisibility(View.GONE);
            holder.switchActive.setVisibility(View.VISIBLE);
        }

        holder.switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onReminderToggle(reminder, isChecked);
                holder.tvTime.setAlpha(isChecked ? 1.0f : 0.5f);
                holder.tvLabel.setAlpha(isChecked ? 1.0f : 0.5f);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onReminderDelete(reminder);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                if (isEditMode) {
                    listener.onReminderDelete(reminder);
                } else {
                    listener.onReminderClick(reminder);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    static class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvLabel;
        SwitchMaterial switchActive;
        ImageView btnDelete;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            switchActive = itemView.findViewById(R.id.switchActive);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
