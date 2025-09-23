package org.example.Util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

public class MessageParser {

    public static class ParsedMessage {
        private String clientId;
        private Date timestamp;
        private String messageType;
        private String payload;

        public ParsedMessage(String clientId, Date timestamp, String messageType, String payload) {
            this.clientId = clientId;
            this.timestamp = timestamp;
            this.messageType = messageType;
            this.payload = payload;
        }

        public String getClientId() {
            return clientId;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public String getMessageType() {
            return messageType;
        }

        public String getPayload() {
            return payload;
        }

        @Override
        public String toString() {
            return "ParsedMessage{ClientId='" + clientId + "', Timestamp=" + timestamp +
                    ", MessageType='" + messageType + "', Payload='" + payload + "'}";
        }

    }

    public static ParsedMessage parseMessage(String messageLine) {
        if (messageLine == null || messageLine.isEmpty()) {
            throw new IllegalArgumentException("Beskedlinjen er tom!");
        }

        String[] parts = messageLine.split("\\|", 4);
        if (parts.length < 4) {
            throw new IllegalArgumentException("Beskedlinjen er ugyldig eller mangler felter: " + messageLine);
        }

        String clientId = parts[0];
        String timestampStr = parts[1];
        String messageType = parts[2];
        String payload = parts[3];

        Date timestamp;
        try {
            timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(timestampStr);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Ugyldigt tidsstempelformat: " + timestampStr);
        }

        return new ParsedMessage(clientId, timestamp, messageType, payload);
    }
}