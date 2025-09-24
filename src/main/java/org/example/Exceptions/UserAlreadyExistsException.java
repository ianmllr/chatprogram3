package org.example.Exceptions;

public class UserAlreadyExistsException extends ChatServerException {
  public UserAlreadyExistsException(String username) {
    super("User '" + username + "' already exists");
  }
}