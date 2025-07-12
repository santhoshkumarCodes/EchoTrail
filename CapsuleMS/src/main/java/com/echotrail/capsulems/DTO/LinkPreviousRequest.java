package com.echotrail.capsulems.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkPreviousRequest {
    private Long capsuleId;
    private Long previousCapsuleId;
}
