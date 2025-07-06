package com.echotrail.capsulems.controller;

import com.echotrail.capsulems.DTO.CapsuleRequest;
import com.echotrail.capsulems.DTO.CapsuleResponse;
import com.echotrail.capsulems.service.CapsuleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraReactiveDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@WebMvcTest(CapsuleController.class)
@EnableAutoConfiguration(exclude = {
    SecurityAutoConfiguration.class,
    DataSourceAutoConfiguration.class,
    JpaRepositoriesAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class,
    MongoRepositoriesAutoConfiguration.class,
    MongoDataAutoConfiguration.class,
    CassandraRepositoriesAutoConfiguration.class,
    CassandraDataAutoConfiguration.class,
    CassandraReactiveDataAutoConfiguration.class
})
class CapsuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CapsuleService capsuleService;

    @Autowired
    private ObjectMapper objectMapper;

    private CapsuleResponse capsuleResponse;
    private CapsuleRequest capsuleRequest;

    @BeforeEach
    void setUp() {
        capsuleResponse = CapsuleResponse.builder()
                .id(1L)
                .title("Test Title")
                .contentHtml("Test Content HTML")
                .isPublic(false)
                .isUnlocked(false)
                .isChained(false)
                .unlockAt(LocalDateTime.now().plusDays(1))
                .build();

        capsuleRequest = CapsuleRequest.builder()
                .title("Test Title")
                .contentMarkdown("Test Content")
                .isPublic(false)
                .isChained(false)
                .unlockAt(LocalDateTime.now().plusDays(1))
                .build();
    }

    @Test
    void createCapsule_shouldReturnCreatedCapsule() throws Exception {
        when(capsuleService.createCapsule(anyLong(), any(CapsuleRequest.class))).thenReturn(capsuleResponse);

        mockMvc.perform(post("/api/v1/capsules")
                        .header("X-UserId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(capsuleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getCapsule_shouldReturnCapsule() throws Exception {
        when(capsuleService.getCapsule(1L, 1L)).thenReturn(capsuleResponse);

        mockMvc.perform(get("/api/v1/capsules/1")
                        .header("X-UserId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getUserCapsules_shouldReturnUserCapsules() throws Exception {
        when(capsuleService.getUserCapsules(1L, false)).thenReturn(Collections.singletonList(capsuleResponse));

        mockMvc.perform(get("/api/v1/capsules")
                        .header("X-UserId", 1L)
                        .param("unlocked", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void deleteCapsule_shouldReturnNoContent() throws Exception {
        doNothing().when(capsuleService).deleteCapsule(anyLong(), anyLong());

        mockMvc.perform(delete("/api/v1/capsules/1")
                        .header("X-UserId", 1L))
                .andExpect(status().isNoContent());

        verify(capsuleService).deleteCapsule(1L, 1L);
    }
}