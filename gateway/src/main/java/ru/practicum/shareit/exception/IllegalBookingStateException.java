package ru.practicum.shareit.exception;

public class IllegalBookingStateException extends RuntimeException {
    public IllegalBookingStateException(String message) {
        super(message);
    }
}
