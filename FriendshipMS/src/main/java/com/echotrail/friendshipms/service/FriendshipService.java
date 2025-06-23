package com.echotrail.friendshipms.service;

import com.echotrail.friendshipms.model.Friendship;
import com.echotrail.friendshipms.model.FriendshipStatus;
import com.echotrail.friendshipms.repository.FriendshipRepository;
import com.echotrail.friendshipms.exception.FriendshipNotFoundException;
import com.echotrail.friendshipms.exception.InvalidFriendshipOperationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;

    @Transactional
    public Friendship sendFriendRequest(Long senderId, Long receiverId) {
        if (senderId.equals(receiverId)) {
            throw new InvalidFriendshipOperationException("Cannot send friend request to yourself");
        }

        // Check if friendship already exists
        friendshipRepository.findFriendship(senderId, receiverId)
                .ifPresent(friendship -> {
                    throw new InvalidFriendshipOperationException("Friendship already exists");
                });

        Friendship friendship = new Friendship();
        friendship.setSenderId(senderId);
        friendship.setReceiverId(receiverId);
        friendship.setStatus(FriendshipStatus.PENDING);

        return friendshipRepository.save(friendship);
    }

    @Transactional
    public Friendship acceptFriendRequest(Long friendshipId, Long userId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new FriendshipNotFoundException("Friendship not found"));

        if (!friendship.getReceiverId().equals(userId)) {
            throw new InvalidFriendshipOperationException("Only receiver can accept friend request");
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new InvalidFriendshipOperationException("Friend request is not in PENDING status");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        return friendshipRepository.save(friendship);
    }

    @Transactional
    public Friendship rejectFriendRequest(Long friendshipId, Long userId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new FriendshipNotFoundException("Friendship not found"));

        if (!friendship.getReceiverId().equals(userId)) {
            throw new InvalidFriendshipOperationException("Only receiver can reject friend request");
        }

        friendship.setStatus(FriendshipStatus.REJECTED);
        return friendshipRepository.save(friendship);
    }

    @Transactional
    public void unfriend(Long userId1, Long userId2) {
        Friendship friendship = friendshipRepository.findFriendship(userId1, userId2)
                .orElseThrow(() -> new FriendshipNotFoundException("Friendship not found"));

        if (friendship.getStatus() != FriendshipStatus.ACCEPTED) {
            throw new InvalidFriendshipOperationException("Users are not friends");
        }

        friendshipRepository.delete(friendship);
    }

    public List<Friendship> getPendingRequests(Long userId) {
        return friendshipRepository.findByReceiverIdAndStatus(userId, FriendshipStatus.PENDING);
    }

    public List<Friendship> getFriends(Long userId) {
        return friendshipRepository.findAllFriendships(userId);
    }

    @Transactional
    public Friendship blockUser(Long userId, Long userToBlockId) {
        Friendship friendship = friendshipRepository.findFriendship(userId, userToBlockId)
                .orElse(new Friendship());

        friendship.setSenderId(userId);
        friendship.setReceiverId(userToBlockId);
        friendship.setStatus(FriendshipStatus.BLOCKED);

        return friendshipRepository.save(friendship);
    }
}