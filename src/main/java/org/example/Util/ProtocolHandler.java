package org.example.Util;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProtocolHandler {


    private final PrintWriter out;

    public ProtocolHandler(PrintWriter out) {
        this.out = out;
    }

    public void sendLogin(String clientId, String username, String password) {
        String message = formatMessage(clientId,"LOGIN", username + "|" + password);
        out.println(message);
    }

    public void sendRegister(String clientId, String username, String password) {
        String message = formatMessage(clientId, "REGISTER", username + "|" + password);
        out.println(message);
    }

    public void sendLogout(String clientId) {
        String message = formatMessage(clientId, "LOGOUT", "logout");
        out.println(message);
    }

    public void sendTextMessage(String clientId, String text) {
        String message = formatMessage(clientId, "TEXT", text);
        out.println(message);
    }

    public void sendJoinRoom(String clientId, String roomName) {
        String message = formatMessage(clientId, "JOIN_ROOM", roomName);
        out.println(message);
    }

    public void sendLeaveRoom(String clientId) {
        String message = formatMessage(clientId, "LEAVE_ROOM", "");
        out.println(message);
    }

    public void sendListUsers(String clientId) {
        String message = formatMessage(clientId, "LIST_USERS", "");
        out.println(message);
    }

    public void sendListRooms(String clientId) {
        String message = formatMessage(clientId, "LIST_ROOMS", "");
        out.println(message);
    }

    public String formatMessage(String clientId, String messageType, String payload) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return clientId + "|" + timestamp + "|" + messageType + "|" + payload;
    }
}
