package com.echotrail.serviceregistry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ServiceRegistryControllerTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @BeforeEach
    public void setup() {
        testRestTemplate = testRestTemplate.withBasicAuth("admin", "password");
    }

    @Test
    public void testServiceRegistrationAndDiscovery() {
        // Given
        String appName = "test-app";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String requestBody = "{\"instance\":{\"hostName\":\"localhost\",\"app\":\"" + appName + "\",\"ipAddr\":\"127.0.0.1\",\"status\":\"UP\",\"port\":{\"@enabled\":\"true\",\"$\":\"8080\"},\"dataCenterInfo\":{\"@class\":\"com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo\",\"name\":\"MyOwn\"}}}";
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        // When
        ResponseEntity<String> response = testRestTemplate.postForEntity(
                "/eureka/apps/" + appName,
                request,
                String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // When
        HttpHeaders acceptHeaders = new HttpHeaders();
        acceptHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
        HttpEntity<String> entity = new HttpEntity<>(acceptHeaders);
        ResponseEntity<String> discoveryResponse = testRestTemplate.getForEntity("/eureka/apps/" + appName, String.class, entity);

        // Then
        assertThat(discoveryResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(discoveryResponse.getBody()).contains(appName.toUpperCase());
    }
}
