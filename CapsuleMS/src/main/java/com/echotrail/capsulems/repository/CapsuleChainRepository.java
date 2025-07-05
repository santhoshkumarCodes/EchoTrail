package com.echotrail.capsulems.repository;

import com.echotrail.capsulems.model.CapsuleChain;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CapsuleChainRepository extends CassandraRepository<CapsuleChain, Long> {
}
