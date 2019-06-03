package com.mycompany.exchangerate.exception;

import com.mycompany.core.exception.AbstractBusinessException;

import org.springframework.http.HttpStatus;

public class ExchangeRateException extends AbstractBusinessException {

    private ProcessingError processingError;

    public ExchangeRateException(ProcessingError processingError) {
        this.processingError = processingError;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return processingError.getHttpStatus();
    }
}
