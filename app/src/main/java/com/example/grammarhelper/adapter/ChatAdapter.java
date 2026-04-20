package com.example.grammarhelper.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.grammarhelper.R;
import com.example.grammarhelper.model.ChatMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_BOT = 2;

    private List<ChatMessage> messages = new ArrayList<>();
    private OnQuizSubmitListener quizSubmitListener;

    // To track current rewrite page for each bot message that contains rewrites
    private Map<Integer, Integer> rewriteIndices = new HashMap<>();

    public interface OnQuizSubmitListener {
        void onQuizSubmit(String answers);
    }

    public ChatAdapter(OnQuizSubmitListener listener) {
        this.quizSubmitListener = listener;
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void removeMessageAt(int position) {
        if (position >= 0 && position < messages.size()) {
            messages.remove(position);
            notifyItemRemoved(position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage msg = messages.get(position);
        if (msg.userMessage != null && !msg.userMessage.isEmpty()) {
            return VIEW_TYPE_USER;
        } else {
            return VIEW_TYPE_BOT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_user, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_bot, parent, false);
            return new BotViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).userText.setText(message.userMessage);
        } else if (holder instanceof BotViewHolder) {
            BotViewHolder botHolder = (BotViewHolder) holder;
            String text = message.botResponse;

            // Reset visibility
            botHolder.quizContainer.setVisibility(View.GONE);
            botHolder.btnSubmitQuiz.setVisibility(View.GONE);
            botHolder.rewriteContainer.setVisibility(View.GONE);

            if (text == null) return;

            // Check if text is JSON for Quiz or Rewrite
            if (text.trim().startsWith("[")) {
                handleQuiz(botHolder, text);
            } else if (text.trim().startsWith("{")) {
                handleRewrite(botHolder, text, position);
            } else {
                botHolder.botText.setText(text);
            }
        }
    }

    private void handleQuiz(BotViewHolder botHolder, String jsonText) {
        try {
            JSONArray quizArray = new JSONArray(jsonText);
            botHolder.botText.setText("Please complete the quiz below:");
            botHolder.quizContainer.removeAllViews();
            botHolder.quizContainer.setVisibility(View.VISIBLE);
            botHolder.btnSubmitQuiz.setVisibility(View.VISIBLE);

            final Map<Integer, RadioGroup> radioGroups = new HashMap<>();

            for (int i = 0; i < quizArray.length(); i++) {
                JSONObject qObj = quizArray.getJSONObject(i);
                int id = qObj.getInt("id");
                String qText = qObj.getString("question");
                JSONArray options = qObj.getJSONArray("options");

                TextView qTv = new TextView(botHolder.itemView.getContext());
                qTv.setText((i + 1) + ". " + qText);
                qTv.setPadding(0, 16, 0, 8);
                botHolder.quizContainer.addView(qTv);

                RadioGroup rg = new RadioGroup(botHolder.itemView.getContext());
                for (int j = 0; j < options.length(); j++) {
                    RadioButton rb = new RadioButton(botHolder.itemView.getContext());
                    rb.setText(options.getString(j));
                    rb.setId(View.generateViewId());
                    rg.addView(rb);
                }
                botHolder.quizContainer.addView(rg);
                radioGroups.put(id, rg);
            }

            botHolder.btnSubmitQuiz.setOnClickListener(v -> {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<Integer, RadioGroup> entry : radioGroups.entrySet()) {
                    int checkedId = entry.getValue().getCheckedRadioButtonId();
                    if (checkedId != -1) {
                        RadioButton rb = entry.getValue().findViewById(checkedId);
                        String selection = rb.getText().toString();
                        sb.append("Answer ").append(entry.getKey()).append(": ").append(selection).append("\n");
                    }
                }
                if (quizSubmitListener != null) {
                    quizSubmitListener.onQuizSubmit(sb.toString());
                    botHolder.btnSubmitQuiz.setEnabled(false);
                }
            });
        } catch (JSONException e) {
            botHolder.botText.setText(jsonText);
        }
    }

    private void handleRewrite(BotViewHolder botHolder, String jsonText, int position) {
        try {
            JSONObject root = new JSONObject(jsonText);
            if (!root.has("rewrites")) {
                botHolder.botText.setText(jsonText);
                return;
            }

            JSONArray rewrites = root.getJSONArray("rewrites");
            botHolder.botText.setText("Here are the rewritten versions of your text:");
            botHolder.rewriteContainer.setVisibility(View.VISIBLE);

            // Get or initialize current index for this message
            if (!rewriteIndices.containsKey(position)) {
                rewriteIndices.put(position, 0);
            }

            updateRewriteUI(botHolder, rewrites, position);

            botHolder.btnPrevRewrite.setOnClickListener(v -> {
                int index = rewriteIndices.get(position);
                if (index > 0) {
                    rewriteIndices.put(position, index - 1);
                    updateRewriteUI(botHolder, rewrites, position);
                }
            });

            botHolder.btnNextRewrite.setOnClickListener(v -> {
                int index = rewriteIndices.get(position);
                if (index < rewrites.length() - 1) {
                    rewriteIndices.put(position, index + 1);
                    updateRewriteUI(botHolder, rewrites, position);
                }
            });

            // Quick style switch buttons
            botHolder.btnStyleProf.setOnClickListener(v -> {
                rewriteIndices.put(position, 0);
                updateRewriteUI(botHolder, rewrites, position);
            });
            botHolder.btnStyleCasual.setOnClickListener(v -> {
                rewriteIndices.put(position, 1);
                updateRewriteUI(botHolder, rewrites, position);
            });
            botHolder.btnStyleShort.setOnClickListener(v -> {
                rewriteIndices.put(position, 2);
                updateRewriteUI(botHolder, rewrites, position);
            });

            // Copy button logic
            botHolder.btnCopyRewrite.setOnClickListener(v -> {
                String contentToCopy = botHolder.rewriteContent.getText().toString();
                ClipboardManager clipboard = (ClipboardManager) botHolder.itemView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Rewritten Text", contentToCopy);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(botHolder.itemView.getContext(), "Copied to clipboard!", Toast.LENGTH_SHORT).show();
            });

        } catch (JSONException e) {
            botHolder.botText.setText(jsonText);
        }
    }

    private void updateRewriteUI(BotViewHolder botHolder, JSONArray rewrites, int position) {
        try {
            int index = rewriteIndices.get(position);
            JSONObject rewrite = rewrites.getJSONObject(index);

            botHolder.rewriteTitle.setText(rewrite.getString("title"));
            botHolder.rewriteContent.setText(rewrite.getString("content"));
            botHolder.rewritePagerInfo.setText((index + 1) + " / " + rewrites.length());

            botHolder.btnPrevRewrite.setEnabled(index > 0);
            botHolder.btnNextRewrite.setEnabled(index < rewrites.length() - 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // User ViewHolder - White text on purple background
    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userText;

        UserViewHolder(View itemView) {
            super(itemView);
            userText = itemView.findViewById(R.id.chatUserText);
        }
    }

    // Bot ViewHolder - Theme-aware text color
    static class BotViewHolder extends RecyclerView.ViewHolder {
        TextView botText;
        LinearLayout quizContainer;
        Button btnSubmitQuiz;
        LinearLayout rewriteContainer;
        TextView rewriteTitle, rewriteContent, rewritePagerInfo;
        Button btnPrevRewrite, btnNextRewrite;
        Button btnStyleProf, btnStyleCasual, btnStyleShort;
        ImageButton btnCopyRewrite;

        BotViewHolder(View itemView) {
            super(itemView);
            botText = itemView.findViewById(R.id.chatBotText);
            quizContainer = itemView.findViewById(R.id.quizContainer);
            btnSubmitQuiz = itemView.findViewById(R.id.btnSubmitQuiz);
            rewriteContainer = itemView.findViewById(R.id.rewriteContainer);
            rewriteTitle = itemView.findViewById(R.id.rewriteTitle);
            rewriteContent = itemView.findViewById(R.id.rewriteContent);
            rewritePagerInfo = itemView.findViewById(R.id.rewritePagerInfo);
            btnPrevRewrite = itemView.findViewById(R.id.btnPrevRewrite);
            btnNextRewrite = itemView.findViewById(R.id.btnNextRewrite);
            btnStyleProf = itemView.findViewById(R.id.btnStyleProf);
            btnStyleCasual = itemView.findViewById(R.id.btnStyleCasual);
            btnStyleShort = itemView.findViewById(R.id.btnStyleShort);
            btnCopyRewrite = itemView.findViewById(R.id.btnCopyRewrite);
        }
    }
}