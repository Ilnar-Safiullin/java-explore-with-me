package ru.practicum.exception;

public class AlreadyExistsException extends RuntimeException {

    public AlreadyExistsException(String entityName, String fieldName, String value) {
        super(String.format("%s с %s = '%s' уже существует", entityName, fieldName, value));
    }
}
