package org.example.user;

import java.io.PrintWriter;
import org.example.user.LoginAuthentification;


public interface ILoginAuthentification {

    void handleRegister(String username, String password, PrintWriter printWriter);
    void handleLogin(String username, String password, PrintWriter printWriter);
    void handleLogout(String username, PrintWriter printWriter);
}
