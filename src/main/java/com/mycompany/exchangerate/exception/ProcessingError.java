package com.mycompany.exchangerate.exception;

import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

public enum ProcessingError {

    INVALID_DOWNSTREAM_RESPONSE(SERVICE_UNAVAILABLE),
    EMPTY_HISTORY(SERVICE_UNAVAILABLE), START_AFTER_END(BAD_REQUEST);

    private HttpStatus httpStatus;

    ProcessingError(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
