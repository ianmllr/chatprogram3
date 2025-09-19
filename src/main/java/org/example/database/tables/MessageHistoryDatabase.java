package org.example.database.tables;

import org.example.database.interfaces.IMessageHistoryDatabase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageHistoryDatabase implements IMessageHistoryDatabase {

    private final org.example.database.tables.IDatabaseConfig dbConfig;

    public MessageHistoryDatabase(org.example.database.tables.IDatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public static class ChatMessage {
        private final String senderId;
        private final String messageType;
        private final String payload;
        private final String timestamp;

        public ChatMessage(String senderId, String messageType, String payload, String timestamp) {
            this.senderId = senderId;
            this.messageType = messageType;
            this.payload = payload;
            this.timestamp = timestamp;
        }

        public String getSenderId() { return senderId; }
        public String getMessageType() { return messageType; }
        public String getPayload() { return payload; }
        public String getTimestamp() { return timestamp; }
    }

    @Override
    public boolean saveMessage(String senderId, String messageType, String payload) {
        String sql = "INSERT INTO message_history (sender_id, message_type, payload, timestamp) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, senderId);
            statement.setString(2, messageType);
            statement.setString(3, payload);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<ChatMessage> getRecentMessages(int limit) {
        List<ChatMessage> messages = new ArrayList<>();
        String sql = "SELECT sender_id, message_type, payload, timestamp FROM message_history " +
                "ORDER BY timestamp DESC LIMIT ?";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, limit);
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                messages.add(new ChatMessage(
                        result.getString("sender_id"),
                        result.getString("message_type"),
                        result.getString("payload"),
                        result.getString("timestamp")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    @Override
    public List<ChatMessage> getMessagesByUser(String senderId, int limit) {
        List<ChatMessage> messages = new ArrayList<>();
        String sql = "SELECT sender_id, message_type, payload, timestamp FROM message_history " +
                "WHERE sender_id = ? ORDER BY timestamp DESC LIMIT ?";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, senderId);
            statement.setInt(2, limit);
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                messages.add(new ChatMessage(
                        result.getString("sender_id"),
                        result.getString("message_type"),
                        result.getString("payload"),
                        result.getString("timestamp")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }
}
