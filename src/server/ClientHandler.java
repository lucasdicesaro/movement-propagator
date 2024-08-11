package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
            String clientIdPackage = MessageHandler.packClientId(clientId);
            out.println(clientIdPackage);
            logger.info("Handshake - Enviado  [" + clientIdPackage + "]");

            // Recibir el puerto UDP del cliente "UDP:<udpPort>"
            String udpPortPackage = in.readLine();
            logger.info("Handshake - Recibido [" + udpPortPackage + "]");
            int clientUdpPort = MessageHandler.unpackPortUDP(udpPortPackage);

            // Se informa al resto de los clientes que clientId se ha conectado
            String messagePackage = MessageHandler.packClientConnected(clientId);
            NetworkUtils.broadcastMessage(messagePackage, clients);
            logger.info("Enviado  [" + messagePackage + "]\n");

            synchronized (clients) {
                clients.add(new Client(clientId, clientSocket, clientUdpPort, out));
            }

            String messageFromClient;
            while ((messageFromClient = in.readLine()) != null) {
                logger.info("Recibido [" + messageFromClient + "]");
                MessageContainer messageContainer = MessageHandler.parseMessage(messageFromClient);
                synchronized (clients) {
                    // Se busca el cliente que mando el mensaje
                    Client client = clients.stream()
                            .filter(each -> each.getId() == Integer.parseInt(messageContainer.getClientId()))
                            .findFirst()
                            .orElse(null);

                    // Actualizar coordenadas
                    switch (messageContainer.getPayload().getContent()) {
                        case "A":
                            client.moveLeft();
                            break;
                        case "S":
                            client.moveDown();
                            break;
                        case "D":
                            client.moveRight();
                            break;
                        case "W":
                            client.moveUp();
                            break;
                    }
                    switch (messageContainer.getPayload().getContent()) {
                        case "A":
                        case "S":
                        case "D":
                        case "W":
                            logger.info("El cliente [" + client.getId() + "] mando un movimiento ["
                                    + messageContainer.getPayload().getContent() + "]\n");
                            messagePackage = MessageHandler.packClientList(clients);
                            dumpClients(clients);
                            break;
                        default:
                            logger.info("El cliente [" + client.getId() + "] mando un mensaje ["
                                    + messageContainer.getPayload().getContent() + "]\n");
                            messagePackage = MessageHandler.packChat(clientId,
                                    messageContainer.getPayload().getContent());

                    }

                    NetworkUtils.broadcastMessage(messagePackage, clients);
                    logger.info("Enviado  [" + messagePackage + "]\n");
                }
            }
        } catch (IOException e) {
            logger.warning("Posible cliente desconectado " + e.getMessage());
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
                String messagePackage = MessageHandler.packClientDisconnected(clientId);
                NetworkUtils.broadcastMessage(messagePackage, clients);
                logger.info("Enviado  [" + messagePackage + "]\n");
            }
        }
    }

    public void dumpClients(Set<Client> clients) {
        for (Client client : clients) {
            System.out.println(client);
        }
    }
}
