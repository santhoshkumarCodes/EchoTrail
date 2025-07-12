package com.echotrail.capsulems.controller;

import com.echotrail.capsulems.DTO.CapsuleChainDTO;
import com.echotrail.capsulems.service.CapsuleChainService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

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

@WebMvcTest(CapsuleChainController.class)
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
class CapsuleChainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CapsuleChainService capsuleChainService;

    @Autowired
    private ObjectMapper objectMapper;

    private CapsuleChainDTO capsuleChainDTO;

    @BeforeEach
    void setUp() {
        capsuleChainDTO = new CapsuleChainDTO(1L, null, 2L, 1L);
    }

    @Test
    void getCapsuleChainById_shouldReturnChain() throws Exception {
        when(capsuleChainService.getCapsuleChainById(1L, 1L)).thenReturn(Optional.of(capsuleChainDTO));

        mockMvc.perform(get("/api/v1/capsule-chains/1")
                        .header("X-UserId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capsuleId").value(1L));
    }

    @Test
    void getPreviousCapsule_shouldReturnPrevious() throws Exception {
        when(capsuleChainService.getPreviousCapsule(2L, 1L)).thenReturn(Optional.of(capsuleChainDTO));

        mockMvc.perform(get("/api/v1/capsule-chains/2/previous")
                        .header("X-UserId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capsuleId").value(1L));
    }

    @Test
    void getNextCapsule_shouldReturnNext() throws Exception {
        when(capsuleChainService.getNextCapsule(1L, 1L)).thenReturn(Optional.of(new CapsuleChainDTO(2L, 1L, null, 1L)));

        mockMvc.perform(get("/api/v1/capsule-chains/1/next")
                        .header("X-UserId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capsuleId").value(2L));
    }

    @Test
    void setPreviousCapsule_shouldReturnOk() throws Exception {
        mockMvc.perform(put("/api/v1/capsule-chains/link/previous")
                        .header("X-UserId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CapsuleChainDTO(2L, 1L, null, 1L))))
                .andExpect(status().isOk());
    }

    @Test
    void setNextCapsule_shouldReturnOk() throws Exception {
        mockMvc.perform(put("/api/v1/capsule-chains/link/next")
                        .header("X-UserId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CapsuleChainDTO(1L, null, 2L, 1L))))
                .andExpect(status().isOk());
    }
}