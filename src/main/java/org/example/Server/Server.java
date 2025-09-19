package org.example.Server;

import org.example.Model.User;
import org.example.Util.ConfigLoader;
import org.example.User.LoginAuthentification;
import org.example.Util.ClientHandler;
import org.example.Util.MessageHandler;
import org.example.Database.Interfaces.IUserDatabase;
import org.example.Database.Repos.UserDatabase;
import org.example.Database.DatabaseConfig;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class Server {
    static ConfigLoader config = new ConfigLoader("config.properties");
    private static final int PORT = config.getInt("PORT");
    private static final int THREAD_POOL_SIZE = config.getInt("THREAD_POOL_SIZE");
    private final Map<Integer, User> userMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        new Server().start();
    }

    public void start() {
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        DatabaseConfig dbConfig = new DatabaseConfig();
        IUserDatabase userDatabase = new UserDatabase(dbConfig);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                LoginAuthentification loginAuth = new LoginAuthentification(userMap, userDatabase);
                MessageHandler messageHandler = new MessageHandler(userMap);
                threadPool.submit(new ClientHandler(socket, messageHandler, loginAuth));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}