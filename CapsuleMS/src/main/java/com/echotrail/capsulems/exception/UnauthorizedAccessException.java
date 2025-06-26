package com.echotrail.capsulems.exception;

public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException() {
        super("Unauthorized capsule access");
    }
}
