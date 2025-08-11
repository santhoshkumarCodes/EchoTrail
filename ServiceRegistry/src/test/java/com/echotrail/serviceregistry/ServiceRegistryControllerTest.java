package com.echotrail.serviceregistry;

import com.echotrail.serviceregistry.model.DataCenterInfo;
import com.echotrail.serviceregistry.model.InstanceInfo;
import com.echotrail.serviceregistry.model.InstanceWrapper;
import com.echotrail.serviceregistry.model.Port;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = { "spring.security.user.name=admin", "spring.security.user.password=password" })
public class ServiceRegistryControllerTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Value("${spring.security.user.name}")
    private String username;

    @Value("${spring.security.user.password}")
    private String password;

    @BeforeEach
    public void setup() {
        testRestTemplate = testRestTemplate.withBasicAuth(username, password);
    }

    @Test
    public void testServiceRegistrationAndDiscovery() throws JsonProcessingException {
        // Given
        String appName = "test-app";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Port port = new Port("true", "8080");
        DataCenterInfo dataCenterInfo = new DataCenterInfo("com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo", "MyOwn");
        InstanceInfo instanceInfo = new InstanceInfo("localhost", appName, "127.0.0.1", "UP", port, dataCenterInfo);
        InstanceWrapper instanceWrapper = new InstanceWrapper(instanceInfo);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(instanceWrapper);

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
