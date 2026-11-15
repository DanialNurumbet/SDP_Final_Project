package com.sportrent.shared.error;

/** 409. Коды: EQUIPMENT_UNAVAILABLE, EQUIPMENT_IN_USE, RENTAL_ALREADY_COMPLETED. */
public class ConflictException extends ApiException {

    public ConflictException(String code, String message) {
        super(409, code, message, null);
    }
}
