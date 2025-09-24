package org.example.Util;

import org.example.Database.Interfaces.IUserDatabase;
import org.example.Model.User;
import org.example.Server.Server;
import org.example.Service.ChatService;
import org.example.Service.MessageService;
import org.example.User.ILoginAuthentication;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Timestamp;

public class MessageHandler {
    private ILoginAuthentication loginAuth;
    private IUserDatabase userDatabase;
    private Socket clientSocket;
    private ClientHandler clientHandler;
    private final ChatService chatService = new ChatService();
    private final MessageService messageService = new MessageService();


    public MessageHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void setLoginAuth(ILoginAuthentication loginAuth) {
        this.loginAuth = loginAuth;
    }

    public void setClientSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void setClientHandler(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    ///


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
                    handleLogout(out);
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
                    handleListRoomUsers(out);
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

        boolean loginSuccessful = Server.userMap.values().stream()
                .anyMatch(user -> user.getUsername().equals(username));

        if (loginSuccessful) {
            clientHandler.setLoggedInUsername(username);

            System.out.println("DEBUG: Login successful. UserMap size: " + Server.userMap.size());
            for (User u : Server.userMap.values()) {
                System.out.println("  - " + u.getUsername());
            }

            out.println("Available commands: /join <room>, /leave, /list_users, /list_rooms, /create_room, /send_dm, /help, /quit");
            SimpleLogger.getInstance().log("User successfully logged in: " + username);
            chatService.joinDefaultRoom(username, out);
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

    private void handleLogout(PrintWriter out) {
        String username = clientHandler.getLoggedInUsername();
        if (username != null) {
            chatService.leaveCurrentRoom(username);
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

        String currentRoom = ChatService.userCurrentRoom.get(username);
        if (currentRoom == null) {
            out.println("Error: Please join a room first");
            SimpleLogger.getInstance().log("User " + username + " tried to send message without joining a room at " + message.getTimestamp());
            return;
        }

        String payload = message.getPayload();

        if (messageService.checkForEmoji(payload)) {
            payload = messageService.convertToEmoji(payload);
        }

        if (payload.startsWith("/")) {
            handleCommand(payload, out);
            return;
        }

        String timestamp = message.getTimestamp().toString();

        chatService.broadcastToRoom(currentRoom, username, timestamp, payload);
        SimpleLogger.getInstance().log("Message from " + username + " in room " + currentRoom + ": " + payload);

    }

    private void handleJoinRoom(MessageParser.ParsedMessage message, PrintWriter out) {
        String username = clientHandler.getLoggedInUsername();
        if (username == null) {
            out.println("Error: Please login first");
            return;
        }

        chatService.joinRoom(username, message.getPayload(), out);
    }

    private void handleCreateRoom(MessageParser.ParsedMessage message, PrintWriter out) {
        String username = clientHandler.getLoggedInUsername();
        if (username == null) {
            out.println("Error: Please login first");
            return;
        }
        chatService.createRoom(message.getPayload(), out, username);
    }

    private void handleLeaveRoom(MessageParser.ParsedMessage message, PrintWriter out) {
        String username = clientHandler.getLoggedInUsername();
        if (username != null) {
            chatService.leaveCurrentRoom(username);
            out.println("Left the current room");
        }
    }

    private void handleListRoomUsers(PrintWriter out) {
        String currentRoom = ChatService.userCurrentRoom.get(clientHandler.getLoggedInUsername());
        chatService.ListRoomUsers(currentRoom, out);
    }

    private void handleListRooms(PrintWriter out) {
        if (ChatService.chatRooms.isEmpty()) {
            out.println("No active rooms");
        } else {
            out.println("Active rooms: " + String.join(", ", ChatService.chatRooms.keySet()));
        }
    }

    private void handleCommand(String command, PrintWriter out) {
        String username = clientHandler.getLoggedInUsername();
        String[] parts = command.split(" ", 2);
        String cmd = parts[0].toLowerCase();

        SimpleLogger.getInstance().log("Command from " + username + ": " + command);

        switch (cmd) {
            case "/join":
                if (parts.length == 2) {
                    chatService.joinRoom(username, parts[1], out);
                } else {
                    out.println("Usage: /join <room_name>");
                }
                break;
            case "/create_room":
                if (parts.length == 2) {
                    chatService.createRoom(parts[1], out, username);
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
                        messageService.handlePrivateMessage(username, recipient, dmMessage);
                        SimpleLogger.getInstance().log("Private message from " + username + " to " + recipient + ": " + dmMessage);
                    } else {
                        out.println("Usage: /send_dm <username> <message>");
                    }
                } else {
                    out.println("Usage: /send_dm <username> <message>");
                }
                break;
            case "/leave":
                chatService.leaveCurrentRoom(username);
                out.println("Left the current room");
                break;
            case "/list_users":
                handleListRoomUsers(out);
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

    private void handleFileTransfer(MessageParser.ParsedMessage message) {

    }

    private void handleUnknown(MessageParser.ParsedMessage message, PrintWriter out) {
        out.println("Unknown command: " + message.getMessageType());
    }



}