package org.example.Util;

import org.example.User.LoginAuthentication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final MessageHandler messageHandler;
    private final LoginAuthentication loginAuth;
    private String loggedInUsername = null;

    public ClientHandler(Socket clientSocket, MessageHandler messageHandler, LoginAuthentication loginAuth) {
        this.clientSocket = clientSocket;
        this.messageHandler = messageHandler;
        this.loginAuth = loginAuth;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            messageHandler.setClientSocket(clientSocket);
            messageHandler.setLoginAuth(loginAuth);
            messageHandler.setClientHandler(this);

            out.println("Welcome to the chat server!");
            out.println("Please login or register to continue.");

            String message;
            while ((message = in.readLine()) != null) {
                MessageParser.ParsedMessage parsed = MessageParser.parseMessage(message);
                messageHandler.handleMessage(parsed, out);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (loggedInUsername != null) {
                loginAuth.handleLogout(loggedInUsername, null);
            }
            try {
                clientSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setLoggedInUsername(String username) {
        this.loggedInUsername = username;
    }

    public String getLoggedInUsername() {
        return loggedInUsername;
    }
}