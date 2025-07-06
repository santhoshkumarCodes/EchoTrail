package com.echotrail.capsulems.service;

import com.echotrail.capsulems.DTO.CapsuleChainDTO;
import com.echotrail.capsulems.exception.CapsuleNotFoundException;
import com.echotrail.capsulems.exception.UnauthorizedAccessException;
import com.echotrail.capsulems.model.CapsuleChain;
import com.echotrail.capsulems.repository.CapsuleChainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.cassandra.core.CassandraBatchOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CapsuleChainServiceTest {

    @Mock
    private CapsuleChainRepository capsuleChainRepository;

    @Mock
    private CassandraTemplate cassandraTemplate;

    @Mock
    private CassandraBatchOperations batchOps;

    @InjectMocks
    private CapsuleChainService capsuleChainService;

    private CapsuleChain capsuleChain1;
    private CapsuleChain capsuleChain2;

    @BeforeEach
    void setUp() {
        capsuleChain1 = new CapsuleChain(1L, null, 2L, 1L);
        capsuleChain2 = new CapsuleChain(2L, 1L, null, 1L);
        lenient().when(cassandraTemplate.batchOps()).thenReturn(batchOps);
        lenient().when(batchOps.update(any(CapsuleChain.class))).thenReturn(batchOps);
    }

    @Test
    void getCapsuleChainById_shouldReturnChain_whenAuthorized() {
        when(capsuleChainRepository.findById(1L)).thenReturn(Optional.of(capsuleChain1));

        Optional<CapsuleChainDTO> result = capsuleChainService.getCapsuleChainById(1L, 1L);

        assertThat(result).isPresent();
        assertThat(result.get().getCapsuleId()).isEqualTo(1L);
    }

    @Test
    void getCapsuleChainById_shouldThrowException_whenNotAuthorized() {
        when(capsuleChainRepository.findById(1L)).thenReturn(Optional.of(capsuleChain1));

        assertThrows(UnauthorizedAccessException.class, () -> capsuleChainService.getCapsuleChainById(1L, 2L));
    }

    @Test
    void getPreviousCapsule_shouldReturnPrevious() {
        when(capsuleChainRepository.findById(2L)).thenReturn(Optional.of(capsuleChain2));
        when(capsuleChainRepository.findById(1L)).thenReturn(Optional.of(capsuleChain1));

        Optional<CapsuleChainDTO> result = capsuleChainService.getPreviousCapsule(2L, 1L);

        assertThat(result).isPresent();
        assertThat(result.get().getCapsuleId()).isEqualTo(1L);
    }

    @Test
    void getNextCapsule_shouldReturnNext() {
        when(capsuleChainRepository.findById(1L)).thenReturn(Optional.of(capsuleChain1));
        when(capsuleChainRepository.findById(2L)).thenReturn(Optional.of(capsuleChain2));

        Optional<CapsuleChainDTO> result = capsuleChainService.getNextCapsule(1L, 1L);

        assertThat(result).isPresent();
        assertThat(result.get().getCapsuleId()).isEqualTo(2L);
    }

    @Test
    void setPreviousCapsuleId_shouldLinkCapsules() {
        CapsuleChain capsuleChain3 = new CapsuleChain(3L, null, null, 1L);
        CapsuleChain capsuleChain4 = new CapsuleChain(4L, null, null, 1L);
        when(capsuleChainRepository.findById(3L)).thenReturn(Optional.of(capsuleChain3));
        when(capsuleChainRepository.findById(4L)).thenReturn(Optional.of(capsuleChain4));

        capsuleChainService.setPreviousCapsuleId(4L, 3L, 1L);

        assertThat(capsuleChain4.getPreviousCapsuleId()).isEqualTo(3L);
        assertThat(capsuleChain3.getNextCapsuleId()).isEqualTo(4L);
        verify(batchOps, times(1)).execute();
    }

    @Test
    void setNextCapsuleId_shouldLinkCapsules() {
        CapsuleChain capsuleChain3 = new CapsuleChain(3L, null, null, 1L);
        CapsuleChain capsuleChain4 = new CapsuleChain(4L, null, null, 1L);
        when(capsuleChainRepository.findById(3L)).thenReturn(Optional.of(capsuleChain3));
        when(capsuleChainRepository.findById(4L)).thenReturn(Optional.of(capsuleChain4));

        capsuleChainService.setNextCapsuleId(3L, 4L, 1L);

        assertThat(capsuleChain3.getNextCapsuleId()).isEqualTo(4L);
        assertThat(capsuleChain4.getPreviousCapsuleId()).isEqualTo(3L);
        verify(batchOps, times(1)).execute();
    }

    @Test
    void getCapsuleChainById_shouldThrowException_whenChainDoesNotExist() {
        when(capsuleChainRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CapsuleNotFoundException.class, () -> capsuleChainService.getCapsuleChainById(1L, 1L));
    }

    @Test
    void setPreviousCapsuleId_shouldThrowException_whenLinkingToItself() {
        assertThrows(IllegalArgumentException.class, () -> capsuleChainService.setPreviousCapsuleId(1L, 1L, 1L));
    }

    @Test
    void setNextCapsuleId_shouldThrowException_whenLinkingToItself() {
        assertThrows(IllegalArgumentException.class, () -> capsuleChainService.setNextCapsuleId(1L, 1L, 1L));
    }

    @Test
    void setPreviousCapsuleId_shouldThrowException_whenAlreadyLinked() {
        capsuleChain1.setNextCapsuleId(3L);
        when(capsuleChainRepository.findById(1L)).thenReturn(Optional.of(capsuleChain1));
        when(capsuleChainRepository.findById(2L)).thenReturn(Optional.of(capsuleChain2));

        assertThrows(IllegalStateException.class, () -> capsuleChainService.setPreviousCapsuleId(2L, 1L, 1L));
    }

    @Test
    void setNextCapsuleId_shouldThrowException_whenAlreadyLinked() {
        capsuleChain1.setNextCapsuleId(3L);
        when(capsuleChainRepository.findById(1L)).thenReturn(Optional.of(capsuleChain1));
        when(capsuleChainRepository.findById(2L)).thenReturn(Optional.of(capsuleChain2));

        assertThrows(IllegalStateException.class, () -> capsuleChainService.setNextCapsuleId(1L, 2L, 1L));
    }
}
