package com.sportrent.storage;

import com.sportrent.shared.error.ApiException;

public class StorageException extends ApiException {
    StorageException(String message, Throwable cause) {
        super(500, "STORAGE_ERROR", message, null);
        initCause(cause);
    }
}
