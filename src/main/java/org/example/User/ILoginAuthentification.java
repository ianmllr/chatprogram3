package org.example.User;

import java.io.PrintWriter;


public interface ILoginAuthentification {

    void handleRegister(String username, String password, PrintWriter printWriter);
    void handleLogin(String username, String password, PrintWriter printWriter);
    void handleLogout(String username, PrintWriter printWriter);
}
