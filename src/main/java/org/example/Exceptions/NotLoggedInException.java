package org.example.Exceptions;

public class NotLoggedInException extends ChatServerException {
    public NotLoggedInException() {
        super("User must be logged in to perform this action");
    }
}