package hcmute.edu.vn.healthtracking.services;

import android.content.Context;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.java.ChatFutures;
import com.google.firebase.ai.java.GenerativeModelFutures;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;
import com.google.firebase.ai.type.GenerativeBackend;

import java.util.List;
import java.util.concurrent.Executor;

public class AiService {
    // Use the GenerativeModelFutures Java compatibility layer which offers
    // support for ListenableFuture and Publisher APIs
    private static final GenerativeModelFutures model = GenerativeModelFutures.from(
            FirebaseAI.getInstance(GenerativeBackend.googleAI())
                    .generativeModel(
                            "gemini-2.0-flash",
                            null,
                            null,
                            null,
                            null,
                            new Content.Builder().addText("You are a helpful and empathetic health assistant. " +
                                    "Your primary goal is to provide accurate and general health information, " +
                                    "wellness tips, and answer questions related to health and lifestyle. " +
                                    "Do not give medical advice, diagnose conditions, or prescribe treatments. " +
                                    "Always advise users to consult a qualified healthcare professional for personal medical concerns. " +
                                    "Maintain a positive, encouraging, and supportive tone.").build()));

    // Chat history
    private static ChatFutures chatSession;

    // Executor to execute call back
    private final Executor mainExecutor;

    public AiService(Context context) {
        mainExecutor = ContextCompat.getMainExecutor(context);
    }

    // Start a new chat session
    // This should be called before using
    private void startNewChat() {
        // The initial greeting for the AI model's context.
        // This is only for the AI's internal history, not for the UI
        List<Content> history = List.of(new Content.Builder().
                setRole("model").
                addText("Hello! I'm your health assistant. How can I help you today?").
                build());
        chatSession = model.startChat(history);
    }

    // Callback interface to handle the result
    public interface ChatCallback {
        void onSuccess(String result);

        void onFailure(Throwable t);
    }

    // Create a chat to model
    public void createChat(String input, ChatCallback callback) {
        // Ensure a chat session exists before sending messages
        if (chatSession == null) {
            this.startNewChat();
        }

        // Provide a prompt that contains text
        Content prompt = new Content.Builder()
                .setRole("user")
                .addText(input)
                .build();

        // Generate content asynchronously
        ListenableFuture<GenerateContentResponse> response = chatSession.sendMessage(prompt);

        Futures.addCallback(response, new FutureCallback<>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                if (resultText != null && !resultText.isEmpty()) {
                    callback.onSuccess(resultText);
                } else {
                    String warnMessage = "AI response text was null or empty.";
                    callback.onFailure(new IllegalStateException(warnMessage));
                }
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                callback.onFailure(t);
            }
        }, mainExecutor);
    }
}