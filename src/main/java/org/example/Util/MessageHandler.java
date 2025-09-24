package org.example.Util;

import org.example.Database.Interfaces.IUserDatabase;
import org.example.Model.User;
import org.example.User.ILoginAuthentification;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Time;
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

//    public MessageHandler(Map<Integer, User> userMap, ILoginAuthentification loginAuth, Socket clientSocket) {
//        this.userMap = userMap;
//        this.loginAuth = loginAuth;
//        this.clientSocket = clientSocket;
//    }

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
            SimpleLogger.getInstance().log("Error handling message: " + e.getMessage());
            out.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Replace your handleLogin method in MessageHandler.java with this:

    private void handleLogin(MessageParser.ParsedMessage message, PrintWriter out) {
        String currentUsername = clientHandler.getLoggedInUsername();
        if (currentUsername != null) {
            out.println("Error: Already logged in as " + currentUsername);
            SimpleLogger.getInstance().log("User " + currentUsername + " attempted second login");
            return;
        }

        String[] credentials = message.getPayload().split("\\|");
        if (credentials.length != 2) {
            out.println("Error: Invalid login format");
            SimpleLogger.getInstance().log("Invalid login payload received: " + message.getPayload());
            return;
        }

        String username = credentials[0].trim();
        String password = credentials[1];

        SimpleLogger.getInstance().log("Login attempt for user: " + username);

        loginAuth.handleLogin(username, password, out, clientSocket);

        boolean loginSuccessful = userMap.values().stream()
                .anyMatch(user -> user.getUsername().equals(username));

        if (loginSuccessful) {
            clientHandler.setLoggedInUsername(username);

            System.out.println("DEBUG: Login successful. UserMap size: " + userMap.size());
            for (User u : userMap.values()) {
                System.out.println("  - " + u.getUsername());
            }

            out.println("Available commands: /join <room>, /leave, /list_users, /list_rooms, /create_room, /send_dm, /help, /quit");
            SimpleLogger.getInstance().log("User successfully logged in: " + username);
            joinDefaultRoom(username, out);
        }
    }

    private void handleRegister(MessageParser.ParsedMessage message, PrintWriter out) {
        String[] reg = message.getPayload().split("\\|");
        if (reg.length == 2) {
            loginAuth.handleRegister(reg[0], reg[1], out, clientSocket);
        } else {
            out.println("Error: Invalid register format");
            SimpleLogger.getInstance().log("Someone tried to register with invalid format at " + new Timestamp(System.currentTimeMillis()));
        }
    }

    private void handleLogout(MessageParser.ParsedMessage message, PrintWriter out) {
        String username = clientHandler.getLoggedInUsername();
        if (username != null) {
            leaveCurrentRoom(username);
            clientHandler.setLoggedInUsername(null);
            out.println("Logout successful");
            SimpleLogger.getInstance().log("User logged out: " + username);
        } else {
            out.println("Error: Not logged in");
            SimpleLogger.getInstance().log("Logout attempt without being logged in at " + new Timestamp(System.currentTimeMillis()));
        }
    }

    private void handleText(MessageParser.ParsedMessage message, PrintWriter out) {
        String username = clientHandler.getLoggedInUsername();
        if (username == null) {
            out.println("Error: Please login first");
            SimpleLogger.getInstance().log("Unauthenticated user tried to send message at " + new Timestamp(System.currentTimeMillis()));
            return;
        }

        String currentRoom = userCurrentRoom.get(username);
        if (currentRoom == null) {
            out.println("Error: Please join a room first");
            SimpleLogger.getInstance().log("User " + username + " tried to send message without joining a room at " + new Timestamp(System.currentTimeMillis()));
            return;
        }

        String payload = message.getPayload();

        if (checkForEmoji(payload)) {
            payload = convertToEmoji(payload);
        }

        if (payload.startsWith("/")) {
            handleCommand(payload, out);
            return;
        }

        String timestamp = message.getTimestamp().toString();

        broadcastToRoom(currentRoom, username, timestamp, payload);
        SimpleLogger.getInstance().log("Message from " + username + " in room " + currentRoom + ": " + payload);

    }

    private void handleCommand(String command, PrintWriter out) {
        String username = clientHandler.getLoggedInUsername();
        String[] parts = command.split(" ", 2);
        String cmd = parts[0].toLowerCase();

        SimpleLogger.getInstance().log("Command from " + username + ": " + command);

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
            case "/send_dm":
                if (parts.length == 2) {
                    String[] dmParts = parts[1].split(" ", 2);
                    if (dmParts.length == 2) {
                        String recipient = dmParts[0];
                        String dmMessage = dmParts[1];
                        handlePrivateMessage(username, recipient, dmMessage);
                        SimpleLogger.getInstance().log("Private message from " + username + " to " + recipient + ": " + dmMessage);
                    } else {
                        out.println("Usage: /send_dm <username> <message>");
                    }
                } else {
                    out.println("Usage: /send_dm <username> <message>");
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
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        chatRooms.computeIfAbsent(roomName, k -> ConcurrentHashMap.newKeySet()).add(username);
        userCurrentRoom.put(username, roomName);

        SimpleLogger.getInstance().log("User " + username + " joined room: " + roomName);

        out.println("Joined room: " + roomName);
        broadcastToRoom(roomName, "SERVER", timestamp.toString(),username + " joined the room");
    }

    private void leaveCurrentRoom(String username) {
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

    private void broadcastToRoom(String roomName, String senderUsername, String timestamp, String messageText) {
        Set<String> usersInRoom = chatRooms.get(roomName);
        if (usersInRoom == null) return;

        String formattedMessage = "[" + roomName + "] " + "[" + timestamp + "]: " + senderUsername + ": " + messageText;

        for (String username : usersInRoom) {
            if (!username.equals(senderUsername)) {
                sendMessageToUser(username, formattedMessage);
                SimpleLogger.getInstance().log("User: " + username + " sent message: " + formattedMessage);
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
        SimpleLogger.getInstance().log("Room named: " + roomName + " created at " + new Timestamp(System.currentTimeMillis()) + " by user: " + clientHandler.getLoggedInUsername());
    }

    private void handleUnknown(MessageParser.ParsedMessage message, PrintWriter out) {
        out.println("Unknown command: " + message.getMessageType());
    }

    // Forsøg på at oprette privat beskeder - ikke færdig
    private void handlePrivateMessage(String sender, String recipient, String message) {
        String formattedMessage = "[PM] " + sender + ": " + message;
        sendMessageToUser(recipient, formattedMessage);
    }

    private boolean checkForEmoji(String payload) {
        return payload.contains(":smile:") || payload.contains(":sad:") || payload.contains(":naughty:") || payload.contains(":skull:");
    }


    private String convertToEmoji(String payload) {
        payload = payload.replace(":smile", new String(Character.toChars(0x1F604))); // 😄
        payload = payload.replace(":sad:", new String(Character.toChars(0x1F641))); // 🙁
        payload = payload.replace(":naughty:", new String(Character.toChars(0x1F608	))); // 😈
        payload = payload.replace(":skull:", new String(Character.toChars(0x1F480))); // 💀
        return payload;
    }
}