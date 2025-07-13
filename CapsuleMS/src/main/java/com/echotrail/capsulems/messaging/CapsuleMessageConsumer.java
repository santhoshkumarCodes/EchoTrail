package com.echotrail.capsulems.messaging;

import com.echotrail.capsulems.messaging.dto.After;
import com.echotrail.capsulems.messaging.dto.DebeziumMessage;
import com.echotrail.capsulems.messaging.dto.DebeziumPayload;
import com.echotrail.capsulems.messaging.dto.EventPayload;
import com.echotrail.capsulems.model.CapsuleChain;
import com.echotrail.capsulems.repository.CapsuleChainRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.cassandra.core.CassandraBatchOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CapsuleMessageConsumer {

    private final ObjectMapper objectMapper;
    private final CapsuleChainRepository capsuleChainRepository;
    private final CassandraTemplate cassandraTemplate;

    private static final String NEXT_CAPSULE_ID_FIELD = "next_capsule_id";
    private static final String PREVIOUS_CAPSULE_ID_FIELD = "previous_capsule_id";

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 2.0),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)
    @KafkaListener(topics = "capsule.public.outbox", groupId = "capsule-ms")
    public void consume(String message) {
        log.info("Received message: {}", message);
        try {
            DebeziumMessage debeziumMessage = objectMapper.readValue(message, DebeziumMessage.class);
            DebeziumPayload payload = debeziumMessage.getPayload();

            if (payload == null || payload.getAfter() == null) {
                log.warn("Payload or after node is missing or null, skipping message");
                return;
            }

            After after = payload.getAfter();
            String eventType = after.getEventType();
            EventPayload eventPayload = objectMapper.readValue(after.getPayload(), EventPayload.class);

            if ("CapsuleCreated".equals(eventType)) {
                if (eventPayload.isChained()) {
                    long capsuleId = eventPayload.getId();
                    long userId = eventPayload.getUserId();
                    if (capsuleChainRepository.findById(capsuleId).isEmpty()) {
                        CapsuleChain capsuleChain = new CapsuleChain();
                        capsuleChain.setCapsuleId(capsuleId);
                        capsuleChain.setUserId(userId);
                        capsuleChainRepository.save(capsuleChain);
                        log.info("Created capsule chain for capsule id: {}", capsuleId);
                    }
                }
            } else if ("CapsuleDeleted".equals(eventType)) {
                long capsuleId = eventPayload.getId();
                boolean isChained = eventPayload.isChained();

                if (isChained) {
                    log.info("Capsule {} is chained. Attempting to delete chain.", capsuleId);
                    Optional<CapsuleChain> capsuleChainOpt = capsuleChainRepository.findById(capsuleId);
                    if (capsuleChainOpt.isPresent()) {
                        log.info("Capsule chain found for capsuleId: {}", capsuleId);
                        CapsuleChain capsuleChain = capsuleChainOpt.get();
                        Long prevId = capsuleChain.getPreviousCapsuleId();
                        Long nextId = capsuleChain.getNextCapsuleId();

                        CassandraBatchOperations batchOps = cassandraTemplate.batchOps();
                        batchOps.delete(capsuleChain);

                        if (prevId != null && nextId != null) {
                            // Case 1: The deleted capsule is in the middle of the chain
                            log.info("Relinking previous capsule {} to next capsule {}", prevId, nextId);
                            updateChainLink(batchOps, prevId, NEXT_CAPSULE_ID_FIELD, nextId);
                            updateChainLink(batchOps, nextId, PREVIOUS_CAPSULE_ID_FIELD, prevId);
                        } else if (prevId != null) {
                            // Case 2: The deleted capsule is at the end of the chain
                            log.info("Updating previous capsule {} to mark the end of the chain", prevId);
                            updateChainLink(batchOps, prevId, NEXT_CAPSULE_ID_FIELD, null);
                        } else if (nextId != null) {
                            // Case 3: The deleted capsule is at the beginning of the chain
                            log.info("Updating next capsule {} to mark the beginning of the chain", nextId);
                            updateChainLink(batchOps, nextId, PREVIOUS_CAPSULE_ID_FIELD, null);
                        }

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
            throw new RuntimeException(e);
        }
    }

    @DltHandler
    public void dlt(String message) {
        log.error("Message moved to DLT: {}", message);
    }

    private void updateChainLink(CassandraBatchOperations batchOps, Long capsuleId, String fieldToUpdate, Long value) {
        capsuleChainRepository.findById(capsuleId).ifPresent(chain -> {
            if (NEXT_CAPSULE_ID_FIELD.equals(fieldToUpdate)) {
                chain.setNextCapsuleId(value);
            } else if (PREVIOUS_CAPSULE_ID_FIELD.equals(fieldToUpdate)) {
                chain.setPreviousCapsuleId(value);
            }
            batchOps.update(chain);
        });
    }
}