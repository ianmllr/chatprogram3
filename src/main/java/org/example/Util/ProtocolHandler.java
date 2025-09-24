package org.example.Util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProtocolHandler {

    public void sendLogin(String username, String password) {
        String message = formatMessage("LOGIN", username + "|" + password);
        out.println(message);
    }

    public void sendRegister(String username, String password) {
        String message = formatMessage("REGISTER", username + "|" + password);
        out.println(message);
    }

    public void sendLogout() {
        String message = formatMessage("LOGOUT", "logout");
        out.println(message);
    }

    public void sendTextMessage(String text) {
        String message = formatMessage("TEXT", text);
        out.println(message);
    }

    public void sendJoinRoom(String roomName) {
        String message = formatMessage("JOIN_ROOM", roomName);
        out.println(message);
    }

    public void sendLeaveRoom() {
        String message = formatMessage("LEAVE_ROOM", "");
        out.println(message);
    }

    public void sendListUsers() {
        String message = formatMessage("LIST_USERS", "");
        out.println(message);
    }

    public void sendListRooms() {
        String message = formatMessage("LIST_ROOMS", "");
        out.println(message);
    }

    public String formatMessage(String messageType, String payload) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return clientId + "|" + timestamp + "|" + messageType + "|" + payload;
    }
}
