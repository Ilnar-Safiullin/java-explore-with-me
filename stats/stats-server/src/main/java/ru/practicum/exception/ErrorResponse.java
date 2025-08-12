package ru.practicum.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private final String status;
    private final String reason;
    private final String message;
    private final LocalDateTime timestamp;
}