package com.echotrail.capsulems.service;

import com.datastax.oss.driver.api.core.cql.BatchStatement;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.echotrail.capsulems.DTO.*;
import com.echotrail.capsulems.exception.*;
import com.echotrail.capsulems.model.*;
import com.echotrail.capsulems.repository.*;
import com.echotrail.capsulems.util.MarkdownProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CapsuleService {

    private final CapsuleRepository capsuleRepository;
    private final CapsuleChainRepository capsuleChainRepository;
    private final MarkdownProcessor markdownProcessor;
    private final CassandraTemplate cassandraTemplate;

    public CapsuleResponse createCapsule(Long userId, CapsuleRequest request) {
        // 1. Save to PostgreSQL
        Capsule capsule = new Capsule();
        capsule.setUserId(userId);
        capsule.setTitle(request.getTitle());
        capsule.setContentMarkdown(request.getContentMarkdown());
        capsule.setContentHtml(markdownProcessor.toHtml(request.getContentMarkdown()));
        capsule.setPublic(request.isPublic());
        capsule.setChained(request.isChained());
        capsule.setUnlockAt(request.getUnlockAt());
        Capsule savedCapsule = capsuleRepository.save(capsule);

        // 2. Save to Cassandra
        if (request.isChained()) {
            try {
                CapsuleChain capsuleChain = new CapsuleChain();
                capsuleChain.setCapsuleId(savedCapsule.getId());
                capsuleChain.setUserId(userId);
                capsuleChainRepository.save(capsuleChain);
            } catch (Exception e) {
                // Compensating action: Delete from PostgreSQL if Cassandra save fails
                capsuleRepository.deleteById(savedCapsule.getId());
                throw new CapsuleCreationException("Failed to create capsule chain", e);
            }
        }

        return mapToResponse(savedCapsule);
    }

    public CapsuleResponse getCapsule(Long userId, Long capsuleId) {
        Capsule capsule = capsuleRepository.findById(capsuleId)
                .orElseThrow(() -> new CapsuleNotFoundException("Capsule not found with id: " + capsuleId));

        if (!capsule.isPublic() && !capsule.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("User not authorized to access this capsule.");
        }

        return mapToResponse(capsule);
    }

    private CapsuleResponse mapToResponse(Capsule capsule) {
        return CapsuleResponse.builder()
                .id(capsule.getId())
                .title(capsule.getTitle())
                .contentHtml(capsule.getContentHtml())
                .isPublic(capsule.isPublic())
                .isUnlocked(capsule.isUnlocked())
                .isChained(capsule.isChained())
                .unlockAt(capsule.getUnlockAt())
                .build();
    }

    public List<CapsuleResponse> getUserCapsules(Long userId, Boolean unlocked) {
        if(unlocked == null) unlocked = false;
        List<Capsule> capsules = capsuleRepository.findByUserIdAndIsUnlocked(userId, unlocked);
        return capsules.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deleteCapsule(Long userId, Long id) {
        // 1. Find the capsule and verify ownership
        Capsule capsule = capsuleRepository.findById(id)
                .orElseThrow(() -> new CapsuleNotFoundException("Capsule not found with id: " + id));

        if (!capsule.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("User not authorized to delete this capsule.");
        }

        // 2. Delete from PostgreSQL
        capsuleRepository.delete(capsule);

        // 3. Delete from Cassandra with compensating action
        if (capsule.isChained()) {
            try {
                Optional<CapsuleChain> capsuleChainOpt = capsuleChainRepository.findById(id);
                if (capsuleChainOpt.isEmpty()) {
                    return; // Or throw an exception if this state is unexpected
                }
                CapsuleChain capsuleChain = capsuleChainOpt.get();
                Long prevId = capsuleChain.getPreviousCapsuleId();
                Long nextId = capsuleChain.getNextCapsuleId();

                Optional<CapsuleChain> prevChainOpt = Optional.ofNullable(prevId).flatMap(capsuleChainRepository::findById);
                Optional<CapsuleChain> nextChainOpt = Optional.ofNullable(nextId).flatMap(capsuleChainRepository::findById);

                BatchStatementBuilder batch = BatchStatement.builder(BatchType.LOGGED);

                prevChainOpt.ifPresent(prev -> {
                    String updateQuery = "UPDATE capsule_chain SET next_capsule_id = ? WHERE capsule_id = ?";
                    batch.addStatement(SimpleStatement.newInstance(updateQuery, nextId, prev.getCapsuleId()));
                });

                nextChainOpt.ifPresent(next -> {
                    String updateQuery = "UPDATE capsule_chain SET previous_capsule_id = ? WHERE capsule_id = ?";
                    batch.addStatement(SimpleStatement.newInstance(updateQuery, prevId, next.getCapsuleId()));
                });

                String deleteQuery = "DELETE FROM capsule_chain WHERE capsule_id = ?";
                batch.addStatement(SimpleStatement.newInstance(deleteQuery, id));

                cassandraTemplate.getCqlOperations().execute(batch.build());

            } catch (Exception e) {
                // Compensating action: Re-save the capsule to PostgreSQL
                capsuleRepository.save(capsule);
                throw new CapsuleDeletionException("Failed to delete capsule chain", e);
            }
        }
    }
}