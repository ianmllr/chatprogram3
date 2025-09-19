package org.example.user;

import org.example.database.interfaces.IUserDatabase;

import java.io.PrintWriter;

public class LoginAuthentification {

    private final IUserDatabase userDatabase;

    public LoginAuthentification(IUserDatabase userDatabase) {
        this.userDatabase = userDatabase;
    }

    public void handleLogin(String username, String password, PrintWriter printWriter) {

        // Tjekker først om felterne er udfyldt
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            printWriter.println("Error: Username and password cannot be empty");
            return;
        }

        // Herefter forsøger vi at autentificere brugeren, er det success kommer du igennem
        boolean success = userDatabase.authenticateUser(username, password);
        if (success) {
            printWriter.println("Login successful");
            org.example.Util.SimpleLogger.log("User logged in: " + username);
        } else {
            printWriter.println("Error: Invalid username or password");
            org.example.Util.SimpleLogger.log("Failed login attempt for user: " + username);
        }
    }


    // Samme princip med registrering af brugere. Hurtigt tjek om tingene er iorden, derefter opretter vi brugeren

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

    public void handleLogout() {
        if ()
    }
}
