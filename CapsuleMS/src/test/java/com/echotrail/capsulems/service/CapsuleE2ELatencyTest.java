package com.echotrail.capsulems.service;

import com.echotrail.capsulems.repository.CapsuleChainRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CapsuleE2ELatencyTest {

    @Autowired
    private com.echotrail.capsulems.service.CapsuleService capsuleService;

    @Autowired
    private CapsuleChainRepository capsuleChainRepository;

    // Connect to local docker services
    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:postgresql://localhost:5433/Capsules");
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "postgres");
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("spring.cassandra.contact-points", () -> "localhost");
        registry.add("spring.cassandra.port", () -> "9042");
        registry.add("spring.cassandra.keyspace-name", () -> "echotrail");
        registry.add("spring.cassandra.local-datacenter", () -> "datacenter1");
        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:29092");
        registry.add("spring.kafka.consumer.group-id", () -> "capsule-ms-test");
        registry.add("spring.cassandra.schema-action", () -> "create_if_not_exists");
        registry.add("eureka.client.enabled", () -> "false");
        registry.add("spring.kafka.consumer.max-poll-records", () -> "500");
        registry.add("spring.kafka.listener.concurrency", () -> "3");
    }

    @Test
    public void testLowLoad_50ConcurrentRequests() {
        runLoadTest("Low Load", 50, true);
    }

    @Test
    public void testRegularLoad_100ConcurrentRequests() {
        // Run a full warmup pass to prime JIT and pools
        System.out.println("=== Executing Warmup Run (100 reqs) ===");
        runLoadTest("Warmup", 100, false);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }

        // Real measurement
        System.out.println("=== Executing Real Run (100 reqs) ===");
        runLoadTest("Regular Load", 100, true);
    }

    @Test
    public void testModerateLoad_200ConcurrentRequests() {
        runLoadTest("Moderate Load", 200, true);
    }

    private void runLoadTest(String testName, int concurrentRequests, boolean verifyLatency) {
        System.out.println("Starting [" + testName + "] with " + concurrentRequests + " concurrent requests...");

        // Generate load
        java.util.stream.IntStream.range(0, concurrentRequests).parallel().forEach(i -> {
            try {
                com.echotrail.capsulems.DTO.CapsuleRequest request = com.echotrail.capsulems.DTO.CapsuleRequest
                        .builder()
                        .title("Load Test Capsule " + i)
                        .contentMarkdown("Content " + i)
                        .unlockAt(LocalDateTime.now().plusHours(1))
                        .isChained(true)
                        .build();
                capsuleService.createCapsule(100L, request);
            } catch (Exception e) {
                // Ignore load generation errors
            }
        });

        // Measure latency of a specific tracked capsule
        long userId = 100L;
        com.echotrail.capsulems.DTO.CapsuleRequest request = com.echotrail.capsulems.DTO.CapsuleRequest.builder()
                .title("Latency Measurement Capsule (" + testName + ")")
                .contentMarkdown("Latency Test Capsule Content")
                .unlockAt(LocalDateTime.now().plusHours(1))
                .isChained(true)
                .build();

        long startTime = System.currentTimeMillis();
        com.echotrail.capsulems.DTO.CapsuleResponse response = capsuleService.createCapsule(userId, request);
        long capsuleId = response.getId();
        System.out.println("Inserted Measurement Capsule ID: " + capsuleId);

        try {
            await().atMost(60, TimeUnit.SECONDS).pollInterval(Duration.ofMillis(10)).until(() -> {
                return capsuleChainRepository.findById(capsuleId).isPresent();
            });
        } catch (Exception e) {
            if (verifyLatency)
                throw e;
            System.out.println("Warmup wait timed out or failed (ignoring)");
            return;
        }

        long endTime = System.currentTimeMillis();
        long latency = endTime - startTime;

        System.out.println("[" + testName + "] Load: " + concurrentRequests + " | Sync Latency: " + latency + "ms");

        // Assertions
        if (verifyLatency) {
            if (concurrentRequests <= 50) {
                assertTrue(latency < 1000, "[" + testName + "] Latency should be < 1000ms, was " + latency);
            } else if (concurrentRequests <= 100) {
                // Aim for < 500ms
                assertTrue(latency < 600,
                        "[" + testName + "] Latency should be < 600ms (aiming for 500), was " + latency);
            } else {
                assertTrue(latency < 3000, "[" + testName + "] Latency should be reasonable, was " + latency);
            }
        }
    }
}
