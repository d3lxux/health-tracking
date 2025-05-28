package hcmute.edu.vn.healthtracking.repositories;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.healthtracking.models.Message;

public class ChatRepositories {
    private static List<Message> messages;

    public static List<Message> getMessages() {
        if (messages == null) {
            messages = new ArrayList<>();
            messages.add(new Message("Hello! I'm your health assistant. How can I help you today?", Message.SENT_BY_BOT));
        }
        return messages;
    }
}
