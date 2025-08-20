package com.echotrail.userms.controller;

import com.echotrail.userms.dto.UserDetailsDTO;
import com.echotrail.userms.model.User;
import com.echotrail.userms.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserDetailsController {

    private final UserService userService;

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDetailsDTO> getUserByUsername(@PathVariable String username) {
        User user = userService.getUserByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));

        return ResponseEntity.ok(convertToDTO(user));
    }

    @GetMapping("/id/{username}")
    public ResponseEntity<Long> getUserIdByUsername(@PathVariable String username, @RequestHeader(value = "X-Gateway-Service", required = false) String gatewayService) {
        User user = userService.getUserByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));

        return ResponseEntity.ok(user.getId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDetailsDTO> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        return ResponseEntity.ok(convertToDTO(user));
    }

    @GetMapping("/exists/{username}")
    public ResponseEntity<Void> usernameExists(@PathVariable String username) {
        if (userService.usernameExists(username)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private UserDetailsDTO convertToDTO(User user) {
        return UserDetailsDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .authProvider(user.getAuthProvider().name())
                .build();
    }
}
