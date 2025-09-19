package org.example.Util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SimpleLogger {
    static org.example.Util.ConfigLoader config = new org.example.Util.ConfigLoader("config.properties");
    private static final String LOG_FILE = config.getString("LOG_NAME"); // filen oprettes hvis den ikke findes

    public static synchronized void log(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println("[" + timestamp + "] " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}