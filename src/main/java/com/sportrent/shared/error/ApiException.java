package com.sportrent.shared.error;

import java.util.Map;

/** Базовая ошибка API. Все модули бросают её наследников, контроллеры ничего не ловят. */
public abstract class ApiException extends RuntimeException {

    private final int status;
    private final String code;
    private final Map<String, String> fieldErrors;

    protected ApiException(int status, String code, String message, Map<String, String> fieldErrors) {
        super(message);
        this.status = status;
        this.code = code;
        this.fieldErrors = fieldErrors == null ? Map.of() : Map.copyOf(fieldErrors);
    }

    public int getStatus() { return status; }

    public String getCode() { return code; }

    public Map<String, String> getFieldErrors() { return fieldErrors; }
}
