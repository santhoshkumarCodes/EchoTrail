package com.echotrail.capsulems.messaging;

import com.echotrail.capsulems.model.CapsuleChain;
import com.echotrail.capsulems.repository.CapsuleChainRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.CassandraBatchOperations;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CapsuleMessageConsumer {

    private final ObjectMapper objectMapper;
    private final CapsuleChainRepository capsuleChainRepository;
    private final CassandraTemplate cassandraTemplate;

    @KafkaListener(topics = "capsule.public.outbox", groupId = "capsule-ms")
    public void consume(String message) {
        log.info("Received message: {}", message);
        try {
            JsonNode rootNode = objectMapper.readTree(message);
            JsonNode payloadNode = rootNode.path("payload");

            if (payloadNode.isMissingNode() || payloadNode.isNull()) {
                log.warn("Payload is missing or null, skipping message");
                return;
            }

            JsonNode afterNode = payloadNode.path("after");
            if (afterNode.isMissingNode() || afterNode.isNull()) {
                log.warn("After node is missing or null, skipping message");
                return;
            }

            String eventType = afterNode.path("event_type").asText();
            String eventPayload = afterNode.path("payload").asText();

            if ("CapsuleCreated".equals(eventType)) {
                JsonNode capsulePayload = objectMapper.readTree(eventPayload);
                if (capsulePayload.path("chained").asBoolean()) {
                    long capsuleId = capsulePayload.path("id").asLong();
                    long userId = capsulePayload.path("userId").asLong();
                    if (capsuleChainRepository.findById(capsuleId).isEmpty()) {
                        CapsuleChain capsuleChain = new CapsuleChain();
                        capsuleChain.setCapsuleId(capsuleId);
                        capsuleChain.setUserId(userId);
                        capsuleChainRepository.save(capsuleChain);
                        log.info("Created capsule chain for capsule id: {}", capsuleId);
                    }
                }
            } else if ("CapsuleDeleted".equals(eventType)) {
                JsonNode deletePayload = objectMapper.readTree(eventPayload);
                long capsuleId = deletePayload.path("id").asLong();
                boolean isChained = deletePayload.path("isChained").asBoolean();

                if (isChained) {
                    log.info("Capsule {} is chained. Attempting to delete chain.", capsuleId);
                    Optional<CapsuleChain> capsuleChainOpt = capsuleChainRepository.findById(capsuleId);
                    if (capsuleChainOpt.isPresent()) {
                        log.info("Capsule chain found for capsuleId: {}", capsuleId);
                        CapsuleChain capsuleChain = capsuleChainOpt.get();
                        Long prevId = capsuleChain.getPreviousCapsuleId();
                        Long nextId = capsuleChain.getNextCapsuleId();

                        CassandraBatchOperations batchOps = cassandraTemplate.batchOps();

                        if (prevId != null) {
                            log.info("Updating previous capsule {} nextId to {}", prevId, nextId);
                            capsuleChainRepository.findById(prevId).ifPresent(prev -> {
                                prev.setNextCapsuleId(nextId);
                                batchOps.update(prev);
                            });
                        }

                        if (nextId != null) {
                            log.info("Updating next capsule {} previousId to {}", nextId, prevId);
                            capsuleChainRepository.findById(nextId).ifPresent(next -> {
                                next.setPreviousCapsuleId(prevId);
                                batchOps.update(next);
                            });
                        }

                        batchOps.delete(capsuleChain);
                        batchOps.execute();

                        log.info("Deleted and re-linked capsule chain for capsule id: {}", capsuleId);
                    } else {
                        log.warn("Capsule chain not found for capsuleId: {}. Skipping chain deletion.", capsuleId);
                    }
                } else {
                    log.info("Capsule {} is not chained. Skipping chain deletion.", capsuleId);
                }
            }

        } catch (Exception e) {
            log.error("Error processing message: {}", message, e);
        }
    }
}
