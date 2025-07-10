package com.echotrail.capsulems.DTO;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CapsuleRequest {
    private String title;
    private String contentMarkdown;
    private boolean isPublic;
    private boolean isChained;
    private LocalDateTime unlockAt;
}