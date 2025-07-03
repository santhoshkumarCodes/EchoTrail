package com.echotrail.friendshipms.repository;

import com.echotrail.friendshipms.model.Friendship;
import com.echotrail.friendshipms.model.FriendshipStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class FriendshipRepositoryTest {

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Test
    public void testFindFriendship() {
        Friendship friendship = new Friendship();
        friendship.setSenderId(1L);
        friendship.setReceiverId(2L);
        friendship.setStatus(FriendshipStatus.PENDING);
        friendshipRepository.save(friendship);

        Optional<Friendship> foundFriendship = friendshipRepository.findFriendship(1L, 2L);
        assertThat(foundFriendship).isPresent();
        assertThat(foundFriendship.get().getSenderId()).isEqualTo(1L);
    }

    @Test
    public void testFindBySenderIdAndStatus() {
        Friendship friendship = new Friendship();
        friendship.setSenderId(1L);
        friendship.setReceiverId(2L);
        friendship.setStatus(FriendshipStatus.PENDING);
        friendshipRepository.save(friendship);

        List<Friendship> pendingRequests = friendshipRepository.findBySenderIdAndStatus(1L, FriendshipStatus.PENDING);
        assertThat(pendingRequests).hasSize(1);
        assertThat(pendingRequests.get(0).getSenderId()).isEqualTo(1L);
    }

    @Test
    public void testFindByReceiverIdAndStatus() {
        Friendship friendship = new Friendship();
        friendship.setSenderId(1L);
        friendship.setReceiverId(2L);
        friendship.setStatus(FriendshipStatus.PENDING);
        friendshipRepository.save(friendship);

        List<Friendship> pendingRequests = friendshipRepository.findByReceiverIdAndStatus(2L, FriendshipStatus.PENDING);
        assertThat(pendingRequests).hasSize(1);
        assertThat(pendingRequests.get(0).getReceiverId()).isEqualTo(2L);
    }

    @Test
    public void testFindAllFriendships() {
        Friendship friendship1 = new Friendship();
        friendship1.setSenderId(1L);
        friendship1.setReceiverId(2L);
        friendship1.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.save(friendship1);

        Friendship friendship2 = new Friendship();
        friendship2.setSenderId(3L);
        friendship2.setReceiverId(1L);
        friendship2.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.save(friendship2);

        List<Friendship> friends = friendshipRepository.findAllFriendships(1L);
        assertThat(friends).hasSize(2);
    }
}