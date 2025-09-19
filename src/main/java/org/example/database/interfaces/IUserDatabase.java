package org.example.database.interfaces;

import org.example.Model.User;

public interface IUserDatabase {
    boolean authenticateUser(String username, String password);
    boolean createUser(String username, String password);
    boolean userExists(String username);
    public User getUserById(int id);
    public int getUserIdByUsername(String username);


    }
