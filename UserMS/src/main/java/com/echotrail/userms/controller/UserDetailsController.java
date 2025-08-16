package com.echotrail.userms.controller;

import com.echotrail.userms.model.User;
import com.echotrail.userms.repository.UserRepository;
import com.echotrail.userms.dto.UserDetailsDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserDetailsController {

    private final UserRepository userRepository;

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDetailsDTO> getUserByUsername(@PathVariable String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));

        return ResponseEntity.ok(convertToDTO(user));
    }

    @GetMapping("/id/{username}")
    public ResponseEntity<Long> getUserIdByUsername(@PathVariable String username, @RequestHeader(value = "X-Gateway-Service", required = false) String gatewayService) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));

        return ResponseEntity.ok(user.getId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDetailsDTO> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        return ResponseEntity.ok(convertToDTO(user));
    }

    @GetMapping("/exists/{username}")
    public ResponseEntity<Boolean> usernameExists(@PathVariable String username) {
        return ResponseEntity.ok(userRepository.existsByUsername(username));
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
