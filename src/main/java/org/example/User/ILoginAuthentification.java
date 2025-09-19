package org.example.User;

import java.io.PrintWriter;
import java.net.Socket;


public interface ILoginAuthentification {

    void handleRegister(String username, String password, PrintWriter printWriter, Socket socket);
    void handleLogin(String username, String password, PrintWriter printWriter);
    void handleLogout(String username, PrintWriter printWriter);
}
