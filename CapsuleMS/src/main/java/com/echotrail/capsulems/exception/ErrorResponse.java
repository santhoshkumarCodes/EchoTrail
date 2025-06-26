package com.echotrail.capsulems.exception;

import lombok.Data;

import java.time.LocalDateTime;

@Data
class ErrorResponse {
    private String message;
    private LocalDateTime timestamp = LocalDateTime.now();

    public ErrorResponse(String message) {
        this.message = message;
    }
}