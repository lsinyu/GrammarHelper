package com.example.grammarhelper.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.grammarhelper.R;
import java.util.List;

public class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.ViewHolder> {

    private List<String> badges;
    private List<Boolean> unlocked;

    public BadgeAdapter(List<String> badges, List<Boolean> unlocked) {
        this.badges = badges;
        this.unlocked = unlocked;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_badge, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.title.setText(badges.get(position));
        if (unlocked.get(position)) {
            holder.icon.setAlpha(1.0f);
            holder.title.setTextColor(Color.parseColor("#0F172A")); // text_primary
        } else {
            holder.icon.setAlpha(0.2f);
            holder.title.setTextColor(Color.parseColor("#64748B")); // text_secondary
        }
    }

    @Override
    public int getItemCount() {
        return badges.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;

        ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.badgeIcon);
            title = itemView.findViewById(R.id.badgeTitle);
        }
    }
}
