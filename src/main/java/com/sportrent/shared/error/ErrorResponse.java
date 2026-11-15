package com.sportrent.shared.error;

import java.util.Map;

/** Единый формат ошибки: {"code": "...", "message": "...", "fieldErrors": {}} */
public record ErrorResponse(String code, String message, Map<String, String> fieldErrors) {
}
