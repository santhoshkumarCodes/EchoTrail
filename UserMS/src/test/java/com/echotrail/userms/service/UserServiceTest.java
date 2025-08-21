package com.echotrail.userms.service;

import com.echotrail.userms.exception.UserAlreadyExistsException;
import com.echotrail.userms.model.User;
import com.echotrail.userms.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void givenNewUser_whenRegisterNewUser_thenSavesUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setPassword("password");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        userService.registerNewUser(user);

        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenExistingUsername_whenRegisterNewUser_thenThrowsUserAlreadyExistsException() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@test.com");

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.registerNewUser(user));
    }

    @Test
    void givenExistingEmail_whenRegisterNewUser_thenThrowsUserAlreadyExistsException() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@test.com");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.registerNewUser(user));
    }

    @Test
    void givenExistingOAuth2User_whenFindOrCreateOAuth2User_thenReturnsExistingUser() {
        User user = new User();
        user.setEmail("test@test.com");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        User result = userService.findOrCreateOAuth2User("test@test.com", "Test User");

        assertEquals(user, result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void givenNewOAuth2User_whenFindOrCreateOAuth2User_thenCreatesAndReturnsNewUser() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class))).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.findOrCreateOAuth2User("test@test.com", "Test User");

        assertEquals("test@test.com", result.getEmail());
        assertEquals("Test User", result.getName());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void givenUsernameExists_whenFindOrCreateOAuth2User_thenCreatesUserWithSuffix() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("test")).thenReturn(true);
        when(userRepository.existsByUsername("test1")).thenReturn(false);
        when(passwordEncoder.encode(any(String.class))).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.findOrCreateOAuth2User("test@test.com", "Test User");

        assertEquals("test1", result.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }


}
