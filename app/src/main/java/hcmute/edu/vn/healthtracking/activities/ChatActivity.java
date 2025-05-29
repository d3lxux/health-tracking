package hcmute.edu.vn.healthtracking.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hcmute.edu.vn.healthtracking.R;
import hcmute.edu.vn.healthtracking.adapters.MessageAdapter;
import hcmute.edu.vn.healthtracking.models.Message;
import hcmute.edu.vn.healthtracking.repositories.ChatRepository;
import hcmute.edu.vn.healthtracking.services.AiService;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private ImageButton sendButton;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    // AI MODEL
    private AiService aiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize views
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);

        // Initialize model
        aiService = new AiService(this);

        // Initialize message list and adapter
        messageList = ChatRepository.getMessages();
        messageAdapter = new MessageAdapter(messageList, this);
        
        // Set up RecyclerView
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(messageAdapter);

        // Set up send button click listener
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageEditText.getText().toString().trim();
                if (!message.isEmpty()) {
                    // Add user message
                    addMessage(message, Message.SENT_BY_USER);
                    messageEditText.setText("");

                    // Process message in background
                    processMessage(message);
                }
            }
        });

        // Scroll chat to the latest message on startup if there's history
        if (!messageList.isEmpty()) {
            chatRecyclerView.smoothScrollToPosition(messageList.size() - 1);
        }
    }

    private void processMessage(String message) {
        // Disable input elements to prevent multiple sends and show a loading state
        setChatInputEnabled(false);
        aiService.createChat(message, new AiService.ChatCallback() {
            @Override
            public void onSuccess(String result) {
                addMessage(result, Message.SENT_BY_BOT);
                setChatInputEnabled(true);
            }
            @Override
            public void onFailure(Throwable t) {
                String errorMessage = "System error, please try again later!";
                addMessage(errorMessage, Message.SENT_BY_BOT);
                setChatInputEnabled(true);
            }
        });
    }

    private void setChatInputEnabled(boolean enabled) {
        sendButton.setEnabled(enabled);
        messageEditText.setEnabled(enabled);
    }

    private void addMessage(String message, int sentBy) {
        messageList.add(new Message(message, sentBy));
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        chatRecyclerView.smoothScrollToPosition(messageList.size() - 1);
    }
} 