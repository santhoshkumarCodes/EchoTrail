package com.echotrail.capsulems.service;

import com.echotrail.capsulems.DTO.CapsuleDeletePayload;
import com.echotrail.capsulems.DTO.CapsuleRequest;
import com.echotrail.capsulems.DTO.CapsuleResponse;
import com.echotrail.capsulems.exception.CapsuleNotFoundException;
import com.echotrail.capsulems.exception.UnauthorizedAccessException;
import com.echotrail.capsulems.model.Capsule;
import com.echotrail.capsulems.outbox.OutboxEventPublisher;
import com.echotrail.capsulems.repository.CapsuleRepository;
import com.echotrail.capsulems.util.MarkdownProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CapsuleService {

    private final CapsuleRepository capsuleRepository;
    private final OutboxEventPublisher outboxEventPublisher;
    private final MarkdownProcessor markdownProcessor;

    @Transactional
    public CapsuleResponse createCapsule(Long userId, CapsuleRequest request) {
        Capsule capsule = toCapsule(userId, request);
        Capsule savedCapsule = capsuleRepository.save(capsule);

        outboxEventPublisher.publish(
                "Capsule",
                savedCapsule.getId().toString(),
                "CapsuleCreated",
                mapToResponse(savedCapsule)
        );

        return mapToResponse(savedCapsule);
    }

    private Capsule toCapsule(Long userId, CapsuleRequest request) {
        Capsule capsule = new Capsule();
        capsule.setUserId(userId);
        capsule.setTitle(request.getTitle());
        capsule.setContentMarkdown(request.getContentMarkdown());
        capsule.setContentHtml(markdownProcessor.toHtml(request.getContentMarkdown()));
        capsule.setPublic(request.isPublic());
        capsule.setChained(request.isChained());
        capsule.setUnlockAt(request.getUnlockAt());
        return capsule;
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
                .userId(capsule.getUserId())
                .title(capsule.getTitle())
                .contentHtml(capsule.getContentHtml())
                .isPublic(capsule.isPublic())
                .isUnlocked(capsule.isUnlocked())
                .isChained(capsule.isChained())
                .unlockAt(capsule.getUnlockAt())
                .build();
    }

    public List<CapsuleResponse> getUserCapsules(Long userId, Boolean unlocked) {
        List<Capsule> capsules = capsuleRepository.findByUserIdAndIsUnlocked(userId, unlocked);
        return capsules.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteCapsule(Long userId, Long id) {
        capsuleRepository.findById(id).ifPresent(capsule -> {
            if (!capsule.getUserId().equals(userId)) {
                throw new UnauthorizedAccessException("User not authorized to delete this capsule.");
            }

            capsuleRepository.delete(capsule);

            outboxEventPublisher.publish(
                    "Capsule",
                    id.toString(),
                    "CapsuleDeleted",
                    new CapsuleDeletePayload(id, capsule.isChained())
            );
        });
    }
}