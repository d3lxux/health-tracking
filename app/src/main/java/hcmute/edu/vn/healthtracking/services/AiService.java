package hcmute.edu.vn.healthtracking.services;

import androidx.annotation.NonNull;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.GenerativeModel;
import com.google.firebase.ai.java.GenerativeModelFutures;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;
import com.google.firebase.ai.type.GenerativeBackend;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.Executor;

public class AiService {
    // Initialize the Gemini Developer API backend service
    GenerativeModel ai = FirebaseAI.getInstance(GenerativeBackend.googleAI())
            .generativeModel("gemini-2.0-flash");

    GenerativeModelFutures model = GenerativeModelFutures.from(ai);
    private final Executor mainExecutor = MoreExecutors.directExecutor();

    // Callback interface to handle the result
    public interface ChatCallback {
        void onSuccess(String result);
        void onFailure(Throwable t);
    }

    public void createChat(String input, ChatCallback callback) {
        // Provide a prompt that contains text
        Content prompt = new Content.Builder()
                .addText(input)
                .build();

        // Generate content asynchronously
        ListenableFuture<GenerateContentResponse> response = model.generateContent(prompt);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                callback.onSuccess(resultText);
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                callback.onFailure(t);
            }
        }, mainExecutor);
    }
}