package com.echotrail.gateway.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for communication with the UserMS service.
 */
@FeignClient(name = "UserMS", path = "/api/users", url = "${user-service.url:}")
public interface UserServiceClient {

    /**
     * Get user ID by username
     * 
     * @param username the username to find the ID for
     * @return the user ID
     */
    @GetMapping("/id/{username}")
    ResponseEntity<Long> getUserIdByUsername(@PathVariable("username") String username);
}
