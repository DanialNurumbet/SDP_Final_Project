package com.sportrent.shared.error;

/** 404. Коды: EQUIPMENT_NOT_FOUND, RENTAL_NOT_FOUND. */
public class NotFoundException extends ApiException {

    public NotFoundException(String code, String message) {
        super(404, code, message, null);
    }
}
