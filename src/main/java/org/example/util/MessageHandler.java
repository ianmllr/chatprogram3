package org.example.util;

import org.example.Model.User;
import org.example.database.interfaces.IUserDatabase;
import org.example.user.ILoginAuthentification;

import java.io.PrintWriter;
import java.util.Map;

public class MessageHandler {
    private Map<Integer, User> userMap;
    ILoginAuthentification loginAuth;
    ILoginAuthentification handleRegister;
    ILoginAuthentification handleLogout;
    IUserDatabase userDatabase;

    public MessageHandler(Map<Integer, User> userMap) {
        this.userMap = userMap;
    }

    public void handleMessage(MessageParser.ParsedMessage message, PrintWriter out) {
        switch (message.getMessageType().toUpperCase()) {
            case "LOGIN":
                String[] login = message.getPayload().split("\\|");
                loginAuth.handleLogin(login[0], login[1], out);
                break;

            case "REGISTER":
                String[] reg = message.getPayload().split("\\|");
                loginAuth.handleRegister(reg[0], reg[1], out);
                break;

            case "LOGOUT":
                loginAuth.handleLogout(message.getPayload(), out);
                break;

            case "TEXT":
                out.println("MSG|" + message.getPayload());
                break;

            case "FILE_TRANSFER":
                out.println("FILE|" + message.getPayload());
                break;

            default:
                out.println("ERR|Unknown command");
        }
    }

    private void handleLogin(MessageParser.ParsedMessage message, PrintWriter printWriter) {
        String[] credentials = message.getPayload().split("\\|");
        if (credentials.length == 2) {
            String username = credentials[0];
            String password = credentials[1];
            loginAuth.handleLogin(username, password, printWriter);
        } else {
            printWriter.println("Error: Invalid login payload");
        }
    }

    private void handleText(MessageParser.ParsedMessage message) {
        if (message.getClientId() != null) {
            try {

                // får clientId'et og parser det til en int
                int clientId = Integer.parseInt(message.getClientId());

                // tjekker Mappet med brugere der er connected lige nu, og får fat i den bruger der har sendt beskeden
                User user = userMap.get(clientId);
                if (user != null) {
                    String username = user.getUsername();
                    String formattedMessage = String.format("MESSAGE|%s|%s", username, message.getPayload());
                    // Broadcast to all users except sender
                    userMap.values().forEach(u -> {
                        if (u.getClientId() != clientId) {
                            try {
                                u.getSocket().getOutputStream().write((formattedMessage + "\n").getBytes());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleFileTransfer(MessageParser.ParsedMessage message) {
        // Handle file transfer logic
    }

    private void handleUnknown(MessageParser.ParsedMessage message) {
        // Respond with error or ignore
    }
}
