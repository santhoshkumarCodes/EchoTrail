package com.echotrail.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public GatewayRoleInterceptor gatewayRoleInterceptor() {
        return new GatewayRoleInterceptor();
    }
}
