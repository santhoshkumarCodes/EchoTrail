package com.echotrail.capsulems.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class CapsuleDeletionException extends RuntimeException {
    public CapsuleDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}
