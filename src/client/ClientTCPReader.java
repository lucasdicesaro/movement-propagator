package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Logger;

public class ClientTCPReader implements Runnable {

    private static final Logger logger;
    private Socket socket;
    private BufferedReader in;

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tF %1$tT %3$s %4$-7s %5$s%n");
        logger = Logger.getLogger("ClientTCPReader");
    }

    public ClientTCPReader(Socket socket, BufferedReader in) {
        this.socket = socket;
        this.in = in;
    }

    @Override
    public void run() {
        try {
            String tcpMessageFromServer;
            while ((tcpMessageFromServer = in.readLine()) != null) {
                logger.info("\nMensaje del servidor: " + tcpMessageFromServer);
                // Por ahora el Servidor no enviara mensajes TCP al cliente.
            }
        } catch (SocketException e) {
            if (!socket.isClosed()) {
                logger.warning("El servidor se ha desconectado. Saliendo...");
                try {
                    socket.close();
                    System.exit(-1);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            } else {
                logger.warning("Socket cerrado - Proceso interrumpido por el cliente. Saliendo...");
                System.exit(-1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
