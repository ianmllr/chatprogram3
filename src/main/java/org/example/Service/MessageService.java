package org.example.Service;

import org.example.Model.User;
import org.example.Server.Server;

import java.io.PrintWriter;

public class MessageService {

    public void sendMessageToUser(String username, String message) {
        for (User user : Server.userMap.values()) {
            if (user.getUsername().equals(username)) {
                try {
                    PrintWriter userOut = new PrintWriter(user.getSocket().getOutputStream(), true);
                    userOut.println(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public void handlePrivateMessage(String sender, String recipient, String message) {
        String formattedMessage = "[PM] " + sender + ": " + message;
        sendMessageToUser(recipient, formattedMessage);
    }

    public boolean checkForEmoji(String payload) {
        return payload.contains(":smile:") || payload.contains(":sad:") || payload.contains(":naughty:") || payload.contains(":skull:");
    }

    public String convertToEmoji(String payload) {
        payload = payload.replace(":smile", new String(Character.toChars(0x1F604))); // 😄
        payload = payload.replace(":sad:", new String(Character.toChars(0x1F641))); // 🙁
        payload = payload.replace(":naughty:", new String(Character.toChars(0x1F608	))); // 😈
        payload = payload.replace(":skull:", new String(Character.toChars(0x1F480))); // 💀
        return payload;
    }

}
