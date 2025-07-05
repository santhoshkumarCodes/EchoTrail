package com.echotrail.capsulems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaRepositories(basePackages = "com.echotrail.capsulems.repository")
@EnableCassandraRepositories(basePackages = "com.echotrail.capsulems.repository")
public class CapsuleMsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CapsuleMsApplication.class, args);
    }

}
