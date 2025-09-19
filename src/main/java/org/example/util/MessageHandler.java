package org.example.util;

import org.example.Model.User;
import java.util.Map;

public class MessageHandler {
    private Map<Integer, User> userMap;

    public MessageHandler(Map<Integer, User> userMap) {
        this.userMap = userMap;
    }

    public void handleMessage(MessageParser.ParsedMessage message) {
        switch (message.getMessageType()) {
            case "LOGIN":
                handleLogin(message);
                break;
            case "TEXT":
                handleText(message);
                break;
            case "FILE_TRANSFER":
                handleFileTransfer(message);
                break;
            default:
                handleUnknown(message);
        }
    }

    private void handleLogin(MessageParser.ParsedMessage message) {
        // Add user to userMap, send welcome, etc.
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
