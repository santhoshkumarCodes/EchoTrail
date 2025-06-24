package com.echotrail.friendshipms.controller;

import com.echotrail.friendshipms.DTO.FriendshipRequest;
import com.echotrail.friendshipms.DTO.FriendshipResponse;
import com.echotrail.friendshipms.model.Friendship;
import com.echotrail.friendshipms.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/friendships")
@RequiredArgsConstructor
@Slf4j
public class FriendshipController {

    private final FriendshipService friendshipService;

    @PostMapping("/request")
    public ResponseEntity<FriendshipResponse> sendFriendRequest(
            @RequestHeader("X-UserId") Long senderId,
            @RequestBody FriendshipRequest request) {
        log.debug("Received friend request from user ID: {} to user ID: {}", senderId, request.getReceiverId());
        Friendship friendship = friendshipService.sendFriendRequest(senderId, request.getReceiverId());
        return ResponseEntity.ok(convertToResponse(friendship));
    }

    @PutMapping("/{friendshipId}/accept")
    public ResponseEntity<FriendshipResponse> acceptFriendRequest(
            @PathVariable Long friendshipId,
            @RequestHeader("X-UserId") Long userId) {
        log.debug("User ID: {} accepting friendship ID: {}", userId, friendshipId);
        Friendship friendship = friendshipService.acceptFriendRequest(friendshipId, userId);
        return ResponseEntity.ok(convertToResponse(friendship));
    }

    @PutMapping("/{friendshipId}/reject")
    public ResponseEntity<FriendshipResponse> rejectFriendRequest(
            @PathVariable Long friendshipId,
            @RequestHeader("X-UserId") Long userId) {
        log.debug("User ID: {} rejecting friendship ID: {}", userId, friendshipId);
        Friendship friendship = friendshipService.rejectFriendRequest(friendshipId, userId);
        return ResponseEntity.ok(convertToResponse(friendship));
    }

    @DeleteMapping("/unfriend/{userId2}")
    public ResponseEntity<Void> unfriend(
            @RequestHeader("X-UserId") Long userId1, 
            @PathVariable Long userId2) {
        log.debug("User ID: {} unfriending user ID: {}", userId1, userId2);
        friendshipService.unfriend(userId1, userId2);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/pending")
    public ResponseEntity<List<FriendshipResponse>> getPendingRequests(@RequestHeader("X-UserId") Long userId) {
        log.debug("Getting pending friend requests for user ID: {}", userId);
        List<FriendshipResponse> pendingRequests = friendshipService.getPendingRequests(userId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(pendingRequests);
    }

    @GetMapping("/friends")
    public ResponseEntity<List<FriendshipResponse>> getFriends(@RequestHeader("X-UserId") Long userId) {
        log.debug("Getting friends list for user ID: {}", userId);
        List<FriendshipResponse> friends = friendshipService.getFriends(userId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(friends);
    }

    @PostMapping("/block/{userToBlock}")
    public ResponseEntity<FriendshipResponse> blockUser(
            @RequestHeader("X-UserId") Long userId,
            @PathVariable Long userToBlock) {
        log.debug("User ID: {} blocking user ID: {}", userId, userToBlock);
        Friendship friendship = friendshipService.blockUser(userId, userToBlock);
        return ResponseEntity.ok(convertToResponse(friendship));
    }

    private FriendshipResponse convertToResponse(Friendship friendship) {
        FriendshipResponse response = new FriendshipResponse();
        response.setId(friendship.getId());
        response.setSenderId(friendship.getSenderId());
        response.setReceiverId(friendship.getReceiverId());
        response.setStatus(friendship.getStatus().name());
        response.setCreatedAt(friendship.getCreatedAt().toString());
        return response;
    }
}