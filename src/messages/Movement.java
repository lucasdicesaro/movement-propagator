package messages;

public class Movement implements Payload {
    private String movement;

    public Movement(String movement) {
        this.movement = movement;
    }

    public String getMovement() {
        return movement;
    }

    @Override
    public String getContent() {
        return movement;
    }
}
