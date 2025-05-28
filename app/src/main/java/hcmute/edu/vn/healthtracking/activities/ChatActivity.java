package hcmute.edu.vn.healthtracking.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hcmute.edu.vn.healthtracking.R;
import hcmute.edu.vn.healthtracking.adapters.MessageAdapter;
import hcmute.edu.vn.healthtracking.models.Message;
import hcmute.edu.vn.healthtracking.services.AiService;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private ImageButton sendButton;
    private List<Message> messageList;
    private MessageAdapter messageAdapter;
    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize views
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);

        // Initialize message list and adapter
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList);
        
        // Set up RecyclerView
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(messageAdapter);

        // Initialize handlers
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        // Add welcome message
        addMessage("Hello! I'm your health assistant. How can I help you today?", Message.SENT_BY_BOT);

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
    }

    private void processMessage(String message) {
        AiService aiService = new AiService();
        aiService.createChat("Hello, how are you?", new AiService.ChatCallback() {
            @Override
            public void onSuccess(String result) {
                // Update UI on main thread
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        addMessage(result, Message.SENT_BY_BOT);
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                // Update UI on main thread
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        addMessage("System error, please try again later!", Message.SENT_BY_BOT);
                    }
                });
            }
        });
    }

    private void addMessage(String message, int sentBy) {
        messageList.add(new Message(message, sentBy));
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        chatRecyclerView.smoothScrollToPosition(messageList.size() - 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
} 