package com.echotrail.capsulems.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkNextRequest {
    @NotNull(message = "Capsule ID cannot be null")
    private Long capsuleId;
    @NotNull(message = "Next capsule ID cannot be null")
    private Long nextCapsuleId;
}
