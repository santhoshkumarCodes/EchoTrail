package com.echotrail.capsulems.service;

import com.echotrail.capsulems.DTO.CapsuleChainDTO;
import com.echotrail.capsulems.exception.CapsuleNotFoundException;
import com.echotrail.capsulems.exception.UnauthorizedAccessException;
import com.echotrail.capsulems.model.CapsuleChain;
import com.echotrail.capsulems.repository.CapsuleChainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CapsuleChainService {

    private final CapsuleChainRepository capsuleChainRepository;
    private final CassandraTemplate cassandraTemplate;

    private CapsuleChain getAndAuthorize(Long capsuleId, Long userId) {
        CapsuleChain capsuleChain = capsuleChainRepository.findById(capsuleId)
                .orElseThrow(() -> new CapsuleNotFoundException("Capsule chain not found for capsule id: " + capsuleId));
        if (!capsuleChain.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("User not authorized to access this capsule.");
        }
        return capsuleChain;
    }

    public Optional<CapsuleChainDTO> getCapsuleChainById(Long capsuleId, Long userId) {
        CapsuleChain capsuleChain = getAndAuthorize(capsuleId, userId);
        return Optional.of(capsuleChain).map(this::convertToDto);
    }

    private CapsuleChainDTO convertToDto(CapsuleChain capsuleChain) {
        return new CapsuleChainDTO(
                capsuleChain.getCapsuleId(),
                capsuleChain.getPreviousCapsuleId(),
                capsuleChain.getNextCapsuleId(),
                capsuleChain.getUserId()
        );
    }

    public Optional<CapsuleChainDTO> getPreviousCapsule(Long capsuleId, Long userId) {
        CapsuleChain currentChain = getAndAuthorize(capsuleId, userId);
        return Optional.ofNullable(currentChain.getPreviousCapsuleId())
                .flatMap(id -> getCapsuleChainById(id, userId));
    }

    public Optional<CapsuleChainDTO> getNextCapsule(Long capsuleId, Long userId) {
        CapsuleChain currentChain = getAndAuthorize(capsuleId, userId);
        return Optional.ofNullable(currentChain.getNextCapsuleId())
                .flatMap(id -> getCapsuleChainById(id, userId));
    }

    public void setPreviousCapsuleId(Long capsuleId, Long previousCapsuleId, Long userId) {
        if (java.util.Objects.equals(capsuleId, previousCapsuleId)) {
            throw new IllegalArgumentException("A capsule cannot be linked to itself.");
        }
        getAndAuthorize(capsuleId, userId);
        getAndAuthorize(previousCapsuleId, userId);

        if (!capsuleChainRepository.setNextCapsuleIdIfNull(previousCapsuleId, capsuleId)) {
            throw new IllegalStateException("Previous capsule is already linked to another capsule. Operation failed.");
        }

        if (!capsuleChainRepository.setPreviousCapsuleIdIfNull(capsuleId, previousCapsuleId)) {
            // Rollback
            capsuleChainRepository.clearNextCapsuleId(previousCapsuleId);
            throw new IllegalStateException("Current capsule is already linked to another capsule. Operation failed and rolled back.");
        }
    }

    public void setNextCapsuleId(Long capsuleId, Long nextCapsuleId, Long userId) {
        if (java.util.Objects.equals(capsuleId, nextCapsuleId)) {
            throw new IllegalArgumentException("A capsule cannot be linked to itself.");
        }
        getAndAuthorize(capsuleId, userId);
        getAndAuthorize(nextCapsuleId, userId);

        if (!capsuleChainRepository.setPreviousCapsuleIdIfNull(nextCapsuleId, capsuleId)) {
            throw new IllegalStateException("Next capsule is already linked to another capsule. Operation failed.");
        }

        if (!capsuleChainRepository.setNextCapsuleIdIfNull(capsuleId, nextCapsuleId)) {
            // Rollback
            capsuleChainRepository.clearPreviousCapsuleId(nextCapsuleId);
            throw new IllegalStateException("Current capsule is already linked to another capsule. Operation failed and rolled back.");
        }
    }
}
