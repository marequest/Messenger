package doitgames.example.whatsappproject.Chat;

import java.util.ArrayList;

public class MessageObject {

    private String messageId;
    private String senderId;
    private String message;
    private String timestamp;

    ArrayList<String> mediaUrlList;

    public MessageObject(String messageId, String senderId, String message) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.message = message;
        this.mediaUrlList = new ArrayList<>();
    }

    public MessageObject(String messageId, String senderId, String message, ArrayList<String> mediaUrlList, String timestamp) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.message = message;
        this.mediaUrlList = mediaUrlList;
        this.timestamp = timestamp;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getMessage() {
        return message;
    }

    public ArrayList<String> getMediaUrlList() {
        return mediaUrlList;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
