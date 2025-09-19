package org.example.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private MessageHandler messageHandler;

    public ClientHandler(Socket clientSocket, MessageHandler messageHandler) {
        this.clientSocket = clientSocket;
        this.messageHandler = messageHandler;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()))) {
            String message;
            while ((message = in.readLine()) != null) {
                MessageParser.ParsedMessage parsed = MessageParser.parseMessage(message);
                messageHandler.handleMessage(parsed);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
