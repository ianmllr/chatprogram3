package org.example.User;

import org.example.Model.User;
import org.example.Database.Interfaces.IUserDatabase;

import java.io.PrintWriter;
import java.util.Map;

public class LoginAuthentification implements ILoginAuthentification {

    private final Map<Integer, User> userMap;
    private final IUserDatabase userDatabase;


    public LoginAuthentification(Map<Integer, User> userMap, IUserDatabase userDatabase) {
        this.userMap = userMap;
        this.userDatabase = userDatabase;
    }

    public void handleLogin(String username, String password, PrintWriter printWriter) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            printWriter.println("Error: Username and password cannot be empty");
            return;
        }

        boolean success = userDatabase.authenticateUser(username, password);
        if (success) {
            int id = userDatabase.getUserIdByUsername(username);
            User user = userDatabase.getUserById(id);

            if (user != null) {
                userMap.put(id, user);
                printWriter.println("Login successful");
                org.example.Util.SimpleLogger.log("User logged in: " + username);
            } else {
                printWriter.println("Error: User not found after authentication");
                org.example.Util.SimpleLogger.log("Authenticated user not found in database: " + username);
            }
        } else {
            printWriter.println("Error: Invalid username or password");
            org.example.Util.SimpleLogger.log("Failed login attempt for user: " + username);
        }
    }

    public void handleRegister(String username, String password, PrintWriter printWriter) {

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
            org.example.Util.SimpleLogger.log("New user registered: " + username);
        } else {
            printWriter.println("Error: Registration failed");
            org.example.Util.SimpleLogger.log("Failed registration attempt for user: " + username);
        }
    }


    public void handleLogout(String username, PrintWriter printWriter) {
        if (username == null || username.isEmpty()) {
            printWriter.println("Error: Username cannot be empty");
            return;
        }

        userMap.values().removeIf(user -> user.getUsername().equals(username));
        printWriter.println("Logout successful");
        org.example.Util.SimpleLogger.log("User logged out: " + username);
    }
}
