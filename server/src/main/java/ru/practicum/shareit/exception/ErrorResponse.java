package ru.practicum.shareit.exception;

import lombok.Data;

@Data
@SuppressWarnings("ClassCanBeRecord")
public class ErrorResponse {
    private final String error;
}
