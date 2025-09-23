package org.example.Util;

import org.example.Database.Interfaces.IUserDatabase;
import org.example.Model.User;
import org.example.User.ILoginAuthentification;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

public class MessageHandler {
    private Map<Integer, User> userMap;
    private ILoginAuthentification loginAuth;
    private IUserDatabase userDatabase;
    private Socket clientSocket;
    private ClientHandler clientHandler;
    private static final Map<String, Set<String>> chatRooms = new ConcurrentHashMap<>();
    private static final Map<String, String> userCurrentRoom = new ConcurrentHashMap<>();

    public MessageHandler(Map<Integer, User> userMap, ILoginAuthentification loginAuth, Socket clientSocket) {
        this.userMap = userMap;
        this.loginAuth = loginAuth;
        this.clientSocket = clientSocket;
    }

    public MessageHandler(Map<Integer, User> userMap) {
        this.userMap = userMap;
    }

    public void setLoginAuth(ILoginAuthentification loginAuth) {
        this.loginAuth = loginAuth;
    }

    public void setClientSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void setClientHandler(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    public void handleMessage(MessageParser.ParsedMessage message, PrintWriter out) {
        try {
            switch (message.getMessageType().toUpperCase()) {
                case "LOGIN":
                    handleLogin(message, out);
                    break;
                case "REGISTER":
                    handleRegister(message, out);
                    break;
                case "LOGOUT":
                    handleLogout(message, out);
                    break;
                case "TEXT":
                    handleText(message, out);
                    break;
                case "JOIN_ROOM":
                    handleJoinRoom(message, out);
                    break;
                case "CREATE_ROOM":
                    handleCreateRoom(message, out);
                    break;
                case "LEAVE_ROOM":
                    handleLeaveRoom(message, out);
                    break;
                case "LIST_USERS":
                    handleListUsers(out);
                    break;
                case "LIST_ROOMS":
                    handleListRooms(out);
                    break;
                case "FILE_TRANSFER":
                    handleFileTransfer(message);
                    break;
                default:
                    handleUnknown(message, out);
            }
        } catch (Exception e) {
            out.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleLogin(MessageParser.ParsedMessage message, PrintWriter out) {
        if (clientHandler.getLoggedInUsername() != null) {
            out.println("Error: Already logged in as " + clientHandler.getLoggedInUsername());
            return;
        }

        String[] credentials = message.getPayload().split("\\|");
        if (credentials.length == 2) {
            String username = credentials[0];
            String password = credentials[1];

            if (loginAuth.authenticateUser(username, password)) {
                clientHandler.setLoggedInUsername(username);
                out.println("Login successful! Welcome " + username);
                out.println("Available commands: /join <room>, /leave, /list_users, /list_rooms, /quit");

                joinDefaultRoom(username, out);
            } else {
                out.println("Error: Invalid username or password");
            }
        } else {
            out.println("Error: Invalid login payload");
        }
    }

    private void handleRegister(MessageParser.ParsedMessage message, PrintWriter out) {
        String[] reg = message.getPayload().split("\\|");
        if (reg.length == 2) {
            loginAuth.handleRegister(reg[0], reg[1], out, clientSocket);
        } else {
            out.println("Error: Invalid register format");
        }
    }

    private void handleLogout(MessageParser.ParsedMessage message, PrintWriter out) {
        String username = clientHandler.getLoggedInUsername();
        if (username != null) {
            leaveCurrentRoom(username);
            clientHandler.setLoggedInUsername(null);
            out.println("Logout successful");
        } else {
            out.println("Error: Not logged in");
        }
    }

    private void handleText(MessageParser.ParsedMessage message, PrintWriter out) {
        String username = clientHandler.getLoggedInUsername();
        if (username == null) {
            out.println("Error: Please login first");
            return;
        }

        String currentRoom = userCurrentRoom.get(username);
        if (currentRoom == null) {
            out.println("Error: Please join a room first");
            return;
        }

        String payload = message.getPayload();

        if (payload.startsWith("/")) {
            handleCommand(payload, out);
            return;
        }

        broadcastToRoom(currentRoom, username, payload);
    }

    private void handleCommand(String command, PrintWriter out) {
        String username = clientHandler.getLoggedInUsername();
        String[] parts = command.split(" ", 2);
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "/join":
                if (parts.length == 2) {
                    joinRoom(username, parts[1], out);
                } else {
                    out.println("Usage: /join <room_name>");
                }
                break;
            case "/create_room":
                if (parts.length == 2) {
                    createRoom(parts[1], out);
                } else {
                    out.println("Usage: /create_room <room_name>");
                }
                break;
            case "/leave":
                leaveCurrentRoom(username);
                out.println("Left the current room");
                break;
            case "/list_users":
                handleListUsers(out);
                break;
            case "/list_rooms":
                handleListRooms(out);
                break;
            case "/quit":
                out.println("Goodbye!");
                break;
            default:
                out.println("Unknown command: " + cmd);
        }
    }

    private void handleJoinRoom(MessageParser.ParsedMessage message, PrintWriter out) {
        String username = clientHandler.getLoggedInUsername();
        if (username == null) {
            out.println("Error: Please login first");
            return;
        }
        joinRoom(username, message.getPayload(), out);
    }

    private void handleCreateRoom(MessageParser.ParsedMessage message, PrintWriter out) {
        String username = clientHandler.getLoggedInUsername();
        if (username == null) {
            out.println("Error: Please login first");
            return;
        }
        createRoom(message.getPayload(), out);
    }

    private void handleLeaveRoom(MessageParser.ParsedMessage message, PrintWriter out) {
        String username = clientHandler.getLoggedInUsername();
        if (username != null) {
            leaveCurrentRoom(username);
            out.println("Left the current room");
        }
    }

    private void handleListUsers(PrintWriter out) {
        String currentRoom = userCurrentRoom.get(clientHandler.getLoggedInUsername());
        if (currentRoom == null) {
            out.println("You are not in any room");
            return;
        }

        Set<String> usersInRoom = chatRooms.get(currentRoom);
        if (usersInRoom == null || usersInRoom.isEmpty()) {
            out.println("No users in room: " + currentRoom);
        } else {
            out.println("Users in room '" + currentRoom + "': " + String.join(", ", usersInRoom));
        }
    }

    private void handleListRooms(PrintWriter out) {
        if (chatRooms.isEmpty()) {
            out.println("No active rooms");
        } else {
            out.println("Active rooms: " + String.join(", ", chatRooms.keySet()));
        }
    }

    private void joinDefaultRoom(String username, PrintWriter out) {
        joinRoom(username, "general", out);
    }

    private void joinRoom(String username, String roomName, PrintWriter out) {
        leaveCurrentRoom(username);

        chatRooms.computeIfAbsent(roomName, k -> ConcurrentHashMap.newKeySet()).add(username);
        userCurrentRoom.put(username, roomName);

        out.println("Joined room: " + roomName);
        broadcastToRoom(roomName, "SERVER", username + " joined the room");
    }

    private void leaveCurrentRoom(String username) {
        String currentRoom = userCurrentRoom.remove(username);
        if (currentRoom != null) {
            Set<String> usersInRoom = chatRooms.get(currentRoom);
            if (usersInRoom != null) {
                usersInRoom.remove(username);
                if (usersInRoom.isEmpty()) {
                    chatRooms.remove(currentRoom);
                } else {
                    broadcastToRoom(currentRoom, "SERVER", username + " left the room");
                }
            }
        }
    }

    private void broadcastToRoom(String roomName, String senderUsername, String messageText) {
        Set<String> usersInRoom = chatRooms.get(roomName);
        if (usersInRoom == null) return;

        String formattedMessage = "[" + roomName + "] " + senderUsername + ": " + messageText;

        for (String username : usersInRoom) {
            if (!username.equals(senderUsername)) {
                sendMessageToUser(username, formattedMessage);
            }
        }
    }


    private void sendMessageToUser(String username, String message) {
        for (User user : userMap.values()) {
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

    private void handleFileTransfer(MessageParser.ParsedMessage message) {

    }

    private void createRoom(String roomName, PrintWriter out) {
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
    }

    private void handleUnknown(MessageParser.ParsedMessage message, PrintWriter out) {
        out.println("Unknown command: " + message.getMessageType());
    }

    // Forsøg på at oprette privat beskeder - ikke færdig
    private void handlePrivateMessage(String sender, String recipient, String message) {
        String formattedMessage = "[PM] " + sender + ": " + message;
        sendMessageToUser(recipient, formattedMessage);
    }
}