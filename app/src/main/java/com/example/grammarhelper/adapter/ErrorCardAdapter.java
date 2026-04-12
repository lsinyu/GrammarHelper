package com.example.grammarhelper.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.grammarhelper.R;
import com.example.grammarhelper.model.GrammarError;
import com.example.grammarhelper.ui.ChatbotActivity;
import java.util.List;

public class ErrorCardAdapter extends RecyclerView.Adapter<ErrorCardAdapter.ViewHolder> {

    private List<GrammarError> errors;
    private Context context;

    public ErrorCardAdapter(Context context, List<GrammarError> errors) {
        this.context = context;
        this.errors = errors;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_error_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GrammarError error = errors.get(position);

        holder.header.setText("❌ " + error.errorType.toUpperCase());
        holder.originalText.setText("\"" + error.originalText + "\"");
        holder.suggestionText.setText(error.suggestion);
        holder.explanationText.setText(error.explanation);

        // Color coding
        if ("Clarity".equals(error.errorType)) {
            holder.header.setTextColor(Color.parseColor("#2563EB"));
        } else if ("Tone".equals(error.errorType)) {
            holder.header.setTextColor(Color.parseColor("#D97706"));
        } else if ("Engagement".equals(error.errorType)) {
             holder.header.setTextColor(Color.parseColor("#16A34A"));
        } else {
             holder.header.setTextColor(Color.parseColor("#DC2626"));
        }

        holder.btnWhy.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatbotActivity.class);
            if (!(context instanceof android.app.Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            intent.putExtra("context", error.originalText);
            context.startActivity(intent);
        });

        holder.btnFix.setOnClickListener(v -> {
             // Logic to fix in parent
             errors.remove(position);
             notifyItemRemoved(position);
        });

        holder.btnSkip.setOnClickListener(v -> {
             errors.remove(position);
             notifyItemRemoved(position);
        });
    }

    @Override
    public int getItemCount() {
        return errors.size();
    }

    public void updateErrors(List<GrammarError> filtered) {
        this.errors = filtered;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView header, originalText, suggestionText, explanationText;
        Button btnWhy, btnSkip, btnFix;

        ViewHolder(View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.errorHeader);
            originalText = itemView.findViewById(R.id.originalText);
            suggestionText = itemView.findViewById(R.id.suggestionText);
            explanationText = itemView.findViewById(R.id.explanationText);
            btnWhy = itemView.findViewById(R.id.btnWhy);
            btnSkip = itemView.findViewById(R.id.btnSkip);
            btnFix = itemView.findViewById(R.id.btnFix);
        }
    }
}
