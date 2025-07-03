package com.echotrail.gateway.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(2)
@Slf4j
public class UserHeaderFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Check if userId attribute is set by JwtAuthorizationFilter
        Object userId = request.getAttribute("userId");
        Object xUserId = request.getAttribute("X-UserId");

        if (userId != null) {
            // Wrap the request to add the X-UserId header
            HeaderMapRequestWrapper requestWrapper = new HeaderMapRequestWrapper(request);
            requestWrapper.addHeader("X-UserId", userId.toString());
            log.debug("Added X-UserId header: {}", userId);

            // Continue with the modified request
            filterChain.doFilter(requestWrapper, response);
        } else if (xUserId != null) {
            // Wrap the request to add the X-UserId header
            HeaderMapRequestWrapper requestWrapper = new HeaderMapRequestWrapper(request);
            requestWrapper.addHeader("X-UserId", xUserId.toString());
            log.debug("Added X-UserId header from X-UserId attribute: {}", xUserId);

            // Continue with the modified request
            filterChain.doFilter(requestWrapper, response);
        } else {
            // Continue with the original request
            filterChain.doFilter(request, response);
        }
    }
}
