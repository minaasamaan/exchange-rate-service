package com.mycompany.core.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(AbstractBusinessException.class)
    public ResponseEntity handleBusinessException(AbstractBusinessException businessException) {
        return ResponseEntity.status(businessException.getHttpStatus()).build();
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        if (status.is5xxServerError()) {
            logger.error("Unhandled error happened during processing", ex);
        }
        return super.handleExceptionInternal(ex, body, headers, status, request);
    }
}
