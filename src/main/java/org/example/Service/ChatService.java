package org.example.Service;

import org.example.Util.SimpleLogger;

import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatService {
    public static final Map<String, Set<String>> chatRooms = new ConcurrentHashMap<>();
    public static final Map<String, String> userCurrentRoom = new ConcurrentHashMap<>();
    private final MessageService messageService = new MessageService();
    public void joinRoom(String username, String roomName, PrintWriter out) {
        leaveCurrentRoom(username);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        chatRooms.computeIfAbsent(roomName, k -> ConcurrentHashMap.newKeySet()).add(username);
        userCurrentRoom.put(username, roomName);

        SimpleLogger.getInstance().log("User " + username + " joined room: " + roomName);

        out.println("Joined room: " + roomName);
        broadcastToRoom(roomName, "SERVER", timestamp.toString(),username + " joined the room");
    }

    public void leaveCurrentRoom(String username) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String currentRoom = userCurrentRoom.remove(username);
        if (currentRoom != null) {
            Set<String> usersInRoom = chatRooms.get(currentRoom);
            if (usersInRoom != null) {
                usersInRoom.remove(username);
                if (usersInRoom.isEmpty()) {
                    chatRooms.remove(currentRoom);
                    SimpleLogger.getInstance().log("User " + username + " left room: " + currentRoom);

                } else {
                    broadcastToRoom(currentRoom, "SERVER", timestamp.toString(),username + " left the room");
                }
            }
        }
    }

    public void createRoom(String roomName, PrintWriter out, String creatorUsername) {
        if (roomName == null || roomName.isEmpty()) {
            out.println("Error: Room name cannot be empty");
            return;
        }

        if (chatRooms.containsKey(roomName)) {
            out.println("Room '" + roomName + "' already exists");
            return;
        }

        chatRooms.putIfAbsent(roomName, ConcurrentHashMap.newKeySet());
        out.println("Room created: " + roomName);
        out.println("Use /join " + roomName + " to join the room");
        SimpleLogger.getInstance().log("Room named: " + roomName + " created at " + new Timestamp(System.currentTimeMillis()) + " by user: " + creatorUsername);
    }

    public void joinDefaultRoom(String username, PrintWriter out) {
        joinRoom(username, "general", out);
    }

    public void broadcastToRoom(String roomName, String senderUsername, String timestamp, String messageText) {
        Set<String> usersInRoom = chatRooms.get(roomName);
        if (usersInRoom == null) return;

        String formattedMessage = "[" + roomName + "] " + "[" + timestamp + "]: " + senderUsername + ": " + messageText;

        for (String username : usersInRoom) {
            if (!username.equals(senderUsername)) {
                messageService.sendMessageToUser(username, formattedMessage);
                SimpleLogger.getInstance().log("User: " + username + " sent message: " + formattedMessage);
            }
        }
    }

    public void ListRoomUsers(String currentRoom, PrintWriter out) {
        if (currentRoom == null) {
            out.println("You are not in any room");
            return;
        }
        Set<String> usersInRoom = ChatService.chatRooms.get(currentRoom);
        if (usersInRoom == null || usersInRoom.isEmpty()) {
            out.println("No users in room: " + currentRoom);
        } else {
            out.println("Users in room '" + currentRoom + "': " + String.join(", ", usersInRoom));
        }
    }

}
