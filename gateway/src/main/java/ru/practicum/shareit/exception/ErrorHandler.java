package ru.practicum.shareit.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    private static final String DESCRIPTION = "Произошла ошибка при обработке входных данных: ";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(final MethodArgumentNotValidException e) {
        log.error(DESCRIPTION, e);
        return Map.of("error", String.format("Ошибка валидации: %s", e.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleConstraintViolation(final ConstraintViolationException e) {
        log.error(DESCRIPTION, e);
        return Map.of("error", String.format("Некорректный параметр запроса: %s", e.getMessage()));
    }

    @ExceptionHandler(IllegalBookingStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgument(final IllegalBookingStateException e) {
        log.error(DESCRIPTION, e);
        return Map.of("error", e.getMessage());
    }
}