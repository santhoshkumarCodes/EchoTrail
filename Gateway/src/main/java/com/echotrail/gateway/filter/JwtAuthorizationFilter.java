package com.echotrail.gateway.filter;

import com.echotrail.gateway.client.UserServiceClient;
import com.echotrail.gateway.config.JwtConfig;
import com.echotrail.gateway.util.JwtUtil;
import feign.FeignException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(1)
@Slf4j
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtConfig jwtConfig;
    private final JwtUtil jwtUtil;
    private final UserServiceClient userServiceClient;

    public JwtAuthorizationFilter(JwtConfig jwtConfig, JwtUtil jwtUtil, UserServiceClient userServiceClient) {
        this.jwtConfig = jwtConfig;
        this.jwtUtil = jwtUtil;
        this.userServiceClient = userServiceClient;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Skip JWT verification for authentication paths
        if (path.startsWith("/api/auth/login") || path.startsWith("/api/auth/register")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(jwtConfig.getHeader());

        if (authHeader == null || !authHeader.startsWith(jwtConfig.getPrefix())) {
            log.error("Missing or invalid Authorization header");
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Missing or invalid Authorization token");
            return;
        }

        String token = authHeader.replace(jwtConfig.getPrefix(), "").trim();

        try {
            if (jwtUtil.validateToken(token)) {
                // Extract claims and add to request for use in downstream services if needed
                String username = jwtUtil.extractUsername(token);
                request.setAttribute("username", username);

                try {
                    // Fetch userId from UserMS service using Feign client
                    log.debug("Attempting to fetch userId for username: {}", username);
                    ResponseEntity<Long> userIdResponse = userServiceClient.getUserIdByUsername(username);
                    if (userIdResponse.getBody() != null) {
                        Long userId = userIdResponse.getBody();

                        // Store userId in request attribute
                        request.setAttribute("userId", userId);

                        log.debug("Added userId {} and username {} to request attributes", userId, username);
                    }
                } catch (FeignException e) {
                    log.warn("Unable to fetch userId for username {}: [{}] during [{}] to [{}] [{}]: [{}] - Status: {}", 
                          username, e.status(), request.getMethod(), e.request().url(), e.request().requestTemplate().feignTarget().type().getSimpleName(), 
                          e.contentUTF8(), e.status());
                } catch (Exception e) {
                    log.error("Unexpected error fetching userId for username {}: {}", username, e.getMessage(), e);
                }

                filterChain.doFilter(request, response);
            } else {
                log.error("Token validation failed");
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Token validation failed");
            }
        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Token validation error");
        }
    }

}
