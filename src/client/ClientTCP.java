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

            String clientName = chooseName(teclado);

            // Enviar al servidor el puerto UDP asignado por el SO
            DatagramSocket udpSocket = new DatagramSocket();
            String udpPortPackage = MessageHandler.packUDPPort(udpSocket.getLocalPort());
            salida.println(udpPortPackage);
            logger.info("Enviado UDP port package [" + udpPortPackage + "]");

            // Enviar al servidor el nombre del cliente
            String clientNameMessage = MessageHandler.packClientName(clientName);
            salida.println(clientNameMessage);
            logger.info("Enviado client name package [" + clientNameMessage + "]");

            int clientId = Integer.parseInt(messageContainer.getClientId());

            Runnable clientUDP = new ClientUDP(udpSocket, clientId);
            new Thread(clientUDP).start();

            Runnable clientTCPReader = new ClientTCPReader(socket, entrada);
            new Thread(clientTCPReader).start();

            String payload;
            while ((payload = teclado.readLine()) != null) {
                if (!payload.isBlank()) {
                    String message = "";
                    switch (payload) {
                        case "Y":
                            payload = teclado.readLine();
                            if (!payload.isBlank()) {
                                message = MessageHandler.packChat(clientId, payload);
                            }
                            break;
                        case "A":
                        case "S":
                        case "D":
                        case "W":
                            message = MessageHandler.packMovement(clientId, payload);
                            break;
                        default:
                            System.out.println("Ingrese A, S, D, W o Y (para mandar un mensaje)");

                    }
                    if (!message.isBlank()) {
                        salida.println(message);
                        logger.info("Enviado  [" + message + "]");
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    final static int MAX_LENGTH = 12; // Máximo número de caracteres permitidos

    public static String chooseName(BufferedReader keyboard) throws IOException {
        String name;

        // Expresión regular para validar alfanumérico sin espacios
        String pattern = "^[a-zA-Z0-9]+$";
        boolean valid;

        do {
            System.out.println("Nombre:");
            name = keyboard.readLine();

            // Verifica si la entrada cumple con el patrón
            valid = name.matches(pattern) && name.length() <= MAX_LENGTH;

            if (!valid) {
                System.out.println("Introduce solo caracteres alfanuméricos sin espacios y menor a " + MAX_LENGTH);
            }
        } while (!valid);

        return name;
    }
}
