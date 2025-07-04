package com.echotrail.friendshipms.service;


import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import com.echotrail.friendshipms.exception.FriendshipNotFoundException;
import com.echotrail.friendshipms.exception.InvalidFriendshipOperationException;
import com.echotrail.friendshipms.model.Friendship;
import com.echotrail.friendshipms.model.FriendshipStatus;
import com.echotrail.friendshipms.repository.FriendshipRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FriendshipServiceTest {

    @Mock
    private FriendshipRepository friendshipRepository;

    @InjectMocks
    private FriendshipService friendshipService;

    @Test
    public void testSendFriendRequest() {
        when(friendshipRepository.findFriendship(1L, 2L)).thenReturn(Optional.empty());
        when(friendshipRepository.save(any(Friendship.class))).thenAnswer(i -> i.getArguments()[0]);

        Friendship friendship = friendshipService.sendFriendRequest(1L, 2L);
        assertThat(friendship.getSenderId()).isEqualTo(1L);
        assertThat(friendship.getReceiverId()).isEqualTo(2L);
        assertThat(friendship.getStatus()).isEqualTo(FriendshipStatus.PENDING);
    }

    @Test
    public void testSendFriendRequestToSelf() {
        assertThrows(InvalidFriendshipOperationException.class, () -> {
            friendshipService.sendFriendRequest(1L, 1L);
        });
    }

    @Test
    public void testSendFriendRequestWhenExists() {
        when(friendshipRepository.findFriendship(1L, 2L)).thenReturn(Optional.of(new Friendship()));
        assertThrows(InvalidFriendshipOperationException.class, () -> {
            friendshipService.sendFriendRequest(1L, 2L);
        });
    }

    @Test
    public void testAcceptFriendRequest() {
        Friendship friendship = new Friendship();
        friendship.setId(1L);
        friendship.setReceiverId(2L);
        friendship.setStatus(FriendshipStatus.PENDING);

        when(friendshipRepository.findById(1L)).thenReturn(Optional.of(friendship));
        when(friendshipRepository.save(any(Friendship.class))).thenAnswer(i -> i.getArguments()[0]);

        Friendship acceptedFriendship = friendshipService.acceptFriendRequest(1L, 2L);
        assertThat(acceptedFriendship.getStatus()).isEqualTo(FriendshipStatus.ACCEPTED);
    }

    @Test
    public void testAcceptFriendRequestNotFound() {
        when(friendshipRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(FriendshipNotFoundException.class, () -> {
            friendshipService.acceptFriendRequest(1L, 2L);
        });
    }

    @Test
    public void testAcceptFriendRequestInvalidReceiver() {
        Friendship friendship = new Friendship();
        friendship.setId(1L);
        friendship.setReceiverId(3L); 
        friendship.setStatus(FriendshipStatus.PENDING);

        when(friendshipRepository.findById(1L)).thenReturn(Optional.of(friendship));

        assertThrows(InvalidFriendshipOperationException.class, () -> {
            friendshipService.acceptFriendRequest(1L, 2L); 
        });
    }

    @Test
    public void testAcceptFriendRequestInvalidStatus() {
        Friendship friendship = new Friendship();
        friendship.setId(1L);
        friendship.setReceiverId(2L);
        friendship.setStatus(FriendshipStatus.ACCEPTED);

        when(friendshipRepository.findById(1L)).thenReturn(Optional.of(friendship));

        assertThrows(InvalidFriendshipOperationException.class, () -> {
            friendshipService.acceptFriendRequest(1L, 2L);
        });
    }

    @Test
    public void testRejectFriendRequest() {
        Friendship friendship = new Friendship();
        friendship.setId(1L);
        friendship.setReceiverId(2L);
        friendship.setStatus(FriendshipStatus.PENDING);

        when(friendshipRepository.findById(1L)).thenReturn(Optional.of(friendship));
        when(friendshipRepository.save(any(Friendship.class))).thenAnswer(i -> i.getArguments()[0]);

        Friendship rejectedFriendship = friendshipService.rejectFriendRequest(1L, 2L);
        assertThat(rejectedFriendship.getStatus()).isEqualTo(FriendshipStatus.REJECTED);
    }

    @Test
    public void testRejectFriendRequestInvalidReceiver() {
        Friendship friendship = new Friendship();
        friendship.setId(1L);
        friendship.setReceiverId(3L);
        friendship.setStatus(FriendshipStatus.PENDING);

        when(friendshipRepository.findById(1L)).thenReturn(Optional.of(friendship));

        assertThrows(InvalidFriendshipOperationException.class, () -> {
            friendshipService.rejectFriendRequest(1L, 2L);
        });
    }

    @Test
    public void testUnfriend() {
        Friendship friendship = new Friendship();
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        when(friendshipRepository.findFriendship(1L, 2L)).thenReturn(Optional.of(friendship));

        friendshipService.unfriend(1L, 2L);

        verify(friendshipRepository).delete(friendship);
    }

    @Test
    public void testUnfriendNotFound() {
        when(friendshipRepository.findFriendship(1L, 2L)).thenReturn(Optional.empty());

        assertThrows(FriendshipNotFoundException.class, () -> {
            friendshipService.unfriend(1L, 2L);
        });
    }

    @Test
    public void testUnfriendNotFriends() {
        Friendship friendship = new Friendship();
        friendship.setStatus(FriendshipStatus.PENDING);
        when(friendshipRepository.findFriendship(1L, 2L)).thenReturn(Optional.of(friendship));

        assertThrows(InvalidFriendshipOperationException.class, () -> {
            friendshipService.unfriend(1L, 2L);
        });
    }

    @Test
    public void testGetPendingRequests() {
        when(friendshipRepository.findByReceiverIdAndStatus(1L, FriendshipStatus.PENDING))
                .thenReturn(Collections.singletonList(new Friendship()));

        List<Friendship> pendingRequests = friendshipService.getPendingRequests(1L);

        assertThat(pendingRequests).hasSize(1);
    }

    @Test
    public void testGetFriends() {
        when(friendshipRepository.findAllFriendships(1L))
                .thenReturn(Collections.singletonList(new Friendship()));

        List<Friendship> friends = friendshipService.getFriends(1L);

        assertThat(friends).hasSize(1);
    }

    @Test
    public void testBlockUser() {
        when(friendshipRepository.findFriendship(1L, 2L)).thenReturn(Optional.empty());
        when(friendshipRepository.save(any(Friendship.class))).thenAnswer(i -> i.getArguments()[0]);

        Friendship blockedFriendship = friendshipService.blockUser(1L, 2L);

        assertThat(blockedFriendship.getStatus()).isEqualTo(FriendshipStatus.BLOCKED);
        assertThat(blockedFriendship.getSenderId()).isEqualTo(1L);
        assertThat(blockedFriendship.getReceiverId()).isEqualTo(2L);
    }

    @Test
    public void testBlockUserWhenFriendshipExists() {
        Friendship friendship = new Friendship();
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        when(friendshipRepository.findFriendship(1L, 2L)).thenReturn(Optional.of(friendship));
        when(friendshipRepository.save(any(Friendship.class))).thenAnswer(i -> i.getArguments()[0]);

        Friendship blockedFriendship = friendshipService.blockUser(1L, 2L);

        assertThat(blockedFriendship.getStatus()).isEqualTo(FriendshipStatus.BLOCKED);
    }
}
