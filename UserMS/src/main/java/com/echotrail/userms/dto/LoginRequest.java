package com.echotrail.userms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @NotBlank(message = "Username cannot be blank")
    private String username;
    @NotBlank(message = "Password cannot be blank")
    private String password;

    @Override
    public String toString() {
        return "LoginRequest{"
                + "username='" + username + "'"
                + '}';
    }
}
