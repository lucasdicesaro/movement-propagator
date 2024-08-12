package client;

import java.net.*;
import java.util.Set;
import java.util.logging.Logger;

import messages.MessageContainer;
import messages.MessageHandler;

public class ClientUDP implements Runnable {

    private static final Logger logger;
    private DatagramSocket udpSocket;
    private int clientId;

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tF %1$tT %3$s %4$-7s %5$s%n");
        logger = Logger.getLogger("ClientUDP");
    }

    public ClientUDP(DatagramSocket udpSocket, int clientId) {
        this.udpSocket = udpSocket;
        this.clientId = clientId;
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (true) {
                udpSocket.receive(packet);
                String receivedMessage = new String(packet.getData(), 0, packet.getLength());
                logger.info("Recibido [" + receivedMessage + "]");
                MessageContainer messageContainer = MessageHandler.parseMessage(receivedMessage);
                if (clientId == Integer.parseInt(messageContainer.getClientId())) {
                    logger.info("Soy yo mismo");
                }
                if (messageContainer.isConnected()) {
                    logger.info("Se ha conectado: " + messageContainer.getClientId());
                } else if (messageContainer.isDisconnected()) {
                    logger.info("Se ha desconectado: " + messageContainer.getClientId());
                } else if (messageContainer.isChat()) {
                    logger.info("[" + messageContainer.getClientId() + "]: " + messageContainer.getContent());
                } else if (messageContainer.isList()) {
                    dumpClients(messageContainer.getClients());
                }
                logger.info("");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dumpClients(Set<messages.Client> clients) {
        for (messages.Client client : clients) {
            logger.info(client.toString());
        }
    }
}
