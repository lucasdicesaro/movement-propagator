package messages;

import java.util.Set;

public class MessageHandler {

  private static final String UDP_PORT_MESSAGE_PATTERN = "UDP:<udpPort>";
  private static final String CLIENT_ID_MESSAGE_PATTERN = "NOD:<clientId>";
  private static final String ACTION_MOVEMENT_PATTERN = "NOD:<clientId>|TYP:MOV=<content>";
  private static final String CHAT_PATTERN = "NOD:<clientId>|TYP:MSG=<content>";
  private static final String CLIENT_LIST_PATTERN = "NOD:<dummy>|TYP:LST=<clientList>";
  private static final String CLIENT_LIST_ITEM_PATTERN = "C_<clientId>#X_<coordX>#Y_<coordY>";
  private static final String CLIENT_CONNECTED_PATTERN = "NOD:<clientId>|CTRL:CON";
  private static final String CLIENT_DISCONNECTED_PATTERN = "NOD:<clientId>|CTRL:DIS";

  // From client:
  // "NOD:<id>|TYP:MSG=<content>" // Chat
  // "NOD:<id>|TYP:MOV=U" // Movement UP
  // "NOD:<id>|TYP:MOV=D"
  // "NOD:<id>|TYP:MOV=L"
  // "NOD:<id>|TYP:MOV=R"
  //
  // From server:
  // Client list
  // "NOD:<id>|TYP:LST=C_00002#X_-0010#Y_00006;C_00001#X_-0001#Y_00001"

  public static String packUDPPort(int udpPort) {
    return UDP_PORT_MESSAGE_PATTERN.replaceFirst("<udpPort>", String.valueOf(udpPort));
  }

  public static int unpackPortUDP(String udpPortPackage) {
    String udpPort = udpPortPackage.split(":")[1];
    return Integer.parseInt(udpPort);
  }

  public static String packClientId(int clientId) {
    return replaceClientId(clientId, CLIENT_ID_MESSAGE_PATTERN);
  }

  public static String packMovement(int clientId, String content) {
    return replaceClientId(clientId, ACTION_MOVEMENT_PATTERN).replaceFirst("<content>", content);
  }

  public static String packChat(int clientId, String content) {
    return replaceClientId(clientId, CHAT_PATTERN).replaceFirst("<content>", content);
  }

  public static String packClientConnected(int clientId) {
    return replaceClientId(clientId, CLIENT_CONNECTED_PATTERN);
  }

  public static String packClientDisconnected(int clientId) {
    return replaceClientId(clientId, CLIENT_DISCONNECTED_PATTERN);
  }

  public static String packClientList(Set<server.Client> clients) {
    String clientList = "";
    int i = 0;
    int clientSize = clients.size();
    for (server.Client client : clients) {
      clientList += CLIENT_LIST_ITEM_PATTERN.replaceFirst("<clientId>", String.format("%05d", client.getId()))
          .replaceFirst("<coordX>", String.format("%05d", client.getX()))
          .replaceFirst("<coordY>", String.format("%05d", client.getY()));
      if (i < clientSize - 1) {
        clientList += ";";
      }
      i++;
    }

    return CLIENT_LIST_PATTERN.replaceFirst("<dummy>", String.format("%05d", 0))
        .replaceFirst("<clientList>", clientList);
  }

  public static MessageContainer parseMessage(String message) {
    String[] tokens = message.split("\\|");
    MessageContainer messageContainer = new MessageContainer();
    for (String token : tokens) {
      parseToken(token, messageContainer);
    }
    return messageContainer;
  }

  public static void parseToken(String token, MessageContainer messageContainer) {
    String[] subtokens = token.split(":");
    if (subtokens.length < 2) {
      // Se espera que cada token tenga el formato <key>:<value>
      // Se ignora si no tiene ese formato.
      return;
    }

    String key = subtokens[0];
    String value = subtokens[1];

    switch (key) {
      case "NOD":
        parseClientId(value, messageContainer);
        break;
      case "TYP":
        parseType(value, messageContainer);
        break;
      case "CTRL":
        parseControl(value, messageContainer);
        break;
      default:
        throw new IllegalArgumentException("Etiqueta invalida: [" + key + "]");
    }
  }

  public static void parseClientId(String clientId, MessageContainer messageContainer) {
    messageContainer.setClientId(clientId);
  }

  public static void parseControl(String subtoken, MessageContainer messageContainer) {
    switch (subtoken) {
      case "CON":
        messageContainer.setConnected(true);
        break;
      case "DIS":
        messageContainer.setDisconnected(true);
        break;
    }
  }

  public static void parseType(String subtoken, MessageContainer messageContainer) {
    String[] subsubtokens = subtoken.split("=");
    if (subsubtokens.length < 2) {
      return;
    }

    String type = subsubtokens[0];
    String value = subsubtokens[1];

    switch (type) {
      case "MSG":
        messageContainer.setChat(true);
        messageContainer.setContent(value);
        break;
      case "MOV":
        messageContainer.setContent(value);
        break;
      case "LST":
        messageContainer.setList(true);
        parseClientList(value, messageContainer);
        break;
      default:
        throw new IllegalArgumentException("Tipo de mensaje invalido: [" + type + "]");
    }
  }

  public static void parseClientList(String list, MessageContainer messageContainer) {
    // "NOD:<id>|TYP:LST=C_00002#X_-0010#Y_00006;C_00001#X_-0001#Y_00001"
    String[] clients = list.split(";");
    for (String client : clients) {
      String[] attributes = client.split("#");
      int clientId = -1;
      int x = -1;
      int y = -1;
      for (String attribute : attributes) {
        String[] keyValue = attribute.split("_");
        String key = keyValue[0];
        String value = keyValue[1];
        switch (key) {
          case "C":
            clientId = Integer.parseInt(value);
            break;
          case "X":
            x = Integer.parseInt(value);
            break;
          case "Y":
            y = Integer.parseInt(value);
            break;
        }
      }
      messageContainer.addClient(new messages.Client(clientId, x, y));
    }
  }

  private static String replaceClientId(int clientId, String pattern) {
    return pattern.replaceFirst("<clientId>", String.format("%05d", clientId));
  }
}
