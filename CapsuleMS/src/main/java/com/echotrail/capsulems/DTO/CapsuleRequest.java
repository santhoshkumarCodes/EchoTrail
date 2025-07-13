package com.echotrail.capsulems.DTO;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CapsuleRequest {
    @NotBlank(message = "Title cannot be blank")
    private String title;
    @NotBlank(message = "Content cannot be blank")
    private String contentMarkdown;
    private boolean isPublic;
    private boolean isChained;
    @NotNull(message = "Unlock date cannot be null")
    @Future(message = "Unlock date must be in the future")
    private LocalDateTime unlockAt;
}