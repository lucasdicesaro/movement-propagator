package messages;

public class Client {
    private int id;
    private int x;
    private int y;

    public Client(int id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "NOD:" + id + "|X:" + String.format("%04d", x) + "|Y:" + String.format("%04d", y);
    }
}
