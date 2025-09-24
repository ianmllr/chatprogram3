package org.example.Service;

import org.example.Database.Repos.UserDatabase;
import org.example.Model.User;
import org.example.Server.Server;
import org.example.Util.SimpleLogger;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

public class AuthenticationService {

    private final UserDatabase userDatabase;

    public boolean registerUser(String username, String password, PrintWriter printWriter, Socket socket) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            printWriter.println("Error: Username and password cannot be empty");
            return false;
        }

        if (userDatabase.userExists(username)) {
            printWriter.println("Error: Username already exists");
            return false;
        }

        boolean success = userDatabase.createUser(username, password);
        if (success) {
            printWriter.println("Registration successful");
            SimpleLogger.getInstance().log("New user: " + username + " registered at IP: " + socket.getInetAddress());
            return true;
        } else {
            printWriter.println("Error: Registration failed");
            SimpleLogger.getInstance().log("Failed registration attempt for user: " + username);
            return false;
        }
    }


    public AuthenticationService(Map<Integer, User> userMap, UserDatabase userDatabase) {
        this.userDatabase = userDatabase;
    }

    public void handleLogin(String username, String password, PrintWriter printWriter, Socket clientSocket) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            printWriter.println("Error: Username and password cannot be empty");
        } else {
            if (authenticateUser(username, password, printWriter, clientSocket)) {
                printWriter.println("Login successful");
                SimpleLogger.getInstance().log("User logged in: " + username);
            } else {
                printWriter.println("Error: Invalid username or password");
                SimpleLogger.getInstance().log("Failed login attempt for user: " + username);

            }
        }
    }

    public void handleRegister(String username, String password, PrintWriter printWriter, Socket socket) {
        if (registerUser(username, password, printWriter, socket)) {
            printWriter.println("Registration successful: " + username);
            SimpleLogger.getInstance().log("User registered: " + username);
        }
    }


    public void handleLogout(String username, PrintWriter printWriter) {
        if (username == null || username.isEmpty()) {
            if (printWriter != null) {
                printWriter.println("Error: Username cannot be empty");
            }
            return;
        }

        boolean removed = Server.userMap.entrySet().removeIf(entry ->
                entry.getValue().getUsername().equals(username));

        if (removed) {
            if (printWriter != null) {
                printWriter.println("Logout successful");
            }
            SimpleLogger.getInstance().log("User logged out: " + username);
        } else {
            if (printWriter != null) {
                printWriter.println("Error: User not found");
            }
        }
    }


    public boolean authenticateUser(String username, String password, PrintWriter printWriter, Socket clientSocket) {
        if (userDatabase.authenticateUser(username, password)) {
            int id = userDatabase.getUserIdByUsername(username);
            User user = new User(username, id, clientSocket);
            Server.userMap.put(id, user);
            return true;
        } else {
            return false;
        }
    }


}
