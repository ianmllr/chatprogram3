package org.example.User;

import org.example.Model.User;
import org.example.Database.Interfaces.IUserDatabase;
import org.example.Service.AuthenticationService;
import org.example.Util.SimpleLogger;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

public class LoginAuthentication implements ILoginAuthentication {

    private final Map<Integer, User> userMap;
    private final IUserDatabase userDatabase;
    private final AuthenticationService authenticationService = new AuthenticationService();

    public LoginAuthentication(Map<Integer, User> userMap, IUserDatabase userDatabase) {
        this.userMap = userMap;
        this.userDatabase = userDatabase;
    }

    @Override
    public boolean authenticateUser(String username, String password) {
        return userDatabase.authenticateUser(username, password);
    }

    @Override
    public void handleLogin(String username, String password, PrintWriter printWriter, Socket clientSocket) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            printWriter.println("Error: Username and password cannot be empty");
        } else {
            if (authenticationService.authenticateUser(username, password, printWriter, clientSocket)) {
                printWriter.println("Login successful");
                SimpleLogger.getInstance().log("User logged in: " + username);
            } else {
                printWriter.println("Error: Invalid username or password");
                SimpleLogger.getInstance().log("Failed login attempt for user: " + username);

            }
        }
    }

    @Override
    public void handleRegister(String username, String password, PrintWriter printWriter, Socket socket) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            printWriter.println("Error: Username and password cannot be empty");
            return;
        }

        if (userDatabase.userExists(username)) {
            printWriter.println("Error: Username already exists");
            return;
        }

        boolean success = userDatabase.createUser(username, password);
        if (success) {
            printWriter.println("Registration successful");
            SimpleLogger.getInstance().log("New user registered: " + username);
        } else {
            printWriter.println("Error: Registration failed");
            SimpleLogger.getInstance().log("Failed registration attempt for user: " + username);
        }
    }

    @Override
    public void handleLogout(String username, PrintWriter printWriter) {
        if (username == null || username.isEmpty()) {
            if (printWriter != null) {
                printWriter.println("Error: Username cannot be empty");
            }
            return;
        }

        boolean removed = userMap.entrySet().removeIf(entry ->
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
}