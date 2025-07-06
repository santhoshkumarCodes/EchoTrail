package com.echotrail.capsulems.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class CapsuleCreationException extends RuntimeException {
    public CapsuleCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
