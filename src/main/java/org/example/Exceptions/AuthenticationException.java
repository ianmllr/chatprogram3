package org.example.Exceptions;

public class AuthenticationException extends ChatServerException {
    public AuthenticationException(String message) {
        super(message);
    }
}