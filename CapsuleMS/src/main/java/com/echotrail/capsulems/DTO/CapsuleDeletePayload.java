package com.echotrail.capsulems.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapsuleDeletePayload {
    private Long id;
    private boolean isChained;
}
