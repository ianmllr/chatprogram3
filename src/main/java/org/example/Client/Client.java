package org.example.Client;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Client {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String clientId;
    private boolean connected = false;
    private boolean loggedIn = false;

    public static void main(String[] args) {
        new Client().start();
    }

    public void start() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connected = true;

            clientId = "client" + System.currentTimeMillis();

            Thread messageReader = new Thread(this::readMessages);
            messageReader.start();

            handleUserInput();

        } catch (IOException e) {
            System.err.println("Could not connect to server: " + e.getMessage());
        }
    }

    private void readMessages() {
        try {
            String message;
            while (connected && (message = in.readLine()) != null) {
                if (message.contains("Login successful")) {
                    loggedIn = true;
                } else if (message.contains("Logout successful") || message.contains("Goodbye!")) {
                    loggedIn = false;
                }
                System.out.println(message);
            }
        } catch (IOException e) {
            if (connected) {
                System.err.println("Connection lost: " + e.getMessage());
            }
        }
    }

    private void handleUserInput() {
        Scanner scanner = new Scanner(System.in);

        while (connected) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) continue;

            if (input.startsWith("/quit") || input.equals("quit")) {
                if (loggedIn) {
                    sendLogout();
                }
                disconnect();
                break;
            }

            if (!loggedIn) {
                handlePreLoginCommands(input);
            } else {
                handlePostLoginCommands(input);
            }
        }
        scanner.close();
    }

    private void handlePreLoginCommands(String input) {
        String[] parts = input.split(" ", 3);
        String command = parts[0].toLowerCase();

        switch (command) {
            case "login":
                if (parts.length == 3) {
                    sendLogin(parts[1], parts[2]);
                } else {
                    System.out.println("Usage: login <username> <password>");
                }
                break;
            case "register":
                if (parts.length == 3) {
                    sendRegister(parts[1], parts[2]);
                } else {
                    System.out.println("Usage: register <username> <password>");
                }
                break;
            default:
                System.out.println("Please login or register first");
                System.out.println("Commands: login <username> <password>, register <username> <password>");
                break;
        }
    }

    private void handlePostLoginCommands(String input) {
        if (input.startsWith("/")) {
            handleSlashCommand(input);
        } else {
            sendTextMessage(input);
        }
    }

    private void handleSlashCommand(String input) {
        String[] parts = input.split(" ", 2);
        String command = parts[0].toLowerCase();

        switch (command) {
            case "/join":
                if (parts.length == 2) {
                    sendJoinRoom(parts[1]);
                } else {
                    System.out.println("Usage: /join <room_name>");
                }
                break;
            case "/leave":
                sendLeaveRoom();
                break;
            case "/list_users":
                sendListUsers();
                break;
            case "/list_rooms":
                sendListRooms();
                break;
            case "/help":
                showHelp();
                break;
            default:
                sendTextMessage(input);
                break;
        }
    }

    private void showHelp() {
        System.out.println("Available commands:");
        System.out.println("/join <room_name> - Join a chat room");
        System.out.println("/leave - Leave current room");
        System.out.println("/list_users - List users in current room");
        System.out.println("/list_rooms - List all active rooms");
        System.out.println("/quit - Exit the chat");
        System.out.println("/help - Show this help message");
        System.out.println("Or just type a message to send to your current room");
    }

    private void sendLogin(String username, String password) {
        String message = formatMessage("LOGIN", username + "|" + password);
        out.println(message);
    }

    private void sendRegister(String username, String password) {
        String message = formatMessage("REGISTER", username + "|" + password);
        out.println(message);
    }

    private void sendLogout() {
        String message = formatMessage("LOGOUT", "logout");
        out.println(message);
    }

    private void sendTextMessage(String text) {
        String message = formatMessage("TEXT", text);
        out.println(message);
    }

    private void sendJoinRoom(String roomName) {
        String message = formatMessage("JOIN_ROOM", roomName);
        out.println(message);
    }

    private void sendLeaveRoom() {
        String message = formatMessage("LEAVE_ROOM", "");
        out.println(message);
    }

    private void sendListUsers() {
        String message = formatMessage("LIST_USERS", "");
        out.println(message);
    }

    private void sendListRooms() {
        String message = formatMessage("LIST_ROOMS", "");
        out.println(message);
    }

    private String formatMessage(String messageType, String payload) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return clientId + "|" + timestamp + "|" + messageType + "|" + payload;
    }

    private void disconnect() {
        connected = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            System.out.println("Disconnected from server.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}