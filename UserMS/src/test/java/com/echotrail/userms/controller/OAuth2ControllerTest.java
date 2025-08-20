package com.echotrail.userms.controller;

import com.echotrail.userms.model.User;
import com.echotrail.userms.security.JwtUtil;
import com.echotrail.userms.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OAuth2Controller.class)
class OAuth2ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void givenValidOAuth2User_whenOauthSuccess_thenReturnsJwtToken() throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "test@test.com");
        attributes.put("name", "Test User");
        OAuth2User oAuth2User = new DefaultOAuth2User(Collections.emptyList(), attributes, "email");

        User user = new User();
        user.setEmail("test@test.com");
        user.setUsername("testuser");

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("testuser")
                .password("")
                .roles("USER")
                .build();

        when(userService.findOrCreateOAuth2User("test@test.com", "Test User")).thenReturn(user);
        when(userService.createUserDetails(any(User.class))).thenReturn(userDetails);
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("test-jwt");

        mockMvc.perform(get("/api/auth/oauth2/success").with(oauth2Login().oauth2User(oAuth2User)))
                .andExpect(status().isOk());
    }

    @Test
    void givenOAuth2UserWithMissingName_whenOauthSuccess_thenReturnsBadRequest() throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "test@test.com");
        OAuth2User oAuth2User = new DefaultOAuth2User(Collections.emptyList(), attributes, "email");

        mockMvc.perform(get("/api/auth/oauth2/success").with(oauth2Login().oauth2User(oAuth2User)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenOAuth2UserWithMissingEmail_whenOauthSuccess_thenReturnsBadRequest() throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", "Test User");
        OAuth2User oAuth2User = new DefaultOAuth2User(Collections.emptyList(), attributes, "name");

        mockMvc.perform(get("/api/auth/oauth2/success").with(oauth2Login().oauth2User(oAuth2User)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void whenOauthError_thenReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/auth/oauth2/error"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenNoOAuth2User_whenOauthSuccess_thenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/oauth2/success"))
                .andExpect(status().isUnauthorized());
    }
}
