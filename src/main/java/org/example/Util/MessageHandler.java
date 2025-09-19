package org.example.Util;

import org.example.Model.User;
import org.example.Database.Interfaces.IUserDatabase;
import org.example.User.ILoginAuthentification;
import java.io.PrintWriter;
import java.util.Map;

public class MessageHandler {
    private Map<Integer, User> userMap;
    private ILoginAuthentification loginAuth;
    private IUserDatabase userDatabase;

    public MessageHandler(Map<Integer, User> userMap) {
        this.userMap = userMap;
    }

    public void handleMessage(MessageParser.ParsedMessage message, PrintWriter out) {
        try {
            switch (message.getMessageType().toUpperCase()) {
                case "LOGIN":
                    handleLogin(message, out);
                    break;
                case "REGISTER":
                    String[] reg = message.getPayload().split("\\|");
                    if (reg.length == 2) {
                        loginAuth.handleRegister(reg[0], reg[1], out);
                    } else {
                        out.println("Error: Invalid register format");
                    }
                    break;
                case "LOGOUT":
                    loginAuth.handleLogout(message.getPayload(), out);
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
        } catch (Exception e) {
            out.println("Server error");
            e.printStackTrace();
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

    private void handleUnknown(MessageParser.ParsedMessage message) {
        System.out.println("ERR|Unknown command: " + message.getMessageType());
    }

    private void handleText(MessageParser.ParsedMessage message) {
        if (message.getClientId() != null) {
            try {
                int clientId = Integer.parseInt(message.getClientId());
                User user = userMap.get(clientId);
                if (user != null) {
                    String username = user.getUsername();
                    String formattedMessage = String.format("MESSAGE|%s|%s", username, message.getPayload());
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

    }
}