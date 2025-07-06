package com.echotrail.capsulems.service;

import com.echotrail.capsulems.DTO.CapsuleChainDTO;
import com.echotrail.capsulems.exception.CapsuleNotFoundException;
import com.echotrail.capsulems.exception.UnauthorizedAccessException;
import com.echotrail.capsulems.model.CapsuleChain;
import com.echotrail.capsulems.repository.CapsuleChainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CapsuleChainService {

    @Autowired
    private CapsuleChainRepository capsuleChainRepository;

    @Autowired
    private CassandraTemplate cassandraTemplate;

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
        getAndAuthorize(capsuleId, userId);
        return getCapsuleChainById(capsuleId, userId)
                .map(CapsuleChainDTO::getPreviousCapsuleId)
                .flatMap(id -> getCapsuleChainById(id, userId));
    }

    public Optional<CapsuleChainDTO> getNextCapsule(Long capsuleId, Long userId) {
        getAndAuthorize(capsuleId, userId);
        return getCapsuleChainById(capsuleId, userId)
                .map(CapsuleChainDTO::getNextCapsuleId)
                .flatMap(id -> getCapsuleChainById(id, userId));
    }

    public void setPreviousCapsuleId(Long capsuleId, Long previousCapsuleId, Long userId) {
        if (java.util.Objects.equals(capsuleId, previousCapsuleId)) {
            throw new IllegalArgumentException("A capsule cannot be linked to itself.");
        }
        CapsuleChain capsuleChain = getAndAuthorize(capsuleId, userId);
        CapsuleChain previousCapsuleChain = getAndAuthorize(previousCapsuleId, userId);

        if (previousCapsuleChain.getNextCapsuleId() != null) {
            throw new IllegalStateException("Previous capsule already has a next capsule.");
        }

        capsuleChain.setPreviousCapsuleId(previousCapsuleId);
        previousCapsuleChain.setNextCapsuleId(capsuleId);

        cassandraTemplate.batchOps().update(capsuleChain).update(previousCapsuleChain).execute();
    }

    public void setNextCapsuleId(Long capsuleId, Long nextCapsuleId, Long userId) {
        if (java.util.Objects.equals(capsuleId, nextCapsuleId)) {
            throw new IllegalArgumentException("A capsule cannot be linked to itself.");
        }
        CapsuleChain capsuleChain = getAndAuthorize(capsuleId, userId);
        CapsuleChain nextCapsuleChain = getAndAuthorize(nextCapsuleId, userId);

        if (nextCapsuleChain.getPreviousCapsuleId() != null) {
            throw new IllegalStateException("Next capsule already has a previous capsule.");
        }

        capsuleChain.setNextCapsuleId(nextCapsuleId);
        nextCapsuleChain.setPreviousCapsuleId(capsuleId);

        cassandraTemplate.batchOps().update(capsuleChain).update(nextCapsuleChain).execute();
    }
}
