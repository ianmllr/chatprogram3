package org.example.client;

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

    public static void main(String[] args) {
        new Client().start();
    }

    public void start() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connected = true;

            System.out.println("Connected to server!");
            System.out.println("Commands: login <username> <password>, register <username> <password>, quit");

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
                System.out.println("Server: " + message);
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

                case "quit":
                    sendLogout();
                    disconnect();
                    break;

                default:
                    sendTextMessage(input);
                    break;
            }
        }
        scanner.close();
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