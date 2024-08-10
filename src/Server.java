
import java.util.logging.Logger;

import server.DiscoveryServer;
import server.ServerTCP;

public class Server {

    private static final Logger logger;

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tF %1$tT %3$s %4$-7s %5$s%n");
        logger = Logger.getLogger("Server");
    }

    public static void main(String[] args) {
        startServers();
    }

    private static void startServers() {
        logger.info("Servidor-Discovery iniciado...");
        new Thread(new DiscoveryServer()).start();

        logger.info("Servidor TCP iniciado...");
        new Thread(new ServerTCP()).start();
    }
}
