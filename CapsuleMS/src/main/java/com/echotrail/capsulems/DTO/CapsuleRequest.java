package com.echotrail.capsulems.DTO;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CapsuleRequest {
    private String title;
    private String contentMarkdown;
    private boolean isPublic;
    private boolean isChained;
    private LocalDateTime unlockAt;
}