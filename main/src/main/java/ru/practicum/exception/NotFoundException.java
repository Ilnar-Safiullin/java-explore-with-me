package ru.practicum.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String entityName, String fieldName, Object value) {
        super(String.format("%s с %s = '%s' не найден", entityName, fieldName, value));
    }
}