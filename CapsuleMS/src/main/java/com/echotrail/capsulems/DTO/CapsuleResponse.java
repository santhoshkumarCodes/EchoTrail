package com.echotrail.capsulems.DTO;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CapsuleResponse {
    private Long id;
    private String title;
    private String contentHtml;
    private boolean isPublic;
    private boolean isUnlocked;
    private boolean isChained;
    private LocalDateTime unlockAt;
    private String mediaUrl;
}