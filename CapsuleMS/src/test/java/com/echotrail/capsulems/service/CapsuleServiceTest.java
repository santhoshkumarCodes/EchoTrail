package com.echotrail.capsulems.service;

import com.echotrail.capsulems.DTO.CapsuleRequest;
import com.echotrail.capsulems.DTO.CapsuleResponse;
import com.echotrail.capsulems.exception.CapsuleNotFoundException;
import com.echotrail.capsulems.exception.UnauthorizedAccessException;
import com.echotrail.capsulems.model.Capsule;
import com.echotrail.capsulems.model.CapsuleChain;
import com.echotrail.capsulems.outbox.OutboxEventPublisher;
import com.echotrail.capsulems.repository.CapsuleRepository;
import com.echotrail.capsulems.util.MarkdownProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CapsuleServiceTest {

    @Mock
    private CapsuleRepository capsuleRepository;

    @Mock
    private OutboxEventPublisher outboxEventPublisher;

    @Mock
    private MarkdownProcessor markdownProcessor;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CapsuleService capsuleService;

    private Capsule capsule;
    private CapsuleRequest capsuleRequest;

    @BeforeEach
    void setUp() {
        capsule = new Capsule(1L, 1L, "Test Title", "Test Content", "Test Content HTML", false, false, false, LocalDateTime.now().plusDays(1), LocalDateTime.now());
        capsuleRequest = CapsuleRequest.builder()
                .title("Test Title")
                .contentMarkdown("Test Content")
                .isPublic(false)
                .isChained(false)
                .unlockAt(LocalDateTime.now().plusDays(1))
                .build();
    }

    @Test
    void createCapsule_shouldCreateAndSaveCapsule() {
        when(markdownProcessor.toHtml(anyString())).thenReturn("Test Content HTML");
        when(capsuleRepository.save(any(Capsule.class))).thenAnswer(invocation -> {
            Capsule capsuleToSave = invocation.getArgument(0);
            capsuleToSave.setId(1L);
            return capsuleToSave;
        });

        CapsuleResponse response = capsuleService.createCapsule(1L, capsuleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Test Title");
        verify(capsuleRepository, times(1)).save(any(Capsule.class));
        verify(outboxEventPublisher, times(1)).publish(any(), any(), any(), any());
    }

    @Test
    void createCapsule_shouldCreateAndSaveChainedCapsule() {
        capsuleRequest.setChained(true);
        when(markdownProcessor.toHtml(anyString())).thenReturn("Test Content HTML");
        when(capsuleRepository.save(any(Capsule.class))).thenAnswer(invocation -> {
            Capsule capsuleToSave = invocation.getArgument(0);
            capsuleToSave.setId(1L);
            return capsuleToSave;
        });

        CapsuleResponse response = capsuleService.createCapsule(1L, capsuleRequest);

        assertThat(response).isNotNull();
        assertThat(response.isChained()).isTrue();
        verify(capsuleRepository, times(1)).save(any(Capsule.class));
        verify(outboxEventPublisher, times(1)).publish(any(), any(), any(), any());
    }

    @Test
    void getCapsule_shouldReturnCapsule_whenPublic() {
        capsule.setPublic(true);
        when(capsuleRepository.findById(1L)).thenReturn(Optional.of(capsule));

        CapsuleResponse response = capsuleService.getCapsule(2L, 1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void getCapsule_shouldReturnCapsule_whenOwner() {
        when(capsuleRepository.findById(1L)).thenReturn(Optional.of(capsule));

        CapsuleResponse response = capsuleService.getCapsule(1L, 1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void getCapsule_shouldThrowUnauthorizedAccess_whenNotOwnerAndNotPublic() {
        when(capsuleRepository.findById(1L)).thenReturn(Optional.of(capsule));

        assertThrows(UnauthorizedAccessException.class, () -> capsuleService.getCapsule(2L, 1L));
    }

    @Test
    void getCapsule_shouldThrowNotFound_whenCapsuleDoesNotExist() {
        when(capsuleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CapsuleNotFoundException.class, () -> capsuleService.getCapsule(1L, 1L));
    }

    @Test
    void getUserCapsules_shouldReturnUserCapsules() {
        when(capsuleRepository.findByUserIdAndIsUnlocked(1L, false)).thenReturn(Collections.singletonList(capsule));

        List<CapsuleResponse> responses = capsuleService.getUserCapsules(1L, false);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void getUserCapsules_shouldReturnEmptyList_whenUserHasNoCapsules() {
        when(capsuleRepository.findByUserIdAndIsUnlocked(1L, false)).thenReturn(Collections.emptyList());

        List<CapsuleResponse> responses = capsuleService.getUserCapsules(1L, false);

        assertThat(responses).isEmpty();
    }

    @Test
    void deleteCapsule_shouldDeleteCapsule_whenOwner() {
        when(capsuleRepository.findById(1L)).thenReturn(Optional.of(capsule));

        capsuleService.deleteCapsule(1L, 1L);

        verify(capsuleRepository, times(1)).delete(capsule);
        verify(outboxEventPublisher, times(1)).publish(any(), any(), any(), any());
    }

    @Test
    void deleteCapsule_shouldThrowUnauthorizedAccess_whenNotOwner() {
        when(capsuleRepository.findById(1L)).thenReturn(Optional.of(capsule));

        assertThrows(UnauthorizedAccessException.class, () -> capsuleService.deleteCapsule(2L, 1L));
    }

    @Test
    void deleteCapsule_shouldThrowNotFound_whenCapsuleDoesNotExist() {
        when(capsuleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CapsuleNotFoundException.class, () -> capsuleService.deleteCapsule(1L, 1L));
    }

    @Test
    void deleteCapsule_shouldUnlinkChainedCapsules() {
        capsule.setChained(true);

        when(capsuleRepository.findById(1L)).thenReturn(Optional.of(capsule));

        capsuleService.deleteCapsule(1L, 1L);

        verify(capsuleRepository, times(1)).delete(capsule);
        verify(outboxEventPublisher, times(1)).publish(any(), any(), any(), any());
    }
}
