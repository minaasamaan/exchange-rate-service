package com.mycompany.core.exception;

import org.springframework.http.HttpStatus;

public abstract class AbstractBusinessException extends RuntimeException {
    public abstract HttpStatus getHttpStatus();
}
