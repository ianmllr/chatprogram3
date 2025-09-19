package org.example.database.interfaces;

public interface IUserDatabase {
    boolean authenticateUser(String username, String password);
    boolean createUser(String username, String password);
    boolean userExists(String username);
}
