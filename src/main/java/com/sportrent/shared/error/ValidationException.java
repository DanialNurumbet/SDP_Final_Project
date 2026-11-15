package com.sportrent.shared.error;

import java.util.Map;

/** 400. Пример: new ValidationException("Срок должен быть от 1 до 30 дней", Map.of("days", "Допустимо 1–30")) */
public class ValidationException extends ApiException {

    public ValidationException(String message) {
        this(message, Map.of());
    }

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(400, "VALIDATION_ERROR", message, fieldErrors);
    }
}
