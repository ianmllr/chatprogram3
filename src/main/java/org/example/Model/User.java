package org.example.Model;

import java.net.Socket;

public class User {
    private String username;
    private int clientId;
    private Socket socket;

    public User(String username, int clientId, Socket socket) {
        this.username = username;
        this.clientId = clientId;
        this.socket = socket;
    }

    public User(int clientId, String username) {
        this.clientId = clientId;
        this.username = username;
        this.socket = null;
    }

    public String getUsername() {
        return username;
    }

    public int getClientId() {
        return clientId;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}