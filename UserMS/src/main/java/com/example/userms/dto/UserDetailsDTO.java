package com.example.userms.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Data Transfer Object for User details.
 * Contains user information without sensitive data like passwords.
 */
@Data
@Builder
public class UserDetailsDTO {
    private Long id;
    private String username;
    private String email;
    private String name;
    private String authProvider;
}
