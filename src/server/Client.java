package server;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
    private int id;
    private String name;
    private InetAddress clientAddress;
    private int clientTcpPort;
    private int clientUdpPort;
    private int x;
    private int y;
    private PrintWriter writer;

    public Client(int id, String name, Socket clientSocket, int clientUdpPort,
            PrintWriter writer) {
        this.id = id;
        this.name = name;
        this.clientAddress = clientSocket.getInetAddress();
        this.clientTcpPort = clientSocket.getPort();
        this.clientUdpPort = clientUdpPort;
        this.writer = writer;
        this.x = 0;
        this.y = 0;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public InetAddress getClientAddress() {
        return clientAddress;
    }

    public int getClientTcpPort() {
        return clientTcpPort;
    }

    public int getClientUdpPort() {
        return clientUdpPort;
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public void moveUp() {
        this.y--;
    }

    public void moveDown() {
        this.y++;
    }

    public void moveRight() {
        this.x++;
    }

    public void moveLeft() {
        this.x--;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return "NOD:" + id + "|NAME:" + name + "|IP:" + clientAddress + "|TCP:" + clientTcpPort + "|UDP:"
                + clientUdpPort + "|X:" + String.format("%04d", x) + "|Y:" + String.format("%04d", y);
    }
}
