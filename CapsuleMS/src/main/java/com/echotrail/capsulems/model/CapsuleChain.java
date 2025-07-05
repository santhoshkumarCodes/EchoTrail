package com.echotrail.capsulems.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("capsule_chain")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapsuleChain {

    @PrimaryKey
    private Long capsuleId;

    private Long previousCapsuleId;

    private Long nextCapsuleId;

    private Long userId;
}