package com.echotrail.gateway.filter;

import com.echotrail.gateway.client.UserServiceClient;
import com.echotrail.gateway.config.JwtConfig;
import com.echotrail.gateway.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class JwtAuthorizationFilterScenariosTest {

    @Mock
    private JwtConfig jwtConfig;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserServiceClient userServiceClient;

    private JwtAuthorizationFilter jwtAuthorizationFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtAuthorizationFilter = new JwtAuthorizationFilter(jwtConfig, jwtUtil, userServiceClient);
    }

    @Test
    void doFilterInternal_withMissingAuthHeader_shouldReturnUnauthorized() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/some-endpoint");

        // When
        MockFilterChain filterChain = new MockFilterChain();
        MockHttpServletResponse response = new MockHttpServletResponse();
        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void doFilterInternal_withInvalidAuthHeader_shouldReturnUnauthorized() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/some-endpoint");
        request.addHeader("Authorization", "Invalid-token");

        when(jwtConfig.getHeader()).thenReturn("Authorization");
        when(jwtConfig.getPrefix()).thenReturn("Bearer ");

        // When
        MockFilterChain filterChain = new MockFilterChain();
        MockHttpServletResponse response = new MockHttpServletResponse();
        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void doFilterInternal_withInvalidToken_shouldReturnUnauthorized() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/some-endpoint");
        request.addHeader("Authorization", "Bearer invalid-token");

        when(jwtConfig.getHeader()).thenReturn("Authorization");
        when(jwtConfig.getPrefix()).thenReturn("Bearer ");
        when(jwtUtil.validateToken("invalid-token")).thenReturn(false);

        // When
        MockFilterChain filterChain = new MockFilterChain();
        MockHttpServletResponse response = new MockHttpServletResponse();
        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void doFilterInternal_withPublicEndpoint_shouldSkipFilter() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/auth/login");

        // When
        MockFilterChain filterChain = new MockFilterChain();
        MockHttpServletResponse response = new MockHttpServletResponse();
        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(200);
    }
}
