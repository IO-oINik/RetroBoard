package ru.edu.retro.dbservice.exceptions;

public class NotFoundEntityException extends RuntimeException {
    public NotFoundEntityException(String message) {
        super(message);
    }
}
