// src/main/java/org/example/server/Server.java
package org.example.server;

import org.example.Model.User;
import org.example.Util.ConfigLoader;
import org.example.user.LoginAuthentification;
import org.example.util.ClientHandler;
import org.example.util.MessageHandler;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.Map;
import java.util.HashMap;

public class Server {
    static ConfigLoader config = new ConfigLoader("config.properties");
    private static final int PORT = config.getInt("PORT");
    private static final int THREAD_POOL_SIZE = config.getInt("THREAD_POOL_SIZE");
    private final Map<Integer, User> userMap = new HashMap<>();

    public static void main(String[] args) {
        new Server().start();
    }

    public void start() {
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                MessageHandler messageHandler = new MessageHandler(userMap);
                LoginAuthentification loginAuth = new LoginAuthentification(userMap);
                threadPool.submit(new ClientHandler(socket, messageHandler, loginAuth));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
