package com.echotrail.capsulems.repository;

import com.echotrail.capsulems.model.CapsuleChain;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CapsuleChainRepository extends CassandraRepository<CapsuleChain, Long> {
    @Query("UPDATE capsule_chain SET next_capsule_id = ?1 WHERE capsule_id = ?0 IF next_capsule_id = null")
    boolean setNextCapsuleIdIfNull(Long capsuleId, Long nextCapsuleId);

    @Query("UPDATE capsule_chain SET previous_capsule_id = ?1 WHERE capsule_id = ?0 IF previous_capsule_id = null")
    boolean setPreviousCapsuleIdIfNull(Long capsuleId, Long previousCapsuleId);

    @Query("UPDATE capsule_chain SET next_capsule_id = null WHERE capsule_id = ?0")
    void clearNextCapsuleId(Long capsuleId);

    @Query("UPDATE capsule_chain SET previous_capsule_id = null WHERE capsule_id = ?0")
    void clearPreviousCapsuleId(Long capsuleId);
}
