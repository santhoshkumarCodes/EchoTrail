package com.echotrail.userms.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    private String username;
    private String password;

    @Override
    public String toString() {
        return "LoginRequest{"
                + "username='" + username + "'"
                + '}';
    }
}
