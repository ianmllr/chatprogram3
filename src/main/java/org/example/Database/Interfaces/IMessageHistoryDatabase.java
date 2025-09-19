package org.example.Database.Interfaces;

import org.example.Database.Repos.MessageHistoryDatabase;

import java.util.List;

public interface IMessageHistoryDatabase {
    boolean saveMessage(String senderId, String messageType, String payload);
    List<MessageHistoryDatabase.ChatMessage> getRecentMessages(int limit);
    List<MessageHistoryDatabase.ChatMessage> getMessagesByUser(String senderId, int limit);
}
