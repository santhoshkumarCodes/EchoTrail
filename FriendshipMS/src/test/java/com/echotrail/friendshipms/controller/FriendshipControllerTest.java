package com.echotrail.friendshipms.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.Collections;

import com.echotrail.friendshipms.DTO.FriendshipRequest;
import com.echotrail.friendshipms.model.Friendship;
import com.echotrail.friendshipms.model.FriendshipStatus;
import com.echotrail.friendshipms.service.FriendshipService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FriendshipController.class)
public class FriendshipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FriendshipService friendshipService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testSendFriendRequest() throws Exception {
        FriendshipRequest request = new FriendshipRequest();
        request.setReceiverId(2L);

        Friendship friendship = new Friendship();
        friendship.setId(1L);
        friendship.setSenderId(1L);
        friendship.setReceiverId(2L);
        friendship.setStatus(FriendshipStatus.PENDING);
        friendship.setCreatedAt(LocalDateTime.now());

        when(friendshipService.sendFriendRequest(1L, 2L)).thenReturn(friendship);

        mockMvc.perform(post("/api/v1/friendships/request")
                .header("X-UserId", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    public void testAcceptFriendRequest() throws Exception {
        Friendship friendship = new Friendship();
        friendship.setId(1L);
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendship.setCreatedAt(LocalDateTime.now());

        when(friendshipService.acceptFriendRequest(1L, 2L)).thenReturn(friendship);

        mockMvc.perform(put("/api/v1/friendships/1/accept")
                .header("X-UserId", 2L))
                .andExpect(status().isOk());
    }

    @Test
    public void testRejectFriendRequest() throws Exception {
        Friendship friendship = new Friendship();
        friendship.setId(1L);
        friendship.setStatus(FriendshipStatus.REJECTED);
        friendship.setCreatedAt(LocalDateTime.now());

        when(friendshipService.rejectFriendRequest(1L, 2L)).thenReturn(friendship);

        mockMvc.perform(put("/api/v1/friendships/1/reject")
                .header("X-UserId", 2L))
                .andExpect(status().isOk());
    }

    @Test
    public void testUnfriend() throws Exception {
        mockMvc.perform(delete("/api/v1/friendships/unfriend/2")
                .header("X-UserId", 1L))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetPendingRequests() throws Exception {
        Friendship friendship = new Friendship();
        friendship.setId(1L);
        friendship.setSenderId(1L);
        friendship.setReceiverId(2L);
        friendship.setStatus(FriendshipStatus.PENDING);
        friendship.setCreatedAt(LocalDateTime.now());

        when(friendshipService.getPendingRequests(2L)).thenReturn(Collections.singletonList(friendship));

        mockMvc.perform(get("/api/v1/friendships/pending")
                .header("X-UserId", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    public void testGetFriends() throws Exception {
        Friendship friendship = new Friendship();
        friendship.setId(1L);
        friendship.setSenderId(1L);
        friendship.setReceiverId(2L);
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendship.setCreatedAt(LocalDateTime.now());

        when(friendshipService.getFriends(1L)).thenReturn(Collections.singletonList(friendship));

        mockMvc.perform(get("/api/v1/friendships/friends")
                .header("X-UserId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    public void testBlockUser() throws Exception {
        Friendship friendship = new Friendship();
        friendship.setId(1L);
        friendship.setSenderId(1L);
        friendship.setReceiverId(2L);
        friendship.setStatus(FriendshipStatus.BLOCKED);
        friendship.setCreatedAt(LocalDateTime.now());

        when(friendshipService.blockUser(1L, 2L)).thenReturn(friendship);

        mockMvc.perform(post("/api/v1/friendships/block/2")
                .header("X-UserId", 1L))
                .andExpect(status().isOk());
    }
}
