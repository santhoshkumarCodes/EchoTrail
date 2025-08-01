package com.echotrail.capsulems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
@EnableDiscoveryClient
public class CapsuleMsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CapsuleMsApplication.class, args);
    }

}
