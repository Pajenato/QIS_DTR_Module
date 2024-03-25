package com.dtr.qpd.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class QisResourceNotFoundException extends RuntimeException {
    public QisResourceNotFoundException(String message){
        super(message);
    }
}
