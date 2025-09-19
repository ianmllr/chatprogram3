package org.example.Database.Interfaces;

import java.sql.Connection;
import java.sql.SQLException;

public interface IDatabaseConfig {
    Connection getConnection() throws SQLException;
    void closeConnection();
}
