package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
@SuppressWarnings("unused")
public class GlobalExceptionHandler {

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleHandlerMethodValidationException(HandlerMethodValidationException e) {
        String message = e.getAllErrors().get(0).getDefaultMessage();
        log.warn("Ошибка валидации параметров: {}", message);
        return new ErrorResponse(
                "BAD_REQUEST",
                "Ошибка валидации параметров",
                message,
                LocalDateTime.now()
        );
    }


    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(NotFoundException e) {
        log.warn("Сущность не найдена: {}", e.getMessage());
        return new ErrorResponse(
                "NOT_FOUND",
                "Запрашиваемый объект не найден.",
                e.getMessage(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(AlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleAlreadyExists(AlreadyExistsException e) {
        log.warn("Сущность уже существует: {}", e.getMessage());
        return new ErrorResponse(
                "CONFLICT",
                "Нарушено ограничение целостности данных.",
                e.getMessage(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingParameter(MissingServletRequestParameterException e) {
        log.warn("Неверный запрос: {}", e.getMessage());
        return new ErrorResponse(
                "BAD_REQUEST",
                "Неверный запрос",
                e.getMessage(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(BadRequestException e) {
        log.warn("Некорректный запрос: {}", e.getMessage());
        return new ErrorResponse(
                "BAD_REQUEST",
                "Некорректные параметры запроса",
                e.getMessage(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflictException(ConflictException e) {
        log.warn("Конфликт: {}", e.getMessage());
        return new ErrorResponse(
                "CONFLICT",
                "Нарушение бизнес-логики.",
                e.getMessage(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("Неправильный запрос: {}", e.getMessage());
        return new ErrorResponse(
                "BAD_REQUEST",
                "Переданы некорректные данные.",
                message,
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(Exception e) {
        log.error("Внутренняя ошибка сервера", e);
        return new ErrorResponse(
                "INTERNAL_ERROR",
                "Внутренняя ошибка сервера.",
                "Произошла непредвиденная ошибка. Администратор уже уведомлён.",
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicateEmail(DataIntegrityViolationException ex) {
        log.error("Проблема с уникальностью Email", ex);
        return new ErrorResponse("CONFLICT",
                "Нарушено ограничение целостности.",
                "не удалось выполнить оператор",
                LocalDateTime.now()
        );
    }
}