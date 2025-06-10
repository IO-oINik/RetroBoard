package ru.edu.retro.apiservice.exceptions;

public class InvalidInviteTokenException extends RuntimeException {
    public InvalidInviteTokenException(String message) {
        super(message);
    }
}
