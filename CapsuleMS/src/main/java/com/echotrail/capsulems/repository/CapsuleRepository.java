package com.echotrail.capsulems.repository;

import com.echotrail.capsulems.model.Capsule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CapsuleRepository extends JpaRepository<Capsule, Long> {

    List<Capsule> findByUserIdAndIsUnlocked(Long userId, boolean isUnlocked);

    @Query("SELECT c FROM Capsule c WHERE c.unlockAt <= :now AND c.isUnlocked = false")
    List<Capsule> findCapsulesReadyToUnlock(@org.springframework.data.repository.query.Param("now") LocalDateTime now);

    List<Capsule> findByIsPublicAndIsUnlocked(boolean isPublic, boolean isUnlocked);
}