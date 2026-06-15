package com.example.bookspace;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookspace.database.entity.ChallengeEntity;
import com.example.bookspace.databinding.ItemChallengeBinding;
import java.util.ArrayList;
import java.util.List;

public class ChallengeAdapter extends RecyclerView.Adapter<ChallengeAdapter.ViewHolder> {
    private List<ChallengeEntity> items = new ArrayList<>();

    public void setItems(List<ChallengeEntity> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChallengeBinding binding = ItemChallengeBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChallengeEntity item = items.get(position);
        holder.binding.tvChallengeTitle.setText(item.title);
        
        int progressPercent = item.targetValue > 0 ? (int) ((item.currentValue * 100.0f) / item.targetValue) : 0;
        holder.binding.pbChallenge.setProgress(progressPercent);
        holder.binding.tvProgressPercent.setText(progressPercent + "%");
        
        String unit = "pages".equals(item.challengeType) ? "trang" : "quyển";
        holder.binding.tvProgressDetail.setText(item.currentValue + " / " + item.targetValue + " " + unit);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemChallengeBinding binding;
        ViewHolder(ItemChallengeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
