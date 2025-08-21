package com.echotrail.userms.controller;

import com.echotrail.userms.model.AuthProvider;
import com.echotrail.userms.model.User;
import com.echotrail.userms.security.JwtUtil;
import com.echotrail.userms.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserDetailsController.class)
@WithMockUser
class UserDetailsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void givenUserExists_whenGetUserByUsername_thenReturnsUser() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        user.setAuthProvider(AuthProvider.JWT);
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/username/testuser"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"username\":\"testuser\",\"authProvider\":\"JWT\"}"));
    }

    @Test
    void givenUserDoesNotExist_whenGetUserByUsername_thenReturnsNotFound() throws Exception {
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/username/testuser"))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenUserExists_whenGetUserIdByUsername_thenReturnsUserId() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/id/testuser"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    void givenUserDoesNotExist_whenGetUserIdByUsername_thenReturnsNotFound() throws Exception {
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/id/testuser"))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenUserExists_whenGetUserById_thenReturnsUser() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setAuthProvider(AuthProvider.JWT);
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":1,\"authProvider\":\"JWT\"}"));
    }

    @Test
    void givenUserDoesNotExist_whenGetUserById_thenReturnsNotFound() throws Exception {
        when(userService.getUserById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenUsernameExists_whenUsernameExists_thenReturnsOk() throws Exception {
        when(userService.usernameExists("testuser")).thenReturn(true);

        mockMvc.perform(get("/api/users/exists/testuser"))
                .andExpect(status().isOk());
    }

    @Test
    void givenUsernameDoesNotExist_whenUsernameExists_thenReturnsNotFound() throws Exception {
        when(userService.usernameExists("testuser")).thenReturn(false);

        mockMvc.perform(get("/api/users/exists/testuser"))
                .andExpect(status().isNotFound());
    }
}

