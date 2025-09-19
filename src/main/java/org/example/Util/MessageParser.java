package org.example.Util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

        // Getters
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

        // Splitter beskeden baseret på separator "|"
        String[] parts = messageLine.split("\\|");
        if (parts.length < 4) {
            throw new IllegalArgumentException("Beskedlinjen er ugyldig eller mangler felter: " + messageLine);
        }

        String clientId = parts[0]; // Client ID
        String timestampStr = parts[1]; // Tidsstempel
        String messageType = parts[2]; // Beskedtype (TEXT, LOGIN, FILE_TRANSFER, etc.)
        String payload = parts[3]; // Indhold (kan være tekst, filnavn, emoji osv.)

        // Parser tidsstempel
        Date timestamp;
        try {
            timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(timestampStr);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Ugyldigt tidsstempelformat: " + timestampStr);
        }

        // Returnerer objektet med parsed data
        return new ParsedMessage(clientId, timestamp, messageType, payload);
    }
}
