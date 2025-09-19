package org.example.util;

import org.example.user.LoginAuthentification;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final MessageHandler messageHandler;
    private final LoginAuthentification loginAuth;

    public ClientHandler(Socket clientSocket, MessageHandler messageHandler, LoginAuthentification loginAuth) {
        this.clientSocket = clientSocket;
        this.messageHandler = messageHandler;
        this.loginAuth = loginAuth;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
    ) {
        String message;
        while ((message = in.readLine()) != null) {
            MessageParser.ParsedMessage parsed = MessageParser.parseMessage(message);
            messageHandler.handleMessage(parsed, out);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

    }
}
