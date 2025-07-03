package com.echotrail.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String message;
    private String path;
    private int status;
    private LocalDateTime timestamp;

    public static ErrorResponse of(String message, String path, int status) {
        return new ErrorResponse(message, path, status, LocalDateTime.now());
    }
}
