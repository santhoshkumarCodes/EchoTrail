package com.echotrail.capsulems.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkPreviousRequest {
    @NotNull(message = "Capsule ID cannot be null")
    private Long capsuleId;
    @NotNull(message = "Previous capsule ID cannot be null")
    private Long previousCapsuleId;
}
