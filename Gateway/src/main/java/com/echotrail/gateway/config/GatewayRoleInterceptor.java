package com.echotrail.gateway.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

/**
 * Interceptor to add Gateway role credentials to UserMS requests
 */
@Component
public class GatewayRoleInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        // Add header to identify requests from gateway service
        template.header("X-Gateway-Service", "true");
    }
}
