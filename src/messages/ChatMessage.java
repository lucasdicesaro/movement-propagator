package messages;

public class ChatMessage implements Payload {
    private String message;

    public ChatMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String getContent() {
        return message;
    }
}
