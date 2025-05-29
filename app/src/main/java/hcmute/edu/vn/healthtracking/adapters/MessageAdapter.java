package hcmute.edu.vn.healthtracking.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hcmute.edu.vn.healthtracking.R;
import hcmute.edu.vn.healthtracking.models.Message;

import io.noties.markwon.Markwon;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private final List<Message> messageList;
    private final Markwon markwon;

    public MessageAdapter(List<Message> messageList, Context context) {
        this.messageList = messageList;
        markwon = Markwon.builder(context).build();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        if (message.getSentBy() == Message.SENT_BY_USER) {
            holder.botMessageContainer.setVisibility(View.GONE);
            holder.rightChatView.setVisibility(View.VISIBLE);
            holder.rightChatView.setText(message.getMessage());
        } else {
            holder.rightChatView.setVisibility(View.GONE);
            holder.botMessageContainer.setVisibility(View.VISIBLE);
            holder.leftChatView.setVisibility(View.VISIBLE);
            markwon.setMarkdown(holder.leftChatView, message.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView leftChatView, rightChatView;
        LinearLayout botMessageContainer;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            leftChatView = itemView.findViewById(R.id.leftChatTextView);
            rightChatView = itemView.findViewById(R.id.rightChatTextView);
            botMessageContainer = itemView.findViewById(R.id.botMessageContainer);
        }
    }
} 