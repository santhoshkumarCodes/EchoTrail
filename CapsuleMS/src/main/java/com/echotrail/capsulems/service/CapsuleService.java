package com.echotrail.capsulems.service;

import com.echotrail.capsulems.DTO.*;
import com.echotrail.capsulems.exception.*;
import com.echotrail.capsulems.model.*;
import com.echotrail.capsulems.repository.*;
import com.echotrail.capsulems.util.MarkdownProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CapsuleService {

    private final CapsuleRepository capsuleRepository;
    private final CapsuleChainRepository capsuleChainRepository;
    private final MarkdownProcessor markdownProcessor;

    @Transactional
    public CapsuleResponse createCapsule(Long userId, CapsuleRequest request) {
        Capsule capsule = new Capsule();
        capsule.setUserId(userId);
        capsule.setTitle(request.getTitle());
        capsule.setContentMarkdown(request.getContentMarkdown());
        capsule.setContentHtml(markdownProcessor.toHtml(request.getContentMarkdown()));
        capsule.setPublic(request.isPublic());
        capsule.setChained(request.isChained());
        capsule.setUnlockAt(request.getUnlockAt());

        Capsule saved = capsuleRepository.save(capsule);

        if (request.isChained()) {
            CapsuleChain capsuleChain = new CapsuleChain();
            capsuleChain.setCapsuleId(saved.getId());
            capsuleChain.setUserId(userId);
            capsuleChainRepository.save(capsuleChain);
        }

        return mapToResponse(saved);
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

    @Transactional
    public void deleteCapsule(Long userId, Long id) {
        Capsule capsule = capsuleRepository.findById(id)
                .orElseThrow(() -> new CapsuleNotFoundException("Capsule not found with id: " + id));

        if (!capsule.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("User not authorized to delete this capsule.");
        }

        if (capsule.isChained()) {
            capsuleChainRepository.findById(id).ifPresent(capsuleChain -> {
                Long prevId = capsuleChain.getPreviousCapsuleId();
                Long nextId = capsuleChain.getNextCapsuleId();

                if (prevId != null) {
                    capsuleChainRepository.findById(prevId).ifPresent(prevChain -> {
                        prevChain.setNextCapsuleId(nextId);
                        capsuleChainRepository.save(prevChain);
                    });
                }

                if (nextId != null) {
                    capsuleChainRepository.findById(nextId).ifPresent(nextChain -> {
                        nextChain.setPreviousCapsuleId(prevId);
                        capsuleChainRepository.save(nextChain);
                    });
                }

                capsuleChainRepository.delete(capsuleChain);
            });
        }

        capsuleRepository.delete(capsule);
    }
}