package com.echotrail.capsulems.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CapsuleNotFoundException extends RuntimeException {
    public CapsuleNotFoundException() {
        super("Capsule not found");
    }

    public CapsuleNotFoundException(String message) {
        super(message);
    }
}
