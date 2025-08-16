package com.echotrail.userms.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String type = "Bearer";
    private String username;

    public LoginResponse(String token, String username) {
        this.token = token;
        this.username = username;
    }
}