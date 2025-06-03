package com.example.userms.controller;

import com.example.userms.dto.LoginResponse;
import com.example.userms.dto.MessageResponse;
import com.example.userms.model.User;
import com.example.userms.security.JwtUtil;
import com.example.userms.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/auth/oauth2")
public class OAuth2Controller {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public OAuth2Controller(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/success")
    public ResponseEntity<?> oauthSuccess(@AuthenticationPrincipal OAuth2User oauth2User) {
        if (oauth2User == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("No OAuth2 user found"));
        }

        try {
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");

            if (email == null || name == null) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Required OAuth2 attributes are missing"));
            }

            User user = userService.findOrCreateOAuth2User(email, name);
            String jwt = jwtUtil.generateToken(userService.createUserDetails(user));

            return ResponseEntity.ok(new LoginResponse(jwt, user.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error processing OAuth2 login: " + e.getMessage()));
        }
    }


    @GetMapping("/error")
    public ResponseEntity<?> oauthError() {
        return ResponseEntity.badRequest()
                .body(new MessageResponse("OAuth 2.0 authentication failed"));
    }
}