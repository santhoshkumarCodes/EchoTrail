package com.echotrail.friendshipms.repository;

import com.echotrail.friendshipms.model.Friendship;
import com.echotrail.friendshipms.model.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    @Query("SELECT f FROM Friendship f WHERE (f.senderId = :userId1 AND f.receiverId = :userId2) " +
            "OR (f.senderId = :userId2 AND f.receiverId = :userId1)")
    Optional<Friendship> findFriendship(Long userId1, Long userId2);

    List<Friendship> findBySenderIdAndStatus(Long senderId, FriendshipStatus status);

    List<Friendship> findByReceiverIdAndStatus(Long receiverId, FriendshipStatus status);

    @Query("SELECT f FROM Friendship f WHERE (f.senderId = :userId OR f.receiverId = :userId) " +
            "AND f.status = 'ACCEPTED'")
    List<Friendship> findAllFriendships(Long userId);
}