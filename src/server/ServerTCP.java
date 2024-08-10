package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class ServerTCP implements Runnable {

    private static final Logger logger;
    private static final int TCP_PORT = 12345;
    private static final AtomicInteger clientIdCounter = new AtomicInteger(1);

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tF %1$tT %3$s %4$-7s %5$s%n");
        logger = Logger.getLogger("ServerTCP");
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                int clientId = clientIdCounter.getAndIncrement();
                logger.info("Cliente conectado (ID: " + clientId + "): "
                        + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

                new Thread(new ClientHandler(clientSocket, clientId)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
