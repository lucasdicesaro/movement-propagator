package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import messages.MessageContainer;
import messages.MessageHandler;
import utils.NetworkUtils;

public class ClientHandler implements Runnable {

    private static final Logger logger;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private int clientId;
    private static Set<Client> clients = new HashSet<>();

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tF %1$tT %3$s %4$-7s %5$s%n");
        logger = Logger.getLogger("ClientHandler");
    }

    public ClientHandler(Socket socket, int clientId) {
        this.clientSocket = socket;
        this.clientId = clientId;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            // Enviar el ID al cliente
            String clientIdPackage = MessageHandler.packMessage(clientId, "");
            out.println(clientIdPackage);
            logger.info("Handshake - Enviado  [" + clientIdPackage + "]");

            // Recibir el puerto UDP del cliente "UDP:<udpPort>"
            String udpPortPackage = in.readLine();
            logger.info("Handshake - Recibido [" + udpPortPackage + "]");
            int clientUdpPort = MessageHandler.unpackPortUDP(udpPortPackage);

            synchronized (clients) {
                clients.add(new Client(clientId, clientSocket, clientUdpPort, out));
            }

            String messageFromClient;
            while ((messageFromClient = in.readLine()) != null) {
                logger.info("Recibido [" + messageFromClient + "]");
                MessageContainer messageContainer = MessageHandler.parseMessage(messageFromClient);
                synchronized (clients) {
                    String messagePackage = MessageHandler.packMessage(messageContainer);
                    NetworkUtils.broadcastMessage(messagePackage, clients);
                    logger.info("Enviado  [" + messagePackage + "]\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            logger.info("El cliente [" + clientId + "] se ha desconectado");
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            synchronized (clients) {
                clients.removeIf(c -> c.getId() == clientId);
                // Se informa al resto de los clientes que clientId se ha desconectado
                String messagePackage = MessageHandler.packClientDown(clientId);
                NetworkUtils.broadcastMessage(messagePackage, clients);
                logger.info("Enviado  [" + messagePackage + "]\n");
            }
        }
    }

    public static class Client {
        private int id;
        private InetAddress clientAddress;
        private int clientTcpPort;
        private int clientUdpPort;
        private PrintWriter writer;

        public Client(int id, Socket clientSocket, int clientUdpPort,
                PrintWriter writer) {
            this.id = id;
            this.clientAddress = clientSocket.getInetAddress();
            this.clientTcpPort = clientSocket.getPort();
            this.clientUdpPort = clientUdpPort;
            this.writer = writer;
        }

        public int getId() {
            return id;
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

        @Override
        public String toString() {
            return "CID:" + id + "|IP:" + clientAddress + "|TCP:" + clientTcpPort + "|UDP:" + clientUdpPort;
        }
    }
}
