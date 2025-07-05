package com.echotrail.capsulems.service;

import com.echotrail.capsulems.DTO.CapsuleChainDTO;
import com.echotrail.capsulems.model.CapsuleChain;
import com.echotrail.capsulems.repository.CapsuleChainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CapsuleChainService {

    @Autowired
    private CapsuleChainRepository capsuleChainRepository;

    private void authorizeUser(Long capsuleId, Long userId) {
        capsuleChainRepository.findById(capsuleId)
                .ifPresent(capsuleChain -> {
                    if (!capsuleChain.getUserId().equals(userId)) {
                        throw new SecurityException("User not authorized to access this capsule.");
                    }
                });
    }

    public Optional<CapsuleChainDTO> getCapsuleChainById(Long capsuleId, Long userId) {
        authorizeUser(capsuleId, userId);
        return capsuleChainRepository.findById(capsuleId)
                .map(this::convertToDto);
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
        authorizeUser(capsuleId, userId);
        return getCapsuleChainById(capsuleId, userId)
                .map(CapsuleChainDTO::getPreviousCapsuleId)
                .flatMap(id -> getCapsuleChainById(id, userId));
    }

    public Optional<CapsuleChainDTO> getNextCapsule(Long capsuleId, Long userId) {
        authorizeUser(capsuleId, userId);
        return getCapsuleChainById(capsuleId, userId)
                .map(CapsuleChainDTO::getNextCapsuleId)
                .flatMap(id -> getCapsuleChainById(id, userId));
    }

    public void setPreviousCapsuleId(Long capsuleId, Long previousCapsuleId, Long userId) {
        authorizeUser(capsuleId, userId);
        authorizeUser(previousCapsuleId, userId);

        CapsuleChain capsuleChain = capsuleChainRepository.findById(capsuleId)
                .orElseThrow(() -> new IllegalArgumentException("Capsule chain not found for capsule id: " + capsuleId));

        CapsuleChain previousCapsuleChain = capsuleChainRepository.findById(previousCapsuleId)
                .orElseThrow(() -> new IllegalArgumentException("Capsule chain not found for previous capsule id: " + previousCapsuleId));

        if (previousCapsuleChain.getNextCapsuleId() != null) {
            throw new IllegalStateException("Previous capsule already has a next capsule.");
        }

        capsuleChain.setPreviousCapsuleId(previousCapsuleId);
        previousCapsuleChain.setNextCapsuleId(capsuleId);

        capsuleChainRepository.save(capsuleChain);
        capsuleChainRepository.save(previousCapsuleChain);
    }

    public void setNextCapsuleId(Long capsuleId, Long nextCapsuleId, Long userId) {
        authorizeUser(capsuleId, userId);
        authorizeUser(nextCapsuleId, userId);

        CapsuleChain capsuleChain = capsuleChainRepository.findById(capsuleId)
                .orElseThrow(() -> new IllegalArgumentException("Capsule chain not found for capsule id: " + capsuleId));

        CapsuleChain nextCapsuleChain = capsuleChainRepository.findById(nextCapsuleId)
                .orElseThrow(() -> new IllegalArgumentException("Capsule chain not found for next capsule id: " + nextCapsuleId));

        if (nextCapsuleChain.getPreviousCapsuleId() != null) {
            throw new IllegalStateException("Next capsule already has a previous capsule.");
        }

        capsuleChain.setNextCapsuleId(nextCapsuleId);
        nextCapsuleChain.setPreviousCapsuleId(capsuleId);

        capsuleChainRepository.save(capsuleChain);
        capsuleChainRepository.save(nextCapsuleChain);
    }
}
