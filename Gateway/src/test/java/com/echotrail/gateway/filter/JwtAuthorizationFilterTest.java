package com.echotrail.gateway.filter;

import com.echotrail.gateway.client.UserServiceClient;
import com.echotrail.gateway.config.JwtConfig;
import com.echotrail.gateway.util.JwtUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class JwtAuthorizationFilterTest {

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
    void doFilterInternal_withValidToken_shouldAddHeadersToRequest() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/some-endpoint");
        request.addHeader("Authorization", "Bearer valid-token");

        when(jwtConfig.getHeader()).thenReturn("Authorization");
        when(jwtConfig.getPrefix()).thenReturn("Bearer ");
        when(jwtUtil.validateToken("valid-token")).thenReturn(true);
        when(jwtUtil.extractUsername("valid-token")).thenReturn("testuser");
        when(userServiceClient.getUserIdByUsername("testuser")).thenReturn(ResponseEntity.ok(1L));

        // When
        MockFilterChain filterChain = new MockFilterChain();
        jwtAuthorizationFilter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);

        // Then
        HeaderMapRequestWrapper wrappedRequest = (HeaderMapRequestWrapper) filterChain.getRequest();
        Assertions.assertNotNull(wrappedRequest);
        assertThat(wrappedRequest.getHeader("X-Username")).isEqualTo("testuser");
        assertThat(wrappedRequest.getHeader("X-UserId")).isEqualTo("1");
    }
}
