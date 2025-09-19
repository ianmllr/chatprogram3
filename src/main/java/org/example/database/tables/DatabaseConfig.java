package org.example.database.tables;

import org.example.Util.ConfigLoader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig implements org.example.database.tables.IDatabaseConfig {
    private final String DB_HOST;
    private final String DB_PORT;
    private final String DB_NAME;
    private final String DB_USER;
    private final String DB_PASSWORD;
    private final String DB_URL;

    private Connection connection;

    public DatabaseConfig() {
        ConfigLoader config = new ConfigLoader("config.properties");
        this.DB_HOST = config.getString("DB_HOST");
        this.DB_PORT = config.getString("DB_PORT");
        this.DB_NAME = config.getString("DB_NAME");
        this.DB_USER = config.getString("DB_USER");
        this.DB_PASSWORD = config.getString("DB_PASSWORD");
        this.DB_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME +
                "?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC";
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                initializeDatabase();
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found", e);
            }
        }
        return connection;
    }

    private void initializeDatabase() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(50) UNIQUE NOT NULL, " +
                    "password VARCHAR(100) NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            statement.execute("CREATE TABLE IF NOT EXISTS message_history (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "sender_id VARCHAR(50) NOT NULL, " +
                    "message_type VARCHAR(20) NOT NULL, " +
                    "payload TEXT NOT NULL, " +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        }
    }

    @Override
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
