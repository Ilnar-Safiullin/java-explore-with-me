package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        String message = "Validation error(s): " + String.join("; ", errors);
        log.warn(message, e);

        return new ErrorResponse(
                "BAD_REQUEST",
                "Ошибка валидации.",
                message,
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Некорректный аргумент: {}", e.getMessage(), e);

        return new ErrorResponse(
                "BAD_REQUEST",
                "Некорректный запрос из-за неверного аргумента.",
                e.getMessage(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        String parameterName = e.getParameterName();
        String parameterType = e.getParameterType();
        String message = String.format("Отсутствует обязательный параметр: '%s' типа '%s'.", parameterName, parameterType);
        log.warn("Отсутствует параметр запроса: {}", message, e);

        return new ErrorResponse(
                "BAD_REQUEST",
                "Отсутствует обязательный параметр запроса.",
                message,
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGenericException(Exception e) {
        log.error("Необработанная ошибка: ", e);
        return new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "Внутренняя ошибка сервера.",
                "Произошла непредвиденная ошибка. Попробуйте позже или обратитесь к администратору.",
                LocalDateTime.now()
        );
    }
}