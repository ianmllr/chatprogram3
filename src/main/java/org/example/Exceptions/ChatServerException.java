package org.example.Exceptions;

public class ChatServerException extends Exception {
  public ChatServerException(String message) {
    super(message);
  }

  public ChatServerException(String message, Throwable cause) {
    super(message, cause);
  }
}