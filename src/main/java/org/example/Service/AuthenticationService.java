package org.example.Service;

import org.example.Database.Repos.DatabaseConfig;
import org.example.Database.Repos.UserDatabase;
import org.example.Model.User;
import org.example.Server.Server;
import org.example.Util.SimpleLogger;

import java.io.PrintWriter;
import java.net.Socket;

public class AuthenticationService {

    private final UserDatabase userDatabase = new UserDatabase(new DatabaseConfig());

    public boolean authenticateUser(String username, String password, PrintWriter printWriter, Socket clientSocket) {
        boolean success = userDatabase.authenticateUser(username, password);
        if (success) {
            int id = userDatabase.getUserIdByUsername(username);
            User user = new User(username, id, clientSocket);
            Server.userMap.put(id, user);
            return true;
        } else {
            return false;
        }
    }
}
