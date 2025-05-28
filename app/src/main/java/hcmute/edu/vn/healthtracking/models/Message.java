package hcmute.edu.vn.healthtracking.models;

public class Message {
    public static final int SENT_BY_USER = 0;
    public static final int SENT_BY_BOT = 1;

    private String message;
    private int sentBy;

    public Message(String message, int sentBy) {
        this.message = message;
        this.sentBy = sentBy;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getSentBy() {
        return sentBy;
    }

    public void setSentBy(int sentBy) {
        this.sentBy = sentBy;
    }
} 