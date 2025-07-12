package com.echotrail.capsulems.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkNextRequest {
    private Long capsuleId;
    private Long nextCapsuleId;
}
