package com.echotrail.capsulems.messaging;

import com.echotrail.capsulems.model.CapsuleChain;
import com.echotrail.capsulems.repository.CapsuleChainRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CapsuleMessageConsumerTest.TestConfig.class, CapsuleMessageConsumer.class})
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" }, topics = {"capsule.public.outbox"})
class CapsuleMessageConsumerTest {

    @Configuration
    static class TestConfig {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @SuppressWarnings("unchecked")
        @Bean
        public KafkaTemplate<String, String> kafkaTemplate() {
            return mock(KafkaTemplate.class);
        }
    }

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @MockBean
    private CapsuleChainRepository capsuleChainRepository;

    @Autowired
    private CapsuleMessageConsumer capsuleMessageConsumer;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void consume_CapsuleCreated_Chained() throws Exception {
        // Given
        String capsulePayload = "{\"id\":1,\"userId\":1,\"chained\":true}";

        ObjectNode afterNode = objectMapper.createObjectNode();
        afterNode.put("event_type", "CapsuleCreated");
        afterNode.put("payload", capsulePayload);

        ObjectNode payloadNode = objectMapper.createObjectNode();
        payloadNode.set("after", afterNode);

        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.set("payload", payloadNode);

        String message = objectMapper.writeValueAsString(rootNode);

        when(capsuleChainRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        capsuleMessageConsumer.consume(message);

        // Then
        verify(capsuleChainRepository, timeout(5000).times(1)).save(any(CapsuleChain.class));
    }

    @Test
    void consume_CapsuleDeleted_Chained() throws Exception {
        // Given
        String deletePayloadContent = "{\"id\":1,\"isChained\":true}";

        ObjectNode afterNode = objectMapper.createObjectNode();
        afterNode.put("event_type", "CapsuleDeleted");
        afterNode.put("payload", deletePayloadContent);

        ObjectNode payloadNode = objectMapper.createObjectNode();
        payloadNode.set("after", afterNode);

        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.set("payload", payloadNode);

        String message = objectMapper.writeValueAsString(rootNode);

        CapsuleChain chain = new CapsuleChain(1L, null, null, 1L);
        when(capsuleChainRepository.findById(1L)).thenReturn(Optional.of(chain));

        // When
        capsuleMessageConsumer.consume(message);

        // Then
        verify(capsuleChainRepository, timeout(5000).times(1)).delete(any(CapsuleChain.class));
    }
}