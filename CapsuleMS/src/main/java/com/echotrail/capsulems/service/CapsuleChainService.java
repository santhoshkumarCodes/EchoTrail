package com.echotrail.capsulems.service;

import com.echotrail.capsulems.DTO.CapsuleChainDTO;
import com.echotrail.capsulems.exception.CapsuleNotFoundException;
import com.echotrail.capsulems.exception.UnauthorizedAccessException;
import com.echotrail.capsulems.model.CapsuleChain;
import com.echotrail.capsulems.repository.CapsuleChainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CapsuleChainService {

    private final CapsuleChainRepository capsuleChainRepository;

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
        linkCapsules(capsuleId, previousCapsuleId, userId, true);
    }

    public void setNextCapsuleId(Long capsuleId, Long nextCapsuleId, Long userId) {
        linkCapsules(capsuleId, nextCapsuleId, userId, false);
    }

    private void linkCapsules(Long capsuleId, Long linkedCapsuleId, Long userId, boolean isPrevious) {
        if (java.util.Objects.equals(capsuleId, linkedCapsuleId)) {
            throw new IllegalArgumentException("A capsule cannot be linked to itself.");
        }
        getAndAuthorize(capsuleId, userId);
        getAndAuthorize(linkedCapsuleId, userId);

        if (isPrevious) {
            if (!capsuleChainRepository.setNextCapsuleIdIfNull(linkedCapsuleId, capsuleId)) {
                throw new IllegalStateException("Previous capsule is already linked to another capsule. Operation failed.");
            }
            if (!capsuleChainRepository.setPreviousCapsuleIdIfNull(capsuleId, linkedCapsuleId)) {
                capsuleChainRepository.clearNextCapsuleId(linkedCapsuleId);
                throw new IllegalStateException("Current capsule is already linked to another capsule. Operation failed and rolled back.");
            }
        } else {
            if (!capsuleChainRepository.setPreviousCapsuleIdIfNull(linkedCapsuleId, capsuleId)) {
                throw new IllegalStateException("Next capsule is already linked to another capsule. Operation failed.");
            }
            if (!capsuleChainRepository.setNextCapsuleIdIfNull(capsuleId, linkedCapsuleId)) {
                capsuleChainRepository.clearPreviousCapsuleId(linkedCapsuleId);
                throw new IllegalStateException("Current capsule is already linked to another capsule. Operation failed and rolled back.");
            }
        }
    }
}
