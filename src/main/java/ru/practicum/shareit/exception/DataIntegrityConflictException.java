package ru.practicum.shareit.exception;

public class DataIntegrityConflictException extends RuntimeException {
    public DataIntegrityConflictException(String message) {
        super(message);
    }
}
