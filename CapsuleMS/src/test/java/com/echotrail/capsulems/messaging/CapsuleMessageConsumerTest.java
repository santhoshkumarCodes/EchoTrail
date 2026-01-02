package com.echotrail.capsulems.messaging;

import com.echotrail.capsulems.model.CapsuleChain;
import com.echotrail.capsulems.repository.CapsuleChainRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.core.CassandraBatchOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { CapsuleMessageConsumerTest.TestConfig.class, CapsuleMessageConsumer.class })
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" }, topics = {
        "capsule.public.outbox" })
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

        @Bean
        public CassandraTemplate cassandraTemplate() {
            return mock(CassandraTemplate.class);
        }
    }

    @MockitoBean
    private CapsuleChainRepository capsuleChainRepository;

    @Autowired
    private CapsuleMessageConsumer capsuleMessageConsumer;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CassandraTemplate cassandraTemplate;

    @Mock
    private CassandraBatchOperations batchOperations;

    @Test
    void consume_CapsuleCreated_Chained() throws Exception {
        // Given
        ObjectNode capsulePayload = objectMapper.createObjectNode();
        capsulePayload.put("id", 1L);
        capsulePayload.put("userId", 1L);
        capsulePayload.put("chained", true);

        String message = objectMapper.writeValueAsString(capsulePayload);

        org.apache.kafka.clients.consumer.ConsumerRecord<String, String> record = new org.apache.kafka.clients.consumer.ConsumerRecord<>(
                "capsule.public.outbox", 0, 0L, "key", message);
        record.headers().add("event_type", "CapsuleCreated".getBytes());

        when(capsuleChainRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        capsuleMessageConsumer.consume(record);

        // Then
        verify(capsuleChainRepository, timeout(5000).times(1)).save(any(CapsuleChain.class));
    }

    @Test
    void consume_CapsuleDeleted_Chained() throws Exception {
        // Given
        ObjectNode deletePayload = objectMapper.createObjectNode();
        deletePayload.put("id", 1L);
        deletePayload.put("chained", true);

        String message = objectMapper.writeValueAsString(deletePayload);

        org.apache.kafka.clients.consumer.ConsumerRecord<String, String> record = new org.apache.kafka.clients.consumer.ConsumerRecord<>(
                "capsule.public.outbox", 0, 0L, "key", message);
        record.headers().add("event_type", "CapsuleDeleted".getBytes());

        CapsuleChain chain = new CapsuleChain(1L, null, null, 1L);
        when(capsuleChainRepository.findById(1L)).thenReturn(Optional.of(chain));
        when(cassandraTemplate.batchOps()).thenReturn(batchOperations);

        // When
        capsuleMessageConsumer.consume(record);

        // Then
        verify(batchOperations, timeout(5000).times(1)).execute();
    }
}
