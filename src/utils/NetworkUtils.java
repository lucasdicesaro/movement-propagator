package utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Set;

import server.ClientHandler.Client;

public class NetworkUtils {

  public static void broadcastMessage(String message, Set<Client> clients) {
    try (DatagramSocket udpSocket = new DatagramSocket()) {
      udpSocket.setBroadcast(true);
      byte[] buffer = message.getBytes();

      for (Client client : clients) {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, client.getClientAddress(),
            client.getClientUdpPort());
        udpSocket.send(packet);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
