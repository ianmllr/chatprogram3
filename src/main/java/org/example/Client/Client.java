package org.example.Client;

import org.example.Util.ConfigLoader;
import org.example.Util.ProtocolHandler;

import java.io.*;
import java.net.*;

import java.util.Scanner;
import java.util.UUID;

public class Client {
    static ConfigLoader config = new ConfigLoader("config.properties");
    private static final String SERVER_HOST = config.getString("SERVER_HOST");
    private static final int SERVER_PORT = config.getInt("PORT");
    String clientId;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean connected = false;
    private boolean loggedIn = false;
    private ProtocolHandler protocolHandler;

    public static void main(String[] args) {
        new Client().start();
    }

    public void start() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connected = true;
            clientId = UUID.randomUUID().toString();

            protocolHandler = new ProtocolHandler(out);
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
        } catch (IOException _) {
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
                    protocolHandler.sendLogout(clientId);
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
                    protocolHandler.sendLogin(clientId, parts[1], parts[2]);
                } else {
                    System.out.println("Usage: login <username> <password>");
                }
                break;
            case "register":
                if (parts.length == 3) {
                    protocolHandler.sendRegister(clientId, parts[1], parts[2]);
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
            protocolHandler.sendTextMessage(clientId, input);
        }
    }

    private void handleSlashCommand(String input) {
        String[] parts = input.split(" ", 2);
        String command = parts[0].toLowerCase();

        switch (command) {
            case "/join":
                if (parts.length == 2) {
                    protocolHandler.sendJoinRoom(clientId, parts[1]);
                } else {
                    System.out.println("Usage: /join <room_name>");
                }
                break;
            case "/leave":
                protocolHandler.sendLeaveRoom(clientId);
                break;
            case "/list_users":
                protocolHandler.sendListUsers(clientId);
                break;
            case "/list_rooms":
                protocolHandler.sendListRooms(clientId);
                break;
            case "/help":
                showHelp();
                break;
            default:
                protocolHandler.sendTextMessage(clientId, input);
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