package client;

import java.io.*;
import java.net.*;
import java.util.logging.Logger;

import messages.MessageContainer;
import messages.MessageHandler;

public class ClientTCP implements Runnable {

    private static final Logger logger;
    private String serverAddress;
    private int tcpPort;

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tF %1$tT %3$s %4$-7s %5$s%n");
        logger = Logger.getLogger("ClientTCP");
    }

    public ClientTCP(String serverAddress, int tcpPort) {
        this.serverAddress = serverAddress;
        this.tcpPort = tcpPort;
    }

    @Override
    public void run() {

        try (Socket socket = new Socket(serverAddress, tcpPort);
                BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in))) {

            // Leer el ClientId asignado por el servidor
            String clientIdPackage = entrada.readLine();
            logger.info("Recibido ClientId package [" + clientIdPackage + "]");

            MessageContainer messageContainer = MessageHandler.parseMessage(clientIdPackage);
            logger.info("ClientId asignado por el servidor: " + messageContainer.getClientId());

            // Enviar al servidor el puerto UDP asignado por el SO
            DatagramSocket udpSocket = new DatagramSocket();
            String udpPortPackage = MessageHandler.packUDPPort(udpSocket.getLocalPort());
            salida.println(udpPortPackage);
            logger.info("Enviado UDP port package [" + udpPortPackage + "]");

            int clientId = Integer.parseInt(messageContainer.getClientId());

            Runnable clientUDP = new ClientUDP(udpSocket, clientId);
            new Thread(clientUDP).start();

            Runnable clientTCPReader = new ClientTCPReader(socket, entrada);
            new Thread(clientTCPReader).start();

            String payload;
            while ((payload = teclado.readLine()) != null) {
                if (!payload.isBlank()) {
                    String message = MessageHandler.packMessage(clientId, payload);
                    salida.println(message);
                    logger.info("Enviado  [" + message + "]");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
